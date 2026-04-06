# CLAUDE.md — Tech Asset Lifecycle & Service Management Portal

This file provides Claude Code with all the context needed to work effectively on this project. Read it fully before making any changes. Each phase has its own detailed section — only read and action the phase you are currently working on.

---

## Project overview

A full-stack internal business application for managing company technology assets and service requests. Employees submit requests for devices, repairs, and software access. IT admins process and track those requests. Managers approve high-cost or restricted items. A system admin configures roles, reference data, and request types.

**Stack:** Angular 17+ frontend · Java 21 / Spring Boot 3.x backend · PostgreSQL · Google Cloud Platform (deployment)

**Auth:** JWT-based authentication with Spring Security. Roles stored in the database. No OAuth2 or external identity provider in scope.

---

## Repository structure

```
tech-asset-service-portal/
├── frontend/                  # Angular 17+ app
│   ├── src/app/
│   │   ├── core/              # Auth service, JWT interceptor, global error handler
│   │   ├── shared/            # Reusable components, pipes, directives
│   │   ├── auth/              # Login page, route guards
│   │   ├── assets/            # Asset list, detail, history timeline
│   │   ├── requests/          # Request list, detail, create form
│   │   ├── approvals/         # Approval queue
│   │   ├── dashboard/         # Charts and summary cards
│   │   └── admin/             # User, role, request type management
│   └── environments/
├── backend/                   # Spring Boot 3.x app
│   └── src/
│       ├── main/java/com/company/portal/
│       │   ├── auth/          # JWT util, security config, login endpoint
│       │   ├── asset/         # Asset controller, service, repo, domain
│       │   ├── request/       # Request controller, service, repo, domain
│       │   ├── approval/      # Approval workflow logic
│       │   ├── dashboard/     # Reporting and metrics queries
│       │   ├── admin/         # User/role/request-type management
│       │   ├── audit/         # Immutable audit log
│       │   ├── notification/  # In-app notifications
│       │   └── common/        # DTOs, exceptions, pagination, mappers
│       └── main/resources/
│           └── db/migration/  # Flyway migration scripts (V1__, V2__, etc.)
├── infra/                     # Dockerfiles, Docker Compose, GCP deployment notes
├── docs/                      # Architecture, API notes, screenshots
├── .github/workflows/         # CI/CD pipelines
├── docker-compose.yml
├── README.md
└── CLAUDE.md
```

---

## Architecture principles

### Backend layering (strictly enforced)

Every feature module follows this structure. Do not skip layers or mix responsibilities.

```
Controller → Service → Repository → Domain/Entity
               ↓
             Mapper
               ↓
              DTO
```

- **Controller**: HTTP routing only. No business logic. Delegates everything to the service.
- **Service**: All business logic, validation, state transition enforcement, audit log writes.
- **Repository**: Spring Data JPA interfaces. Custom JPQL/native queries where needed.
- **Domain/Entity**: JPA-annotated entities. No business logic inside entities.
- **DTO**: All API inputs and outputs. Never expose a JPA entity directly over the API.
- **Mapper**: MapStruct or manual mappers between entity and DTO.

### Frontend structure

- `core/` — singleton services (AuthService, JWT interceptor, error interceptor). Imported once in AppModule.
- `shared/` — reusable dumb components, pipes, Angular Material imports.
- Feature modules (`auth/`, `assets/`, `requests/`, etc.) — each has its own routing, components, and service.
- Components do not call `HttpClient` directly. All HTTP calls go through a typed service class.

---

## Key domain concepts

### Asset lifecycle states

Enforce transitions in `AssetService`. Throw a domain exception for invalid transitions. Do not let the controller handle this.

```
AVAILABLE    → ASSIGNED
ASSIGNED     → UNDER_REPAIR
ASSIGNED     → RECLAIMED → AVAILABLE
ASSIGNED     → RETIRED
UNDER_REPAIR → AVAILABLE
UNDER_REPAIR → RETIRED
ANY STATE    → LOST
```

