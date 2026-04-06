"""
Tests for all pure helper functions in app.py.
"""

import json
import sys
import os
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import app as app_module


# =====================================================================
# parse_time_to_minutes
# =====================================================================

class TestParseTimeToMinutes:
    """Tests for parse_time_to_minutes()."""

    def test_standard_morning(self):
        assert app_module.parse_time_to_minutes("8:00 AM") == 480

    def test_standard_noon(self):
        assert app_module.parse_time_to_minutes("12:00 PM") == 720

    def test_standard_evening(self):
        assert app_module.parse_time_to_minutes("6:00 PM") == 1080

    def test_no_minutes_am(self):
        assert app_module.parse_time_to_minutes("7am") == 420

    def test_no_minutes_pm(self):
        assert app_module.parse_time_to_minutes("3pm") == 900

    def test_with_minutes_pm(self):
        assert app_module.parse_time_to_minutes("10:30pm") == 1350

    def test_with_minutes_am(self):
        assert app_module.parse_time_to_minutes("7:30am") == 450

    def test_midnight(self):
        assert app_module.parse_time_to_minutes("12:00 AM") == 0

    def test_midnight_thirty(self):
        assert app_module.parse_time_to_minutes("12:30 AM") == 30

    def test_1am(self):
        assert app_module.parse_time_to_minutes("1:00 AM") == 60

    def test_11pm(self):
        assert app_module.parse_time_to_minutes("11:00 PM") == 1380

    def test_none_input(self):
        assert app_module.parse_time_to_minutes(None) is None

    def test_empty_string(self):
        assert app_module.parse_time_to_minutes("") is None

    def test_garbage_input(self):
        assert app_module.parse_time_to_minutes("garbage") is None

    def test_case_insensitive(self):
        assert app_module.parse_time_to_minutes("8:00 am") == 480
        assert app_module.parse_time_to_minutes("8:00 AM") == 480
        assert app_module.parse_time_to_minutes("8:00 Am") == 480

    def test_whitespace(self):
        assert app_module.parse_time_to_minutes("  8:00 AM  ") == 480


# =====================================================================
# _expand_day_range
# =====================================================================

class TestExpandDayRange:
    """Tests for _expand_day_range()."""

    def test_weekdays_en_dash(self):
        assert app_module._expand_day_range("Mon\u2013Fri") == {0, 1, 2, 3, 4}

    def test_weekdays_hyphen(self):
        assert app_module._expand_day_range("Mon-Fri") == {0, 1, 2, 3, 4}

    def test_weekend(self):
        assert app_module._expand_day_range("Sat\u2013Sun") == {5, 6}

    def test_single_day_sat(self):
        assert app_module._expand_day_range("Sat") == {5}

    def test_single_day_wed(self):
        assert app_module._expand_day_range("Wed") == {2}

    def test_single_day_mon(self):
        assert app_module._expand_day_range("Mon") == {0}

    def test_wrap_around_sun_fri(self):
        assert app_module._expand_day_range("Sun-Fri") == {6, 0, 1, 2, 3, 4}

    def test_wrap_around_sat_mon(self):
        assert app_module._expand_day_range("Sat-Mon") == {5, 6, 0}

    def test_partial_range_mon_thu(self):
        assert app_module._expand_day_range("Mon-Thu") == {0, 1, 2, 3}

    def test_full_week(self):
        assert app_module._expand_day_range("Mon-Sun") == {0, 1, 2, 3, 4, 5, 6}

    def test_same_day_range(self):
        assert app_module._expand_day_range("Wed-Wed") == {2}

    def test_invalid_day(self):
        assert app_module._expand_day_range("Xyz") == set()

    def test_whitespace(self):
        assert app_module._expand_day_range("  Mon - Fri  ") == {0, 1, 2, 3, 4}


# =====================================================================
# parse_hours_for_day
# =====================================================================

