# BuckeyeMealPlanner

A personalized, AI-powered meal planning application for students at The Ohio State University. It uses the `gemini-2.5-flash` model via Google Gen AI to generate 7-day meal plans based on real-time dining menus (fetched directly from the Nutrislice API), dietary preferences, purchased meal plans, and USDA nutritional guidelines.

## Features
- **Real-Time Menus**: Parses over 5,900 live menu items from 34 campus dining locations.
- **Smart Combining**: Identifies and connects base meals with add-ons (e.g., Pizza Dough + Pepperoni).
- **Nutritional Adherence**: Calculates per-meal limits dynamically and strictly enforces protein/calorie/carb constraints using chain-of-thought prompting.
- **Item Swap**: Swap any meal item instantly with a dynamically recommended, nutritionally-similar alternative from the same location block.
- **Flexible Data**: Integrates 7 different OSU meal plans directly into the generation rules.
- **Auto-Updated Data**: A GitHub Actions workflow re-fetches and commits fresh menu data every day at 5:00 AM ET.

## Tech Stack
- **Backend / Routing:** Python 3.12 (Flask + Gunicorn)
- **Frontend:** HTML, Vanilla CSS, JS
- **AI Integration:** Google Gen AI SDK (`google-genai`) — `gemini-2.5-flash`
- **Data Gathering:** Python (`urllib`, `json`, `csv`)
- **Deployment:** Docker, Google Cloud Run (GCP)

## Project Structure

```
buckeye-meal-planner/
├── app.py                    # Flask app — routes, Gemini calls, session logic
├── extract_data.py           # Pulls live menu data from the Nutrislice API
├── meals_classifier.py       # Classifies raw items (base meal vs. add-on)
├── requirements.txt
├── Dockerfile                # Production image (Gunicorn on port 5000)
├── docker-compose.yml        # Local dev (maps host port 5050 → container 5000)
├── .env.example              # Template for required environment variables
├── data/
│   ├── osu_meals.json            # Raw menu items from Nutrislice (auto-updated daily)
│   ├── osu_meals_classified.json # Classified items used at runtime by app.py
│   ├── meal_plans.json           # OSU meal plan definitions (auto-updated daily)
│   ├── meal_plans.csv            # Source CSV for meal plan data
│   ├── locations.csv             # Campus dining location metadata
│   └── pdfs/                     # Original PDF menus used for reference
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
        └── daily-update.yml  # Cron job: re-fetches menu data every morning
```

## Prerequisites

