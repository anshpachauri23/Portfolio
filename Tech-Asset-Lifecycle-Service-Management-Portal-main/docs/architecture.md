# Architecture Overview

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Frontend | Angular 17+, Angular Material, RxJS, TypeScript (strict) |
| Backend | Java 21, Spring Boot 3.2.x, Spring Security, Spring Data JPA, Lombok |
| Database | PostgreSQL 16, Flyway (9 migrations, V1–V9) |
| Auth | Stateless JWT (no session, no OAuth2) |
| File Storage | Local filesystem (dev) — same `StorageService` interface, swap implementation for GCS |
| Infrastructure | Docker, Docker Compose, GCP Cloud Run, Cloud SQL |
| CI | GitHub Actions |

---

## Backend Layering (strictly enforced)

```
HTTP Request
     │
     ▼
Controller          ← HTTP routing only. No business logic.
     │
     ▼
Service             ← All business logic, state machines, RBAC (@PreAuthorize), audit writes
     │         │
     ▼         ▼
Repository    AuditService / NotificationService
     │
     ▼
JPA Entity          ← ORM mapping only. No business logic in entities.
     │
     ▼
Flyway Migration    ← All schema changes. ddl-auto=validate only.
```

Controllers never write audit records, never touch repositories directly, and never expose JPA entities. All API inputs and outputs are DTOs. Mapping between entity and DTO is done in manual mapper classes.

---

## Frontend Structure

```
src/app/
├── core/
│   ├── auth/           AuthService, JwtInterceptor, AuthGuard, RoleGuard
│   ├── services/       AssetService, ServiceRequestService, NotificationService, etc.
│   └── models/         TypeScript interfaces (User, Asset, ServiceRequest, …)
├── features/
│   ├── auth/login/     Login page
│   ├── shell/          App shell: sidenav, toolbar, role-filtered nav
│   ├── assets/         Asset list, detail, create/edit form
│   ├── requests/       My requests, request queue, request detail, request form
│   ├── approvals/      Approval queue (MANAGER)
│   ├── dashboard/      Summary cards, charts (TECHNICIAN/MANAGER/ADMIN)
│   └── admin/          User management, request type management (ADMIN)
└── shared/
    ├── components/     StatusBadge, ConfirmDialog, NotificationBell
    └── pipes/          StatusLabelPipe
```

Components never call `HttpClient` directly. All HTTP goes through a typed service in `core/services/`.

---

## Authentication Flow

1. `POST /api/auth/login` — backend validates credentials, returns JWT
2. JWT is stored in-memory as a `BehaviorSubject<string | null>` in `AuthService`
3. `JwtInterceptor` (functional `HttpInterceptorFn`) attaches `Authorization: Bearer <token>` to every outgoing request
4. `JwtAuthenticationFilter` (backend `OncePerRequestFilter`) validates the token on each request, extracts email and role, and sets `SecurityContextHolder` — no database hit per request
5. `@PreAuthorize` annotations on service methods enforce RBAC
6. **Token is lost on page refresh** (intentional — in-memory only). User must log in again.

JWT claims: `sub` (email), `role`, `userId`, `exp`.

---

## Asset State Machine

Enforced in `AssetService.updateStatus()`. Invalid transitions throw `InvalidStateTransitionException` (HTTP 409).

```
AVAILABLE ──────────────────────────────► ASSIGNED
    │                                         │
    │◄─────────────────────────────────────── │ (reclaim)
    │                                         │
    ▼                                         ▼
UNDER_REPAIR ◄──────────────────────────── (from ASSIGNED)
    │
    ▼
AVAILABLE or RETIRED

LOST (reachable from ASSIGNED)
    │
    ▼
RECLAIMED → AVAILABLE or RETIRED

RETIRED (terminal — no transitions out)
```

Every status change writes an `asset_history` record via `AuditService`.

---

## Service Request Workflow

Initial status is determined by the `approval_required` flag on the selected `RequestType`:

```
approval_required = false:
  [create] → IN_PROGRESS → WAITING_FOR_USER → COMPLETED → CLOSED

approval_required = true:
  [create] → PENDING_APPROVAL
               │             │
               ▼             ▼
         IN_PROGRESS      REJECTED
               │
               ▼
        WAITING_FOR_USER → COMPLETED → CLOSED
```

When a request reaches `PENDING_APPROVAL`, one `approval_steps` record is created and assigned to the requester's manager (falls back to any MANAGER-role user if no manager is set).

---

## Approval Flow

1. Employee creates request with `approval_required = true` → status becomes `PENDING_APPROVAL`
2. `ApprovalService.createApprovalStep()` creates an `approval_steps` row and notifies the manager
3. Manager calls `POST /api/requests/{id}/approve` or `/reject` with an optional comment
4. `approve` → sets step decision to `APPROVED`, advances request to `IN_PROGRESS`, notifies requester
5. `reject` → sets step decision to `REJECTED`, closes request, notifies requester

---

## Audit Logging

Every state-changing operation writes an `audit_logs` record via `AuditService`:

- `AuditService.log(entityType, entityId, action, actorEmail, before, after)`
- `before` and `after` are serialized to JSON and stored in JSONB columns
- Runs in `REQUIRES_NEW` transaction — audit log is committed even if the outer transaction rolls back
- Insert-only — no updates or deletes on `audit_logs` ever

Wired in: `AssetService`, `ServiceRequestService`, `ApprovalService`, `AdminUserService`, `AdminRequestTypeService`.

---

## Notifications

`NotificationService.notify()` creates `notifications` rows for:

| Event | Recipient |
|-------|-----------|
| Request submitted | Assigned technician (if set) |
| Request reaches `PENDING_APPROVAL` | Assigned approver (manager) |
| Request approved/rejected | Requester |
| Request completed | Requester |
| Asset assigned | Assigned user |

The frontend polls `GET /api/notifications/unread-count` every 60 seconds to update the bell badge.

---

## File Attachments

`StorageService` interface abstracts file I/O:
- `LocalStorageService` implementation stores files on the local filesystem under `./uploads/`
- Swap to a GCS implementation for production by implementing the same interface with `@Profile("gcp")`

Attachments are linked to a `service_requests` record by `entity_type` + `entity_id`. Metadata (filename, size, content type, storage key) is stored in the `attachments` table.

---

## Security Boundaries

- Angular `AuthGuard` / `RoleGuard` are UX conveniences — they redirect unauthenticated users to `/login`
- **Real security** is on the backend: `JwtAuthenticationFilter` validates every token, `@PreAuthorize` on service methods enforces roles
- CORS is explicitly configured in `SecurityConfig` to allow only trusted origins (localhost:4200 and the production Cloud Run frontend URL)
- Passwords are stored as bcrypt hashes (cost 12)
- JWT secret is read from an environment variable — never hardcoded