### Service request workflow states

```
DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED → IN_PROGRESS → WAITING_FOR_USER → COMPLETED → CLOSED
                  ↘ (if approval_required = false on RequestType)
                    IN_PROGRESS → WAITING_FOR_USER → COMPLETED → CLOSED
                                     REJECTED (from PENDING_APPROVAL only)
```

Whether approval is required is determined by the `approval_required` flag on the `RequestType` record. This flag is configurable by the admin. There is no rules engine — it is a simple boolean per request type.

### User roles

| Role        | What they can do                                                      |
|-------------|-----------------------------------------------------------------------|
| EMPLOYEE    | View own assets, create and view own requests                         |
| TECHNICIAN  | View and update all assets and requests, add service notes            |
| MANAGER     | Approve or reject pending requests, view team-level dashboard         |
| ADMIN       | Full access including user management, roles, and request type config |

RBAC is enforced at the **service layer** using `@PreAuthorize`. Angular route guards are a UX convenience only — they are not the security boundary.

---

## Coding standards

### Java / Spring Boot

- Java 21. Use records for DTOs where immutability is appropriate.
- Constructor injection only. No `@Autowired` field injection.
- `@Valid` on all request bodies. Return meaningful validation error messages.
- Centralized exception handling via `@RestControllerAdvice`.
- All list endpoints use Spring Data `Pageable` for pagination.
- Flyway for all schema changes. Never use `ddl-auto=create` or `ddl-auto=update` in non-test profiles.
- `@Transactional` at the service layer for multi-step operations.
- SLF4J for logging: INFO for business events, DEBUG for internals. Include a request/correlation ID.
- Every state-changing service method writes an `AuditLog` record via `AuditService`.

### Angular / TypeScript

- Strict TypeScript. No `any` except with a comment justifying it.
- Angular Reactive Forms for all forms. No template-driven forms.
- Route guards (`CanActivate`) for role-based page protection.
- Global HTTP error handling in an interceptor. No raw error objects shown to users.
- Angular Material as the UI component library. Do not mix in other UI libraries.

### General

- Self-documenting names. Comments only for non-obvious logic.
- Short, single-purpose methods.
- No secrets or credentials in source control. Use `.env` locally and Secret Manager on GCP.

---

## API conventions

- Base path: `/api`
- Auth header: `Authorization: Bearer <token>`
- Success envelope:
  ```json
  { "data": {}, "message": "Asset created", "timestamp": "2024-01-15T10:30:00Z" }
  ```
- Error envelope:
  ```json
  { "error": "INVALID_STATE_TRANSITION", "message": "Asset cannot move from RETIRED to ASSIGNED", "timestamp": "..." }
  ```
- Paginated list response:
  ```json
  { "content": [], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5 }
  ```
- `PATCH` for partial updates and status changes. `PUT` for full resource replacement.
- All dates and timestamps in ISO 8601 UTC format.

---

## Database conventions

- Migration files: `backend/src/main/resources/db/migration/V{n}__{description}.sql`
- `snake_case` for all table and column names.
- Every table has: `id` (UUID preferred, or BIGSERIAL), `created_at`, `updated_at` (both `timestamp with time zone`).
- Soft deletes for auditable entities using `active boolean` or `deleted_at`.
- Explicit foreign key constraints with meaningful names.
- Never alter an already-applied migration. Always create a new one.

### Core entities
`users`, `roles`, `departments`, `request_types`, `assets`, `asset_assignments`, `asset_history`, `service_requests`, `request_comments`, `approval_steps`, `notifications`, `audit_logs`, `attachments`

---

## Audit logging

Every create, update, delete, approve, and reject action on a core entity must produce an `audit_logs` record.

Fields: `entity_type`, `entity_id`, `action`, `actor_id`, `before_json`, `after_json`, `created_at`

Use a shared `AuditService` injected into feature services. Controllers never write audit records directly.