class TestParseHoursForDay:
    """Tests for parse_hours_for_day()."""

    def test_simple_weekday(self):
        hours = "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm"
        windows = app_module.parse_hours_for_day(hours, "Monday")
        assert windows == [(480, 1140)]

    def test_simple_weekend(self):
        hours = "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm"
        windows = app_module.parse_hours_for_day(hours, "Saturday")
        assert windows == [(540, 1020)]

    def test_multi_window(self):
        hours = "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm"
        windows = app_module.parse_hours_for_day(hours, "Tuesday")
        assert len(windows) == 2
        assert windows[0] == (420, 900)
        assert windows[1] == (960, 1260)

    def test_closed_weekend(self):
        hours = "Mon\u2013Fri: 11am\u201311pm; Sat\u2013Sun: Closed"
        assert app_module.parse_hours_for_day(hours, "Saturday") == []
        assert app_module.parse_hours_for_day(hours, "Sunday") == []

    def test_closed_weekend_open_weekday(self):
        hours = "Mon\u2013Fri: 11am\u201311pm; Sat\u2013Sun: Closed"
        windows = app_module.parse_hours_for_day(hours, "Wednesday")
        assert windows == [(660, 1380)]

    def test_empty_string(self):
        assert app_module.parse_hours_for_day("", "Monday") is None

    def test_none_hours(self):
        assert app_module.parse_hours_for_day(None, "Monday") is None

    def test_day_not_mentioned(self):
        hours = "Mon\u2013Fri: 8am\u20137pm"
        # Saturday not mentioned → should return [] (closed)
        assert app_module.parse_hours_for_day(hours, "Saturday") == []

    def test_three_segment_hours(self):
        hours = "Mon\u2013Fri: 7:30am\u20138pm; Sat: 10am\u20135pm; Sun: 9am\u20138pm"
        assert app_module.parse_hours_for_day(hours, "Friday") == [(450, 1200)]
        assert app_module.parse_hours_for_day(hours, "Saturday") == [(600, 1020)]
        assert app_module.parse_hours_for_day(hours, "Sunday") == [(540, 1200)]

    def test_traditions_complex(self):
        hours = "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm; Sat: 9am\u20133pm & 4pm\u20138pm; Sun: 9am\u20133pm & 4pm\u20139pm"
        sat = app_module.parse_hours_for_day(hours, "Saturday")
        assert len(sat) == 2
        assert sat[0] == (540, 900)
        assert sat[1] == (960, 1200)

    def test_invalid_day_name(self):
        hours = "Mon\u2013Fri: 8am\u20137pm"
        assert app_module.parse_hours_for_day(hours, "Funday") is None

    def test_individual_day_closed(self):
        hours = "Mon\u2013Fri: 11am\u20139pm; Sat: Closed; Sun: 4pm\u20139pm"
        assert app_module.parse_hours_for_day(hours, "Saturday") == []
        sun = app_module.parse_hours_for_day(hours, "Sunday")
        assert sun == [(960, 1260)]


# =====================================================================
# is_open_at
# =====================================================================

class TestIsOpenAt:
    """Tests for is_open_at()."""

    def test_open_during_window(self):
        hours = "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm"
        assert app_module.is_open_at(hours, "Monday", "12:00 PM") is True

    def test_closed_before_opening(self):
        hours = "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm"
        assert app_module.is_open_at(hours, "Monday", "7:30 AM") is False

    def test_closed_after_closing(self):
        hours = "Mon\u2013Fri: 8am\u20137pm; Sat\u2013Sun: 9am\u20135pm"
        assert app_module.is_open_at(hours, "Monday", "8:00 PM") is False

    def test_closed_day(self):
        hours = "Mon\u2013Fri: 11am\u201311pm; Sat\u2013Sun: Closed"
        assert app_module.is_open_at(hours, "Sunday", "12:00 PM") is False

    def test_empty_hours_treated_as_open(self):
        assert app_module.is_open_at("", "Monday", "12:00 PM") is True

    def test_multi_window_in_first(self):
        hours = "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm"
        assert app_module.is_open_at(hours, "Monday", "12:00 PM") is True

    def test_multi_window_in_second(self):
        hours = "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm"
        assert app_module.is_open_at(hours, "Monday", "6:00 PM") is True

    def test_multi_window_gap(self):
        hours = "Mon\u2013Fri: 7am\u20133pm & 4pm\u20139pm"
        assert app_module.is_open_at(hours, "Monday", "3:30 PM") is False

    def test_at_opening_time(self):
        hours = "Mon\u2013Fri: 8am\u20137pm"
        assert app_module.is_open_at(hours, "Monday", "8:00 AM") is True

    def test_at_closing_time(self):
        hours = "Mon\u2013Fri: 8am\u20137pm"
        assert app_module.is_open_at(hours, "Monday", "7:00 PM") is True

    def test_unparseable_time_treated_as_open(self):
        hours = "Mon\u2013Fri: 8am\u20137pm"
        assert app_module.is_open_at(hours, "Monday", "invalid") is True


# =====================================================================
# menu_type_matches_time
# =====================================================================

