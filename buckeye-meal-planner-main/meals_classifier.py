"""
OSU Meals JSON Classifier
=========================
Adds 'item_type' and 'parent_item' fields to every item in an OSU dining JSON file.

- item_type: "item" (standalone menu item) or "addon_ingredient" (component/add-on/topping)
- parent_item: list of actual standalone item names from the same place + section that
               the add-on can be part of, or null for standalone items / add-ons with
               no parent items in their group.

Usage:
    python classify_osu_meals.py input.json output.json
    python classify_osu_meals.py osu_meals.json                  # outputs osu_meals_classified.json
"""

import json
import re
import sys
import os
from collections import defaultdict


# ============================================================
# ADD-ON / INGREDIENT NAME PATTERNS  (checked FIRST)
# ============================================================

ADDON_PATTERNS_RAW = [
    # --- Breads / bases / shells ---
    r'^(plain |jalapeno cheddar |honey wheat |everything |blueberry |cinnamon raisin |asiago cheese )?bagel$',
    r'^gluten free bagel thin$',
    r'^(flour |corn |gluten free )?tortilla$',
    r'^pizza dough$',
    r'^(gluten free )?cauliflower pizza crust$',
    r'^(rustic white|multigrain|multigrain seeded|ciabatta|gluten free hoagie|asiago ciabatta) (bun|bread|bread roll|roll)$',
    r'^gluten free hamburger bun$',
    r'^vegan brioche bun$',
    r'^pullman wheat bread$',
    r'^white bread$',
    r'^(italian |wheat |marble rye )?bread$',
    r'^dinner roll$',
    r'^texas toast$',
    r'^pita bread$',
    r'^naan bread$',
    r'^southern style biscuit$',
    r'^buttermilk biscuit$',
    r'^english muffin$',
    r'^croissant$',

    # --- Sauces / dressings / condiments ---
    r'sauce$',
    r'dressing$',
    r'^(roasted )?garlic oil$',
    r'^cholula hot sauce$',
    r'^pancake maple syrup$',
    r'^sugar free syrup$',
    r'^sour cream',
    r'^sour crema$',
    r'^pico de gallo$',
    r'^guacamole$',
    r'salsa\b',
    r'^sriracha',
    r'\bmayo\b',
    r'^ketchup$',
    r'^(yellow |dijon )?mustard$',
    r'^light mayonnaise$',
    r'^hot honey$',
    r'^clover honey$',
    r'^queso blanco$',

    # --- Cheeses ---
    r'cheese$',
    r'^(sliced |shredded |crumbled |diced )*(american|cheddar|provolone|swiss|gouda|mozzarella|pepper jack|feta|parmesan|white cheddar|monterey jack|vegan cheddar|plant-based cheddar)\b',

    # --- Proteins / meats as ingredients ---
    r'^(sliced |diced |halal )*(ham|turkey breast|pepperoni|salami|hard salami|pastrami)$',
    r'^bacon$',
    r'^turkey bacon$',
    r'^bacon crumbles$',
    r'^peppered bacon$',
    r'^(egg|vegan egg) patty$',
    r'^egg whites?$',
    r'^eggs?$',
    r'^(diced )?hard boiled eggs?$',
    r'^scrambled eggs?$',
    r'^(turkey |pork |plant-based )?sausage (patty|links|crumbles)$',
    r'^halal chicken sausage$',
    r'^sausage crumbles$',
    r'^(plant-based )?chorizo( crumble)?$',
    r'^diced (bacon|ham)$',
    # Build-your-own station proteins (fillings)
    r'^halal (taco |)beef$',
    r'^(mexican )?shredded chicken$',
    r'^(halal |sliced )?pepperoni$',
    r'^fajita chicken$',
    r'^(spicy )?chicken breast$',
    r'^grilled marinated chicken$',
    r'^(stir fry |teriyaki )?chicken$',
    r'^pork al pastor$',
    r'^mojo pork$',
    r'^ropa vieja$',
    r'^(plant-based |halal )?meatballs?$',
    r'^plant-based (meatball|tenders?)$',
    r'^(fried |gluten free )?breaded (chicken|tofu)$',
    r'^(fried )?gluten free breaded (chicken|tofu)$',
    r'^marinated tofu$',
    r'^cajun shrimp$',
    r'^curried chicken$',
    r'^halal meatballs$',
    r'^hoisin braised beef$',
    r'^plant-based bulgogi beef$',
    r'^beef brisket$',

    # --- Vegetables / toppings ---
    r'^(alfalfa )?sprouts$',
    r'^lettuce leaf$',
    r'^(shredded )?(romaine|iceberg)( lettuce)?$',
    r'^(diced |sliced |cooked |quick roasted )*(tomato|tomatoes|red onion|yellow onion|mushrooms?|green bell pepper|red bell pepper|jalapeno)s?$',
    r'^spinach$',
    r'^avocado$',
    r'^(steamed )?broccoli$',
    r'^baby corn$',
    r'^snow peas$',
    r'^bean sprouts$',
    r'^water chestnuts$',
    r'^green onion$',
    r'^(shredded )?kale',
    r'^napa cabbage$',
    r'^english cucumber$',
    r'^pickles?$',
    r'^(crinkle cut )?pickle (chips|stackers)$',
    r'^(banana|cherry|roasted|roasted red) peppers?$',
    r'^artichoke hearts$',
    r'^(diced )?(sweet potato|sweet potato hash)$',
    r'^celery$',
    r'^baby carrots$',
    r'^grape tomato$',
    r'^(green |red )?bell pepper$',
    r'^red onion$',
    r'^peppers & onions$',
    r'^(sliced )?black olives$',
    r'^fajita vegetable blend$',
    r'^italian green beans$',
    r'^garlic green beans$',
    r'^(low sodium |refried |black |pinto |corn & black bean )?(beans|bean)$',
    r'^spicy roasted potatoes$',
    r'^corn salsa$',
    r'^corn & black bean salsa$',

    # --- Fruits as toppings ---
    r'^(watermelon|cantaloupe|pineapple|honeydew) (chunks?)?$',
    r'^cantaloupe$',
    r'^honeydew$',
    r'^strawberries$',
    r'^blueberries$',
    r'^(apple slices|red seedless grapes|mandarin oranges|grapefruit|cubed mango|dragon fruit cubes|diced apples)$',
    r'^banana$',

    # --- Spreads ---
    r'^(plain |strawberry |plant-based )?cream cheese$',
    r'^butter$',
    r'^salted butter$',
    r'^(creamy )?peanut butter$',
    r'^almond butter$',
    r'^nutella$',
    r'^(grape )?jelly$',
    r'^strawberry preserves$',
    r'^(roasted )?(garlic |red pepper )?hummus$',

    # --- Toppings / mix-ins / garnishes ---
    r'^(chocolate |mini chocolate )?chips$',
    r'^oreo topping$',
    r'^golden raisins$',
    r'^dried (sweetened )?cranberries$',
    r'^chia seeds$',
    r'^(gluten free )?granola$',
    r'^ground cinnamon$',
    r'^light brown sugar$',
    r'^whipped cream$',
    r'^(vanilla |strawberry )?yogurt$',
    r'^(plain |fat-free plain )?greek yogurt$',
    r'^vanilla pudding$',
    r'^sweetened shredded coconut$',
    r'^everything bagel seasoning$',
    r'^pistachios$',
    r'^almonds$',
    r'^(white )?sesame seeds$',
    r'^(tri[- ]?color )?(salted )?tortilla (strips|chips)$',
    r'^coconut chia pudding$',
    r'^cottage cheese$',

    # --- Noodles / rice / base starches ---
    r'^(rice|lo mein|ramen) noodles$',
    r'^(jasmine|brown|basmati|cilantro lime|cilantro lime brown|steamed white) rice$',
    r'^(cavatappi|penne|red lentil) pasta$',
    r'^garlic bread$',
    r'^(white corn |yellow )?tortilla chips$',

    # --- Milk types ---
    r'^(2%|skim|soy|oat|almond|whole) milk$',

    # --- Syrups / flavor shots ---
    r'syrup$',

    # --- Supplements / protein ---
    r'supplement$',
    r'^whey protein$',
    r'^soy protein',

    # --- Cooking oils ---
    r'^oil$',
    r'^oil pan spray$',
    r'^(extra virgin )?olive oil$',

    # --- Seasonings ---
    r'seasoning$',

    # --- Other toppings / garnishes ---
    r'^(light amber )?honey$',
    r'^pumpkin seeds$',
    r'^energy bite crumble$',
    r'^peanut butter cup$',
    r'^(cilantro|lemongrass)$',
    r'^minced (ginger|garlic)$',

    # --- Other individual components ---
    r'^gluten free tamari soy sauce$',
    r'^kimchi$',
    r'^sweet soy sauce$',
    r'^tamari kale$',
    r'^jalapeno pepper$',
]