---

## Testing requirements

### Backend
- JUnit 5 + Mockito for all service classes.
- `@WebMvcTest` or `@SpringBootTest` + MockMvc for REST endpoint tests.
- Testcontainers for database integration tests. Do not mock the database in integration tests.
- Must cover: approval flow, invalid state transitions, RBAC checks, audit log writes.
- Target: 70%+ meaningful line coverage on business logic.

### Frontend
- Jasmine/Karma for component and service unit tests.
- Cover route guards and form validators.
- Cypress or Playwright for end-to-end smoke tests on: login, create request, approve request, asset status update.

### Running tests
```bash
# Backend
cd backend
./mvnw test          # unit tests only
./mvnw verify        # unit + integration tests

# Frontend
cd frontend
npm test             # unit tests
npm run e2e          # end-to-end tests
```

---

## Local development setup

### Prerequisites
- Java 21
- Node.js 20+
- Angular CLI: `npm install -g @angular/cli`
- Docker and Docker Compose

### Start local environment
```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Backend (Flyway runs migrations on startup)
cd backend
cp .env.example .env    # fill in local values
./mvnw spring-boot:run

# 3. Frontend
cd frontend
cp environments/environment.example.ts environments/environment.ts
npm install
ng serve
```

| Service    | URL                                   |
|------------|---------------------------------------|
| Frontend   | http://localhost:4200                 |
| Backend    | http://localhost:8080                 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| pgAdmin    | http://localhost:5050 (if enabled)    |

### Backend `.env` variables
```
SPRING_PROFILES_ACTIVE=local
DB_HOST=localhost
DB_PORT=5432
DB_NAME=portal_db
DB_USER=portal_user
DB_PASSWORD=local_password
JWT_SECRET=local_dev_secret_replace_in_prod
GCP_PROJECT_ID=
GCS_BUCKET_NAME=
MAIL_PROVIDER_API_KEY=
```

---

## CI/CD and deployment (Phase 3 only)

| Workflow           | Trigger                        | Purpose                                               |
|--------------------|--------------------------------|-------------------------------------------------------|
| `ci.yml`           | PR and push to `main`          | Lint, test, build frontend and backend                |
| `docker-build.yml` | Push to `main` or version tags | Build and push Docker images to Artifact Registry     |
| `deploy-gcp.yml`   | Manual dispatch or release tag | Deploy to Cloud Run and Firebase Hosting              |

### GCP services used

| Component     | GCP Service                     |
|---------------|---------------------------------|
| Backend API   | Cloud Run                       |
| Frontend      | Firebase Hosting                |
| Database      | Cloud SQL (PostgreSQL)          |
| Attachments   | Cloud Storage                   |
| Images        | Artifact Registry               |
| Secrets       | Secret Manager                  |
| Observability | Cloud Logging + Cloud Monitoring|

Use Workload Identity Federation for GitHub Actions → GCP auth. No long-lived service account keys.

---

## Common pitfalls

- Never expose JPA entities in API responses. Always map to a DTO first.
- Never put business logic in a controller. It belongs in the service.
- Never skip Flyway. Do not use `ddl-auto=update` in any non-test profile.
- Never rely only on Angular route guards for access control. Always enforce RBAC on the backend too.
- Never transition asset or request state without writing a history/audit record.
- Never commit `.env` files or any file containing real credentials.
- Never add a new endpoint without a test and a Swagger annotation.

---

---

# PHASES

Each phase is a self-contained work unit. Start a fresh Claude Code session for each phase. Paste the relevant phase section below along with this full CLAUDE.md as your starting context. Complete and verify each phase fully before starting the next.

---

## Phase 1 — Foundation and MVP

**Goal:** A working, runnable application with auth, asset management, and service request management. Every feature in this phase must be tested before moving on.

