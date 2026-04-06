# BuckeyeMealPlanner — Project Context for Claude

## Project Overview

BuckeyeMealPlanner is a personalized, AI-powered meal planning application for students at The Ohio State University. It uses the `gemini-2.5-flash` model via Google Gen AI to generate 7-day meal plans based on real-time dining menus (fetched directly from the Nutrislice API), dietary preferences, purchased meal plans, and USDA nutritional guidelines.

---

## Core Features (Existing)

- **Real-Time Menus** — Parses over 5,900 live menu items from 34 campus dining locations via the Nutrislice API.
- **Smart Combining** — Identifies and connects base meals with add-ons (e.g., Pizza Dough + Pepperoni).
- **Nutritional Adherence** — Calculates per-meal limits dynamically and strictly enforces protein/calorie/carb constraints using chain-of-thought prompting.
- **Item Swap** — Swap any meal item instantly with a dynamically recommended, nutritionally-similar alternative from the same location block.
- **Flexible Meal Plans** — Integrates 7 different OSU meal plans directly into the generation rules.
- **Auto-Updated Data** — A GitHub Actions workflow re-fetches and commits fresh menu data every day at 5:00 AM ET.

---

## New Features (To Be Implemented)

### 1. Meal Time Preferences

Allow users to specify what times of day they eat their meals before generating the plan. This personalizes the schedule so generated plans reflect real eating habits rather than assuming fixed breakfast/lunch/dinner windows.

#### User-Facing Behavior

- Before (or alongside) submitting the meal plan generation form, the user sees a **Meal Times** section.
- For each meal slot (e.g., Breakfast, Lunch, Dinner, Snack), the user can pick a preferred time using a time picker input.
- Meal slots should be optional and collapsible — users who skip a slot signal they don't eat that meal.
- The selected times are passed to the `/generate` route along with existing preferences.

#### Backend Changes (`app.py`)

- Accept `meal_times` as a JSON object in the POST body of `/generate`. Example shape:
  ```json
  {
    "breakfast": "08:00",
    "lunch": "12:30",
    "dinner": "18:00",
    "snack": "15:00"
  }
  ```
- Store `meal_times` in the Flask session alongside existing preferences.
- Inject the meal times into the Gemini prompt as a constraint block. Example prompt addition:
  ```
  The user eats meals at the following times:
  - Breakfast: 8:00 AM
  - Lunch: 12:30 PM
  - Snack: 3:00 PM
  - Dinner: 6:00 PM
  Please label each meal in the plan with its scheduled time.
  ```
- If no times are provided, fall back to generic slot names (Breakfast, Lunch, Dinner) without time labels, preserving backward compatibility.

#### Frontend Changes (`templates/`)

- Add a `<div id="meal-times">` section to the main form in the relevant Jinja2 template.
- Use `<input type="time">` elements for each meal slot.
- Use JavaScript to collect the values and include them in the JSON payload sent to `/generate`.
- Display the scheduled time alongside each meal in the rendered plan output.

#### Data/Session

- No new data files are needed. Meal times are ephemeral per session.
- Add `meal_times` to the session object stored in `app.py` alongside other user prefs.

---

### 2. Calendar Export

Allow users to export their generated 7-day meal plan to their calendar app of choice (Google Calendar, Apple Calendar, Microsoft Outlook/Teams, or any iCalendar-compatible app).

#### Export Formats

| Format | Target Apps | Notes |
|--------|-------------|-------|
| `.ics` (iCalendar) | Apple Calendar, Outlook, any RFC 5545-compliant app | Universal fallback; single file download |
| Google Calendar deep link | Google Calendar (web) | Opens "create event" pre-filled in a new tab; no OAuth needed |
| Microsoft Graph API (optional/advanced) | Outlook, Teams calendar | Requires OAuth; can be a stretch goal |

**Recommended initial approach:** Support `.ics` download and a Google Calendar deep-link button. This covers all major platforms without requiring OAuth.

#### New API Route (`app.py`)

```
GET /api/export-calendar
```

- Reads the current session's generated meal plan (already stored in session).
- Reads `meal_times` from session to assign correct `DTSTART`/`DTEND` per event.
- Builds a `.ics` file using Python's `icalendar` library (add to `requirements.txt`).
- Each meal in the 7-day plan becomes a separate `VEVENT`:
  - `SUMMARY`: meal name(s) (e.g., "Breakfast — Scrambled Eggs, Toast")
  - `DTSTART` / `DTEND`: derived from the meal's scheduled day + user-supplied meal time; default duration 30 minutes if not specified.
  - `LOCATION`: dining hall / location block name (already present in the meal data).
  - `DESCRIPTION`: nutritional summary (calories, protein, carbs) from the plan.
  - `UID`: deterministic UUID derived from `{user_session_id}-{day}-{meal_slot}`.
- Returns the `.ics` file as a downloadable `text/calendar` response with `Content-Disposition: attachment`.

#### Google Calendar Deep Link