ADDON_PATTERNS = [re.compile(p, re.IGNORECASE) for p in ADDON_PATTERNS_RAW]


# ============================================================
# STANDALONE ITEM NAME PATTERNS  (checked SECOND)
# ============================================================
# These identify names that are clearly complete dishes.

ITEM_KEYWORDS = [
    r'\bsandwich\b',
    r'\bsub\b',
    r'\bbowl\b',
    r'\bburger\b',
    r'\bwrap\b',
    r'\bquesadilla\b',
    r'\bpizza\b',
    r'\bhot dog\b',
    r'\bpretzel dog\b',
    r'\bbento\b',
    r'\bramen\b',
    r'\byakisoba\b',
    r'\bstir.?fry\b',
    r'\bskillet\b',
    r'\bparfait\b',
    r'\bsmoothie\b',
    r'\bmocha\b',
    r'\blatte\b',
    r'\bcappuccino\b',
    r'\bchai\b',
    r'\bnuggets\b',
    r'\btenders\b',
    r'\bwings\b',
    r'\bpancakes?\b',
    r'\bwaffle\b',
    r'\bfrench toast\b',
    r'\bscramble\b',
    r'\bfrittata\b',
    r'\bblintz\b',
    r'\bomelette?\b',
    r'\bbread pudding\b',
    r'\bmonkey bread\b',
    r'\bdonut\b',
    r'\bcinnamon roll\b',
    r'\bchurros?\b',
    r'\boats & yogurt\b',
    r'\bovernight oats?\b',
    r'\boatmeal\b',
    r'\bbiryani\b',
    r'\bmasala\b',
    r'\bsamosa\b',
    r'\bgumbo\b',
    r'\bpierog',
    r'\bchicken parmesan\b',
    r'\beggplant parmesan\b',
    r'\breuben\b',
    r'\bcalzone\b',
    r'\bstromboli\b',
    r'\bbreadstick',
    r'\bflatbread\b',
    r'\bcroissant sandwich\b',
    r'\bbagel sandwich\b',
    r'\bpinwheel\b',
    r'\bmacaroni salad\b',
    r'\bpotato salad\b',
]