### 1.1 Project scaffolding
- Initialise the Spring Boot project (Maven, Java 21, Spring Web, Spring Security, Spring Data JPA, Flyway, Validation, OpenAPI/Springdoc, Lombok).
- Initialise the Angular project (Angular 17+, strict mode, Angular Material, Angular Router).
- Set up `docker-compose.yml` with `postgres` and optional `pgadmin` services.
- Create `.env.example` for the backend.
- **Verify:** `docker-compose up -d postgres` starts cleanly, backend starts and connects to DB, frontend serves on port 4200.

### 1.2 Database foundation (Flyway)
`V1__init_schema.sql` — create tables:
- `departments`: id, name, created_at, updated_at
- `users`: id, name, email, employee_code, password_hash, role ENUM, department_id FK, manager_id FK (self-ref), active, created_at, updated_at
- `request_types`: id, name, description, approval_required boolean, active, created_at, updated_at

`V2__seed_reference_data.sql` — insert:
- 3 departments: Engineering, Operations, HR
- 5 request types: New Device, Replacement, Repair, Software Access, Return Asset — `approval_required = true` for New Device and Software Access only
- 4 demo users (one per role: EMPLOYEE, TECHNICIAN, MANAGER, ADMIN) with bcrypt-hashed passwords

### 1.3 JWT authentication (backend)
- `POST /api/auth/login` — accepts email + password, returns JWT and user info (id, name, role)
- `GET /api/auth/me` — returns current user from JWT
- `POST /api/auth/logout` — stateless, return 200
- Spring Security config: permit `/api/auth/**`, require auth for everything else
- JWT utility: generate, validate, extract claims. Role stored as a JWT claim.
- Custom `UserDetailsService` loading from the `users` table

### 1.4 Auth (frontend)
- Login page with email/password Reactive Form, validation messages, error display
- `AuthService`: calls `/api/auth/login`, stores JWT in memory (not localStorage), exposes current user as Observable
- JWT interceptor: attaches `Authorization: Bearer` header to all outgoing requests
- `AuthGuard`: redirects to `/login` if no valid token
- `RoleGuard`: checks role claim for role-protected routes
- App shell with top nav and sidebar showing role-appropriate navigation links

### 1.5 Asset management (backend)
`V3__asset_tables.sql`:
- `assets`: id, asset_tag, serial_number, asset_type ENUM, vendor, model, status ENUM, purchase_date, warranty_expiry, assigned_user_id FK, location, cost_center, created_at, updated_at
- `asset_history`: id, asset_id FK, event_type, from_status, to_status, actor_id FK, notes, created_at

Asset status ENUM: `AVAILABLE, ASSIGNED, UNDER_REPAIR, RECLAIMED, LOST, RETIRED`

Endpoints:
- `GET /api/assets` — paginated, filterable by status/type/assigned_user
- `POST /api/assets` — ADMIN/TECHNICIAN only
- `GET /api/assets/{id}` — all roles
- `PUT /api/assets/{id}` — ADMIN/TECHNICIAN only
- `PATCH /api/assets/{id}/status` — validate transition, write asset_history record
- `POST /api/assets/{id}/assign` — assign to a user, write asset_history
- `GET /api/assets/{id}/history` — all roles

State transition validation lives in `AssetService`. Throw `InvalidStateTransitionException` for illegal transitions.

### 1.6 Asset management (frontend)
- Asset list page: table with asset tag, type, status badge, assigned user, vendor; filter bar; pagination
- Asset detail page: metadata + history timeline of asset_history events
- Create/edit asset form (ADMIN/TECHNICIAN): all fields with validation
- Status change action (TECHNICIAN/ADMIN): dropdown showing valid next states only, confirmation dialog

### 1.7 Service request management (backend)
`V4__request_tables.sql`:
- `service_requests`: id, request_type_id FK, title, description, requester_id FK, asset_id FK nullable, priority ENUM, status ENUM, assigned_to FK nullable, approval_required boolean, created_at, updated_at, due_date, closed_at
- `request_comments`: id, request_id FK, author_id FK, body text, created_at

