#!/usr/bin/env python3
"""
Extract menu data from OSU Nutrislice API and CSVs.
Generates data/osu_meals.json and data/meal_plans.json.

Usage:
  docker compose run --rm web python extract_data.py

API: https://osu.api.nutrislice.com/menu/api/weeks/school/{slug}/menu-type/all-day/{date}/
"""

import csv
import json
import os
import sys
import time
from datetime import datetime, timedelta
from urllib.request import urlopen, Request
from urllib.error import HTTPError, URLError

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
LOCATIONS_CSV = os.path.join(DATA_DIR, "locations.csv")
MEAL_PLANS_CSV = os.path.join(DATA_DIR, "meal_plans.csv")

API_BASE = "https://osu.api.nutrislice.com/menu/api/weeks/school"
MENU_TYPE = "all-day"

# Map location names from CSV → Nutrislice URL slugs
# Slug format: lowercase, hyphens, no special chars
LOCATION_SLUGS = {
    "12th Avenue Bread Company": "12th-avenue-bread-company",
    "Berry Café": "berry-cafe",
    "Café Carmenton": "cafe-carmenton",
    "Caffeine Element": "the-caffeine-element",
    "CFAES Café": "cfaes-cafe",
    "Coffey Road Café at Vet Med": "the-coffey-road-cafe-at-vet-med",
    "Connecting Grounds": "connecting-grounds",
    "Courtside Café": "courtside-cafe",
    "Crane Café": "crane-cafe",
    "Curl Market": "curl-market",
    "Donatos at Oxley's": "oxleys-to-go",
    "Espress-OH": "espressoh",
    "Hamilton Café": "hamilton-cafe",
    "Juice @ RPAC": "juice-2",
    "Juice North": "juice-north",
    "KSA Café": "ksa-cafe",
    "Marketplace Coffee Shop": "marketplace-c-store",
    "Marketplace on Neil": "marketplace",
    "Mirror Lake Eatery": "mirror-lake-eatery",
    "Oxley's by the Numbers": "oxleys-by-the-numbers",
    "Postle Café": "postle-cafe",
    "Sloopy's Diner": "sloopys-diner",
    "Terra Byte Café": "terra-byte-cafe",
    "The Campus Grind – McPherson": "the-campus-grind-mcpherson",
    "Traditions at Kennedy": "traditions-at-kennedy",
    "Traditions at Scott": "traditions-at-scott",
    "Traditions at Morrill": "traditions-at-morrill",
    "Union Market": "union-market",
    "Woody's Tavern": "woodys-tavern",
}

# Dietary icon slugs we care about
DIETARY_SLUGS = {
    "vegan": "vegan",
    "vegetarian": "vegetarian",
    "no-gluten": "no-gluten",
    "halal": "halal",
}

ALLERGEN_SLUGS = {
    "coconut": "coconut",
    "shellfish": "shellfish",
    "dairy": "dairy",
    "eggs": "eggs",
    "fish": "fish",
    "peanuts": "peanuts",
    "sesame": "sesame",
    "soy": "soy",
    "tree-nuts": "tree-nuts",
    "wheat": "wheat",
}


# ---------------------------------------------------------------------------
# CSV Parsers
# ---------------------------------------------------------------------------