- Python 3.12+
- A **Google Gemini API key** — get one free at [aistudio.google.com](https://aistudio.google.com/)
- Docker (optional, for containerized local dev or building the production image)

## Running Locally

1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd buckeye-meal-planner
   ```

2. **Set up a virtual environment:**
   ```bash
   python3 -m venv venv
   source venv/bin/activate  # Windows: venv\Scripts\activate
   ```

3. **Install dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment variables:**
   ```bash
   cp .env.example .env
   ```
   Edit `.env` and fill in the values:
   ```
   GEMINI_API_KEY=<your_gemini_api_key>
   SECRET_KEY=<any_random_string_for_flask_sessions>
   ```
   > If `SECRET_KEY` is omitted, Flask generates a random one at startup — sessions will not survive server restarts.

5. **(Optional) Refresh menu data:**
   By default, pre-fetched JSON files are included in the repo and updated daily by CI.
   Run this only if you need to pull the latest data immediately:
   ```bash
   python3 extract_data.py
   ```

6. **Start the Flask development server:**
   ```bash
   flask run --port=5000
   ```
   Open `http://localhost:5000` in your browser.

   **Alternative — Docker Compose (recommended for parity with production):**
   ```bash
   docker compose up --build -d
   ```
   Open `http://localhost:5050` (Compose maps `5050 → 5000` inside the container).

## API Routes

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Main page |
| `POST` | `/generate` | Stream a 7-day meal plan via Gemini |
| `GET` | `/api/alternatives` | Fetch nutritionally-similar swap options for an item |
| `GET` | `/api/meals` | Return the full classified meal dataset |

## Testing

The project includes 200+ test cases covering all modules.

| File | What it covers |
|------|----------------|
| `tests/conftest.py` | Shared fixtures (sample meals, Flask test client, mock data) |
| `tests/test_app_helpers.py` | Pure helper functions in `app.py` (~80 tests) |
| `tests/test_app_routes.py` | Flask routes: `/`, `/generate`, `/api/alternatives`, `/api/meals` (~20 tests) |
| `tests/test_extract_data.py` | CSV parsing and Nutrislice API extraction (~25 tests) |
| `tests/test_meals_classifier.py` | Item classification and add-on detection (~60 tests) |

```bash
# Run all tests
pytest tests/ -v

# Run a specific file
pytest tests/test_app_helpers.py -v

# Run with coverage report
pip install pytest-cov
pytest tests/ --cov=. --cov-report=term-missing
```

## Automated Data Updates (GitHub Actions)

`.github/workflows/daily-update.yml` runs `extract_data.py` every day at **5:00 AM ET** and commits any changed `data/osu_meals.json` and `data/meal_plans.json` back to both the `main` and `test` branches. No manual action is needed to keep menu data current.

You can also trigger it manually from **Actions → Daily Meal Planner Data Update → Run workflow** in the GitHub UI.

## Deploying to GCP (Google Cloud Run via Console)

The app is containerized and ready to run as a serverless Cloud Run service. These steps use only the GCP Console — no `gcloud` CLI required.

### One-time setup

1. **Enable APIs**: In the GCP Console, go to **APIs & Services → Enable APIs** and enable:
   - Cloud Run API
   - Artifact Registry API
   - Cloud Build API

2. **Create an Artifact Registry repository** (stores your Docker image):
   - Navigate to **Artifact Registry → Repositories → Create Repository**.
   - Format: **Docker** | Mode: **Standard** | Region: pick one (e.g., `us-central1`).
   - Note the full repository path shown (e.g., `us-central1-docker.pkg.dev/<project-id>/<repo-name>`).

### Build and push the image

You can build directly in GCP without Docker installed locally:

1. Go to **Cloud Build → Triggers** (or just use **Cloud Shell**).
2. Open **Cloud Shell** (terminal icon, top-right of console) and run:
   ```bash
   # Authenticate and set project
   gcloud config set project <your-project-id>

   # Clone your repo if not already there
   git clone <your-repo-url> && cd buckeye-meal-planner

   # Build and push to Artifact Registry
   gcloud builds submit --tag \
     us-central1-docker.pkg.dev/<project-id>/<repo-name>/buckeye-meal-planner:latest
   ```

### Deploy to Cloud Run

1. Navigate to **Cloud Run → Create Service**.
2. Select **Deploy one revision from an existing container image** and choose the image you just pushed.
3. Set the **Service name** (e.g., `buckeye-meal-planner`) and **Region**.
4. Under **Container, Networking, Security → Container**:
   - Set **Container port** to `5000`.
5. Under **Variables & Secrets**, add these environment variables:
   - `GEMINI_API_KEY` — your Gemini API key
   - `SECRET_KEY` — a long random string (e.g., output of `python3 -c "import secrets; print(secrets.token_hex(32))"`)
6. Under **Authentication**, choose **Allow unauthenticated invocations** if the app should be publicly accessible.
7. Click **Create** and wait for the service URL to appear.

### Redeploying after code changes

1. Push your changes to GitHub.
2. In Cloud Shell, pull the latest code, rebuild, and push the new image:
   ```bash
   git pull
   gcloud builds submit --tag \
     us-central1-docker.pkg.dev/<project-id>/<repo-name>/buckeye-meal-planner:latest
   ```
3. Cloud Run automatically deploys the new image. You can also click **Edit & Deploy New Revision** in the console and point it to the updated tag.
