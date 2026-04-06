"""
BuckeyeMealPlanner — Flask Application
Generates personalized weekly meal plans using Google Gemini.
"""

import json
import os
import re
import traceback
import urllib.parse
import uuid
from collections import OrderedDict
from datetime import date, datetime, timedelta

from dotenv import load_dotenv
from flask import Flask, render_template, request, jsonify, session, redirect, url_for, Response, stream_with_context

load_dotenv()

app = Flask(__name__)
app.secret_key = os.environ.get("SECRET_KEY", os.urandom(32))

# Module-level storage for plan results (Flask cookie-session can't be
# modified inside a streaming generator because headers are already sent).
_plan_results = {}

# Stores the last plan result per export_id so /api/export-calendar can access
# it after show_plan has already popped the entry from _plan_results.
# Capped at 500 entries (OrderedDict evicts oldest when full) to prevent unbounded growth.
_EXPORT_CACHE_MAX = 500
_export_cache: OrderedDict = OrderedDict()

# ---------------------------------------------------------------------------
# Load data
# ---------------------------------------------------------------------------
DATA_DIR = os.path.join(os.path.dirname(__file__), "data")


def load_json(filename):
    path = os.path.join(DATA_DIR, filename)
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    return {} if filename.endswith("plans.json") else []


MEALS = load_json("osu_meals_classified.json")
MEAL_PLANS = load_json("meal_plans.json")

# ---------------------------------------------------------------------------
# Campus area mapping (corrected per user)
# ---------------------------------------------------------------------------
CAMPUS_AREAS = {
    "South": [
        "Crane Café",
        "Traditions at Kennedy",
        "Postle Café",
        "Union Market",
        "Marketplace C-Store",
        "12th Avenue Bread Company",
        "Sloopy's Diner",
        "Mirror Lake Eatery",
        "Woody's Tavern",
    ],
    "North": [
        "Curl Market",
        "Connecting Grounds",
        "Terra Byte Café",
        "The Campus Grind – McPherson",
        "Traditions at Scott",
        "KSA Café",
        "Juice North",
    ],
    "West": [
        "Berry Café",
        "Hamilton Café",
        "Courtside Café",
        "CFAES Café",
        "Café Carmenton",
        "Oxley's by the Numbers",
    ],
}

# Reverse lookup: place → area
PLACE_TO_AREA = {}
for area, places in CAMPUS_AREAS.items():
    for p in places:
        PLACE_TO_AREA[p] = area

# ---------------------------------------------------------------------------
# USDA Dietary Guidelines for Americans 2020-2025 (adults 19-30)
# ---------------------------------------------------------------------------
USDA_DEFAULTS = {
    "calories": 2200,    # 2,000–2,400 range, midpoint
    "min_protein": 50,   # 46-56g/day RDA
    "max_carbs": 300,    # ~45-65% of 2200 cal
}

# ---------------------------------------------------------------------------
# Google Gemini client
# ---------------------------------------------------------------------------

_gemini_client = None


def get_gemini_model():
    """Lazy-initialize Google Gemini client."""
    global _gemini_client
    if _gemini_client is not None:
        return _gemini_client

    api_key = os.getenv("GEMINI_API_KEY")

    if not api_key:
        raise RuntimeError(
            "Missing GEMINI_API_KEY in environment. "
            "Copy .env.example → .env and fill in your credentials."
        )

    from google import genai
    _gemini_client = genai.Client(api_key=api_key)
    
    return _gemini_client


# ---------------------------------------------------------------------------
# WatsonX (Legacy Implementation - Preserved for Hackathon Judging)
# ---------------------------------------------------------------------------
"""
_wx_model = None

def get_watsonx_model():
    global _wx_model
    if _wx_model is not None:
        return _wx_model

    api_key = os.getenv("WATSONX_API_KEY")
    project_id = os.getenv("WATSONX_PROJECT_ID")
    url = os.getenv("WATSONX_URL", "https://us-south.ml.cloud.ibm.com")

    from ibm_watsonx_ai.foundation_models import ModelInference
    from ibm_watsonx_ai import Credentials

    credentials = Credentials(url=url, api_key=api_key)
    _wx_model = ModelInference(
        model_id="meta-llama/llama-3-3-70b-instruct",
        credentials=credentials,
        project_id=project_id,
        params={
            "max_tokens": 4096,
            "temperature": 0.2,
        },
    )
    return _wx_model
"""


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def get_places_for_areas(areas):
    """Get list of place names for given campus area names."""
    places = []
    for area in areas:
        places.extend(CAMPUS_AREAS.get(area, []))
    return places


def _hhmm_to_12h(hhmm_str):
    """Convert '08:00' (24-hour <input type=time>) to '8:00 AM' (12-hour display)."""
    if not hhmm_str:
        return None
    parts = hhmm_str.split(":")
    if len(parts) != 2:
        return None
    try:
        h, m = int(parts[0]), int(parts[1])
    except ValueError:
        return None
    period = "AM" if h < 12 else "PM"
    h12 = h % 12 or 12
    return f"{h12}:{m:02d} {period}"


def build_gcal_link(day_name, meals, event_date):
    """Build a Google Calendar deep link for all meals in a single day (all-day event)."""
    details = "\n".join(
        f"{m.get('time', '')} \u2013 {m.get('item', '')} @ {m.get('place', '')} "
        f"({m.get('calories', '')} cal, {m.get('protein', '')}g protein)"
        for m in meals
    )
    locations = ", ".join(dict.fromkeys(m.get("place", "") for m in meals if m.get("place")))
    date_str = event_date.strftime("%Y%m%d")
    next_date_str = (event_date + timedelta(days=1)).strftime("%Y%m%d")
    params = {
        "action": "TEMPLATE",
        "text": f"{day_name} Meals \u2013 BuckeyeMealPlanner",
        "dates": f"{date_str}/{next_date_str}",
        "details": details,
        "location": locations,
    }
    return "https://calendar.google.com/calendar/render?" + urllib.parse.urlencode(params)


def build_ics(result):
    """Build an iCalendar (.ics) byte string from a plan result dict.

    Each meal becomes a VEVENT starting at the meal's scheduled time on the
    corresponding day of the current week (Monday-based).
    """
    from icalendar import Calendar, Event as IcsEvent

    plan_data = result.get("plan", {})
    cal = Calendar()
    cal.add("prodid", "-//BuckeyeMealPlanner//EN")
    cal.add("version", "2.0")
    cal.add("x-wr-calname", "BuckeyeMealPlanner Weekly Plan")

    today = date.today()
    week_monday = today - timedelta(days=today.weekday())

    for day_name, meals in plan_data.get("plan", {}).items():
        offset = DAY_NAME_TO_NUM.get(day_name, 0)
        event_date = week_monday + timedelta(days=offset)

        for i, meal in enumerate(meals):
            ev = IcsEvent()
            ev.add("summary", f"{meal.get('item', 'Meal')} @ {meal.get('place', '')}")

            mins = parse_time_to_minutes(meal.get("time", ""))
            if mins is not None:
                h, m = divmod(mins, 60)
                dt_start = datetime(event_date.year, event_date.month, event_date.day, h, m)
            else:
                dt_start = datetime(event_date.year, event_date.month, event_date.day, 12, 0)
            dt_end = dt_start + timedelta(minutes=30)

            ev.add("dtstart", dt_start)
            ev.add("dtend", dt_end)
            ev.add("location", meal.get("place", ""))
            ev.add(
                "description",
                f"Protein: {meal.get('protein', 0)}g | Carbs: {meal.get('carbs', 0)}g | Calories: {meal.get('calories', 0)}",
            )
            ev.add("uid", f"bmp-{day_name}-{i}-{event_date.isoformat()}@buckeyemealplanner")
            cal.add_component(ev)

    return cal.to_ical()


