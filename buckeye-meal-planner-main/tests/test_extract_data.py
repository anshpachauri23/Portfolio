"""
Tests for extract_data.py — CSV parsing, API response extraction, fetch_menu mocking.
"""

import csv
import json
import os
import sys
import tempfile
from unittest.mock import patch, MagicMock
from urllib.error import HTTPError, URLError
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
import extract_data


# =====================================================================
# parse_hours_csv
# =====================================================================

class TestParseHoursCsv:
    """Tests for parse_hours_csv()."""

    def test_valid_csv(self, tmp_path):
        csv_path = tmp_path / "locations.csv"
        csv_path.write_text(
            "Name,Location,Hours of Operation (Spring 2026)\n"
            "Crane Caf\u00e9,100 W. 18th Ave,Mon\u2013Fri: 8am\u20137pm\n"
            "Curl Market,200 Curl Drive,Mon\u2013Fri: 10:30am\u201310pm\n"
        )
        with patch.object(extract_data, "LOCATIONS_CSV", str(csv_path)):
            result = extract_data.parse_hours_csv()
        assert "Crane Caf\u00e9" in result
        assert result["Crane Caf\u00e9"]["address"] == "100 W. 18th Ave"
        assert "8am" in result["Crane Caf\u00e9"]["hours"]
        assert "Curl Market" in result
        assert len(result) == 2

    def test_missing_file(self, tmp_path):
        with patch.object(extract_data, "LOCATIONS_CSV", str(tmp_path / "missing.csv")):
            result = extract_data.parse_hours_csv()
        assert result == {}

    def test_empty_csv(self, tmp_path):
        csv_path = tmp_path / "locations.csv"
        csv_path.write_text("Name,Location,Hours of Operation (Spring 2026)\n")
        with patch.object(extract_data, "LOCATIONS_CSV", str(csv_path)):
            result = extract_data.parse_hours_csv()
        assert result == {}

    def test_blank_name_skipped(self, tmp_path):
        csv_path = tmp_path / "locations.csv"
        csv_path.write_text(
            "Name,Location,Hours of Operation (Spring 2026)\n"
            ",Nowhere,Never\n"
            "Real Place,Somewhere,Always\n"
        )
        with patch.object(extract_data, "LOCATIONS_CSV", str(csv_path)):
            result = extract_data.parse_hours_csv()
        assert "" not in result
        assert "Real Place" in result
        assert len(result) == 1


# =====================================================================
# parse_meal_plans_csv
# =====================================================================

class TestParseMealPlansCsv:
    """Tests for parse_meal_plans_csv()."""

    def test_valid_csv(self, tmp_path):
        csv_path = tmp_path / "meal_plans.csv"
        csv_path.write_text(
            "Component,Scarlet 14,Gray 10\n"
            "Visits,14,10\n"
            "Dining Dollars,200,200\n"
        )
        with patch.object(extract_data, "MEAL_PLANS_CSV", str(csv_path)):
            result = extract_data.parse_meal_plans_csv()
        assert "Scarlet 14" in result
        assert "Gray 10" in result
        assert result["Scarlet 14"]["Visits"] == "14"
        assert result["Gray 10"]["Dining Dollars"] == "200"

    def test_missing_file(self, tmp_path):
        with patch.object(extract_data, "MEAL_PLANS_CSV", str(tmp_path / "missing.csv")):
            result = extract_data.parse_meal_plans_csv()
        assert result == {}

    def test_empty_csv(self, tmp_path):
        csv_path = tmp_path / "meal_plans.csv"
        csv_path.write_text("")
        with patch.object(extract_data, "MEAL_PLANS_CSV", str(csv_path)):
            result = extract_data.parse_meal_plans_csv()
        assert result == {}

    def test_header_only(self, tmp_path):
        csv_path = tmp_path / "meal_plans.csv"
        csv_path.write_text("Component,Scarlet 14\n")
        with patch.object(extract_data, "MEAL_PLANS_CSV", str(csv_path)):
            result = extract_data.parse_meal_plans_csv()
        # Header-only (< 2 rows) returns empty dict
        assert result == {}

    def test_single_plan(self, tmp_path):
        csv_path = tmp_path / "meal_plans.csv"
        csv_path.write_text(
            "Component,Traditions\n"
            "Visits,Access every hour\n"
            "Dining Dollars,100\n"
        )
        with patch.object(extract_data, "MEAL_PLANS_CSV", str(csv_path)):
            result = extract_data.parse_meal_plans_csv()
        assert len(result) == 1
        assert result["Traditions"]["Visits"] == "Access every hour"