Request status ENUM: `DRAFT, SUBMITTED, PENDING_APPROVAL, APPROVED, IN_PROGRESS, WAITING_FOR_USER, COMPLETED, CLOSED, REJECTED`

Endpoints:
- `GET /api/requests` — EMPLOYEE sees own only; TECHNICIAN/MANAGER/ADMIN see all; filterable by status/type
- `POST /api/requests` — any authenticated user
- `GET /api/requests/{id}` — requester or TECHNICIAN+ only
- `PATCH /api/requests/{id}/status` — TECHNICIAN/ADMIN only; enforce valid transitions
- `POST /api/requests/{id}/comments` — any user with access to the request

On creation: copy `approval_required` from the selected `RequestType`. If true, set initial status to `PENDING_APPROVAL`. If false, set to `IN_PROGRESS`.

### 1.8 Service request management (frontend)
- My requests page (EMPLOYEE): table of own requests with status badges, create button
- Request queue page (TECHNICIAN/ADMIN): all requests with status/type filters
- Request detail page: metadata, status badge, SLA dates, comment thread, status update action
- Create request form: select request type (show approval warning if approval_required), optionally link asset, fill title/description/priority

### 1.9 Phase 1 tests
Backend:
- `AuthControllerTest`: login success, bad credentials, protected endpoint without token
- `AssetServiceTest`: all valid transitions succeed, all invalid transitions throw exception, assign writes history record
- `AssetControllerTest`: CRUD works, EMPLOYEE cannot create (403)
- `RequestServiceTest`: creation sets correct initial status based on approval_required, invalid transitions throw exception
- `RequestControllerTest`: EMPLOYEE only sees own requests, comment creation works

Frontend:
- `AuthService`: login stores token, logout clears state
- `AuthGuard`: unauthenticated user is redirected to /login
- Asset list and detail component smoke tests

**Phase 1 is done when:** All endpoints return correct responses, role guards work on both frontend and backend, state machines reject invalid transitions, all Phase 1 tests pass, and the app is fully usable end-to-end with the seed users.

---

## Phase 2 — Business Logic and Completeness

**Goal:** Approval workflow, audit logging, notifications, dashboard, and file attachments. Phase 1 must be fully working before starting here.

### 2.1 Approval workflow (backend)
`V5__approval_tables.sql`:
- `approval_steps`: id, request_id FK, approver_id FK, decision ENUM (PENDING, APPROVED, REJECTED), comment, decided_at, sequence_order, created_at

When a request reaches `PENDING_APPROVAL`, create one `approval_steps` record assigned to the requester's manager. If the requester has no manager, assign to any MANAGER-role user.

Endpoints:
- `POST /api/requests/{id}/approve` — MANAGER only; sets decision to APPROVED, advances request to IN_PROGRESS
- `POST /api/requests/{id}/reject` — MANAGER only; sets decision to REJECTED, sets request to REJECTED

### 2.2 Approval queue (frontend)
- Approval queue page (MANAGER only): list of requests in PENDING_APPROVAL assigned to the current user
- Approve/reject actions with a comment dialog
- Approval history section added to the request detail page

### 2.3 Audit logging
`V6__audit_log.sql`:
- `audit_logs`: id, entity_type varchar, entity_id varchar, action varchar, actor_id FK, before_json jsonb, after_json jsonb, created_at

`AuditService` with one method: `log(entityType, entityId, action, actorId, before, after)`

Wire into: `AssetService` (create, update, status change, assign), `RequestService` (create, status change, approve, reject), `UserService` (create, update, deactivate).

`audit_logs` is insert-only. No update or delete on this table ever.

Admin endpoint: `GET /api/admin/audit-logs` — ADMIN only, paginated, filterable by entity_type and actor_id

### 2.4 In-app notifications
`V7__notifications.sql`:
- `notifications`: id, user_id FK, title, body, read boolean, entity_type, entity_id, created_at

