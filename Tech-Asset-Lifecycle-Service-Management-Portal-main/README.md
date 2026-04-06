# Tech Asset & Service Management Portal

An internal business application for managing company technology assets and service requests. Employees submit requests for devices, repairs, and software access. IT staff process and track those requests. Managers approve restricted items. Admins manage users, roles, and request types.

## Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 17+, Angular Material, strict TypeScript |
| Backend | Java 21, Spring Boot 3.2.x, Spring Security (JWT) |
| Database | PostgreSQL 16, Flyway migrations |
| Deployment | GCP Cloud Run (backend + frontend), Cloud SQL |
| CI | GitHub Actions |

## Seed users (all passwords: `Password1!`)

| Email | Role |
|-------|------|
| `alice@company.com` | ADMIN |
| `mike@company.com` | MANAGER |
| `tina@company.com` | TECHNICIAN |
| `emma@company.com` | EMPLOYEE |

## Prerequisites

- Docker and Docker Compose
- Java 21 (local backend dev)
- Node.js 20+ (local frontend dev)
- Angular CLI (`npm install -g @angular/cli`)

## Local development

```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Backend — Flyway runs all migrations on startup
cd backend
cp .env.example .env      # fill in local values
./mvnw spring-boot:run    # http://localhost:8080

# 3. Frontend
cd frontend
npm install
ng serve                  # http://localhost:4200
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| pgAdmin | http://localhost:5050 (if `pgadmin` Docker profile is active) |

## Full stack via Docker Compose

```bash
docker-compose up -d --build
```

Starts frontend, backend, and PostgreSQL together. The Angular app is served by Nginx on port 4200.

## Running tests

```bash
# Backend unit + integration tests
cd backend && ./mvnw verify

# Frontend unit tests
cd frontend && npm test
```

## Documentation

| Doc | Purpose |
|-----|---------|
| [Architecture](docs/architecture.md) | System design, layering, workflows |
| [API Reference](docs/api.md) | All endpoints, auth, request/response shapes |
| [Data Model](docs/data-model.md) | Database tables and relationships |
| [Deployment](docs/deployment.md) | GCP Cloud Run setup guide |
| [Testing](docs/testing.md) | Test strategy and how to run each suite |