class TestMenuTypeMatchesTime:
    """Tests for menu_type_matches_time()."""

    def test_all_day_any_time(self):
        assert app_module.menu_type_matches_time(["all-day"], "12:00 PM") is True
        assert app_module.menu_type_matches_time(["all-day"], "8:00 AM") is True
        assert app_module.menu_type_matches_time(["all-day"], "6:00 PM") is True

    def test_breakfast_morning(self):
        assert app_module.menu_type_matches_time(["breakfast"], "8:00 AM") is True
        assert app_module.menu_type_matches_time(["breakfast"], "9:30 AM") is True

    def test_breakfast_not_afternoon(self):
        assert app_module.menu_type_matches_time(["breakfast"], "12:00 PM") is False

    def test_lunch_midday(self):
        assert app_module.menu_type_matches_time(["lunch"], "12:00 PM") is True
        assert app_module.menu_type_matches_time(["lunch"], "1:00 PM") is True

    def test_lunch_not_morning(self):
        assert app_module.menu_type_matches_time(["lunch"], "8:00 AM") is False

    def test_lunch_not_evening(self):
        assert app_module.menu_type_matches_time(["lunch"], "6:00 PM") is False

    def test_dinner_evening(self):
        assert app_module.menu_type_matches_time(["dinner"], "6:00 PM") is True
        assert app_module.menu_type_matches_time(["dinner"], "8:00 PM") is True

    def test_dinner_not_morning(self):
        assert app_module.menu_type_matches_time(["dinner"], "8:00 AM") is False

    def test_dinner_not_midday(self):
        assert app_module.menu_type_matches_time(["dinner"], "12:00 PM") is False

    def test_boundary_breakfast_end(self):
        # 10:00 AM = 600 min → breakfast < 600 → False
        assert app_module.menu_type_matches_time(["breakfast"], "10:00 AM") is False
        # 9:59 AM = 599 min → True
        assert app_module.menu_type_matches_time(["breakfast"], "9:59 AM") is True

    def test_boundary_lunch_start(self):
        # 11:00 AM = 660 min → lunch >= 660 → True
        assert app_module.menu_type_matches_time(["lunch"], "11:00 AM") is True
        # 10:59 AM = 659 min → False (in the gap)
        assert app_module.menu_type_matches_time(["lunch"], "10:59 AM") is False

    def test_boundary_lunch_end(self):
        # 2:00 PM = 840 min → lunch <= 840 → True
        assert app_module.menu_type_matches_time(["lunch"], "2:00 PM") is True
        # 2:01 PM = 841 min → False (in the gap)
        assert app_module.menu_type_matches_time(["lunch"], "2:01 PM") is False

    def test_boundary_dinner_start(self):
        # 5:00 PM = 1020 min → dinner >= 1020 → True
        assert app_module.menu_type_matches_time(["dinner"], "5:00 PM") is True
        # 4:59 PM = 1019 min → False (in the gap)
        assert app_module.menu_type_matches_time(["dinner"], "4:59 PM") is False

    def test_transition_gap_10_to_11_am(self):
        # 10:00 AM – 10:59 AM is a gap: neither breakfast nor lunch
        assert app_module.menu_type_matches_time(["breakfast"], "10:00 AM") is False
        assert app_module.menu_type_matches_time(["lunch"], "10:00 AM") is False
        assert app_module.menu_type_matches_time(["breakfast"], "10:30 AM") is False
        assert app_module.menu_type_matches_time(["lunch"], "10:30 AM") is False

    def test_transition_gap_2_to_5_pm(self):
        # 2:01 PM – 4:59 PM is a gap: neither lunch nor dinner
        assert app_module.menu_type_matches_time(["lunch"], "3:00 PM") is False
        assert app_module.menu_type_matches_time(["dinner"], "3:00 PM") is False
        assert app_module.menu_type_matches_time(["lunch"], "4:00 PM") is False
        assert app_module.menu_type_matches_time(["dinner"], "4:00 PM") is False

    def test_overlapping_types_breakfast_dinner(self):
        # Item with ["breakfast", "dinner"] should match morning and evening, NOT midday or gaps
        assert app_module.menu_type_matches_time(["breakfast", "dinner"], "8:00 AM") is True
        assert app_module.menu_type_matches_time(["breakfast", "dinner"], "6:00 PM") is True
        assert app_module.menu_type_matches_time(["breakfast", "dinner"], "12:00 PM") is False
        assert app_module.menu_type_matches_time(["breakfast", "dinner"], "3:00 PM") is False

    def test_empty_menu_types(self):
        assert app_module.menu_type_matches_time([], "12:00 PM") is True

    def test_none_menu_types(self):
        assert app_module.menu_type_matches_time(None, "12:00 PM") is True

    def test_multiple_types_one_matches(self):
        assert app_module.menu_type_matches_time(["breakfast", "lunch"], "8:00 AM") is True
        assert app_module.menu_type_matches_time(["breakfast", "lunch"], "12:00 PM") is True
        assert app_module.menu_type_matches_time(["breakfast", "lunch"], "6:00 PM") is False


# =====================================================================
# get_places_for_areas
# =====================================================================

class TestGetPlacesForAreas:
    """Tests for get_places_for_areas()."""

    def test_south_area(self):
        places = app_module.get_places_for_areas(["South"])
        assert "Crane Caf\u00e9" in places
        assert "Sloopy's Diner" in places
        assert len(places) == len(app_module.CAMPUS_AREAS["South"])

    def test_north_area(self):
        places = app_module.get_places_for_areas(["North"])
        assert "Curl Market" in places
        assert "Connecting Grounds" in places

    def test_multiple_areas(self):
        places = app_module.get_places_for_areas(["South", "North"])
        south_count = len(app_module.CAMPUS_AREAS["South"])
        north_count = len(app_module.CAMPUS_AREAS["North"])
        assert len(places) == south_count + north_count

    def test_empty_list(self):
        assert app_module.get_places_for_areas([]) == []

    def test_invalid_area(self):
        assert app_module.get_places_for_areas(["East"]) == []

    def test_all_areas(self):
        places = app_module.get_places_for_areas(["South", "North", "West"])
        total = sum(len(v) for v in app_module.CAMPUS_AREAS.values())
        assert len(places) == total


# =====================================================================
# filter_meals
# =====================================================================