For each individual meal event, or for the entire week, construct a URL:

```
https://calendar.google.com/calendar/render?action=TEMPLATE
  &text=<meal name>
  &dates=<YYYYMMDDTHHMMSS>/<YYYYMMDDTHHMMSS>
  &details=<nutritional info>
  &location=<dining hall>
```

- Encode all values with `urllib.parse.urlencode`.
- Expose as a helper function `build_gcal_link(event: dict) -> str` in `app.py`.
- On the frontend, render a "Add to Google Calendar" button per day or for the full week.

#### Frontend Changes (`templates/`)

- After the meal plan renders, show an **Export** section with two options:
  1. **Download .ics** — triggers `GET /api/export-calendar`, downloads the file. Works for Apple Calendar, Outlook, and any iCal-compatible app.
  2. **Open in Google Calendar** — opens the Google Calendar deep link in a new tab. For the full week, this can either open events one-by-one (with a note to the user) or use the `.ics` import flow.
- Add a brief note: *"To import into Apple Calendar or Outlook, open the downloaded `.ics` file. For Microsoft Teams calendar, import via Outlook."*

#### Dependencies to Add (`requirements.txt`)

```
icalendar>=5.0.0
```

#### Testing (`tests/test_app_routes.py`)

Add test cases covering:
- `GET /api/export-calendar` with a valid session returns `200` and `Content-Type: text/calendar`.
- `GET /api/export-calendar` with no active session returns `400` or redirects.
- `.ics` output contains the correct number of `VEVENT` blocks (7 days × number of meal slots).
- `DTSTART` values correctly reflect user-supplied `meal_times`.
- `build_gcal_link()` returns a well-formed URL with all required parameters encoded.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend / Routing | Python 3.12, Flask, Gunicorn |
| Frontend | HTML, Vanilla CSS, JavaScript |
| AI Integration | Google Gen AI SDK (`google-genai`), `gemini-2.5-flash` |
| Data Gathering | Python (`urllib`, `json`, `csv`) |
| Calendar Export | `icalendar` (new), `urllib.parse` |
| PDF Export | `weasyprint` (new) |
| Deployment | Docker, Google Cloud Run (GCP) |

---

## Project Structure

```
buckeye-meal-planner/
├── app.py                    # Flask app — routes, Gemini calls, session logic
├── extract_data.py           # Pulls live menu data from the Nutrislice API
├── meals_classifier.py       # Classifies raw items (base meal vs. add-on)
├── requirements.txt
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── data/
│   ├── osu_meals.json
│   ├── osu_meals_classified.json
│   ├── meal_plans.json
│   ├── meal_plans.csv
│   ├── locations.csv
│   └── pdfs/
├── static/
│   └── theme.js
├── templates/                # Jinja2 HTML templates
├── tests/
│   ├── conftest.py
│   ├── test_app_helpers.py
│   ├── test_app_routes.py
│   ├── test_extract_data.py
│   └── test_meals_classifier.py
└── .github/
    └── workflows/
        └── daily-update.yml
```

---

## API Routes

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Main page |
| `POST` | `/generate` | Stream a 7-day meal plan via Gemini; accepts `meal_times` in request body |
| `GET` | `/api/alternatives` | Fetch nutritionally-similar swap options for an item |
| `GET` | `/api/meals` | Return the full classified meal dataset |
| `GET` | `/api/export-calendar` | *(New)* Export the active meal plan as a `.ics` calendar file |
| `GET` | `/api/export-pdf` | *(New)* Export the active meal plan as a real PDF download |

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GEMINI_API_KEY` | Yes | Google Gemini API key from aistudio.google.com |
| `SECRET_KEY` | Recommended | Flask session secret; auto-generated if omitted (sessions won't survive restarts) |

---

## Running Locally

```bash
# 1. Clone and enter the repo
git clone <your-repo-url>
cd buckeye-meal-planner

# 2. Create and activate a virtual environment
python3 -m venv venv
source venv/bin/activate       # Windows: venv\Scripts\activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Configure environment variables
cp .env.example .env
# Edit .env: set GEMINI_API_KEY and SECRET_KEY

# 5. (Optional) Refresh menu data immediately
python3 extract_data.py

# 6. Start the dev server
flask run --port=5000
# Open http://localhost:5000

# --- OR use Docker Compose for production parity ---
docker compose up --build -d
# Open http://localhost:5050
```

---

## Testing

```bash
# Run all tests
pytest tests/ -v

# Run a specific file
pytest tests/test_app_helpers.py -v