ITEM_PATTERNS = [re.compile(p, re.IGNORECASE) for p in ITEM_KEYWORDS]

# Brand prefixes that always indicate a complete menu item
BRAND_PREFIXES = [
    "donato's", "sloopy's", "woody's", "farmer's fridge",
]


# ============================================================
# SECTIONS THAT ARE ENTIRELY STANDALONE ITEMS
# ============================================================

ALL_ITEMS_SECTIONS = {
    "Featured Items", "Ramen Bowls", "Rice Bowls", "Yakisoba",
    "Bento Box", "Stir-Fry", "Wings", "Hot Dogs",
    "Tenders and Fries", "Panini",
    "Grab & Go", "Farmer's Fridge", "Gluten Free Cooler",
    "Beverages & Milkshakes", "Desserts", "Baked Goods",
    "Soups", "Salads", "Sushi",
    "Breakfast Sandwiches",
    "Lunch and Dinner",
    "Pasta & Choolaah Indian BBQ",
    "Passport - Tacos",
    "Latin-Inspired", "Asian-Inspired",
    "Yogurt Bowls",
    "Sushi - Sides",
    "Soups & EVO",
    "Cereals",
    "Hot Beverages",
    "Cold Beverages",
    "Iced Beverages",
    "Frozen Beverages",
}

# Sections where build-your-own dominates (used for edge-case heuristics)
BUILD_HEAVY_SECTIONS = {
    "Burrito", "Omelets", "Poke Bowls", "Mongolian", "Salad Bar",
    "Yogurt Bar", "Cold Bar", "Action", "Deli", "Subs",
    "Pasta & Rice Bowls", "Flavors", "Milk Options", "Bread Options",
    "Condiments", "Breakfast/All-Day Bowls & Parfaits",
}


# ============================================================
# REQUIRED BASE PATTERNS PER SECTION
# ============================================================
# For each section, these patterns identify ingredients that are
# *required* to build the dish (the vessel, base, or structural
# component) vs optional toppings/extras.
#
# An add-on matching any pattern for its section → "required"
# An add-on not matching any pattern → "optional"

