# GCP Deployment Guide

## Strategy

- **GitHub Actions** runs tests on every push to `main` (`.github/workflows/ci.yml`)
- **GCP Cloud Run** watches the repo and builds + deploys automatically whenever you push to `main`
- Tests and deployment are decoupled — Cloud Run triggers on push regardless of CI result. Add branch protection rules if you want to gate deploys on CI passing.

Both the backend and frontend are deployed as separate Cloud Run services. The frontend is a static Angular build served by Nginx — it has no server-side logic.

---

## Prerequisites

- GCP project with billing enabled
- Owner or Editor IAM role on the project
- GitHub repo accessible from GCP (done during Cloud Run setup)

---

## Step 1 — Enable Required GCP APIs

In Cloud Shell or locally with `gcloud`:

```bash
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  sqladmin.googleapis.com
```

---

## Step 2 — Create the Database (Cloud SQL)

1. **Cloud SQL → Create Instance → PostgreSQL 16**
2. Instance ID: `portal-db`
3. Region: e.g. `us-central1`
4. Set a strong root password
5. Under **Connections**: enable Public IP (or Private IP if using VPC)
6. Click **Create** (~5 minutes)

Once created:
1. **Databases → Create Database**: name it `portal_db`
2. **Users → Add User Account**: username `portal_user`, set a password

Note your **Connection Name**: `your-project-id:us-central1:portal-db` — needed in Step 3.

---

## Step 3 — Deploy the Backend (Cloud Run)

1. **Cloud Run → Create Service**
2. **Continuously deploy from a repository → Set Up with Cloud Build**
   - Provider: GitHub
   - Select your repository
   - Branch: `^main$`
   - Build type: **Dockerfile**
   - Source location: `/backend`
   - Dockerfile path: `Dockerfile`
3. Service name: `portal-backend`
4. Region: same as Cloud SQL
5. Authentication: **Allow unauthenticated invocations** (JWT handles auth)
6. **Container tab:**
   - Port: `8080`
   - Memory: `512 MiB` minimum, `1 GiB` recommended
   - CPU: `1`
7. **Connections tab:**
   - Add Cloud SQL connection → select `portal-db`
   - This creates a secure Unix socket — the `DB_HOST` env var uses the connection name format
8. **Variables & Secrets tab** — add these environment variables:

| Variable | Value |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `gcp` |
| `DB_HOST` | your Connection Name, e.g. `your-project:us-central1:portal-db` |
| `DB_NAME` | `portal_db` |
| `DB_USER` | `portal_user` |
| `DB_PASSWORD` | the password you set in Step 2 |
| `JWT_SECRET` | base64-encoded random string (min 32 bytes) |
| `JWT_EXPIRY_MS` | `86400000` (24 hours) |

Generate a JWT secret:
```bash
openssl rand -base64 32
```

9. Click **Create**

Cloud Run triggers a build and deploy on every push to `main`. First deploy takes ~4 minutes.

**Verify:**
```
GET https://your-backend-url/api/actuator/health
→ {"status":"UP"}
```

---

## Step 4 — Configure the Frontend API URL

The Angular app bakes the backend URL into the production bundle at build time. You must set the URL **before** deploying.

Edit `frontend/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-backend-url.run.app',   // ← your actual backend Cloud Run URL
};
```

The `angular.json` production configuration uses `fileReplacements` to swap `environment.ts` with `environment.prod.ts` at build time. This is already configured — you only need to update the URL value.

Commit and push this change so the Cloud Run build picks it up.

---

## Step 5 — Deploy the Frontend (Cloud Run)

Repeat Step 3 with these differences:

| Setting | Value |
|---------|-------|
| Source location | `/frontend` |
| Dockerfile path | `Dockerfile` |
| Service name | `portal-frontend` |
| Container port | `80` |
| Memory | `256 MiB` |

No environment variables are needed for the frontend — the API URL is compiled into the static bundle.

**Note:** The Nginx configuration (`frontend/nginx.conf`) serves static files only with `try_files` for Angular client-side routing. It does **not** proxy `/api/` requests — those go directly from the browser to the backend Cloud Run URL. This is why the API URL must be baked into the bundle.

---

## Step 6 — Allow CORS from Frontend

The backend `SecurityConfig` explicitly lists allowed CORS origins. After deploying the frontend, add its Cloud Run URL to the allowed origins list.

In `backend/src/main/java/com/company/portal/common/config/SecurityConfig.java`:

```java
config.setAllowedOrigins(List.of(
    "http://localhost:4200",
    "https://your-frontend-url.run.app"   // ← add your frontend Cloud Run URL
));
```

Commit, push, and wait for the backend to redeploy.

---

## Step 7 — Verify End-to-End

1. Open the frontend Cloud Run URL
2. Log in as `alice@company.com` / `Password1!` (ADMIN seed user)
3. Verify: assets page loads, create a service request, check the dashboard
4. Check logs: **Cloud Run → portal-backend → Logs** — structured JSON logs appear per request

---

## CI Pipeline (GitHub Actions)

File: `.github/workflows/ci.yml`

Triggers on every push to `main` and every pull request.

| Job | Steps |
|-----|-------|
| `backend-test` | Java 21 setup → Maven cache → `./mvnw verify` → upload test results + JAR |
| `frontend-test` | Node 20 setup → npm cache → `npm ci` → `ng test --browsers=ChromeHeadless` → production build |

To require CI to pass before merging PRs: **GitHub → Settings → Branches → Add rule → Require status checks** → select `backend-test` and `frontend-test`.

---

## Environment Variables Reference

| Variable | Service | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | Backend | Set to `gcp` on Cloud Run |
| `DB_HOST` | Backend | Cloud SQL connection name (`project:region:instance`) |
| `DB_NAME` | Backend | Database name (`portal_db`) |
| `DB_USER` | Backend | Database user |
| `DB_PASSWORD` | Backend | Database password |
| `JWT_SECRET` | Backend | Base64 string, min 32 bytes |
| `JWT_EXPIRY_MS` | Backend | Token lifetime in ms (86400000 = 24h) |

---

## Dockerfile Paths

| Service | Source location | Dockerfile path |
|---------|----------------|-----------------|
| Backend | `/backend` | `Dockerfile` |
| Frontend | `/frontend` | `Dockerfile` |

---

## Seed Users

All seed users have password `Password1!`:

| Email | Role |
|-------|------|
| alice@company.com | ADMIN |
| mike@company.com | MANAGER |
| tina@company.com | TECHNICIAN |
| emma@company.com | EMPLOYEE |

Flyway runs all 9 migrations (`V1–V9`) automatically on backend startup.