def filter_meals(diet, allergens=None, places=None):
    """Filter meals by dietary preference and optionally by place."""
    filtered = MEALS

    if places:
        filtered = [m for m in filtered if m["place"] in places]

    if diet == "vegan":
        filtered = [m for m in filtered if m.get("dietary", {}).get("vegan")]
    elif diet == "vegetarian":
        filtered = [m for m in filtered if m.get("dietary", {}).get("vegetarian")]
    elif diet == "gluten-free":
        filtered = [m for m in filtered if m.get("dietary", {}).get("no-gluten")]
    elif diet == "halal":
        filtered = [m for m in filtered if m.get("dietary", {}).get("halal")]

    # Filter out any meals containing the user's selected allergens to avoid
    if allergens:
        for allergen in allergens:
            filtered = [m for m in filtered if not m.get("allergens", {}).get(allergen, False)]

    return filtered


# ---------------------------------------------------------------------------
# Hours-aware scheduling helpers
# ---------------------------------------------------------------------------

DAY_ABBREV_TO_NUM = {
    "Mon": 0, "Tue": 1, "Wed": 2, "Thu": 3, "Fri": 4, "Sat": 5, "Sun": 6,
}
DAY_NAME_TO_NUM = {
    "Monday": 0, "Tuesday": 1, "Wednesday": 2, "Thursday": 3,
    "Friday": 4, "Saturday": 5, "Sunday": 6,
}


def parse_time_to_minutes(time_str):
    """Convert a time string like '8:00 AM', '7am', '10:30pm' to minutes since midnight."""
    if not time_str:
        return None
    t = time_str.strip().lower().replace(".", "")
    m = re.match(r"(\d{1,2})(?::(\d{2}))?\s*(am|pm)", t)
    if not m:
        return None
    hour = int(m.group(1))
    minute = int(m.group(2)) if m.group(2) else 0
    period = m.group(3)
    if period == "am" and hour == 12:
        hour = 0
    elif period == "pm" and hour != 12:
        hour += 12
    return hour * 60 + minute


def _expand_day_range(day_range_str):
    """Expand a day range like 'Mon–Fri' or 'Sat' into a set of day numbers."""
    s = day_range_str.strip()
    # Replace en-dash with hyphen for uniform handling
    s = s.replace("\u2013", "-").replace("\u2014", "-")

    # Check for range (e.g., "Mon-Fri")
    if "-" in s:
        parts = [p.strip() for p in s.split("-", 1)]
        start = DAY_ABBREV_TO_NUM.get(parts[0])
        end = DAY_ABBREV_TO_NUM.get(parts[1])
        if start is None or end is None:
            return set()
        if start <= end:
            return set(range(start, end + 1))
        else:
            # Wrap-around (e.g., Sun-Fri: 6,0,1,2,3,4)
            return set(range(start, 7)) | set(range(0, end + 1))

    # Single day (e.g., "Sat")
    num = DAY_ABBREV_TO_NUM.get(s)
    return {num} if num is not None else set()


def parse_hours_for_day(hours_str, day_name):
    """Parse an hours string and return open windows for the given day.

    Returns:
        list of (open_min, close_min) tuples — open windows for that day
        Empty list — location is explicitly closed
        None — hours data unavailable (treat as always open)
    """
    if not hours_str or not hours_str.strip():
        return None

    day_num = DAY_NAME_TO_NUM.get(day_name)
    if day_num is None:
        return None

    # Split by semicolons into segments
    segments = [seg.strip() for seg in hours_str.split(";")]

    for seg in segments:
        # Split on first colon to separate day-range from time-range
        # Handle time strings that also have colons (e.g., "10:30am")
        # The day part never has digits, so find the first ": " or ":" after letters
        colon_idx = seg.find(":")
        if colon_idx == -1:
            continue

        day_part = seg[:colon_idx].strip()
        time_part = seg[colon_idx + 1:].strip()

        # Expand the day range
        days = _expand_day_range(day_part)
        if day_num not in days:
            continue

        # Check if closed
        if time_part.lower() == "closed":
            return []

        # Parse time windows (split by &)
        windows = []
        for window_str in time_part.split("&"):
            window_str = window_str.strip()
            # Replace en-dash with hyphen
            window_str = window_str.replace("\u2013", "-").replace("\u2014", "-")
            # Split into open-close pair
            time_parts = [t.strip() for t in window_str.split("-")]
            if len(time_parts) != 2:
                continue
            open_min = parse_time_to_minutes(time_parts[0])
            close_min = parse_time_to_minutes(time_parts[1])
            if open_min is not None and close_min is not None:
                windows.append((open_min, close_min))

        return windows

    # Day not mentioned in any segment — treat as closed
    return []


def is_open_at(hours_str, day_name, time_str):
    """Check if a location is open at a given day/time.

    Returns True if open, True if hours unknown, False if closed.
    """
    windows = parse_hours_for_day(hours_str, day_name)
    if windows is None:
        return True  # unknown hours, give benefit of the doubt
    if not windows:
        return False  # explicitly closed

    meal_min = parse_time_to_minutes(time_str)
    if meal_min is None:
        return True  # can't parse time, give benefit of the doubt

    for open_min, close_min in windows:
        if open_min <= meal_min <= close_min:
            return True
    return False


def menu_type_matches_time(menu_types, time_str):
    """Check if a Traditions item's menu_type matches the scheduled time.

    breakfast → before 10:00 AM  (< 600)
    lunch     → 11:00 AM to 2:00 PM  (660–840)
    dinner    → 5:00 PM or later  (>= 1020)
    all-day   → always valid

    Transition gaps (10–11 AM and 2–5 PM) intentionally match NO
    period-specific type, so items aren't mis-scheduled.
    """
    if not menu_types:
        return True
    if "all-day" in menu_types:
        return True

    meal_min = parse_time_to_minutes(time_str)
    if meal_min is None:
        return True

    for mt in menu_types:
        if mt == "breakfast" and meal_min < 600:          # before 10:00 AM
            return True
        if mt == "lunch" and 660 <= meal_min <= 840:      # 11:00 AM to 2:00 PM
            return True
        if mt == "dinner" and meal_min >= 1020:            # 5:00 PM or later
            return True

    return False