_REQUIRED_BASE_PATTERNS_RAW = {
    # Burrito: you need a tortilla or chips as the vessel
    "Burrito": [
        r'tortilla',
        r'tortilla chips',
    ],

    # Omelets: you need eggs
    "Omelets": [
        r'^eggs?$',
        r'^egg whites?$',
    ],

    # Poke Bowls: you need a rice/grain base
    "Poke Bowls": [
        r'rice$',
    ],

    # Mongolian: you need noodles or rice as the base
    "Mongolian": [
        r'noodles$',
        r'rice$',
    ],

    # Pizza: you need dough/crust, sauce, and cheese
    "Pizza": [
        r'(pizza )?dough',
        r'crust',
        r'pizza sauce',
        r'(shredded |fresh |vegan shredded )?mozzarella',
    ],

    # Pasta: you need pasta
    "Pasta": [
        r'pasta$',
        r'tortellini$',
    ],

    # Pasta & Rice Bowls: you need pasta or rice
    "Pasta & Rice Bowls": [
        r'pasta$',
        r'rice$',
    ],

    # Subs: you need bread/bun/roll
    "Subs": [
        r'bun$',
        r'roll$',
        r'bread$',
        r'croissant$',
        r'english muffin',
        r'bagel',
        r'ciabatta',
    ],

    # Deli: you need bread/bun/roll
    "Deli": [
        r'bun$',
        r'roll$',
        r'bread',
        r'ciabatta',
    ],

    # Sandwiches: you need bread/bagel
    "Sandwiches": [
        r'bagel',
        r'bread$',
    ],

    # Salad Bar: you need lettuce/greens as the base
    "Salad Bar": [
        r'lettuce',
        r'spring mix',
        r'arugula',
        r'chopped romaine',
    ],

    # Yogurt Bar: you need yogurt as the base
    "Yogurt Bar": [
        r'yogurt$',
    ],

    # Cold Bar: you need yogurt as the base
    "Cold Bar": [
        r'yogurt$',
    ],

    # Acai bowls: you need the acai sorbet base
    "Acai and Fresh Fruit Bowls": [
        r'acai sorbet',
    ],

    # Milk Options: the milk choice is required for the beverage
    "Milk Options": [
        r'milk$',
    ],

    # Bread Options: the bread choice is required
    "Bread Options": [
        r'.',  # everything in this section is a required choice
    ],

    # Breakfast/All-Day Bowls: yogurt, oats, pudding, cottage cheese bases
    "Breakfast/All-Day Bowls & Parfaits": [
        r'yogurt$',
        r'greek yogurt$',
        r'overnight oats',
        r'chia pudding',
        r'cottage cheese$',
        r'granola',
    ],

    # Grains and Greens: oatmeal and grain bases
    "Grains and Greens": [
        r'oatmeal',
        r'quinoa',
        r'rice$',
    ],

    # Griddle: syrup is required (you need something on pancakes)
    "Griddle": [
        r'syrup',
    ],

    # Grill / Hot Bar / Fired Up / Patio: buns are required for burgers
    "Grill": [
        r'bun$',
    ],
    "Hot Bar": [
        r'bun$',
    ],
    "Fired Up": [
        r'bun$',
    ],
    "Patio": [
        r'bun$',
    ],

    # Hot Food: bread/bun for sandwiches
    "Hot Food": [
        r'bun$',
        r'bread$',
        r'croissant$',
    ],

    # Home: tortilla for tacos/burritos, rice/potato base
    "Home": [
        r'tortilla',
        r'rice$',
    ],

    # Action: eggs for omelet station, tortilla for taco station
    "Action": [
        r'^eggs?$',
        r'^egg$',
        r'tortilla',
    ],
}

# Compile them
REQUIRED_BASE_PATTERNS = {}
for section, patterns in _REQUIRED_BASE_PATTERNS_RAW.items():
    REQUIRED_BASE_PATTERNS[section] = [
        re.compile(p, re.IGNORECASE) for p in patterns
    ]


def classify_addon_type(item_name: str, section: str) -> str:
    """
    For an add-on/ingredient, determine if it is 'required' (a base
    component you must have to build the dish) or 'optional' (a
    topping, extra, or modifier you can skip).
    """
    patterns = REQUIRED_BASE_PATTERNS.get(section, [])
    for pattern in patterns:
        if pattern.search(item_name.strip()):
            return "required"
    return "optional"


# ============================================================
# CLASSIFICATION LOGIC
# ============================================================

def matches_addon_pattern(name: str) -> bool:
    """Check if an item name matches known add-on/ingredient patterns."""
    for pattern in ADDON_PATTERNS:
        if pattern.search(name.strip()):
            return True
    return False