# =====================================================================
# extract_items_from_api
# =====================================================================

class TestExtractItemsFromApi:
    """Tests for extract_items_from_api()."""

    def test_valid_response(self, sample_nutrislice_response):
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "Mon-Fri: 8am-5pm", "address": "123 Main St"},
        )
        # Should have 2 items (Grilled Chicken Sandwich + Veggie Wrap)
        # Mystery Item (no nutrition) and None food should be skipped
        assert len(items) == 2

    def test_item_structure(self, sample_nutrislice_response):
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "Mon-Fri: 8am-5pm", "address": "123 Main St"},
        )
        item = items[0]
        assert item["place"] == "Test Location"
        assert item["item"] == "Grilled Chicken Sandwich"
        assert item["section"] == "Featured Items"
        assert item["nutrition"]["protein"] == 35
        assert item["nutrition"]["carbs"] == 40
        assert item["nutrition"]["calories"] == 450
        assert item["hours"] == "Mon-Fri: 8am-5pm"
        assert item["address"] == "123 Main St"

    def test_dietary_flags(self, sample_nutrislice_response):
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
        )
        # First item has "no-gluten" icon
        assert items[0]["dietary"]["no-gluten"] is True
        assert items[0]["dietary"]["vegan"] is False
        # Second item has "vegetarian" icon
        assert items[1]["dietary"]["vegetarian"] is True

    def test_allergen_flags(self, sample_nutrislice_response):
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
        )
        # Second item has "dairy" icon
        assert items[1]["allergens"]["dairy"] is True
        assert items[1]["allergens"]["wheat"] is False

    def test_section_tracking(self, sample_nutrislice_response):
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
        )
        # Both items should be in "Featured Items" section
        assert items[0]["section"] == "Featured Items"
        assert items[1]["section"] == "Featured Items"

    def test_deduplication(self, sample_nutrislice_response):
        """Same item appearing twice should only be extracted once."""
        # Duplicate the first day's items
        sample_nutrislice_response["days"].append(sample_nutrislice_response["days"][0])
        items = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
        )
        names = [i["item"] for i in items]
        assert len(names) == len(set(names))

    def test_shared_seen_set(self, sample_nutrislice_response):
        """Passing a seen set should enable cross-call deduplication."""
        seen = set()
        items1 = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
            seen=seen,
        )
        items2 = extract_data.extract_items_from_api(
            sample_nutrislice_response,
            "Test Location",
            {"hours": "", "address": ""},
            seen=seen,
        )
        assert len(items1) == 2
        assert len(items2) == 0  # All already seen

    def test_no_food_key_skipped(self):
        data = {"days": [{"menu_items": [{"is_section_title": False, "food": None}]}]}
        items = extract_data.extract_items_from_api(data, "Loc", {"hours": "", "address": ""})
        assert items == []

    def test_empty_nutrition_skipped(self):
        data = {
            "days": [{
                "menu_items": [{
                    "is_section_title": False,
                    "food": {
                        "name": "No Nutrition",
                        "rounded_nutrition_info": {},
                        "icons": {"food_icons": []},
                    },
                }]
            }]
        }
        items = extract_data.extract_items_from_api(data, "Loc", {"hours": "", "address": ""})
        assert items == []

    def test_empty_name_skipped(self):
        data = {
            "days": [{
                "menu_items": [{
                    "is_section_title": False,
                    "food": {
                        "name": "",
                        "rounded_nutrition_info": {"calories": 100, "g_protein": 5, "g_carbs": 10},
                        "icons": {"food_icons": []},
                    },
                }]
            }]
        }
        items = extract_data.extract_items_from_api(data, "Loc", {"hours": "", "address": ""})
        assert items == []

    def test_none_data(self):
        items = extract_data.extract_items_from_api(None, "Loc", {"hours": "", "address": ""})
        assert items == []

    def test_no_days_key(self):
        items = extract_data.extract_items_from_api({"other": "data"}, "Loc", {"hours": "", "address": ""})
        assert items == []

    def test_section_title_tracked(self):
        data = {
            "days": [{
                "menu_items": [
                    {"is_section_title": True, "text": "Soups"},
                    {
                        "is_section_title": False,
                        "food": {
                            "name": "Tomato Soup",
                            "rounded_nutrition_info": {"calories": 150, "g_protein": 5, "g_carbs": 20},
                            "icons": {"food_icons": []},
                        },
                    },
                    {"is_section_title": True, "text": "Salads"},
                    {
                        "is_section_title": False,
                        "food": {
                            "name": "Caesar Salad",
                            "rounded_nutrition_info": {"calories": 300, "g_protein": 15, "g_carbs": 25},
                            "icons": {"food_icons": []},
                        },
                    },
                ]
            }]
        }
        items = extract_data.extract_items_from_api(data, "Loc", {"hours": "", "address": ""})
        assert items[0]["section"] == "Soups"
        assert items[1]["section"] == "Salads"

    def test_location_info_empty(self):
        data = {
            "days": [{
                "menu_items": [{
                    "is_section_title": False,
                    "food": {
                        "name": "Test Item",
                        "rounded_nutrition_info": {"calories": 100, "g_protein": 5, "g_carbs": 10},
                        "icons": {"food_icons": []},
                    },
                }]
            }]
        }
        items = extract_data.extract_items_from_api(data, "Loc", {})
        assert items[0]["hours"] == ""
        assert items[0]["address"] == ""