`NotificationService` creates notifications for:
- Request submitted → notify assigned TECHNICIAN (if any)
- Request reaches PENDING_APPROVAL → notify the approver (manager)
- Request approved/rejected → notify the requester
- Request completed → notify the requester
- Asset assigned to user → notify that user

Endpoints:
- `GET /api/notifications` — current user's notifications, unread first
- `PATCH /api/notifications/{id}/read` — mark one as read
- `PATCH /api/notifications/read-all` — mark all as read

Frontend:
- Notification bell in top nav with unread count badge
- Dropdown showing recent notifications with mark-as-read
- Poll for new notifications every 60 seconds (simple interval, no WebSocket needed)

### 2.5 Dashboard (backend)
Endpoints:
- `GET /api/dashboard/summary` — open request count by status, asset count by status
- `GET /api/dashboard/request-trends` — request counts grouped by month for the last 6 months
- `GET /api/dashboard/assets-by-status` — asset counts per status
- `GET /api/dashboard/sla-aging` — requests grouped by days past due_date (0–3, 4–7, 7+)

MANAGER sees team-scoped data (their direct reports). ADMIN and TECHNICIAN see all. EMPLOYEE gets 403.

### 2.6 Dashboard (frontend)
- Summary stat cards (open requests, assets by status)
- Bar/line chart for request trends
- Doughnut chart for assets by status
- SLA aging table
- Use ngx-charts or Chart.js — pick one, stay consistent
- EMPLOYEE does not see the dashboard in navigation

### 2.7 File attachments
`V8__attachments.sql`:
- `attachments`: id, entity_type, entity_id, file_name, file_size, content_type, storage_key, uploaded_by FK, created_at

`StorageService` interface with:
- Local filesystem implementation (for `local` Spring profile)
- GCS implementation (for `gcp` profile)

Endpoints:
- `POST /api/requests/{id}/attachments` — multipart upload, TECHNICIAN or requester only
- `GET /api/requests/{id}/attachments` — list attachments
- `GET /api/attachments/{id}/download` — stream file to client

Frontend:
- File upload component on request detail page (file picker or drag-and-drop)
- Attachment list: name, size, uploader, date, download link

### 2.8 Admin pages (frontend)
- User management page: list, create, edit (name, email, role, department, manager), deactivate
- Request type management page: list, toggle approval_required, create new type
- Both pages are ADMIN only with route guard

### 2.9 Phase 2 tests
Backend:
- `ApprovalServiceTest`: approval advances status, rejection sets REJECTED, no-manager fallback assigns correctly
- `AuditServiceTest`: all wired service methods produce audit records with correct before/after JSON
- `NotificationServiceTest`: correct notifications created for each trigger
- `DashboardControllerTest`: summary returns expected shape, EMPLOYEE gets 403
- `AttachmentServiceTest`: upload saves file and DB record, download streams correct content

Frontend:
- Approval queue: renders pending items, approve action calls correct endpoint
- Notification bell: shows unread count, mark as read updates state
- Dashboard: charts render with mocked API data

**Phase 2 is done when:** Approval flow works end-to-end, audit records are written for all state changes, notifications appear in the bell, dashboard charts show real data, file uploads and downloads work.

---

## Phase 3 — Production Readiness

**Goal:** Dockerize, set up CI/CD, deploy to GCP, add observability, and finalize documentation. Phase 2 must be fully working before starting here.

### 3.1 Dockerfiles
`backend/Dockerfile`:
- Multi-stage: Maven build stage → JRE 21 runtime stage
- Expose port 8080, run as non-root user

`frontend/Dockerfile`:
- Multi-stage: Node build stage → Nginx serving stage
- `nginx.conf` with `try_files` for Angular client-side routing
- Expose port 80

### 3.2 Docker Compose (full local stack)
Update `docker-compose.yml`:
- `postgres` with health check
- `backend` depending on postgres, reads from `.env`
- `frontend` proxying `/api` to backend
- `pgadmin` (optional, profile-gated)