def matches_item_pattern(name: str) -> bool:
    """Check if an item name matches known complete-dish patterns."""
    name_lower = name.lower()

    for brand in BRAND_PREFIXES:
        if name_lower.startswith(brand):
            return True

    for pattern in ITEM_PATTERNS:
        if pattern.search(name_lower):
            return True

    return False


def classify_single_item(item: dict) -> str:
    """
    Classify a single item as 'item' or 'addon_ingredient'.

    Priority order:
      1. If section is known all-items → item
      2. If name matches addon pattern → addon  (addon patterns checked FIRST)
      3. If name matches item pattern → item
      4. Section-specific heuristics
      5. General calorie/word-count heuristic
    """
    name = item["item"]
    cal = item["nutrition"]["calories"]
    section = item["section"]

    # --- 1. Sections where everything is always a standalone item ---
    if section in ALL_ITEMS_SECTIONS:
        return "item"

    # --- 2. Add-on patterns (FIRST — these are more specific) ---
    if matches_addon_pattern(name):
        return "addon_ingredient"

    # --- 3. Item patterns (complete dish names) ---
    if matches_item_pattern(name):
        return "item"

    # --- 4. Section-specific heuristics ---

    if section == "Smoothies":
        return "addon_ingredient" if cal <= 15 else "item"

    if section == "Acai and Fresh Fruit Bowls":
        if cal <= 60 and "bowl" not in name.lower() and "acai" not in name.lower():
            return "addon_ingredient"
        return "item"

    if section == "Pizza":
        return "addon_ingredient" if cal < 350 else "item"

    if section == "Griddle":
        return "item"

    # --- 5. General heuristic ---
    word_count = len(name.split())

    # High-cal + descriptive name → item
    if cal >= 200 and word_count >= 3:
        return "item"

    # Very low-cal + short name → addon
    if cal < 50 and word_count <= 3:
        return "addon_ingredient"

    # In build-heavy sections, lean toward addon for short names
    if section in BUILD_HEAVY_SECTIONS and word_count <= 3:
        return "addon_ingredient"

    # Default
    return "item"


# ============================================================
# SYNTHETIC PARENT ITEM NAMES
# ============================================================
# When a (place, section) group has zero standalone items (everything
# is an add-on/ingredient), a synthetic parent item is created using
# this mapping.  The synthetic item represents the final dish the
# customer would build from those components.

SECTION_TO_SYNTHETIC_ITEM = {
    "Burrito":                          "Build Your Own Burrito",
    "Omelets":                          "Build Your Own Omelet",
    "Poke Bowls":                       "Build Your Own Poke Bowl",
    "Mongolian":                        "Build Your Own Mongolian Bowl",
    "Salad Bar":                        "Build Your Own Salad",
    "Yogurt Bar":                       "Build Your Own Yogurt Bowl",
    "Cold Bar":                         "Build Your Own Cold Bowl",
    "Action":                           "Build Your Own Action Station Plate",
    "Deli":                             "Build Your Own Deli Sandwich",
    "Subs":                             "Build Your Own Sub",
    "Pasta & Rice Bowls":               "Build Your Own Pasta & Rice Bowl",
    "Pizza":                            "Build Your Own Pizza",
    "Flavors":                          "Build Your Own Flavored Beverage",
    "Milk Options":                     "Build Your Own Beverage",
    "Bread Options":                    "Build Your Own Bread Selection",
    "Condiments":                       "Build Your Own Condiment Selection",
    "Breakfast/All-Day Bowls & Parfaits": "Build Your Own Breakfast Bowl",
    "Grains and Greens":                "Build Your Own Grains & Greens Bowl",
    "Pasta":                            "Build Your Own Pasta Dish",
    "Home":                             "Build Your Own Home Station Plate",
    "Sandwiches":                       "Build Your Own Sandwich",
    "Hot Bar":                          "Build Your Own Hot Bar Plate",
    "Grill":                            "Build Your Own Grill Plate",
    "Hot Food":                         "Build Your Own Hot Food Plate",
    "Fired Up":                         "Build Your Own Fired Up Plate",
    "Patio":                            "Build Your Own Patio Plate",
    "Smoothies":                        "Build Your Own Smoothie",
    "Acai and Fresh Fruit Bowls":       "Build Your Own Acai Bowl",
    "Breakfast":                        "Build Your Own Breakfast Plate",
    "Griddle":                          "Build Your Own Griddle Plate",
    "Sides":                            "Build Your Own Sides Plate",
    "Stir-Fry":                         "Build Your Own Stir-Fry",
    "Yakisoba":                         "Build Your Own Yakisoba",
    "Ramen Bowls":                      "Build Your Own Ramen Bowl",
}