def find_alternatives(target_cal, target_protein, target_carbs, diet, places=None, exclude_name=None, allergens=None):
    """Find menu items with similar nutritional values (±30%)."""
    candidates = filter_meals(diet, allergens, places)
    scored = []

    for m in candidates:
        n = m["nutrition"]
        if m.get("item_type") == "addon_ingredient":
            continue
        # Synthetic items might have None calories
        cal_val = n.get("calories") or 0
        if cal_val < 50:
            continue
        if exclude_name and m["item"].lower() == exclude_name.lower():
            continue

        # Score by how close nutrition is (lower = better match)
        cal_diff = abs(cal_val - target_cal) / max(target_cal, 1)
        # Handle potential None for protein/carbs in scoring too
        p_val = n.get("protein") or 0
        c_val = n.get("carbs") or 0
        prot_diff = abs(p_val - target_protein) / max(target_protein, 1)
        carb_diff = abs(c_val - target_carbs) / max(target_carbs, 1)
        score = cal_diff + prot_diff + carb_diff

        # Only include if within ±40% of calories
        if target_cal > 0 and abs(cal_val - target_cal) / target_cal > 0.4:
            continue

        scored.append((score, m))

    scored.sort(key=lambda x: x[0])
    # Return top 10 unique items
    seen = set()
    results = []
    for _, m in scored:
        key = m["item"].lower()
        if key in seen:
            continue
        seen.add(key)
        
        place = m.get("place", "")
        cash = 12.0 if "traditions" in place.lower() else 8.0
        dd = cash * 0.65
        cost_text = f"1 Swipe | ${cash:.2f} (${dd:.2f} DD)"

        results.append({
            "item": m["item"],
            "place": m["place"],
            "address": m.get("address", ""),
            "protein": m["nutrition"]["protein"],
            "carbs": m["nutrition"]["carbs"],
            "calories": m["nutrition"]["calories"],
            "cost_text": cost_text,
            "cash_price": cash
        })
        if len(results) >= 10:
            break
    return results


