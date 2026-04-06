"""
Shared fixtures for BuckeyeMealPlanner test suite.
"""

import json
import os
import sys
import pytest

# Add project root to path so we can import modules
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))


# ---------------------------------------------------------------------------
# Sample meal data
# ---------------------------------------------------------------------------

@pytest.fixture
def sample_meal():
    """A single fully-populated meal dict."""
    return {
        "place": "Crane Caf\u00e9",
        "item": "Grilled Chicken Sandwich",
        "section": "Featured Items",
        "menu_types": ["all-day"],
        "nutrition": {"protein": 35, "carbs": 40, "calories": 450},
        "hours": "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm",
        "address": "100 W. 18th Ave, Columbus, OH 43210",
        "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False},
        "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                      "fish": False, "peanuts": False, "sesame": False, "soy": False,
                      "tree-nuts": False, "wheat": True},
        "item_type": "item",
        "parent_item": None,
        "addon_type": None,
        "is_synthetic": False,
    }


@pytest.fixture
def sample_meals():
    """Diverse list of meals for testing filters, alternatives, validation."""
    return [
        # 1. All-day, South campus, non-veg
        {
            "place": "Crane Caf\u00e9",
            "item": "Grilled Chicken Sandwich",
            "section": "Featured Items",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 35, "carbs": 40, "calories": 450},
            "hours": "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm",
            "address": "100 W. 18th Ave",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": True},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 2. Vegan item, South campus
        {
            "place": "12th Avenue Bread Company",
            "item": "Vegan Black Bean Burger",
            "section": "Featured Items",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 20, "carbs": 45, "calories": 380},
            "hours": "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm",
            "address": "251 W. 12th Avenue",
            "dietary": {"vegan": True, "vegetarian": True, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": True,
                          "tree-nuts": False, "wheat": True},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 3. Vegetarian, gluten-free, North campus
        {
            "place": "Curl Market",
            "item": "Garden Salad Bowl",
            "section": "Salads",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 10, "carbs": 20, "calories": 200},
            "hours": "Mon\u2013Fri: 10:30am\u201310pm; Sat\u2013Sun: 10:30am\u20138pm",
            "address": "200 Curl Drive",
            "dietary": {"vegan": False, "vegetarian": True, "no-gluten": True, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": True, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 4. Traditions BREAKFAST item
        {
            "place": "Traditions at Kennedy",
            "item": "Scrambled Eggs",
            "section": "Breakfast",
            "menu_types": ["breakfast"],
            "nutrition": {"protein": 11, "carbs": 2, "calories": 142},
            "hours": "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm & 4pm\u20138pm; Sun: 9am\u20133pm & 4pm\u20139pm",
            "address": "251 W. 12th Avenue",
            "dietary": {"vegan": False, "vegetarian": True, "no-gluten": True, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": True,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 5. Traditions LUNCH item
        {
            "place": "Traditions at Kennedy",
            "item": "Chicken and Wild Rice Soup",
            "section": "Soups",
            "menu_types": ["lunch"],
            "nutrition": {"protein": 15, "carbs": 30, "calories": 250},
            "hours": "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm & 4pm\u20138pm; Sun: 9am\u20133pm & 4pm\u20139pm",
            "address": "251 W. 12th Avenue",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": True, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 6. Traditions DINNER item
        {
            "place": "Traditions at Scott",
            "item": "Beef Stir-Fry",
            "section": "Lunch and Dinner",
            "menu_types": ["dinner"],
            "nutrition": {"protein": 30, "carbs": 35, "calories": 420},
            "hours": "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm & 4pm\u20138pm; Sun: 9am\u20133pm & 4pm\u20139pm",
            "address": "120 Scott Hall",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": True,
                          "tree-nuts": False, "wheat": True},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 7. Closed on weekends (weekday-only location)
        {
            "place": "Postle Caf\u00e9",
            "item": "Turkey Club Wrap",
            "section": "Grab & Go",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 28, "carbs": 32, "calories": 400},
            "hours": "Mon\u2013Fri: 7am\u20132:30pm; Sat\u2013Sun: Closed",
            "address": "305 W. 12th Ave",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": True, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": True},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 8. Halal item, West campus
        {
            "place": "CFAES Caf\u00e9",
            "item": "Halal Chicken Rice Bowl",
            "section": "Featured Items",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 32, "carbs": 55, "calories": 520},
            "hours": "Mon\u2013Fri: 8am\u20134pm; Sat\u2013Sun: Closed",
            "address": "100 CFAES",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": True},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 9. Addon ingredient (should be excluded from alternatives)
        {
            "place": "Traditions at Kennedy",
            "item": "Flour Tortilla",
            "section": "Burrito",
            "menu_types": ["lunch"],
            "nutrition": {"protein": 3, "carbs": 25, "calories": 120},
            "hours": "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm; Sun: 9am\u20133pm",
            "address": "251 W. 12th Avenue",
            "dietary": {"vegan": True, "vegetarian": True, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": True},
            "item_type": "addon_ingredient",
            "parent_item": ["Build Your Own Burrito"],
            "addon_type": "required",
            "is_synthetic": False,
        },
        # 10. Low-calorie item (should be excluded from alternatives)
        {
            "place": "Crane Caf\u00e9",
            "item": "Side of Ketchup",
            "section": "Condiments",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 0, "carbs": 5, "calories": 20},
            "hours": "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm",
            "address": "100 W. 18th Ave",
            "dietary": {"vegan": True, "vegetarian": True, "no-gluten": True, "halal": True},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "addon_ingredient",
            "parent_item": None,
            "addon_type": "optional",
            "is_synthetic": False,
        },
        # 11. Another item near 450 cal (for alternatives testing)
        {
            "place": "Sloopy's Diner",
            "item": "Classic Cheeseburger",
            "section": "Featured Items",
            "menu_types": ["all-day"],
            "nutrition": {"protein": 30, "carbs": 42, "calories": 480},
            "hours": "Mon\u2013Fri: 10am\u201310pm; Sat\u2013Sun: 11am\u20139pm",
            "address": "1570 N High St",
            "dietary": {"vegan": False, "vegetarian": False, "no-gluten": False, "halal": False},
            "allergens": {"coconut": False, "shellfish": False, "dairy": True, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": True},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
        # 12. Traditions item with overlapping menu_types (breakfast + dinner)
        {
            "place": "Traditions at Scott",
            "item": "Hash Browns",
            "section": "Breakfast",
            "menu_types": ["breakfast", "dinner"],
            "nutrition": {"protein": 3, "carbs": 28, "calories": 210},
            "hours": "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm & 4pm\u20138pm; Sun: 9am\u20133pm & 4pm\u20139pm",
            "address": "120 Scott Hall",
            "dietary": {"vegan": True, "vegetarian": True, "no-gluten": True, "halal": True},
            "allergens": {"coconut": False, "shellfish": False, "dairy": False, "eggs": False,
                          "fish": False, "peanuts": False, "sesame": False, "soy": False,
                          "tree-nuts": False, "wheat": False},
            "item_type": "item",
            "parent_item": None,
            "addon_type": None,
            "is_synthetic": False,
        },
    ]


@pytest.fixture
def sample_meal_plans():
    """Sample meal plans dict."""
    return {
        "Scarlet 14": {
            "Visits": "14",
            "Visit Exchange": "Included",
            "Dining Dollars": "200",
            "BuckID Cash": "150",
            "Price per Semester (TG 26)": "2,895",
        },
        "Declining Balance": {
            "Visits": "Not Included",
            "Visit Exchange": "Not Included",
            "Dining Dollars": "1,679",
            "BuckID Cash": "Option to add",
            "Price per Semester (TG 26)": "2,583",
        },
        "Traditions": {
            "Visits": "Access every hour",
            "Visit Exchange": "Not Included",
            "Dining Dollars": "100",
            "BuckID Cash": "Option to add",
            "Price per Semester (TG 26)": "2,373",
        },
    }


@pytest.fixture
def mock_gemini_response():
    """A valid AI response JSON string for a 1-day plan."""
    plan = {
        "summary": {
            "daily_protein_avg": 75,
            "daily_carbs_avg": 100,
            "daily_calories_avg": 1200,
        },
        "plan": {
            "Monday": [
                {"time": "8:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "12:00 PM", "place": "Curl Market", "item": "Garden Salad Bowl",
                 "protein": 10, "carbs": 20, "calories": 200},
                {"time": "6:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Tuesday": [
                {"time": "8:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "12:00 PM", "place": "Curl Market", "item": "Garden Salad Bowl",
                 "protein": 10, "carbs": 20, "calories": 200},
                {"time": "6:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Wednesday": [
                {"time": "8:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "12:00 PM", "place": "Curl Market", "item": "Garden Salad Bowl",
                 "protein": 10, "carbs": 20, "calories": 200},
                {"time": "6:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Thursday": [
                {"time": "8:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "12:00 PM", "place": "Curl Market", "item": "Garden Salad Bowl",
                 "protein": 10, "carbs": 20, "calories": 200},
                {"time": "6:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Friday": [
                {"time": "8:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "12:00 PM", "place": "Curl Market", "item": "Garden Salad Bowl",
                 "protein": 10, "carbs": 20, "calories": 200},
                {"time": "6:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Saturday": [
                {"time": "10:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "1:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
            "Sunday": [
                {"time": "10:00 AM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                 "protein": 35, "carbs": 40, "calories": 450},
                {"time": "1:00 PM", "place": "Sloopy's Diner", "item": "Classic Cheeseburger",
                 "protein": 30, "carbs": 42, "calories": 480},
            ],
        },
    }
    return json.dumps(plan)


@pytest.fixture
def sample_nutrislice_response():
    """Mock Nutrislice API response with 2 items across 1 day."""
    return {
        "days": [
            {
                "date": "2026-03-02",
                "menu_items": [
                    {"is_section_title": True, "text": "Featured Items"},
                    {
                        "is_section_title": False,
                        "food": {
                            "name": "Grilled Chicken Sandwich",
                            "rounded_nutrition_info": {
                                "calories": 450,
                                "g_protein": 35,
                                "g_carbs": 40,
                            },
                            "icons": {
                                "food_icons": [
                                    {"slug": "no-gluten"},
                                ]
                            },
                        },
                    },
                    {
                        "is_section_title": False,
                        "food": {
                            "name": "Veggie Wrap",
                            "rounded_nutrition_info": {
                                "calories": 320,
                                "g_protein": 12,
                                "g_carbs": 55,
                            },
                            "icons": {
                                "food_icons": [
                                    {"slug": "vegetarian"},
                                    {"slug": "dairy"},
                                ]
                            },
                        },
                    },
                    # Item with no food (should be skipped)
                    {
                        "is_section_title": False,
                        "food": None,
                    },
                    # Item with no nutrition (should be skipped)
                    {
                        "is_section_title": False,
                        "food": {
                            "name": "Mystery Item",
                            "rounded_nutrition_info": {},
                            "icons": {"food_icons": []},
                        },
                    },
                ],
            }
        ]
    }


# ---------------------------------------------------------------------------
# Flask test client
# ---------------------------------------------------------------------------

@pytest.fixture
def flask_app(sample_meals, sample_meal_plans):
    """Flask app with patched data for testing."""
    import app as app_module

    # Patch the global data
    original_meals = app_module.MEALS
    original_plans = app_module.MEAL_PLANS
    app_module.MEALS = sample_meals
    app_module.MEAL_PLANS = sample_meal_plans
    app_module.app.config["TESTING"] = True

    yield app_module.app

    # Restore
    app_module.MEALS = original_meals
    app_module.MEAL_PLANS = original_plans


@pytest.fixture
def client(flask_app):
    """Flask test client."""
    return flask_app.test_client()