# Run with coverage
pip install pytest-cov
pytest tests/ --cov=. --cov-report=term-missing
```

### Test Coverage Overview

| File | What it covers |
|------|----------------|
| `tests/conftest.py` | Shared fixtures (sample meals, Flask test client, mock data) |
| `tests/test_app_helpers.py` | Pure helper functions in `app.py` (~80 tests) |
| `tests/test_app_routes.py` | Flask routes including `/api/export-calendar` (~20+ tests) |
| `tests/test_extract_data.py` | CSV parsing and Nutrislice API extraction (~25 tests) |
| `tests/test_meals_classifier.py` | Item classification and add-on detection (~60 tests) |

---

## Automated Data Updates (GitHub Actions)

`.github/workflows/daily-update.yml` runs `extract_data.py` every day at **5:00 AM ET** and commits any changed `data/osu_meals.json` and `data/meal_plans.json` back to both `main` and `test` branches. No manual action is needed.

Trigger manually: **Actions → Daily Meal Planner Data Update → Run workflow**.

---

## Deploying to GCP (Google Cloud Run)

### One-Time Setup

1. Enable **Cloud Run API**, **Artifact Registry API**, and **Cloud Build API** in GCP Console.
2. Create an Artifact Registry repository (Format: Docker, Mode: Standard).

### Build & Push

```bash
gcloud config set project <your-project-id>
git clone <your-repo-url> && cd buckeye-meal-planner
gcloud builds submit --tag \
  us-central1-docker.pkg.dev/<project-id>/<repo-name>/buckeye-meal-planner:latest
```

### Deploy

1. Go to **Cloud Run → Create Service**, select the pushed image.
2. Set **Container port** to `5000`.
3. Add environment variables: `GEMINI_API_KEY` and `SECRET_KEY`.
4. Set authentication to **Allow unauthenticated invocations** for public access.
5. Click **Create**.

### Redeploy After Changes

```bash
git pull
gcloud builds submit --tag \
  us-central1-docker.pkg.dev/<project-id>/<repo-name>/buckeye-meal-planner:latest
```

Cloud Run automatically deploys the new revision.

---

## Implementation Checklist for New Features

### Meal Time Preferences

- [ ] Add `meal_times` time-picker UI to the main form template
- [ ] Collect `meal_times` in JavaScript and include in `/generate` POST payload
- [ ] Update `/generate` route in `app.py` to parse and store `meal_times` in session
- [ ] Inject `meal_times` into the Gemini prompt as a constraint block
- [ ] Display scheduled times alongside each meal in the rendered plan
- [ ] Add tests for prompt injection logic and session storage

### PDF Export

- [ ] Add `weasyprint` to `requirements.txt`
- [ ] Update `Dockerfile` to install WeasyPrint system dependencies before `pip install`:
  ```dockerfile
  RUN apt-get update && apt-get install -y \
    libpango-1.0-0 libpangoft2-1.0-0 libcairo2 libgdk-pixbuf2.0-0 \
    libffi-dev shared-mime-info && rm -rf /var/lib/apt/lists/*
  ```
- [ ] Create `templates/meal_plan_pdf.html` — print-only template (no nav/header/footer chrome, OSU scarlet `#CC0000` accents, one day per section, clean typography)
- [ ] Add `GET /api/export-pdf` route in `app.py`: reads meal plan from session, renders `meal_plan_pdf.html` via WeasyPrint, returns `application/pdf` with `Content-Disposition: attachment; filename="meal-plan.pdf"`; returns 400 if no session meal plan exists
- [ ] Add "Save as PDF" outline button to the export UI section — use `fetch` + `Blob` + a temporary `<a>` tag to trigger download without page navigation
- [ ] Add tests in `test_app_routes.py`:
  - `GET /api/export-pdf` with valid session → 200, `Content-Type: application/pdf`
  - `GET /api/export-pdf` with no session → 400
  - Response `Content-Disposition` header contains `meal-plan.pdf`

### Export UI Redesign

Replace the existing export section in the results template with this layout:

- **Primary button** (scarlet `#CC0000` fill, white text, download icon): "Download .ics" → `GET /api/export-calendar`
- **Secondary button** (outline style): "Save as PDF" → `GET /api/export-pdf` via fetch/Blob download
- **Thin vertical divider**
- **Labeled day group**: small muted label "Add to Google Calendar by day" above a row of 7 compact outline buttons (Mon–Sun), each opening the existing Google Cal deep link for that day in a new tab
- **Help text** (small, muted, below all buttons): ".ics works with Apple Calendar and Outlook — just open the downloaded file. Google Calendar: use the day buttons above, or import the .ics via Settings → Import. Microsoft Teams: import via Outlook."

### Calendar Export

- [ ] Add `icalendar>=5.0.0` to `requirements.txt`
- [ ] Implement `build_ics(meal_plan, meal_times) -> bytes` helper in `app.py`
- [ ] Implement `build_gcal_link(event: dict) -> str` helper in `app.py`
- [ ] Add `GET /api/export-calendar` route returning `.ics` download
- [ ] Add **Export** UI section to the meal plan results template
- [ ] Add "Download .ics" button (triggers the new route)
- [ ] Add "Open in Google Calendar" button (uses deep link)
- [ ] Add user note about Apple Calendar / Outlook import steps
- [ ] Write route and helper tests in `test_app_routes.py`