def build_prompt(inputs, constraints, available_meals, allergens=None):
    """Construct the prompt for meal plan generation."""
    meal_plan = inputs.get("meal_plan", "Scarlet 14")
    diet = inputs.get("diet", "non-vegetarian")
    weekday_meals = int(inputs.get("weekday_meals", "3"))
    weekend_meals = int(inputs.get("weekend_meals", "3"))

    # Resolve meal times early so _get_slot_tags can close over them
    _mt = inputs.get("meal_times", {})
    weekday_times = _mt.get("weekday", []) if isinstance(_mt, dict) else []
    weekend_times = _mt.get("weekend", []) if isinstance(_mt, dict) else []

    # Curate food list: separate into main items, required ingredients, and optional add-ons
    main_items = []
    required_addons = []
    optional_addons = []
    seen = set()

    # Stratified selection: Keep all synthetic items, and top-N by protein per location
    # to ensure diversity in the prompt (and that Traditions aren't buried).
    by_place = {}
    synthetic_items = []

    for m in available_meals:
        n = m["nutrition"]
        # Handle synthetic items with None nutrition (always keep them)
        if m.get("is_synthetic"):
            synthetic_items.append(m)
            continue

        cal_val = n.get("calories") or 0
        if cal_val < 10:
            continue
            
        key = (m["item"].lower(), m["place"].lower())
        if key in seen:
            continue
        seen.add(key)
        
        if m.get("item_type") == "addon_ingredient":
            if m.get("addon_type") == "required":
                required_addons.append(m)
            else:
                optional_addons.append(m)
        else:
            place = m["place"]
            if place not in by_place:
                by_place[place] = []
            by_place[place].append(m)

    # Pick top N items per location — Traditions locations get more slots
    TRADITIONS_PLACES = {"Traditions at Scott", "Traditions at Kennedy", "Traditions at Morrill"}
    TRADITIONS_PER_LOC = 16
    OTHER_PER_LOC = 5
    TOTAL_CAP = 180

    stratified_mains = []
    remaining_mains = []
    for place, items in by_place.items():
        items.sort(key=lambda x: x["nutrition"].get("protein") or 0, reverse=True)
        cap = TRADITIONS_PER_LOC if place in TRADITIONS_PLACES else OTHER_PER_LOC
        stratified_mains.extend(items[:cap])
        remaining_mains.extend(items[cap:])

    # Fill the rest of the main_items up to TOTAL_CAP (including synthetic)
    remaining_mains.sort(key=lambda x: x["nutrition"].get("protein") or 0, reverse=True)

    # Start with all synthetic items
    main_items = synthetic_items + stratified_mains

    # Add more high-protein items until we hit the cap or run out
    slots_left = max(0, TOTAL_CAP - len(main_items))
    main_items.extend(remaining_mains[:slots_left])

    # Final sort for the prompt
    main_items.sort(key=lambda x: (x.get("is_synthetic", False), x["nutrition"].get("protein") or 0), reverse=True)
    required_addons.sort(key=lambda x: x["item"])
    optional_addons.sort(key=lambda x: x["item"])

    # ------------------------------------------------------------------
    # Slot-availability filtering (when user specified meal times)
    # ------------------------------------------------------------------
    # For each item, compute which weekday and weekend meal slots it is
    # open for.  Items closed at ALL specified times are excluded entirely.
    # ------------------------------------------------------------------
    def _get_slot_tags(m):
        """Return (wd_open_slots, we_open_slots, should_exclude)."""
        if not weekday_times and not weekend_times:
            return [], [], False  # no time filtering requested

        hours = m.get("hours", "")
        wd_open = [i + 1 for i, t in enumerate(weekday_times)
                   if is_open_at(hours, "Monday", t)]
        we_open = [i + 1 for i, t in enumerate(weekend_times)
                   if is_open_at(hours, "Saturday", t)]

        wd_blocked = bool(weekday_times) and not wd_open
        we_blocked = bool(weekend_times) and not we_open

        if weekday_times and weekend_times:
            exclude = wd_blocked and we_blocked
        elif weekday_times:
            exclude = wd_blocked
        else:
            exclude = we_blocked

        return wd_open, we_open, exclude

    # Filter main_items and cache slot info keyed by object id
    _item_slots = {}
    filtered_mains = []
    for m in main_items:
        wd_open, we_open, exclude = _get_slot_tags(m)
        if exclude:
            continue
        _item_slots[id(m)] = (wd_open, we_open)
        filtered_mains.append(m)

    # Safety guard: if time-filtering leaves too few usable items on either
    # side (weekday or weekend), drop that side's constraint rather than
    # crashing.  We check each side independently so a valid weekday time
    # can still be honoured even if the weekend time is impossible.
    MIN_SIDE_ITEMS = 15
    _both_dropped = False  # True only when BOTH sides ran dry

    if weekday_times or weekend_times:
        wd_items = [m for m in filtered_mains
                    if _item_slots.get(id(m), ([], []))[0]]  # has at least one wd slot
        we_items = [m for m in filtered_mains
                    if _item_slots.get(id(m), ([], []))[1]]  # has at least one we slot

        if weekday_times and len(wd_items) < MIN_SIDE_ITEMS:
            weekday_times = []
        if weekend_times and len(we_items) < MIN_SIDE_ITEMS:
            weekend_times = []

        if not weekday_times and not weekend_times:
            # Both sides are impossible — revert to the unfiltered item list
            _item_slots = {}
            _both_dropped = True
            # main_items already holds the unfiltered set from before the loop
        else:
            main_items = filtered_mains
    else:
        main_items = filtered_mains

    # Separate Traditions items from main items for dedicated sections
    non_traditions = []
    traditions_breakfast = []
    traditions_lunch = []
    traditions_dinner = []

    for m in main_items:
        place = m.get("place", "")
        if place in TRADITIONS_PLACES:
            mtypes = m.get("menu_types", ["all-day"])
            if "all-day" in mtypes:
                traditions_breakfast.append(m)
                traditions_lunch.append(m)
                traditions_dinner.append(m)
            else:
                if "breakfast" in mtypes:
                    traditions_breakfast.append(m)
                if "lunch" in mtypes:
                    traditions_lunch.append(m)
                if "dinner" in mtypes:
                    traditions_dinner.append(m)
        else:
            non_traditions.append(m)

    def _format_item_line(m):
        n = m["nutrition"]
        p_val = n.get("protein") if n.get("protein") is not None else "?"
        c_val = n.get("carbs") if n.get("carbs") is not None else "?"
        cal_val = n.get("calories") if n.get("calories") is not None else "?"
        hours_info = m.get("hours", "")
        menu_types = m.get("menu_types", ["all-day"])
        mt_tag = f" ({'/'.join(menu_types)})" if menu_types != ["all-day"] else ""

        if weekday_times or weekend_times:
            wd_open, we_open = _item_slots.get(id(m), ([], []))
            slot_parts = []
            if weekday_times:
                slot_parts.append("WD:" + ("+".join(str(s) for s in wd_open) if wd_open else "none"))
            if weekend_times:
                slot_parts.append("WE:" + ("+".join(str(s) for s in we_open) if we_open else "none"))
            slot_tag = f" [Open: {', '.join(slot_parts)}]"
        else:
            slot_tag = ""

        return (
            f"- {m['item']} @ {m['place']} [{m.get('section', '')}] | "
            f"{cal_val} cal, {p_val}g P, {c_val}g C | "
            f"Hours: {hours_info or 'unknown'}{mt_tag}{slot_tag}"
        )

    meal_lines = ["\n[MAIN ITEMS]"]
    for m in non_traditions:
        meal_lines.append(_format_item_line(m))

    if traditions_breakfast:
        meal_lines.append("\n[TRADITIONS — BREAKFAST (7-10 AM)]")
        for m in traditions_breakfast:
            meal_lines.append(_format_item_line(m))

    if traditions_lunch:
        meal_lines.append("\n[TRADITIONS — LUNCH (11 AM-2 PM)]")
        for m in traditions_lunch:
            meal_lines.append(_format_item_line(m))

    if traditions_dinner:
        meal_lines.append("\n[TRADITIONS — DINNER (5-8 PM)]")
        for m in traditions_dinner:
            meal_lines.append(_format_item_line(m))

    if required_addons:
        meal_lines.append("\n[REQUIRED INGREDIENTS (Must select to build dish)]")
        # Increase limit for required addons as well
        for m in required_addons[:60]:
            n = m["nutrition"]
            p_val = n.get("protein") or 0
            c_val = n.get("carbs") or 0
            cal_val = n.get("calories") or 0
            meal_lines.append(f"- {m['item']} @ {m['place']} [{m.get('section', '')}] | {cal_val} cal, {p_val}g P, {c_val}g C")

    if optional_addons:
        meal_lines.append("\n[OPTIONAL ADD-ONS]")
        for m in optional_addons[:60]:
            n = m["nutrition"]
            p_val = n.get("protein") or 0
            c_val = n.get("carbs") or 0
            cal_val = n.get("calories") or 0
            meal_lines.append(f"- {m['item']} @ {m['place']} [{m.get('section', '')}] | {cal_val} cal, {p_val}g P, {c_val}g C")

    meals_text = "\n".join(meal_lines)

    # Build constraints — use USDA defaults if nothing toggled
    target_protein = int(constraints.get("min_protein", USDA_DEFAULTS['min_protein']))
    target_carbs = int(constraints.get("max_carbs", USDA_DEFAULTS['max_carbs']))
    target_cal = int(constraints.get("max_calories", USDA_DEFAULTS['calories']))

    constraint_lines = []
    if constraints.get("min_protein"):
        constraint_lines.append(f"- MINIMUM {target_protein}g protein PER DAY")
    if constraints.get("max_carbs"):
        constraint_lines.append(f"- MAXIMUM {target_carbs}g carbs PER DAY")
    if constraints.get("max_calories"):
        constraint_lines.append(f"- MAXIMUM {target_cal} calories PER DAY")

    if not constraint_lines:
        constraint_lines.append(
            f"- Target ~{target_cal} calories per day "
            f"(USDA Dietary Guidelines 2020-2025)"
        )
        constraint_lines.append(
            f"- MINIMUM {target_protein}g protein per day"
        )
        constraint_lines.append(
            f"- MAXIMUM {target_carbs}g carbs per day"
        )

    constraints_text = "\n".join(constraint_lines)
    
    # Calculate per-meal targets to help the LLM
    avg_meals = round((weekday_meals * 5 + weekend_meals * 2) / 7.0)
    pm_protein = round(target_protein / avg_meals)
    pm_carbs = round(target_carbs / avg_meals)
    pm_cal = round(target_cal / avg_meals)

    # Meal time preferences block
    if weekday_times or weekend_times:
        parts = ["\nMEAL TIME PREFERENCES:\nThe user eats at these specific times:\n"]
        if weekday_times:
            wday_lines = "\n".join(f"  - Meal {i+1}: {t}" for i, t in enumerate(weekday_times))
            parts.append(f"Weekday meals:\n{wday_lines}\n")
        else:
            parts.append("Weekday meals: spread across default breakfast/lunch/dinner windows.\n")
        if weekend_times:
            wend_lines = "\n".join(f"  - Meal {i+1}: {t}" for i, t in enumerate(weekend_times))
            parts.append(f"Weekend meals:\n{wend_lines}\n")
        else:
            parts.append("Weekend meals: spread across default breakfast/lunch/dinner windows.\n")
        parts.append(
            "Use EXACTLY these times as the \"time\" field values in the JSON output "
            "(weekday times for Mon–Fri, weekend times for Sat–Sun).\n"
        )
        meal_times_block = "".join(parts)
        time_instruction = (
            "7. Use the exact meal times from MEAL TIME PREFERENCES above as the \"time\" field "
            "for each meal (weekday times for Mon–Fri, weekend times for Sat–Sun)."
        )
        hours_instruction = (
            "9. IMPORTANT: Each item shows which meal slots it is available for in an [Open: ...] tag "
            "(e.g., '[Open: WD:1+2, WE:1]' means available for weekday meal slots 1 and 2, and weekend meal slot 1; "
            "'[Open: WD:none]' means the location is CLOSED during all weekday meal times you specified). "
            "You MUST only schedule an item for a slot number listed under its Open tag. "
            "Never use an item whose Open tag shows 'none' for that day type."
        )
    else:
        meal_times_block = ""
        time_instruction = (
            "7. Spread meals across breakfast (7-10 AM), lunch (11 AM-2 PM), dinner (5-8 PM). "
            "Do NOT schedule meals in transition gaps (10-11 AM or 2-5 PM)."
        )
        hours_instruction = (
            "9. IMPORTANT: Each item has operating hours listed. Do NOT schedule an item at a time "
            "when its location is closed. Weekend locations may have reduced hours or be closed entirely."
        )

    # If the safety guard dropped the time filter, override the instructions
    if _both_dropped:
        # Neither side had enough open items — fall back to generic scheduling
        meal_times_block = ""
        time_instruction = (
            "7. Spread meals across breakfast (7-10 AM), lunch (11 AM-2 PM), dinner (5-8 PM). "
            "NOTE: The user's requested meal times could not be honoured because too few "
            "restaurants matching the selected filters are open at those times. "
            "A default schedule has been used instead."
        )
        hours_instruction = (
            "9. IMPORTANT: Each item has operating hours listed. Do NOT schedule an item at a time "
            "when its location is closed. Weekend locations may have reduced hours or be closed entirely."
        )

    prompt = f"""Create a 7-day meal plan for an OSU student using ONLY items from the list below.

REQUIREMENTS:
- Monday through Friday: exactly {weekday_meals} meals each day
- Saturday and Sunday: exactly {weekend_meals} meals each day
- Diet: {diet}
- Meal Plan: {meal_plan}
{f"- ALLERGEN RESTRICTIONS: User is allergic to {', '.join(allergens)}. NEVER suggest items containing these allergens. The menu list below has been pre-filtered, but you MUST NOT invent or hallucinate any items not in the list." if allergens else ""}
- CRITICAL: ONLY use items from the provided menu list. Do NOT invent or hallucinate any items.
{meal_times_block}
DAILY NUTRITION TARGETS:
{constraints_text}

PER-MEAL GUIDELINES:
To successfully hit the daily targets, aim for approximately:
- {pm_protein}g or more protein per meal
- {pm_carbs}g or fewer carbs per meal
- {pm_cal} or fewer calories per meal

MENU ITEMS (use these exactly):
{meals_text}

INSTRUCTIONS:
1. Compose exactly {weekday_meals} meals per weekday and {weekend_meals} meals per weekend day.
2. Build each meal by selecting ONE [MAIN ITEM]. If that item's section has [REQUIRED INGREDIENTS] available from the EXACT SAME LOCATION, you MUST include at least one to build the base of the dish.
3. You may attach 0 or more [OPTIONAL ADD-ONS] from the EXACT SAME LOCATION AND EXACT SAME SECTION.
4. Combine the item names in the JSON (e.g., "Build Your Own Pizza + Pizza Crust + Pepperoni") and SUM their nutrition for that meal entry.
5. CRITICAL: For each day, actively sum the nutrition of the meals you selected. Ensure the total protein is >= {target_protein}g, carbs <= {target_carbs}g, and calories <= {target_cal}.
6. STRONGLY PREFER selecting 2-3 Traditions items per meal to maximize nutrition per swipe. Traditions locations (Scott, Kennedy, Morrill) are All-You-Care-To-Eat buffets.
{time_instruction}
8. Vary items day-to-day. Do not repeat the exact same base item on consecutive days.
{hours_instruction}
10. You MUST only use Traditions items from the section matching the meal time. Breakfast Traditions items go at breakfast (7-10 AM), lunch items at lunch (11 AM-2 PM), dinner items at dinner (5-8 PM).
11. Compute the 7-day average daily protein, carbs, and calories for the summary.

Return ONLY this JSON (no other text):
{{
  "summary": {{
    "daily_protein_avg": <number>,
    "daily_carbs_avg": <number>,
    "daily_calories_avg": <number>
  }},
  "plan": {{
    "Monday": [
      {{"time":"8:00 AM","place":"<location>","item":"<name>","protein":<g>,"carbs":<g>,"calories":<num>}},
      {{"time":"12:00 PM","place":"<location>","item":"<name>","protein":<g>,"carbs":<g>,"calories":<num>}},
      {{"time":"6:00 PM","place":"<location>","item":"<name>","protein":<g>,"carbs":<g>,"calories":<num>}}
    ],
    "Tuesday": [...],
    "Wednesday": [...],
    "Thursday": [...],
    "Friday": [...],
    "Saturday": [...],
    "Sunday": [...]
  }}
}}"""

    return prompt