**Verify:** `docker-compose up` starts all services, app is fully usable at http://localhost:4200.

### 3.3 CI pipeline (`.github/workflows/ci.yml`)
Triggers: pull_request, push to main

Steps: checkout → set up Java 21 → set up Node 20 → cache Maven/npm → backend tests (`./mvnw verify`) → frontend unit tests → Angular production build → Spring Boot package build → upload artifacts

Fail fast: any failure stops the workflow.

### 3.4 Docker build pipeline (`.github/workflows/docker-build.yml`)
Triggers: push to main, version tags (`v*.*.*`)

Steps: authenticate to Artifact Registry via Workload Identity Federation → build and tag backend image → build and tag frontend image → push both to Artifact Registry

### 3.5 GCP deploy pipeline (`.github/workflows/deploy-gcp.yml`)
Triggers: manual dispatch or push to `release` branch

Steps: authenticate to GCP → deploy backend to Cloud Run (env vars from Secret Manager) → deploy frontend to Firebase Hosting → smoke check `GET /api/actuator/health` returns 200

### 3.6 Observability
Backend:
- Spring Boot Actuator: expose `/actuator/health` and `/actuator/info`
- Structured JSON logging in non-local profiles (logback config)
- `CorrelationIdFilter`: generate UUID per request, add to MDC so all log lines for a request share the same ID
- Health endpoint used as Cloud Run liveness and readiness probe

GCP:
- Cloud Logging receives structured JSON logs automatically from Cloud Run stdout
- Create a basic Cloud Monitoring dashboard: request count, error rate, p95 latency for the backend Cloud Run service

### 3.7 Seed data and demo environment
`V9__demo_seed_data.sql`:
- 10 demo assets across types and statuses
- 8 demo service requests across statuses and request types
- Asset history records and request comments
- Approval step records for requests in PENDING_APPROVAL

Run this migration only in `local` and `demo` profiles using Flyway's `locations` config to include a `db/seed` folder selectively.

### 3.8 Documentation
Complete or update:
- `README.md`: overview, prerequisites, local setup, Swagger link
- `docs/architecture.md`: system diagram, component responsibilities, approval workflow data flow
- `docs/api.md`: Swagger UI link, auth flow overview, endpoint groups
- `docs/deployment.md`: GCP setup checklist, manual deploy steps, environment variable reference
- `docs/testing.md`: test strategy per layer, how to run each suite, coverage targets
- `docs/data-model.md`: ER diagram (generated from schema), entity descriptions and relationships
- `docs/screenshots/`: screenshots of every major page committed to the repo

### 3.9 Phase 3 verification
- `docker-compose up` runs the full stack with no manual steps
- CI pipeline passes on a clean branch
- Deployed Cloud Run backend returns 200 on `/actuator/health`
- Smoke test deployed app: login, view assets, create a request, approve a request

**Phase 3 is done when:** CI passes, Docker Compose works, app is live on GCP, observability is active, all documentation is complete and accurate.

---

## Stretch goals (after Phase 3)

- Bulk asset import from CSV upload
- Barcode / QR code based asset lookup
- SSO with Google Identity (add OAuth2 on top of existing JWT auth)
- SLA breach alerting via Cloud Monitoring
- Vendor recommendation for replacement requests
- Space and team asset allocation view

---

## Documentation files to maintain

| File                    | Purpose                                         |
|-------------------------|-------------------------------------------------|
| `README.md`             | Overview and local setup                        |
| `docs/architecture.md`  | System design and data flow                     |
| `docs/api.md`           | API reference / Swagger link                    |
| `docs/deployment.md`    | GCP deployment steps and checklist              |
| `docs/testing.md`       | Test strategy and how to run each suite         |
| `docs/data-model.md`    | ER diagram and entity descriptions              |
| `docs/screenshots/`     | UI screenshots for each major page              |