def parse_hours_csv():
    """Parse locations CSV → {name: {address, hours}}."""
    locations = {}
    if not os.path.exists(LOCATIONS_CSV):
        print(f"  WARNING: {LOCATIONS_CSV} not found")
        return locations
    with open(LOCATIONS_CSV, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            name = row.get("Name", "").strip()
            if name:
                locations[name] = {
                    "address": row.get("Location", "").strip(),
                    "hours": row.get("Hours of Operation (Spring 2026)", "").strip(),
                }
    return locations


def parse_meal_plans_csv():
    """Parse meal-plans CSV → dict keyed by plan name."""
    plans = {}
    if not os.path.exists(MEAL_PLANS_CSV):
        print(f"  WARNING: {MEAL_PLANS_CSV} not found")
        return plans
    with open(MEAL_PLANS_CSV, newline="", encoding="utf-8") as f:
        rows = list(csv.reader(f))
    if len(rows) < 2:
        return plans
    plan_names = [h.strip() for h in rows[0][1:]]
    for pn in plan_names:
        plans[pn] = {}
    for row in rows[1:]:
        component = row[0].strip()
        for i, pn in enumerate(plan_names):
            plans[pn][component] = row[i + 1].strip() if i + 1 < len(row) else ""
    return plans


# ---------------------------------------------------------------------------
# Nutrislice API
# ---------------------------------------------------------------------------

def fetch_menu(slug, date_str, menu_type="all-day"):
    """Fetch weekly menu for a location from Nutrislice API."""
    url = f"{API_BASE}/{slug}/menu-type/{menu_type}/{date_str}/"
    req = Request(url, headers={"User-Agent": "OSU-Meal-Planner/1.0"})
    try:
        with urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except HTTPError as e:
        if e.code == 404:
            return None
        print(f"    HTTP {e.code} for {slug}")
        return None
    except (URLError, Exception) as e:
        print(f"    Error fetching {slug}: {e}")
        return None


def extract_items_from_api(data, location_name, location_info, seen=None):
    """Parse Nutrislice API response into our item format."""
    items = []
    if seen is None:
        seen = set()

    if not data or "days" not in data:
        return items

    for day in data["days"]:
        current_section = ""
        for menu_item in day.get("menu_items", []):
            if menu_item.get("is_section_title"):
                current_section = menu_item.get("text", "").strip()
                continue

            food = menu_item.get("food")
            if not food:
                continue

            name = food.get("name", "").strip()
            if not name:
                continue

            nutrition = food.get("rounded_nutrition_info", {})
            if not nutrition:
                continue

            calories = int(nutrition.get("calories", 0) or 0)
            protein = int(nutrition.get("g_protein", 0) or 0)
            carbs = int(nutrition.get("g_carbs", 0) or 0)

            # Dedup by (name, cal, carbs, protein)
            key = (name.lower(), calories, carbs, protein)
            if key in seen:
                continue
            seen.add(key)

            # Extract dietary and allergen flags from food icons
            dietary = {tag: False for tag in DIETARY_SLUGS}
            allergens = {tag: False for tag in ALLERGEN_SLUGS}
            
            icons = food.get("icons", {}).get("food_icons", [])
            for icon in icons:
                slug = icon.get("slug", "")
                
                # Check dietary flags
                for tag, tag_slug in DIETARY_SLUGS.items():
                    if slug == tag_slug:
                        dietary[tag] = True
                        
                # Check allergen flags
                for tag, tag_slug in ALLERGEN_SLUGS.items():
                    if slug == tag_slug:
                        allergens[tag] = True

            items.append({
                "place": location_name,
                "item": name,
                "section": current_section,
                "nutrition": {
                    "protein": protein,
                    "carbs": carbs,
                    "calories": calories,
                },
                "hours": location_info.get("hours", ""),
                "address": location_info.get("address", ""),
                "dietary": dietary,
                "allergens": allergens,
            })

    return items


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    os.makedirs(DATA_DIR, exist_ok=True)

    print("=" * 60)
    print("  OSU Meal Planner — Data Extraction (Nutrislice API)")
    print("=" * 60)

    # 1. Locations CSV
    print("\n1. Parsing locations CSV...")
    locations = parse_hours_csv()
    print(f"   Found {len(locations)} dining locations")

    # 2. Meal plans CSV
    print("\n2. Parsing meal plans CSV...")
    meal_plans = parse_meal_plans_csv()
    print(f"   Found {len(meal_plans)} meal plans: {', '.join(meal_plans.keys())}")
    meal_plans_out = os.path.join(DATA_DIR, "meal_plans.json")
    with open(meal_plans_out, "w", encoding="utf-8") as f:
        json.dump(meal_plans, f, indent=2, ensure_ascii=False)
    print(f"   → {meal_plans_out}")

    # 3. Fetch menus from Nutrislice API
    today = datetime.now()
    date_str = today.strftime("%Y/%m/%d")
    print(f"\n3. Fetching menus from Nutrislice API (week of {date_str})...")

    all_items = []
    success_count = 0
    fail_count = 0

    for loc_name, slug in sorted(LOCATION_SLUGS.items()):
        loc_info = locations.get(loc_name, {})
        print(f"   [{slug}] → {loc_name}...", end=" ", flush=True)

        menu_types_to_try = ["all-day"]
        if "traditions" in slug:
            menu_types_to_try = ["breakfast", "lunch", "dinner"]

        loc_items = []
        loc_seen = set()
        
        for m_type in menu_types_to_try:
            data = fetch_menu(slug, date_str, menu_type=m_type)
            if data:
                items = extract_items_from_api(data, loc_name, loc_info, seen=loc_seen)
                loc_items.extend(items)
            
            time.sleep(1) # Rate limit

        if loc_items:
            print(f"✓ {len(loc_items)} items")
            all_items.extend(loc_items)
            success_count += 1
        else:
            print("✗ no data")
            fail_count += 1

        # Rate limit: small delay between requests
        time.sleep(1)

    # 4. Write output
    meals_out = os.path.join(DATA_DIR, "osu_meals.json")
    with open(meals_out, "w", encoding="utf-8") as f:
        json.dump(all_items, f, indent=2, ensure_ascii=False)

    print(f"\n{'=' * 60}")
    print(f"  TOTAL: {len(all_items)} menu items from {success_count} locations")
    print(f"  Failed: {fail_count} locations")
    print(f"  Output: {meals_out}")
    print(f"{'=' * 60}")


if __name__ == "__main__":
    main()