def parse_ai_response(text):
    """Parse JSON from the AI response, handling potential formatting issues."""
    text = text.strip()
    text = re.sub(r"^```(?:json)?\s*", "", text)
    text = re.sub(r"\s*```$", "", text)

    match = re.search(r"\{.*\}", text, re.DOTALL)
    if match:
        try:
            return json.loads(match.group())
        except json.JSONDecodeError:
            pass

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        return None


def _fuzzy_find_meal(item_name, place, meal_lookup):
    """Try to find a meal in the lookup using progressively looser matching.

    Order: exact (item, place) → exact item name → substring (min 5 chars).
    Returns the matched meal record or None.
    """
    # 1. Exact (item, place)
    key = (item_name.lower(), place.lower())
    if key in meal_lookup:
        return meal_lookup[key]

    # 2. Exact item name, any place
    item_lower = item_name.lower()
    for k, v in meal_lookup.items():
        if k[0] == item_lower:
            return v

    # 3. Substring match (min 5 chars to avoid false positives)
    if len(item_lower) >= 5:
        for k, v in meal_lookup.items():
            if item_lower in k[0] or k[0] in item_lower:
                return v

    return None


def validate_and_fix_plan(plan_data, available_meals, diet, allergens, places):
    """Validate each meal's scheduled time against location hours and Traditions
    menu types. Replace invalid meals with suitable alternatives.

    Returns (plan_data, swaps_list).
    """
    # Build lookup from the already-filtered available_meals so fuzzy matching
    # never resolves to an item outside the user's diet/allergen constraints.
    meal_lookup = {}
    for m in available_meals:
        key = (m["item"].lower(), m["place"].lower())
        if key not in meal_lookup:
            meal_lookup[key] = m

    # Pre-filter available meals (already filtered by diet/allergens/places)
    valid_pool = [m for m in available_meals if m.get("item_type") != "addon_ingredient"]

    swaps = []

    if "plan" not in plan_data:
        return plan_data, swaps

    for day_name, meals in plan_data["plan"].items():
        if not isinstance(meals, list):
            continue
        for i, meal in enumerate(meals):
            item_name = meal.get("item", "")
            place = meal.get("place", "")
            time_str = meal.get("time", "")

            # Look up the item in our database using fuzzy matching
            db_item = _fuzzy_find_meal(item_name, place, meal_lookup)
            if not db_item:
                # Flag unmatched items instead of silently skipping
                swaps.append({
                    "day": day_name,
                    "time": time_str,
                    "original": f"{item_name} @ {place}",
                    "replacement": f"{item_name} @ {place}",
                    "reason": "not found in database (unverified)",
                })
                continue

            hours = db_item.get("hours", "")
            menu_types = db_item.get("menu_types", ["all-day"])

            hours_ok = is_open_at(hours, day_name, time_str)
            menu_ok = menu_type_matches_time(menu_types, time_str)

            if hours_ok and menu_ok:
                continue  # valid, no action needed

            reason = []
            if not hours_ok:
                reason.append("location closed")
            if not menu_ok:
                reason.append(f"menu type {menu_types} doesn't match time")

            # Find a replacement from the pool that IS valid at this day/time
            target_cal = meal.get("calories") or 0
            target_prot = meal.get("protein") or 0
            target_carbs = meal.get("carbs") or 0

            candidates = []
            for c in valid_pool:
                c_hours = c.get("hours", "")
                c_menu_types = c.get("menu_types", ["all-day"])
                c_cal = (c["nutrition"].get("calories") or 0)
                if c_cal < 50:
                    continue
                if not is_open_at(c_hours, day_name, time_str):
                    continue
                if not menu_type_matches_time(c_menu_types, time_str):
                    continue

                # Score by nutritional similarity
                cal_diff = abs(c_cal - target_cal) / max(target_cal, 1)
                p_diff = abs((c["nutrition"].get("protein") or 0) - target_prot) / max(target_prot, 1)
                c_diff = abs((c["nutrition"].get("carbs") or 0) - target_carbs) / max(target_carbs, 1)
                score = cal_diff + p_diff + c_diff
                candidates.append((score, c))

            if not candidates:
                continue  # no valid replacement found, keep original

            candidates.sort(key=lambda x: x[0])
            replacement = candidates[0][1]

            swaps.append({
                "day": day_name,
                "time": time_str,
                "original": f"{item_name} @ {place}",
                "replacement": f"{replacement['item']} @ {replacement['place']}",
                "reason": ", ".join(reason),
            })

            # Apply the swap
            meal["item"] = replacement["item"]
            meal["place"] = replacement["place"]
            meal["protein"] = replacement["nutrition"].get("protein") or 0
            meal["carbs"] = replacement["nutrition"].get("carbs") or 0
            meal["calories"] = replacement["nutrition"].get("calories") or 0

    return plan_data, swaps


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route("/")
def index():
    """Render the input form."""
    places = sorted(set(m["place"] for m in MEALS))
    return render_template(
        "index.html",
        meal_plans=MEAL_PLANS,
        places=places,
        total_items=len(MEALS),
        campus_areas=CAMPUS_AREAS,
        usda_defaults=USDA_DEFAULTS,
    )