# =====================================================================
# fetch_menu (mocked network)
# =====================================================================

class TestFetchMenu:
    """Tests for fetch_menu() with mocked network calls."""

    def test_successful_fetch(self):
        mock_response = MagicMock()
        mock_response.read.return_value = json.dumps({"days": []}).encode("utf-8")
        mock_response.__enter__ = MagicMock(return_value=mock_response)
        mock_response.__exit__ = MagicMock(return_value=False)

        with patch("extract_data.urlopen", return_value=mock_response):
            result = extract_data.fetch_menu("test-slug", "2026/03/02")
        assert result == {"days": []}

    def test_404_returns_none(self):
        error = HTTPError(
            url="http://example.com",
            code=404,
            msg="Not Found",
            hdrs=MagicMock(),
            fp=MagicMock(),
        )
        with patch("extract_data.urlopen", side_effect=error):
            result = extract_data.fetch_menu("bad-slug", "2026/03/02")
        assert result is None

    def test_500_returns_none(self):
        error = HTTPError(
            url="http://example.com",
            code=500,
            msg="Server Error",
            hdrs=MagicMock(),
            fp=MagicMock(),
        )
        with patch("extract_data.urlopen", side_effect=error):
            result = extract_data.fetch_menu("error-slug", "2026/03/02")
        assert result is None

    def test_network_error_returns_none(self):
        with patch("extract_data.urlopen", side_effect=URLError("Network unreachable")):
            result = extract_data.fetch_menu("any-slug", "2026/03/02")
        assert result is None

    def test_timeout_returns_none(self):
        with patch("extract_data.urlopen", side_effect=TimeoutError("Timed out")):
            result = extract_data.fetch_menu("any-slug", "2026/03/02")
        assert result is None

    def test_url_construction(self):
        mock_response = MagicMock()
        mock_response.read.return_value = b'{"days": []}'
        mock_response.__enter__ = MagicMock(return_value=mock_response)
        mock_response.__exit__ = MagicMock(return_value=False)

        with patch("extract_data.urlopen", return_value=mock_response) as mock_urlopen:
            extract_data.fetch_menu("crane-cafe", "2026/03/02", menu_type="breakfast")
            call_args = mock_urlopen.call_args
            req = call_args[0][0]
            assert "crane-cafe" in req.full_url
            assert "breakfast" in req.full_url
            assert "2026/03/02" in req.full_url
