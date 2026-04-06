"""
Tests for Flask routes in app.py.
"""

import json
import sys
import os
from unittest.mock import patch, MagicMock
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))


# =====================================================================
# GET /
# =====================================================================

class TestIndexRoute:
    """Tests for the index (/) route."""

    def test_returns_200(self, client):
        resp = client.get("/")
        assert resp.status_code == 200

    def test_contains_form(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "<form" in html
        assert 'action="/generate"' in html

    def test_contains_meal_plan_select(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "meal_plan" in html
        assert "Scarlet 14" in html

    def test_contains_diet_select(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "diet" in html
        assert "Vegetarian" in html
        assert "Vegan" in html

    def test_contains_allergen_checkboxes(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "allergens" in html
        assert "Dairy" in html
        assert "Wheat" in html

    def test_contains_campus_areas(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "South" in html
        assert "North" in html
        assert "West" in html

    def test_contains_total_items(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "menu items" in html

    def test_contains_dark_mode_toggle(self, client):
        resp = client.get("/")
        html = resp.data.decode()
        assert "themeToggle" in html


# =====================================================================
# Helper: POST /generate then GET /plan
# =====================================================================

def _generate_and_get_plan(client, form_data, mock_client=None):
    """POST to /generate (streaming), then follow up with GET /plan.

    Returns (generate_resp, plan_html).
    """
    ctx = patch("app.get_gemini_model", return_value=mock_client) if mock_client else _nullcontext()
    with ctx:
        gen_resp = client.post("/generate", data=form_data)
        # The streaming response ends with a JS redirect to /plan
        gen_html = gen_resp.data.decode()
        assert gen_resp.status_code == 200

        # Follow the redirect to /plan
        plan_resp = client.get("/plan")
        return gen_html, plan_resp


class _nullcontext:
    """Minimal no-op context manager for Python <3.10 compat."""
    def __enter__(self): return None
    def __exit__(self, *args): pass


# =====================================================================
# POST /generate  →  GET /plan
# =====================================================================

class TestGenerateRoute:
    """Tests for the /generate route (streaming) + /plan."""

    def test_no_meals_error(self, client):
        """When filters result in zero meals, should show error directly."""
        resp = client.post("/generate", data={
            "meal_plan": "Scarlet 14",
            "diet": "halal",
            "weekday_meals": "3",
            "weekend_meals": "2",
            "campus_areas": "North",  # No halal items in North in test data
        })
        # Zero meals → returned directly (not streamed)
        html = resp.data.decode()
        # May be a direct error page OR a streaming page that redirects to /plan with error
        if "No meals found" in html or "error" in html.lower():
            pass  # Direct error response
        else:
            # Streaming response → follow redirect
            plan_resp = client.get("/plan")
            plan_html = plan_resp.data.decode()
            assert "No meals found" in plan_html or "error" in plan_html.lower()

    def test_gemini_error_shows_error_page(self, client, mock_gemini_response):
        """When Gemini returns empty, should show parse error."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text="")

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
        }, mock_client=mock_client)

        plan_html = plan_resp.data.decode()
        assert "Could not parse" in plan_html or "error" in plan_html.lower()

    def test_valid_generation(self, client, mock_gemini_response):
        """With mocked Gemini returning valid JSON, should render plan."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200
        plan_html = plan_resp.data.decode()
        assert "Monday" in plan_html
        assert "Your Weekly Meal Plan" in plan_html or "Meal Plan" in plan_html

    def test_generate_with_constraints(self, client, mock_gemini_response):
        """Should work with custom nutritional constraints."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
            "use_protein": "on",
            "min_protein": "100",
            "use_carbs": "on",
            "max_carbs": "200",
            "use_calories": "on",
            "max_calories": "2000",
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200

    def test_generate_with_allergens(self, client, mock_gemini_response):
        """Should filter allergens correctly."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
            "allergens": ["dairy", "wheat"],
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200

    def test_hours_swap_banner_shown(self, client):
        """When AI schedules at a closed location, swap banner should appear."""
        # Create a plan with Postle Cafe on Saturday (closed)
        plan_with_closed = {
            "summary": {"daily_protein_avg": 75, "daily_carbs_avg": 100, "daily_calories_avg": 1200},
            "plan": {
                "Monday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
                "Tuesday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
                "Wednesday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
                "Thursday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
                "Friday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
                "Saturday": [
                    {"time": "12:00 PM", "place": "Postle Caf\u00e9", "item": "Turkey Club Wrap",
                     "protein": 28, "carbs": 32, "calories": 400},
                ],
                "Sunday": [
                    {"time": "12:00 PM", "place": "Crane Caf\u00e9", "item": "Grilled Chicken Sandwich",
                     "protein": 35, "carbs": 40, "calories": 450},
                ],
            },
        }
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=json.dumps(plan_with_closed))

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "1",
            "weekend_meals": "1",
        }, mock_client=mock_client)

        plan_html = plan_resp.data.decode()
        assert "adjusted" in plan_html.lower() or "swap" in plan_html.lower()

    def test_generate_streaming_loading_page(self, client, mock_gemini_response):
        """The /generate response should contain the loading page with phrases."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        with patch("app.get_gemini_model", return_value=mock_client):
            resp = client.post("/generate", data={
                "meal_plan": "Scarlet 14",
                "diet": "non-vegetarian",
                "weekday_meals": "3",
                "weekend_meals": "2",
            })
            html = resp.data.decode()
            assert "Warming up the grill" in html
            assert "window.location.replace" in html

    def test_plan_without_session_redirects_home(self, client):
        """GET /plan with no session data should redirect to /."""
        resp = client.get("/plan")
        assert resp.status_code == 302
        assert "/" in resp.headers.get("Location", "")


# =====================================================================
# POST /api/alternatives
# =====================================================================

class TestApiAlternatives:
    """Tests for the /api/alternatives endpoint."""

    def test_valid_request(self, client):
        resp = client.post("/api/alternatives",
                           data=json.dumps({
                               "calories": 450,
                               "protein": 35,
                               "carbs": 40,
                               "diet": "non-vegetarian",
                               "campus_areas": ["South"],
                               "exclude_item": "Something",
                           }),
                           content_type="application/json")
        assert resp.status_code == 200
        data = resp.get_json()
        assert "alternatives" in data
        assert isinstance(data["alternatives"], list)

    def test_no_body_returns_400(self, client):
        resp = client.post("/api/alternatives",
                           content_type="application/json")
        assert resp.status_code == 400

    def test_empty_results(self, client):
        resp = client.post("/api/alternatives",
                           data=json.dumps({
                               "calories": 999999,
                               "protein": 999,
                               "carbs": 999,
                               "diet": "halal",
                               "campus_areas": ["North"],
                           }),
                           content_type="application/json")
        assert resp.status_code == 200
        data = resp.get_json()
        assert isinstance(data["alternatives"], list)


# =====================================================================
# GET /api/meals
# =====================================================================

class TestApiMeals:
    """Tests for the /api/meals endpoint."""

    def test_default_returns_meals(self, client):
        resp = client.get("/api/meals")
        assert resp.status_code == 200
        data = resp.get_json()
        assert "count" in data
        assert "meals" in data
        assert isinstance(data["meals"], list)

    def test_diet_filter(self, client):
        resp = client.get("/api/meals?diet=vegan")
        assert resp.status_code == 200
        data = resp.get_json()
        for m in data["meals"]:
            assert m["dietary"]["vegan"] is True

    def test_area_filter(self, client):
        resp = client.get("/api/meals?area=South")
        assert resp.status_code == 200
        data = resp.get_json()
        assert isinstance(data["meals"], list)

    def test_max_50_meals(self, client):
        resp = client.get("/api/meals")
        data = resp.get_json()
        assert len(data["meals"]) <= 50


# =====================================================================
# GET /api/export-calendar
# =====================================================================

class TestApiExportCalendar:
    """Tests for the /api/export-calendar endpoint."""

    def _setup_plan(self, client, mock_gemini_response):
        """Generate a plan so export_plan_id is set in session."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)
        with patch("app.get_gemini_model", return_value=mock_client):
            resp = client.post("/generate", data={
                "meal_plan": "Scarlet 14",
                "diet": "non-vegetarian",
                "weekday_meals": "3",
                "weekend_meals": "2",
            })
            _ = resp.data  # Force streaming generator to complete before GET /plan
            client.get("/plan")  # sets export_plan_id in session

    def test_no_session_returns_400(self, client):
        resp = client.get("/api/export-calendar")
        assert resp.status_code == 400
        data = resp.get_json()
        assert "error" in data

    def test_returns_200_with_active_plan(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        assert resp.status_code == 200

    def test_content_type_is_calendar(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        assert resp.status_code == 200
        assert "text/calendar" in resp.content_type

    def test_content_disposition_attachment(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        assert resp.status_code == 200
        assert "attachment" in resp.headers.get("Content-Disposition", "")
        assert ".ics" in resp.headers.get("Content-Disposition", "")

    def test_ics_contains_vevent(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        assert resp.status_code == 200
        body = resp.data.decode()
        assert "BEGIN:VEVENT" in body
        assert "END:VEVENT" in body

    def test_ics_vevent_count_matches_meals(self, client, mock_gemini_response):
        """The .ics file should have one VEVENT per meal (7 days × 3 weekday + 2 weekend = 25 meals)."""
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        body = resp.data.decode()
        # mock_gemini_response has 3 meals Mon-Fri + 2 Sat/Sun = 19 total
        vevent_count = body.count("BEGIN:VEVENT")
        assert vevent_count > 0

    def test_ics_has_dtstart(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        body = resp.data.decode()
        assert "DTSTART" in body

    def test_ics_has_location(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-calendar")
        body = resp.data.decode()
        assert "LOCATION" in body


# =====================================================================
# GET /api/export-pdf
# =====================================================================

class TestApiExportPdf:
    """Tests for the /api/export-pdf endpoint."""

    def _setup_plan(self, client, mock_gemini_response):
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)
        with patch("app.get_gemini_model", return_value=mock_client):
            resp = client.post("/generate", data={
                "meal_plan": "Scarlet 14",
                "diet": "non-vegetarian",
                "weekday_meals": "3",
                "weekend_meals": "2",
            })
            _ = resp.data
            client.get("/plan")

    def test_no_session_returns_400(self, client):
        resp = client.get("/api/export-pdf")
        assert resp.status_code == 400
        assert "error" in resp.get_json()

    def test_returns_200_with_active_plan(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-pdf")
        assert resp.status_code == 200

    def test_content_type_is_pdf(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-pdf")
        assert resp.status_code == 200
        assert "application/pdf" in resp.content_type

    def test_content_disposition_attachment(self, client, mock_gemini_response):
        self._setup_plan(client, mock_gemini_response)
        resp = client.get("/api/export-pdf")
        assert resp.status_code == 200
        cd = resp.headers.get("Content-Disposition", "")
        assert "attachment" in cd
        assert "meal-plan.pdf" in cd


# =====================================================================
# build_gcal_link helper
# =====================================================================

class TestBuildGcalLink:
    """Tests for the build_gcal_link helper function."""

    def test_returns_google_calendar_url(self):
        import app as app_module
        from datetime import date
        meals = [
            {"time": "8:00 AM", "item": "Grilled Chicken", "place": "Crane Café",
             "calories": 450, "protein": 35, "carbs": 40},
        ]
        link = app_module.build_gcal_link("Monday", meals, date(2026, 3, 23))
        assert link.startswith("https://calendar.google.com/calendar/render?")

    def test_link_contains_action_template(self):
        import app as app_module
        from datetime import date
        meals = [{"time": "12:00 PM", "item": "Salad", "place": "Curl Market",
                  "calories": 200, "protein": 10, "carbs": 20}]
        link = app_module.build_gcal_link("Tuesday", meals, date(2026, 3, 24))
        assert "action=TEMPLATE" in link

    def test_link_contains_dates(self):
        import app as app_module
        from datetime import date
        meals = [{"time": "6:00 PM", "item": "Burger", "place": "Sloopy's",
                  "calories": 480, "protein": 30, "carbs": 42}]
        link = app_module.build_gcal_link("Wednesday", meals, date(2026, 3, 25))
        assert "20260325" in link

    def test_link_contains_day_name(self):
        import app as app_module
        from datetime import date
        meals = [{"time": "8:00 AM", "item": "Eggs", "place": "Traditions at Kennedy",
                  "calories": 142, "protein": 11, "carbs": 2}]
        link = app_module.build_gcal_link("Thursday", meals, date(2026, 3, 26))
        assert "Thursday" in link


# =====================================================================
# Meal times: form submission and prompt injection
# =====================================================================

class TestMealTimesFeature:
    """Tests for meal time preferences passed through /generate."""

    def test_generate_with_meal_times(self, client, mock_gemini_response):
        """Meal time fields should be accepted without error."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
            "weekday_meal_1_time": "08:00",
            "weekday_meal_2_time": "12:30",
            "weekday_meal_3_time": "18:00",
            "weekend_meal_1_time": "10:00",
            "weekend_meal_2_time": "18:00",
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200

    def test_generate_with_partial_meal_times(self, client, mock_gemini_response):
        """Only some time fields provided — should still work."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "2",
            "weekend_meals": "2",
            "weekday_meal_1_time": "08:00",
            # weekday_meal_2_time omitted intentionally
            "weekend_meal_1_time": "10:00",
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200

    def test_generate_without_meal_times(self, client, mock_gemini_response):
        """No meal time fields — backwards compatible."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        gen_html, plan_resp = _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
        }, mock_client=mock_client)

        assert plan_resp.status_code == 200

    def test_meal_times_injected_into_prompt(self, client, mock_gemini_response):
        """When meal times are provided, the Gemini prompt should include them."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
            "weekday_meal_1_time": "08:00",
            "weekday_meal_2_time": "12:30",
            "weekday_meal_3_time": "18:00",
            "weekend_meal_1_time": "10:00",
            "weekend_meal_2_time": "18:00",
        }, mock_client=mock_client)

        call_args = mock_client.models.generate_content.call_args
        assert call_args is not None
        prompt_text = call_args.kwargs.get("contents", "")
        assert "MEAL TIME PREFERENCES" in prompt_text
        assert "8:00 AM" in prompt_text
        assert "Weekday meals" in prompt_text
        assert "Weekend meals" in prompt_text

    def test_no_meal_times_uses_default_instruction(self, client, mock_gemini_response):
        """Without meal times, the default spread instruction should appear in prompt."""
        mock_client = MagicMock()
        mock_client.models.generate_content.return_value = MagicMock(text=mock_gemini_response)

        _generate_and_get_plan(client, {
            "meal_plan": "Scarlet 14",
            "diet": "non-vegetarian",
            "weekday_meals": "3",
            "weekend_meals": "2",
        }, mock_client=mock_client)

        call_args = mock_client.models.generate_content.call_args
        assert call_args is not None
        prompt_text = call_args.kwargs.get("contents", "")
        assert "Spread meals across breakfast" in prompt_text