def _generate_core(inputs, constraints, diet, allergens, places, available_meals):
    """Core meal-plan generation logic (shared by streaming and non-streaming paths).

    Returns (plan_dict, error_html_or_None).
    """
    # Build prompt and call Google Gemini 2.5 Flash
    prompt = build_prompt(inputs, constraints, available_meals, allergens)
    client = get_gemini_model()

    system_instruction = "You are a meal planning assistant. You respond ONLY with valid JSON, no markdown, no explanation, no extra text."

    try:
        chat_response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
            config={
                'system_instruction': system_instruction,
                'response_mime_type': 'application/json',
                'temperature': 0.2,
            }
        )
        response = chat_response.text

        # Log token usage and estimated cost (gemini-2.5-flash pricing as of 2026-03)
        # Prices: input $0.15/1M tokens (≤200K), output $0.60/1M tokens (non-thinking)
        usage = getattr(chat_response, 'usage_metadata', None)
        if usage:
            in_tok = getattr(usage, 'prompt_token_count', 0) or 0
            out_tok = getattr(usage, 'candidates_token_count', 0) or 0
            cost_usd = (in_tok * 0.15 + out_tok * 0.60) / 1_000_000
            print(
                f"[Gemini usage] input={in_tok} tokens, output={out_tok} tokens, "
                f"estimated_cost=${cost_usd:.5f}"
            )
    except Exception as e:
        print(f"Gemini API Error: {e}")
        response = ""

    # ---------------------------------------------------------------------------
    # WatsonX API Call (Legacy Implementation - Preserved for Hackathon Judging)
    # ---------------------------------------------------------------------------
    """
    model = get_watsonx_model()
    chat_response = model.chat(
        messages=[
            {"role": "system", "content": "You are a meal planning assistant. You respond ONLY with valid JSON, no markdown, no explanation, no extra text."},
            {"role": "user", "content": prompt},
        ]
    )
    response = chat_response.get("choices", [{}])[0].get("message", {}).get("content", "")
    """

    # Parse response
    plan = parse_ai_response(response)
    if not plan:
        return None, render_template(
            "error.html",
            error="Could not parse the AI-generated meal plan. Please try again.",
            raw_response=response,
        )

    # Server-side: validate hours / menu types and swap invalid meals
    if "plan" in plan:
        plan, hours_swaps = validate_and_fix_plan(
            plan, available_meals, diet, allergens, places
        )
        if hours_swaps:
            plan["hours_swaps"] = hours_swaps

    # Server-side: recompute daily averages from actual plan data, and inject pricing
    if "plan" in plan:
        daily_totals = []
        for day_name, meals in plan["plan"].items():
            if isinstance(meals, list):
                day_p = 0
                day_c = 0
                day_cal = 0
                for m in meals:
                    # Clean up item name: strip "@ Location" or "[Section]" if AI included them
                    item_name = m.get("item", "")
                    if " @ " in item_name:
                        item_name = item_name.split(" @ ")[0]
                    if " [" in item_name:
                        item_name = item_name.split(" [")[0]
                    m["item"] = item_name.strip()

                    place = m.get("place", "")

                    # Find matching address from MEALS database
                    address = ""
                    for src_meal in MEALS:
                        if src_meal.get("place") == place:
                            address = src_meal.get("address", "")
                            break
                    m["address"] = address

                    cash = 12.0 if "traditions" in place.lower() else 8.0
                    dd = cash * 0.65
                    m["cost_text"] = f"1 Swipe | ${cash:.2f} (${dd:.2f} DD)"
                    m["cash_price"] = cash

                    day_p += (m.get("protein") or 0)
                    day_c += (m.get("carbs") or 0)
                    day_cal += (m.get("calories") or 0)

                daily_totals.append((day_p, day_c, day_cal))
        if daily_totals:
            n = len(daily_totals)
            plan["summary"] = {
                "daily_protein_avg": round(sum(t[0] for t in daily_totals) / n),
                "daily_carbs_avg": round(sum(t[1] for t in daily_totals) / n),
                "daily_calories_avg": round(sum(t[2] for t in daily_totals) / n),
            }

        # --- Budget Calculation ---
        all_meals = []
        for day_name, meals in plan["plan"].items():
            if isinstance(meals, list):
                all_meals.extend(meals)

        # Sort meals by price descending to use swipes on expensive items first
        all_meals.sort(key=lambda m: m.get("cash_price", 8.0), reverse=True)

        plan_name = inputs["meal_plan"]
        plan_details = MEAL_PLANS.get(plan_name, {})

        swipes_str = plan_details.get("Visits", "0")
        if "Access every hour" in swipes_str or "Unlimited" in swipes_str:
            swipes_allowed = 999
        elif "Not Included" in swipes_str:
            swipes_allowed = 0
        else:
            swipes_allowed = int(swipes_str)

        dd_str = plan_details.get("Dining Dollars", "0").replace(",", "")
        try:
            dd_stipend = float(dd_str)
        except ValueError:
            dd_stipend = 0.0

        swipes_used = 0
        dd_used = 0.0

        for m in all_meals:
            cash = m.get("cash_price", 8.0)
            if swipes_used < swipes_allowed:
                swipes_used += 1
            else:
                dd_used += cash * 0.65

        dd_weekly_budget = round(dd_stipend / 15.0, 2)  # approx 15 weeks in semester

        plan_type = "swipes" if swipes_allowed > 0 else "dining_dollars"

        plan["budget"] = {
            "plan_type": plan_type,
            "swipes_allowed": "Unlimited" if swipes_allowed == 999 else swipes_allowed,
            "swipes_used": swipes_used,
            "dd_stipend": dd_stipend,
            "dd_used_weekly": round(dd_used, 2),
            "dd_weekly_budget": dd_weekly_budget,
            "dd_difference": round(dd_weekly_budget - dd_used, 2)
        }

    return plan, None


