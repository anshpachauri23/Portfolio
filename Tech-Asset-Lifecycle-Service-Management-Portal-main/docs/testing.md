# Testing Strategy

## Overview

| Layer | Framework | How to run |
|-------|-----------|-----------|
| Backend unit tests | JUnit 5 + Mockito | `./mvnw test` |
| Backend integration tests | JUnit 5 + Testcontainers (real PostgreSQL) | `./mvnw verify` |
| Frontend unit tests | Jasmine + Karma | `npm test` |

---

## Backend Tests

### Running

```bash
cd backend

# Unit tests only (fast — no Docker required)
./mvnw test

# Unit + integration tests (requires Docker for Testcontainers)
./mvnw verify
```

### Test files

| File | Type | What it covers |
|------|------|---------------|
| `AuthControllerTest` | `@WebMvcTest` | Login success, wrong credentials (401), missing fields (400), protected endpoint without token (401) |
| `AssetServiceTest` | Unit (Mockito) | All valid state transitions pass, all invalid transitions throw `InvalidStateTransitionException`, assign() writes `AssetHistory` |
| `AssetControllerTest` | `@WebMvcTest` | CRUD endpoints, EMPLOYEE cannot POST assets (403), 404 on unknown ID |
| `AssetIntegrationTest` | `@SpringBootTest` + Testcontainers | Full asset lifecycle against a real PostgreSQL database |
| `RequestServiceTest` | Unit (Mockito) | `approval_required=true` → `PENDING_APPROVAL`, `approval_required=false` → `IN_PROGRESS`, invalid transitions throw |
| `RequestControllerTest` | `@WebMvcTest` | EMPLOYEE only sees own requests, comment creation, queue restricted to TECHNICIAN/MANAGER/ADMIN |
| `ApprovalServiceTest` | Unit (Mockito) | Approve advances status, reject sets REJECTED, no-manager fallback works |
| `AuditServiceTest` | Unit (Mockito) | Records are written for create/update/status-change, `before_json`/`after_json` serialized correctly |
| `NotificationServiceTest` | Unit (Mockito) | Correct notifications created for each trigger event |
| `DashboardControllerTest` | `@WebMvcTest` | Summary endpoint returns correct shape, EMPLOYEE gets 403 |
| `AttachmentServiceTest` | Unit (Mockito) | Upload saves file + DB record, download streams correct content |
| `AdminUserServiceTest` | Unit (Mockito) | User create/update/deactivate writes audit records, duplicate email throws |
| `AdminRequestTypeServiceTest` | Unit (Mockito) | Create/update/toggle writes audit records |

### Test configuration

Tests use a shared `src/test/resources/application.properties` with JWT values so all `@WebMvcTest` and unit tests work without setting environment variables:

```properties
jwt.secret=dGVzdC1zZWNyZXQtZm9yLWNpLXBpcGVsaW5lLW9ubHktbm90LXVzZWQtaW4tcHJvZA==
jwt.expiry-ms=86400000
```

Integration tests (`AssetIntegrationTest`) use `@Testcontainers` + `@DynamicPropertySource` to spin up a real PostgreSQL container — do not mock the database for integration tests.

---

## Frontend Tests

### Running

```bash
cd frontend

# Headless (CI mode)
ng test --browsers=ChromeHeadless --watch=false

# Interactive (dev mode, auto-reruns on file change)
npm test
```

### Test files

| File | What it covers |
|------|---------------|
| `auth.service.spec.ts` | `login()` stores token, `logout()` clears token and navigates to `/login`, `isLoggedIn()` returns false after logout |
| `auth.guard.spec.ts` | Unauthenticated user is redirected to `/login`, authenticated user passes through |
| `role.guard.spec.ts` | User with matching role passes, user with wrong role is redirected |
| `asset-list.component.spec.ts` | Renders without errors, calls `AssetService.getAssets()` on init |
| `asset-detail.component.spec.ts` | Renders without errors, loads asset and history |
| `approval-queue.component.spec.ts` | Renders without errors, shows pending approvals |
| `dashboard.component.spec.ts` | Renders without errors, calls dashboard service on init |
| `user-management.component.spec.ts` | Renders Add User button, form validators work, form shows on toggle |
| `request-type-management.component.spec.ts` | Renders without errors, calls `getRequestTypes()` on init |
| `notification-bell.component.spec.ts` | Renders without errors, displays unread count |
| `app.component.spec.ts` | App component creates and renders router outlet |

---

## CI Pipeline

GitHub Actions (`.github/workflows/ci.yml`) runs on every push to `main` and every pull request:

```
push/PR to main
    ├── backend-test job
    │     ├── Set up Java 21
    │     ├── Cache Maven dependencies
    │     ├── ./mvnw verify
    │     └── Upload test results + JAR artifact
    └── frontend-test job
          ├── Set up Node 20
          ├── Cache npm dependencies
          ├── npm ci
          ├── ng test --browsers=ChromeHeadless --watch=false
          └── npm run build -- --configuration=production
```

Both jobs must pass for a build to be considered healthy. Cloud Run will still trigger a build from `main` even if CI fails — consider adding branch protection rules to block merges on CI failure.

---

## What is NOT tested (known gaps)

- End-to-end (Cypress/Playwright) tests — not yet implemented
- Frontend HTTP service tests — only component smoke tests exist
- Notification polling behavior
- File upload/download via frontend