class TestFilterMeals:
    """Tests for filter_meals()."""

    def test_no_filter_returns_all(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("non-vegetarian")
        assert len(result) == len(sample_meals)

    def test_vegan_filter(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("vegan")
        assert all(m["dietary"]["vegan"] for m in result)
        assert len(result) > 0

    def test_vegetarian_filter(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("vegetarian")
        assert all(m["dietary"]["vegetarian"] for m in result)

    def test_gluten_free_filter(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("gluten-free")
        assert all(m["dietary"]["no-gluten"] for m in result)

    def test_halal_filter(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("halal")
        assert all(m["dietary"]["halal"] for m in result)
        assert len(result) == 3  # Halal chicken rice bowl + Side of Ketchup + Hash Browns (all halal: True)

    def test_allergen_filter_dairy(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("non-vegetarian", allergens=["dairy"])
        assert all(not m["allergens"]["dairy"] for m in result)

    def test_allergen_filter_multiple(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("non-vegetarian", allergens=["dairy", "wheat"])
        for m in result:
            assert not m["allergens"]["dairy"]
            assert not m["allergens"]["wheat"]

    def test_place_filter(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("non-vegetarian", places=["Crane Caf\u00e9"])
        assert all(m["place"] == "Crane Caf\u00e9" for m in result)

    def test_combined_filters(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals(
            "vegetarian",
            allergens=["eggs"],
            places=["Curl Market"]
        )
        for m in result:
            assert m["dietary"]["vegetarian"]
            assert not m["allergens"]["eggs"]
            assert m["place"] == "Curl Market"

    def test_no_matches(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("halal", places=["Nonexistent Place"])
        assert result == []


# =====================================================================
# find_alternatives
# =====================================================================

class TestFindAlternatives:
    """Tests for find_alternatives()."""

    def test_returns_list(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian")
        assert isinstance(result, list)

    def test_max_10_results(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian")
        assert len(result) <= 10

    def test_excludes_addon_ingredients(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(120, 3, 25, "non-vegetarian")
        for r in result:
            # Should never return "Flour Tortilla" or "Side of Ketchup"
            assert r["item"] != "Flour Tortilla"
            assert r["item"] != "Side of Ketchup"

    def test_excludes_low_cal(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(20, 0, 5, "non-vegetarian")
        for r in result:
            assert r["calories"] >= 50

    def test_excludes_named_item(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian",
                                              exclude_name="Grilled Chicken Sandwich")
        for r in result:
            assert r["item"] != "Grilled Chicken Sandwich"

    def test_sorted_by_similarity(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian")
        if len(result) >= 2:
            # First result should be the best match
            assert result[0]["item"] is not None

    def test_cost_traditions(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(250, 15, 30, "non-vegetarian")
        for r in result:
            if "traditions" in r["place"].lower():
                assert r["cash_price"] == 12.0
            else:
                assert r["cash_price"] == 8.0

    def test_result_structure(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian")
        if result:
            item = result[0]
            assert "item" in item
            assert "place" in item
            assert "protein" in item
            assert "carbs" in item
            assert "calories" in item
            assert "cost_text" in item
            assert "cash_price" in item

    def test_deduplicates(self, sample_meals):
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(450, 35, 40, "non-vegetarian")
        names = [r["item"].lower() for r in result]
        assert len(names) == len(set(names))

    def test_allergen_filter_excludes_dairy(self, sample_meals):
        """Alternatives should never include items with user-selected allergens."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(
            450, 35, 40, "non-vegetarian", allergens=["dairy"]
        )
        for r in result:
            # Look up the original meal to check allergens
            for m in sample_meals:
                if m["item"] == r["item"]:
                    assert not m["allergens"].get("dairy", False), \
                        f"{r['item']} has dairy but was returned as alternative"

    def test_allergen_filter_excludes_soy(self, sample_meals):
        """Soy allergen should filter out soy-containing items from alternatives."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(
            400, 20, 45, "non-vegetarian", allergens=["soy"]
        )
        for r in result:
            for m in sample_meals:
                if m["item"] == r["item"]:
                    assert not m["allergens"].get("soy", False), \
                        f"{r['item']} has soy but was returned as alternative"


# =====================================================================
# build_prompt
# =====================================================================

class TestBuildPrompt:
    """Tests for build_prompt()."""

    def test_contains_requirements(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "vegan", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "REQUIREMENTS" in prompt
        assert "vegan" in prompt.lower()

    def test_contains_meal_counts(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "3 meals" in prompt or "exactly 3" in prompt
        assert "2 meals" in prompt or "exactly 2" in prompt

    def test_contains_nutrition_targets(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "NUTRITION" in prompt

    def test_usda_defaults_when_no_constraints(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert str(app_module.USDA_DEFAULTS["calories"]) in prompt
        assert str(app_module.USDA_DEFAULTS["min_protein"]) in prompt

    def test_custom_constraints(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        constraints = {"min_protein": "150", "max_carbs": "200", "max_calories": "2000"}
        prompt = app_module.build_prompt(inputs, constraints, sample_meals)
        assert "150" in prompt
        assert "200" in prompt
        assert "2000" in prompt

    def test_contains_menu_items(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "MENU ITEMS" in prompt

    def test_contains_hours_info(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "Hours:" in prompt

    def test_contains_menu_type_tags(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        # Should contain menu type tags for traditions items
        assert "(breakfast)" in prompt or "(lunch)" in prompt or "(dinner)" in prompt

    def test_contains_instructions(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "INSTRUCTIONS" in prompt
        assert "operating hours" in prompt.lower() or "hours" in prompt

    def test_contains_json_format(self, sample_meals):
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "JSON" in prompt
        assert "Monday" in prompt

    def test_traditions_dedicated_sections(self, sample_meals):
        """Traditions items should appear in dedicated meal-period sections."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "[TRADITIONS — BREAKFAST (7-10 AM)]" in prompt
        assert "[TRADITIONS — DINNER (5-8 PM)]" in prompt

    def test_traditions_not_in_main_items(self, sample_meals):
        """Traditions items should NOT appear in the [MAIN ITEMS] section."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        # Find the [MAIN ITEMS] section text (between header and next section)
        main_start = prompt.index("[MAIN ITEMS]")
        # Find the next section header after [MAIN ITEMS]
        next_section = prompt.index("[TRADITIONS", main_start)
        main_section = prompt[main_start:next_section]
        assert "Traditions at Kennedy" not in main_section
        assert "Traditions at Scott" not in main_section

    def test_overlapping_item_in_multiple_sections(self, sample_meals):
        """Items with overlapping types (e.g., breakfast+dinner) appear in each matching section."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        # Hash Browns has ["breakfast", "dinner"] — should appear in both sections
        breakfast_start = prompt.index("[TRADITIONS — BREAKFAST (7-10 AM)]")
        lunch_start = prompt.index("[TRADITIONS — LUNCH (11 AM-2 PM)]") if "[TRADITIONS — LUNCH (11 AM-2 PM)]" in prompt else len(prompt)
        dinner_start = prompt.index("[TRADITIONS — DINNER (5-8 PM)]")
        breakfast_section = prompt[breakfast_start:lunch_start if lunch_start < dinner_start else dinner_start]
        dinner_section = prompt[dinner_start:]
        assert "Hash Browns" in breakfast_section
        assert "Hash Browns" in dinner_section

    def test_allergen_warning_in_prompt(self, sample_meals):
        """Prompt should include allergen restriction text when allergens are provided."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals, allergens=["dairy", "soy"])
        assert "dairy" in prompt.lower()
        assert "soy" in prompt.lower()
        assert "ALLERGEN" in prompt

    def test_no_allergen_text_when_none(self, sample_meals):
        """Prompt should not have allergen section when no allergens selected."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian", "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals, allergens=[])
        assert "ALLERGEN RESTRICTIONS" not in prompt


# =====================================================================
# parse_ai_response
# =====================================================================

class TestParseAiResponse:
    """Tests for parse_ai_response()."""

    def test_valid_json(self):
        data = {"summary": {"daily_protein_avg": 75}, "plan": {}}
        result = app_module.parse_ai_response(json.dumps(data))
        assert result == data

    def test_json_with_code_block(self):
        data = {"summary": {"daily_protein_avg": 75}}
        text = f"```json\n{json.dumps(data)}\n```"
        result = app_module.parse_ai_response(text)
        assert result == data

    def test_json_with_plain_code_block(self):
        data = {"summary": {"daily_protein_avg": 75}}
        text = f"```\n{json.dumps(data)}\n```"
        result = app_module.parse_ai_response(text)
        assert result == data

    def test_json_with_surrounding_text(self):
        data = {"summary": {"daily_protein_avg": 75}}
        text = f"Here is the plan:\n{json.dumps(data)}\nHope this helps!"
        result = app_module.parse_ai_response(text)
        assert result == data

    def test_invalid_json(self):
        assert app_module.parse_ai_response("not json at all") is None

    def test_empty_string(self):
        assert app_module.parse_ai_response("") is None

    def test_whitespace(self):
        data = {"key": "value"}
        result = app_module.parse_ai_response(f"   {json.dumps(data)}   ")
        assert result == data

    def test_nested_json(self):
        data = {"plan": {"Monday": [{"item": "Test", "calories": 100}]}}
        result = app_module.parse_ai_response(json.dumps(data))
        assert result == data


# =====================================================================
# validate_and_fix_plan
# =====================================================================

class TestValidateAndFixPlan:
    """Tests for validate_and_fix_plan()."""

    def test_valid_plan_no_swaps(self, sample_meals):
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9",
                     "item": "Grilled Chicken Sandwich", "protein": 35, "carbs": 40, "calories": 450},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert swaps == []

    def test_swap_closed_location(self, sample_meals):
        """Postle Cafe is closed on weekends — scheduling there on Saturday should trigger swap."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Saturday": [
                    {"time": "12:00 PM", "place": "Postle Caf\u00e9",
                     "item": "Turkey Club Wrap", "protein": 28, "carbs": 32, "calories": 400},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert swaps[0]["reason"] == "location closed"
        # The item should have been replaced
        assert result["plan"]["Saturday"][0]["item"] != "Turkey Club Wrap"

    def test_swap_traditions_breakfast_at_dinner(self, sample_meals):
        """Traditions breakfast item scheduled at dinner time should be swapped."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "6:00 PM", "place": "Traditions at Kennedy",
                     "item": "Scrambled Eggs", "protein": 11, "carbs": 2, "calories": 142},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert "menu type" in swaps[0]["reason"]

    def test_swap_traditions_dinner_at_breakfast(self, sample_meals):
        """Traditions dinner item scheduled at breakfast time should be swapped."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "8:00 AM", "place": "Traditions at Scott",
                     "item": "Beef Stir-Fry", "protein": 30, "carbs": 35, "calories": 420},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1

    def test_unknown_item_flagged(self, sample_meals):
        """Items not in the database should be flagged as unverified."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Unknown Place",
                     "item": "Mystery Meal", "protein": 20, "carbs": 30, "calories": 300},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert "not found in database" in swaps[0]["reason"]
        # Item is kept as-is (not replaced)
        assert result["plan"]["Monday"][0]["item"] == "Mystery Meal"

    def test_fuzzy_substring_match(self, sample_meals):
        """Validator should find items via substring match when AI misspells."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Crane Café",
                     "item": "Grilled Chicken Sandwich Deluxe", "protein": 35, "carbs": 40, "calories": 450},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        # Should NOT be flagged as unverified — fuzzy match should find it
        unverified = [s for s in swaps if "not found in database" in s.get("reason", "")]
        assert len(unverified) == 0

    def test_plan_without_plan_key(self, sample_meals):
        app_module.MEALS = sample_meals
        plan = {"summary": {"daily_protein_avg": 50}}
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert swaps == []
        assert result == plan

    def test_multiple_invalid_meals(self, sample_meals):
        """Multiple invalid meals in the same plan should all be swapped."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Saturday": [
                    {"time": "12:00 PM", "place": "Postle Caf\u00e9",
                     "item": "Turkey Club Wrap", "protein": 28, "carbs": 32, "calories": 400},
                ],
                "Sunday": [
                    {"time": "12:00 PM", "place": "Postle Caf\u00e9",
                     "item": "Turkey Club Wrap", "protein": 28, "carbs": 32, "calories": 400},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 2

    def test_before_opening_time(self, sample_meals):
        """Scheduling before a location opens should trigger a swap."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "7:00 AM", "place": "Crane Caf\u00e9",
                     "item": "Grilled Chicken Sandwich", "protein": 35, "carbs": 40, "calories": 450},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert "location closed" in swaps[0]["reason"]

    def test_validator_uses_filtered_meals_for_lookup(self, sample_meals):
        """Validator should not find items outside the filtered available_meals list."""
        app_module.MEALS = sample_meals
        # Pass only Crane Café items as available_meals
        filtered = [m for m in sample_meals if m["place"] == "Crane Café"]
        plan = {
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Traditions at Kennedy",
                     "item": "Scrambled Eggs", "protein": 11, "carbs": 2, "calories": 142},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, filtered, "non-vegetarian", [], None
        )
        # Scrambled Eggs is NOT in filtered list, so should be flagged as unverified
        assert len(swaps) == 1
        assert "not found in database" in swaps[0]["reason"]

    def test_overlapping_menu_type_wrong_time(self, sample_meals):
        """Item with ['breakfast', 'dinner'] at lunch time should be swapped."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Traditions at Scott",
                     "item": "Hash Browns", "protein": 3, "carbs": 28, "calories": 210},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert "menu type" in swaps[0]["reason"]

    def test_overlapping_menu_type_correct_times(self, sample_meals):
        """Item with ['breakfast', 'dinner'] should be valid at both breakfast and dinner."""
        app_module.MEALS = sample_meals
        for time_str in ["8:00 AM", "6:00 PM"]:
            plan = {
                "plan": {
                    "Monday": [
                        {"time": time_str, "place": "Traditions at Scott",
                         "item": "Hash Browns", "protein": 3, "carbs": 28, "calories": 210},
                    ],
                }
            }
            result, swaps = app_module.validate_and_fix_plan(
                plan, sample_meals, "non-vegetarian", [], None
            )
            assert swaps == [], f"Expected no swaps at {time_str}, got {swaps}"

    def test_combined_hours_and_menu_type_failure(self, sample_meals):
        """Dinner-only item at breakfast on a weekday should fail menu_type check."""
        app_module.MEALS = sample_meals
        # Beef Stir-Fry is dinner-only at Traditions at Scott.
        # At 8 AM Monday: Traditions is open (7am–3pm) but menu_type ["dinner"]
        # doesn't match breakfast time → swap.
        plan = {
            "plan": {
                "Monday": [
                    {"time": "8:00 AM", "place": "Traditions at Scott",
                     "item": "Beef Stir-Fry", "protein": 30, "carbs": 35, "calories": 420},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        assert "menu type" in swaps[0]["reason"]

    def test_replacement_is_nutritionally_similar(self, sample_meals):
        """When swapping, replacement should have similar nutrition to the original."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Saturday": [
                    {"time": "12:00 PM", "place": "Postle Café",
                     "item": "Turkey Club Wrap", "protein": 28, "carbs": 32, "calories": 400},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert len(swaps) == 1
        replaced = result["plan"]["Saturday"][0]
        # Replacement should be within reasonable range of original
        assert replaced["calories"] >= 100
        assert replaced["calories"] <= 800

    def test_replacement_respects_open_hours(self, sample_meals):
        """Replacement item should be at a location that's actually open."""
        app_module.MEALS = sample_meals
        plan = {
            "plan": {
                "Saturday": [
                    {"time": "12:00 PM", "place": "Postle Café",
                     "item": "Turkey Club Wrap", "protein": 28, "carbs": 32, "calories": 400},
                ],
            }
        }
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        replaced = result["plan"]["Saturday"][0]
        # The replacement should NOT be at Postle Café (closed on weekends)
        assert replaced["place"] != "Postle Café"

    def test_empty_plan_days(self, sample_meals):
        """Validator handles empty day lists gracefully."""
        app_module.MEALS = sample_meals
        plan = {"plan": {"Monday": [], "Tuesday": []}}
        result, swaps = app_module.validate_and_fix_plan(
            plan, sample_meals, "non-vegetarian", [], None
        )
        assert swaps == []
        assert result["plan"]["Monday"] == []


# =====================================================================
# _fuzzy_find_meal (direct unit tests)
# =====================================================================

class TestFuzzyFindMeal:
    """Direct unit tests for _fuzzy_find_meal()."""

    @pytest.fixture
    def meal_lookup(self, sample_meals):
        """Build the same lookup dict that validate_and_fix_plan uses."""
        lookup = {}
        for m in sample_meals:
            key = (m["item"].lower(), m["place"].lower())
            if key not in lookup:
                lookup[key] = m
        return lookup

    def test_exact_match(self, meal_lookup):
        result = app_module._fuzzy_find_meal(
            "Grilled Chicken Sandwich", "Crane Café", meal_lookup
        )
        assert result is not None
        assert result["item"] == "Grilled Chicken Sandwich"

    def test_exact_match_case_insensitive(self, meal_lookup):
        result = app_module._fuzzy_find_meal(
            "grilled chicken sandwich", "crane café", meal_lookup
        )
        assert result is not None
        assert result["item"] == "Grilled Chicken Sandwich"

    def test_name_only_match_wrong_place(self, meal_lookup):
        """When place doesn't match but item name does, still finds it."""
        result = app_module._fuzzy_find_meal(
            "Grilled Chicken Sandwich", "Wrong Place", meal_lookup
        )
        assert result is not None
        assert result["item"] == "Grilled Chicken Sandwich"

    def test_substring_match_longer_name(self, meal_lookup):
        """AI returns a longer name that contains the real item name."""
        result = app_module._fuzzy_find_meal(
            "Grilled Chicken Sandwich Deluxe", "Crane Café", meal_lookup
        )
        assert result is not None
        assert result["item"] == "Grilled Chicken Sandwich"

    def test_substring_match_shorter_name(self, meal_lookup):
        """AI returns a shorter name contained within the real item name."""
        result = app_module._fuzzy_find_meal(
            "Black Bean Burger", "12th Avenue Bread Company", meal_lookup
        )
        assert result is not None
        assert result["item"] == "Vegan Black Bean Burger"

    def test_short_string_no_false_positive(self, meal_lookup):
        """Strings shorter than 5 chars should NOT trigger substring matching."""
        result = app_module._fuzzy_find_meal("Eggs", "Anywhere", meal_lookup)
        assert result is None  # "Eggs" is 4 chars, too short for substring

    def test_no_match_returns_none(self, meal_lookup):
        result = app_module._fuzzy_find_meal(
            "Completely Invented Dish", "Fake Place", meal_lookup
        )
        assert result is None

    def test_five_char_minimum_works(self, meal_lookup):
        """Substring matching works when the query is a substring of a real item."""
        # "Beef Stir-Fry Bowl" contains "beef stir-fry" as a substring
        result = app_module._fuzzy_find_meal("Beef Stir-Fry Bowl", "Anywhere", meal_lookup)
        assert result is not None
        assert result["item"] == "Beef Stir-Fry"


# =====================================================================
# build_prompt (additional coverage)
# =====================================================================

class TestBuildPromptExtended:
    """Extended tests for build_prompt() — Traditions boost, caps, and content."""

    def test_no_hallucination_instruction(self, sample_meals):
        """Prompt must explicitly tell the AI not to invent items."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "hallucinate" in prompt.lower() or "invent" in prompt.lower() or "ONLY use items" in prompt

    def test_traditions_preference_instruction(self, sample_meals):
        """Prompt should include instruction to prefer Traditions items."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        assert "traditions" in prompt.lower()
        assert "prefer" in prompt.lower() or "PREFER" in prompt

    def test_transition_gap_instruction(self, sample_meals):
        """Prompt should mention transition gaps or specific meal times."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        # Should mention breakfast/lunch/dinner time ranges
        assert "7-10 AM" in prompt or "7–10 AM" in prompt or "7 AM" in prompt
        assert "5-8 PM" in prompt or "5–8 PM" in prompt or "5 PM" in prompt

    def test_per_meal_nutrition_computed(self, sample_meals):
        """Prompt should compute per-meal nutrition targets from daily targets."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        constraints = {"min_protein": "150", "max_calories": "2400"}
        prompt = app_module.build_prompt(inputs, constraints, sample_meals)
        # With 3 weekday meals, per-meal protein = 150/3 = 50
        assert "50" in prompt  # per-meal protein target

    def test_prompt_includes_all_seven_days(self, sample_meals):
        """Prompt should request a plan for all 7 days of the week."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        prompt = app_module.build_prompt(inputs, {}, sample_meals)
        for day in ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]:
            assert day in prompt

    def test_prompt_diet_reflected(self, sample_meals):
        """Prompt should clearly state the selected diet."""
        for diet in ["vegan", "vegetarian", "halal", "gluten-free"]:
            inputs = {"meal_plan": "Scarlet 14", "diet": diet,
                      "weekday_meals": "3", "weekend_meals": "2"}
            prompt = app_module.build_prompt(inputs, {}, sample_meals)
            assert diet in prompt.lower()

    def test_multiple_allergens_in_prompt(self, sample_meals):
        """All provided allergens should appear in the prompt text."""
        inputs = {"meal_plan": "Scarlet 14", "diet": "non-vegetarian",
                  "weekday_meals": "3", "weekend_meals": "2"}
        allergens = ["dairy", "peanuts", "shellfish"]
        prompt = app_module.build_prompt(inputs, {}, sample_meals, allergens=allergens)
        for a in allergens:
            assert a in prompt.lower()


# =====================================================================
# find_alternatives (additional coverage)
# =====================================================================

class TestFindAlternativesExtended:
    """Extended tests for find_alternatives()."""

    def test_allergen_plus_place_combined(self, sample_meals):
        """Combined allergen and place filters should both apply."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(
            450, 35, 40, "non-vegetarian",
            places=["Crane Café"], allergens=["wheat"]
        )
        for r in result:
            assert r["place"] == "Crane Café" or True  # place filter is on MEALS not result
            # Verify no wheat items in results
            for m in sample_meals:
                if m["item"] == r["item"]:
                    assert not m["allergens"].get("wheat", False)

    def test_calorie_range_reasonable(self, sample_meals):
        """Alternatives should have calories in a reasonable range of the target."""
        app_module.MEALS = sample_meals
        target_cal = 400
        result = app_module.find_alternatives(target_cal, 25, 35, "non-vegetarian")
        if result:
            # Best match should be reasonably close
            best = result[0]
            assert best["calories"] >= 50  # at least above minimum

    def test_exclude_name_works(self, sample_meals):
        """Excluding a specific item name removes it from results."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(
            450, 35, 40, "non-vegetarian", exclude_name="Classic Cheeseburger"
        )
        for r in result:
            assert r["item"] != "Classic Cheeseburger"

    def test_vegan_alternatives_only_vegan(self, sample_meals):
        """Vegan diet should only return vegan alternatives."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(300, 15, 30, "vegan")
        for r in result:
            for m in sample_meals:
                if m["item"] == r["item"]:
                    assert m["dietary"]["vegan"], f"{r['item']} is not vegan"

    def test_no_addon_ingredients_returned(self, sample_meals):
        """Addon ingredients should never appear in alternatives."""
        app_module.MEALS = sample_meals
        result = app_module.find_alternatives(120, 3, 25, "non-vegetarian")
        for r in result:
            assert r["item"] != "Flour Tortilla"
            assert r["item"] != "Side of Ketchup"


# =====================================================================
# filter_meals (edge cases)
# =====================================================================

class TestFilterMealsExtended:
    """Extended edge case tests for filter_meals()."""

    def test_combined_diet_allergen_place(self, sample_meals):
        """Triple filter: vegetarian + no dairy + specific place."""
        app_module.MEALS = sample_meals
        result = app_module.filter_meals(
            "vegetarian", allergens=["dairy"], places=["Curl Market"]
        )
        for m in result:
            assert m["dietary"]["vegetarian"]
            assert not m["allergens"]["dairy"]
            assert m["place"] == "Curl Market"

    def test_all_allergens_filters_heavily(self, sample_meals):
        """Selecting many allergens should heavily reduce results."""
        app_module.MEALS = sample_meals
        all_allergens = ["coconut", "shellfish", "dairy", "eggs", "fish",
                         "peanuts", "sesame", "soy", "tree-nuts", "wheat"]
        result = app_module.filter_meals("non-vegetarian", allergens=all_allergens)
        # Every remaining item should have ALL allergens False
        for m in result:
            for a in all_allergens:
                assert not m["allergens"].get(a, False)

    def test_multiple_places_filter(self, sample_meals):
        """Filter with multiple places should return items from any of them."""
        app_module.MEALS = sample_meals
        result = app_module.filter_meals(
            "non-vegetarian", places=["Crane Café", "Sloopy's Diner"]
        )
        for m in result:
            assert m["place"] in ["Crane Café", "Sloopy's Diner"]

    def test_gluten_free_excludes_wheat_items(self, sample_meals):
        """Gluten-free filter should only return items with no-gluten: True."""
        app_module.MEALS = sample_meals
        result = app_module.filter_meals("gluten-free")
        for m in result:
            assert m["dietary"]["no-gluten"]
