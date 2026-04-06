# Data Model

PostgreSQL 16 database managed entirely through Flyway migrations (V1–V9). Schema changes are never applied via `ddl-auto` — always through a new numbered migration file.

---

## Conventions

- All table and column names are `snake_case`
- Every table has `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- Most mutable tables also have `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- Soft deletes use an `active BOOLEAN` column — no hard deletes on auditable entities
- IDs are `BIGSERIAL` (auto-increment integer). UUIDs are not used.
- Java ENUMs map to `VARCHAR` columns with `CHECK` constraints — avoids PostgreSQL ENUM type limitations

---

## Tables

### departments (V1)
Organizational units that users belong to.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| name | VARCHAR(100) UNIQUE | e.g. "Engineering" |
| code | VARCHAR(20) UNIQUE | e.g. "ENG" |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

Seed data: Engineering (ENG), Operations (OPS), HR (HR)

---

### users (V1)
Employees and system accounts. One record per person.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| name | VARCHAR(150) | |
| email | VARCHAR(255) UNIQUE | Used as login and JWT subject |
| employee_code | VARCHAR(50) UNIQUE | Optional HR identifier |
| password_hash | VARCHAR(255) | bcrypt cost 12 |
| role | VARCHAR(20) | CHECK: EMPLOYEE, TECHNICIAN, MANAGER, ADMIN |
| department_id | BIGINT FK → departments | Nullable |
| manager_id | BIGINT FK → users | Self-reference. Nullable. Used for approval routing |
| active | BOOLEAN DEFAULT true | Soft delete flag |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

Seed data (all passwords: `Password1!`):

| Email | Role | Department |
|-------|------|-----------|
| alice@company.com | ADMIN | Engineering |
| mike@company.com | MANAGER | Engineering |
| tina@company.com | TECHNICIAN | Operations |
| emma@company.com | EMPLOYEE | HR (manager: mike) |

---

### request_types (V1)
Configurable catalogue of support request categories.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| name | VARCHAR(100) UNIQUE | |
| description | TEXT | |
| approval_required | BOOLEAN DEFAULT false | Drives initial request status |
| active | BOOLEAN DEFAULT true | Inactive types hidden from employees |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

Seed data:

| Name | Approval Required |
|------|------------------|
| New Device | true |
| Device Replacement | false |
| Repair | false |
| Software Access | true |
| Return Asset | false |

---

### assets (V3)
Physical and software assets owned by the company.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| asset_tag | VARCHAR(50) UNIQUE | Human-readable identifier |
| serial_number | VARCHAR(100) | |
| asset_type | VARCHAR(50) | LAPTOP, DESKTOP, MONITOR, KEYBOARD, MOUSE, PHONE, TABLET, OTHER |
| vendor | VARCHAR(100) | |
| model | VARCHAR(100) | |
| status | VARCHAR(20) | CHECK: AVAILABLE, ASSIGNED, UNDER_REPAIR, RECLAIMED, LOST, RETIRED |
| purchase_date | DATE | |
| warranty_expiry | DATE | |
| assigned_user_id | BIGINT FK → users | Nullable. Set when status = ASSIGNED |
| location | VARCHAR(200) | Physical location or office |
| cost_center | VARCHAR(100) | |
| notes | TEXT | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### asset_history (V3)
Immutable event log of every status change and assignment for an asset.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| asset_id | BIGINT FK → assets CASCADE | |
| event_type | VARCHAR(50) | e.g. STATUS_CHANGE, ASSIGN |
| from_status | VARCHAR(20) | Previous status |
| to_status | VARCHAR(20) | New status |
| actor_id | BIGINT FK → users | Who made the change |
| notes | TEXT | |
| created_at | TIMESTAMPTZ | |

---

### service_request_seq (V3)
PostgreSQL sequence for generating request numbers in the format `SR-YYYYMMDD-NNNNN`.
Starts at 1000.

---