# Loading page sent immediately to keep the browser connection alive
_LOADING_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Generating... | BuckeyeMealPlanner</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="/static/style.css">
<style>
body{margin:0;display:flex;align-items:center;justify-content:center;min-height:100vh;background:#121212}
.ld{text-align:center;max-width:360px;padding:2rem}
.ld h2{color:#fff;font-size:1.25rem;font-weight:600;margin-bottom:0.25rem;transition:opacity .3s ease}
.ld p{color:#aaa;font-size:0.85rem}
</style>
</head>
<body>
<div class="ld">
  <div class="spinner-border text-light mb-3" style="width:3rem;height:3rem" role="status"></div>
  <h2 id="ph">Warming up the grill...</h2>
  <p>This usually takes 1-2 minutes</p>
</div>
<script>
var phrases=[
"Warming up the grill...","Tossing the salad...","Balancing your macros...",
"Scouting Traditions menus...","Preheating the oven...","Chopping fresh veggies...",
"Seasoning to perfection...","Plating your week...","Stirring the soup pot...",
"Checking what's open on campus...","Rolling the perfect burrito...",
"Simmering the stir-fry...","Flipping pancakes...","Brewing something good...",
"Counting every gram of protein...","Slicing the avocado...",
"Taste-testing for quality...","Crunching the nutrition numbers...",
"Drizzling the finishing sauce...","Almost ready to serve..."
];
var i=0,el=document.getElementById("ph");
setInterval(function(){i=(i+1)%phrases.length;el.style.opacity=0;setTimeout(function(){el.textContent=phrases[i];el.style.opacity=1},300)},2500);
</script>
<!-- keep-alive -->
"""

# Invisible comment sent every 5 seconds to keep Safari from timing out
_KEEP_ALIVE = "<!-- k -->\n"


@app.route("/generate", methods=["POST"])
def generate():
    """Stream a loading page, generate the plan, then redirect via JS."""
    # ---- Capture form data before entering the generator ----
    inputs = {
        "meal_plan": request.form.get("meal_plan", "Scarlet 14"),
        "diet": request.form.get("diet", "non-vegetarian"),
        "weekday_meals": request.form.get("weekday_meals", "3"),
        "weekend_meals": request.form.get("weekend_meals", "3"),
    }
    campus_areas = request.form.getlist("campus_areas")
    if not campus_areas:
        campus_areas = list(CAMPUS_AREAS.keys())
    inputs["campus_areas"] = campus_areas

    constraints = {}
    if request.form.get("use_protein") == "on":
        constraints["min_protein"] = request.form.get("min_protein", "100")
    if request.form.get("use_carbs") == "on":
        constraints["max_carbs"] = request.form.get("max_carbs", "300")
    if request.form.get("use_calories") == "on":
        constraints["max_calories"] = request.form.get("max_calories", "2500")

    weekday_count = int(inputs.get("weekday_meals", 3))
    weekend_count = int(inputs.get("weekend_meals", 2))
    meal_times = {"weekday": [], "weekend": []}
    for i in range(1, weekday_count + 1):
        val = request.form.get(f"weekday_meal_{i}_time", "").strip()
        converted = _hhmm_to_12h(val)
        if converted:
            meal_times["weekday"].append(converted)
    for i in range(1, weekend_count + 1):
        val = request.form.get(f"weekend_meal_{i}_time", "").strip()
        converted = _hhmm_to_12h(val)
        if converted:
            meal_times["weekend"].append(converted)
    inputs["meal_times"] = meal_times

    diet = inputs["diet"]
    allergens = request.form.getlist("allergens")
    inputs["allergens"] = allergens
    places = get_places_for_areas(campus_areas)
    available_meals = filter_meals(diet, allergens, places)

    MIN_MEALS_FOR_GENERATION = 20
    if len(available_meals) < MIN_MEALS_FOR_GENERATION:
        if not available_meals:
            msg = (
                "No meals found matching your campus areas, dietary preference, and allergen restrictions. "
                "Try selecting more campus areas or removing some restrictions."
            )
        else:
            msg = (
                f"Only {len(available_meals)} menu item(s) matched your filters — not enough to build a "
                f"varied 7-day plan (minimum {MIN_MEALS_FOR_GENERATION} required). "
                "Try selecting more campus areas, a less restrictive diet, or fewer allergens to avoid."
            )
        return render_template("error.html", error=msg)

    import time as _time

    # Generate a unique key and store it in the session *before* streaming
    # starts.  Flask's cookie-session encodes headers on the first yield,
    # so any session mutations inside the generator are silently lost.
    plan_id = str(uuid.uuid4())
    session["plan_id"] = plan_id

    def stream():
        # 1. Send the loading page immediately
        yield _LOADING_PAGE

        # 2. Run generation in a background thread, sending keep-alive
        import threading
        result_holder = {}

        def _run():
            try:
                plan, error_html = _generate_core(
                    inputs, constraints, diet, allergens, places, available_meals
                )
                result_holder["plan"] = plan
                result_holder["error"] = error_html
            except Exception as exc:
                traceback.print_exc()
                result_holder["plan"] = None
                result_holder["exc"] = str(exc)

        t = threading.Thread(target=_run)
        t.start()

        # Keep connection alive while waiting
        while t.is_alive():
            yield _KEEP_ALIVE
            _time.sleep(5)
        t.join()

        plan = result_holder.get("plan")
        error_html = result_holder.get("error")
        exc_msg = result_holder.get("exc")

        if exc_msg:
            _plan_results[plan_id] = {"error": f"An error occurred: {exc_msg}"}
        elif error_html or not plan:
            _plan_results[plan_id] = {"error_html": error_html or ""}
        else:
            _plan_results[plan_id] = {
                "plan": plan,
                "inputs": inputs,
                "constraints": constraints,
            }

        yield '<script>window.location.replace("/plan");</script>\n'

    return Response(stream_with_context(stream()), mimetype="text/html")


@app.route("/plan")
def show_plan():
    """Display the generated meal plan from session data."""
    plan_id = session.pop("plan_id", None)
    if not plan_id:
        return redirect(url_for("index"))

    result = _plan_results.pop(plan_id, None)
    if not result:
        return redirect(url_for("index"))

    # Check for error states
    if "error" in result:
        return render_template("error.html", error=result["error"])
    if "error_html" in result:
        return result["error_html"]

    # Store for /api/export-calendar — bounded to prevent memory leak
    export_id = str(uuid.uuid4())
    session["export_plan_id"] = export_id
    _export_cache[export_id] = result
    if len(_export_cache) > _EXPORT_CACHE_MAX:
        _export_cache.popitem(last=False)  # evict oldest entry

    # Build Google Calendar deep links (one all-day event per day)
    today = date.today()
    week_monday = today - timedelta(days=today.weekday())
    gcal_links = {}
    for day_name, meals in result["plan"].get("plan", {}).items():
        if meals:
            event_date = week_monday + timedelta(days=DAY_NAME_TO_NUM.get(day_name, 0))
            gcal_links[day_name] = build_gcal_link(day_name, meals, event_date)

    return render_template(
        "plan.html",
        plan=result["plan"],
        inputs=result["inputs"],
        constraints=result["constraints"],
        gcal_links=gcal_links,
    )


@app.route("/api/alternatives", methods=["POST"])
def api_alternatives():
    """Find similar items for the swap feature."""
    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    cal = int(data.get("calories", 0))
    protein = int(data.get("protein", 0))
    carbs = int(data.get("carbs", 0))
    diet = data.get("diet", "non-vegetarian")
    campus_areas = data.get("campus_areas", list(CAMPUS_AREAS.keys()))
    allergens = data.get("allergens", [])
    exclude = data.get("exclude_item", "")

    places = get_places_for_areas(campus_areas)
    alternatives = find_alternatives(cal, protein, carbs, diet, places, exclude, allergens)

    return jsonify({"alternatives": alternatives})


@app.route("/api/export-calendar")
def api_export_calendar():
    """Download the active meal plan as an .ics file."""
    export_id = session.get("export_plan_id")
    if not export_id:
        return jsonify({"error": "No active meal plan. Generate a plan first."}), 400
    result = _export_cache.get(export_id)
    if not result:
        return jsonify({"error": "Plan not found. Please generate a new plan."}), 400

    try:
        ics_bytes = build_ics(result)
    except ImportError:
        return jsonify({"error": "Calendar export unavailable (missing icalendar package)."}), 500

    return Response(
        ics_bytes,
        mimetype="text/calendar",
        headers={"Content-Disposition": "attachment; filename=buckeye-meal-plan.ics"},
    )


@app.route("/api/export-pdf")
def api_export_pdf():
    """Download the active meal plan as a PDF file."""
    export_id = session.get("export_plan_id")
    if not export_id:
        return jsonify({"error": "No active meal plan. Generate a plan first."}), 400
    result = _export_cache.get(export_id)
    if not result:
        return jsonify({"error": "Plan not found. Please generate a new plan."}), 400

    try:
        from weasyprint import HTML as WeasyprintHTML
    except ImportError:
        return jsonify({"error": "PDF export unavailable (missing weasyprint package)."}), 500

    generated_date = date.today().strftime("%B %d, %Y")
    html_str = render_template(
        "meal_plan_pdf.html",
        plan=result["plan"],
        inputs=result["inputs"],
        generated_date=generated_date,
    )
    pdf_bytes = WeasyprintHTML(string=html_str, base_url=request.host_url).write_pdf()
    return Response(
        pdf_bytes,
        mimetype="application/pdf",
        headers={"Content-Disposition": "attachment; filename=meal-plan.pdf"},
    )


@app.route("/api/restaurant-hours")
def api_restaurant_hours():
    """Return the aggregate open time window for the given filters.

    Query params:
        campus_areas — comma-separated area names (e.g. "South,North")
        diet         — dietary preference (non-vegetarian, vegetarian, vegan, etc.)
        allergens    — comma-separated allergens to exclude

    Response shape:
        {
          "weekday": {"min": "07:00", "max": "23:00"},
          "weekend": {"min": "09:00", "max": "22:00"}
        }
    """
    areas_param = request.args.get("campus_areas", "")
    area_list = [a.strip() for a in areas_param.split(",") if a.strip()] if areas_param else []
    places = get_places_for_areas(area_list) if area_list else None

    diet = request.args.get("diet", "non-vegetarian")
    allergens_param = request.args.get("allergens", "")
    allergens = [a.strip() for a in allergens_param.split(",") if a.strip()] if allergens_param else []

    filtered = filter_meals(diet, allergens, places)

    def _aggregate(day_name):
        opens, closes = [], []
        for m in filtered:
            windows = parse_hours_for_day(m.get("hours", ""), day_name)
            if not windows:
                continue
            for o, c in windows:
                opens.append(o)
                closes.append(c)
        if not opens:
            return {"min": "07:00", "max": "23:00"}

        def mins_to_hhmm(mins):
            return f"{mins // 60:02d}:{mins % 60:02d}"

        return {"min": mins_to_hhmm(min(opens)), "max": mins_to_hhmm(max(closes))}

    return jsonify({
        "weekday": _aggregate("Monday"),
        "weekend": _aggregate("Saturday"),
    })


@app.route("/api/meals")
def api_meals():
    """API endpoint to get filtered meals."""
    diet = request.args.get("diet", "non-vegetarian")
    area = request.args.get("area")
    places = get_places_for_areas([area]) if area else None
    meals = filter_meals(diet, places)
    return jsonify({"count": len(meals), "meals": meals[:50]})


# ---------------------------------------------------------------------------
if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