def _make_synthetic_item(sample_entry: dict, item_name: str) -> dict:
    """
    Create a synthetic parent item by copying the place/section/hours/address
    metadata from a sample entry in the same group.
    """
    return {
        "place":      sample_entry["place"],
        "item":       item_name,
        "section":    sample_entry["section"],
        "nutrition":  {"protein": None, "carbs": None, "calories": None},
        "hours":      sample_entry.get("hours", ""),
        "address":    sample_entry.get("address", ""),
        "dietary":    {k: None for k in sample_entry.get("dietary", {})},
        "allergens":  {k: None for k in sample_entry.get("allergens", {})},
        "item_type":  "item",
        "parent_item": None,
        "addon_type": None,
        "is_synthetic": True,
    }


# ============================================================
# MAIN CLASSIFICATION + PARENT ASSIGNMENT
# ============================================================

def classify(data: list[dict]) -> list[dict]:
    """
    Classify every item in the dataset.

    Multi-pass approach:
      1) Classify each item as 'item' or 'addon_ingredient'.
      2) Identify (place, section) groups with zero items.
      3) Create a synthetic parent item for each orphan group.
      4) Assign parent_item for every add-on.
    """

    # --- Pass 1: Classify ---
    classified = []
    for item in data:
        new_item = dict(item)
        new_item["item_type"] = classify_single_item(item)
        new_item["is_synthetic"] = False
        classified.append(new_item)

    # --- Pass 2: Build parent lookups ---
    items_by_group = defaultdict(list)
    for entry in classified:
        if entry["item_type"] == "item":
            key = (entry["place"], entry["section"])
            if entry["item"] not in items_by_group[key]:
                items_by_group[key].append(entry["item"])

    # --- Pass 3: Identify orphan groups & create synthetic parents ---
    # Collect all (place, section) keys that have add-ons
    addon_groups = set()
    sample_entries = {}
    for entry in classified:
        if entry["item_type"] == "addon_ingredient":
            key = (entry["place"], entry["section"])
            addon_groups.add(key)
            if key not in sample_entries:
                sample_entries[key] = entry

    synthetic_items = []
    for key in addon_groups:
        if key not in items_by_group or len(items_by_group[key]) == 0:
            place, section = key
            # Determine synthetic item name
            item_name = SECTION_TO_SYNTHETIC_ITEM.get(
                section, f"Build Your Own {section}"
            )
            synth = _make_synthetic_item(sample_entries[key], item_name)
            synthetic_items.append(synth)
            items_by_group[key].append(item_name)

    classified.extend(synthetic_items)

    # --- Pass 4: Assign parent_item and addon_type ---
    for entry in classified:
        if entry["item_type"] == "item":
            entry["parent_item"] = None
            entry["addon_type"] = None
        else:
            key = (entry["place"], entry["section"])
            parents = items_by_group.get(key, [])
            entry["parent_item"] = parents if parents else None
            entry["addon_type"] = classify_addon_type(
                entry["item"], entry["section"]
            )

    return classified


def print_summary(data: list[dict]) -> None:
    """Print classification summary statistics."""
    items_count = sum(1 for x in data if x["item_type"] == "item")
    synthetic_count = sum(1 for x in data if x.get("is_synthetic"))
    real_items = items_count - synthetic_count
    addons_count = sum(1 for x in data if x["item_type"] == "addon_ingredient")
    addons_with_parents = sum(
        1 for x in data
        if x["item_type"] == "addon_ingredient" and x["parent_item"]
    )
    addons_without = addons_count - addons_with_parents

    print(f"Total entries:              {len(data)}")
    print(f"Standalone items:           {items_count}")
    print(f"  - real items:             {real_items}")
    print(f"  - synthetic parents:      {synthetic_count}")
    print(f"Add-ons/Ingredients:        {addons_count}")
    print(f"  - with parent item(s):    {addons_with_parents}")
    print(f"  - no parent (orphans):    {addons_without}")


# ============================================================
# CLI
# ============================================================

def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    input_path = sys.argv[1]

    if len(sys.argv) >= 3:
        output_path = sys.argv[2]
    else:
        base, ext = os.path.splitext(input_path)
        output_path = f"{base}_classified{ext}"

    print(f"Reading:  {input_path}")
    with open(input_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    result = classify(data)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

    print(f"Written:  {output_path}")
    print()
    print_summary(result)


if __name__ == "__main__":
    main()