### service_requests (V4)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| request_number | VARCHAR(30) UNIQUE | Format: SR-20260101-01000 |
| request_type_id | BIGINT FK → request_types | |
| title | VARCHAR(255) | |
| description | TEXT | |
| requester_id | BIGINT FK → users | |
| asset_id | BIGINT FK → assets | Nullable. Linked asset if relevant |
| priority | VARCHAR(20) | CHECK: LOW, MEDIUM, HIGH, CRITICAL |
| status | VARCHAR(30) | See status enum below |
| assigned_to | BIGINT FK → users | Nullable. Technician handling the request |
| approval_required | BOOLEAN DEFAULT false | Copied from request_type at creation time |
| due_date | DATE | |
| closed_at | TIMESTAMPTZ | Set when status = CLOSED or REJECTED |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

Status values: `DRAFT`, `SUBMITTED`, `PENDING_APPROVAL`, `APPROVED`, `IN_PROGRESS`, `WAITING_FOR_USER`, `COMPLETED`, `CLOSED`, `REJECTED`

---

### request_comments (V4)
Comment thread on a service request.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| request_id | BIGINT FK → service_requests CASCADE | |
| author_id | BIGINT FK → users | |
| body | TEXT | |
| created_at | TIMESTAMPTZ | |

---

### approval_steps (V6)
One row per approval gate on a request. Currently one gate per request (sequence_order = 1).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| request_id | BIGINT FK → service_requests | |
| approver_id | BIGINT FK → users | Manager assigned to approve |
| decision | VARCHAR(20) | CHECK: PENDING, APPROVED, REJECTED |
| comment | TEXT | Optional decision comment |
| decided_at | TIMESTAMPTZ | Nullable until decision is made |
| sequence_order | INT | For future multi-step approval (currently always 1) |
| created_at | TIMESTAMPTZ | |

---

### audit_logs (V7)
Immutable record of every create, update, status change, approval, and rejection on core entities.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| entity_type | VARCHAR(50) | e.g. ASSET, SERVICE_REQUEST, USER |
| entity_id | VARCHAR(50) | String ID of the affected entity |
| action | VARCHAR(50) | e.g. CREATE, STATUS_CHANGE, APPROVE |
| actor_id | BIGINT FK → users | Nullable |
| before_json | JSONB | State before change |
| after_json | JSONB | State after change |
| created_at | TIMESTAMPTZ | |

Insert-only. No update or delete ever touches this table.

---

### notifications (V8)
In-app notification inbox per user.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users | Recipient |
| title | VARCHAR(255) | |
| body | TEXT | |
| read | BOOLEAN DEFAULT false | |
| entity_type | VARCHAR(50) | e.g. SERVICE_REQUEST, ASSET |
| entity_id | VARCHAR(50) | For deep-linking to the related entity |
| created_at | TIMESTAMPTZ | |

---

### attachments (V9)
File attachments linked to service requests (extensible to other entities via entity_type/entity_id).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| entity_type | VARCHAR(50) | e.g. SERVICE_REQUEST |
| entity_id | VARCHAR(50) | ID of the related entity |
| file_name | VARCHAR(255) | Original filename |
| file_size | BIGINT | Size in bytes |
| content_type | VARCHAR(100) | MIME type |
| storage_key | VARCHAR(500) | File path (local) or GCS object key |
| uploaded_by | BIGINT FK → users | |
| created_at | TIMESTAMPTZ | |

---

## Entity Relationship Summary

```
departments ←── users ──────────────────────────────────► assets
                  │                                           │
                  │ (manager_id self-ref)                     │ (assigned_user_id)
                  │                                           │
                  ▼                                           ▼
           service_requests ─────────────────────► asset_history
                  │
          ┌───────┼────────────────┐
          ▼       ▼                ▼
   request_   approval_      request_      attachments
   comments    steps         types
                  │
                  ▼
            notifications
                  │
            audit_logs (entity_type + entity_id links to any table)
```
