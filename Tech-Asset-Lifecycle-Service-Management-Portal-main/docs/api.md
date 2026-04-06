# API Reference

Base path: `/api`
Backend runs on `http://localhost:8080` locally and on the Cloud Run service URL in production.
Interactive docs: `http://localhost:8080/swagger-ui.html`

---

## Authentication

All endpoints except `POST /api/auth/login` and `POST /api/auth/logout` require:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## Response Envelopes

**Success**
```json
{
  "data": {},
  "message": "Asset created",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Error**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "message": "Asset cannot move from RETIRED to ASSIGNED",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Paginated list** (inside `data`)
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Auth

### POST /api/auth/login
**Public.** Authenticate and receive a JWT.

Request body:
```json
{ "email": "emma@company.com", "password": "Password1!" }
```

Response `data`:
```json
{
  "token": "<JWT>",
  "userId": 4,
  "name": "Emma Employee",
  "email": "emma@company.com",
  "role": "EMPLOYEE"
}
```

### GET /api/auth/me
**Authenticated.** Returns the current user's profile decoded from the JWT.

### POST /api/auth/logout
**Authenticated.** Stateless — returns 200. The client discards the token.

---

## Assets

### GET /api/assets
**Authenticated.** Returns paginated list with optional filters.

Query params: `status`, `assetType`, `assignedUserId`, `page`, `size`, `sort`

### POST /api/assets
**ADMIN or TECHNICIAN only.**

Request body fields: `assetTag`, `serialNumber`, `assetType`, `vendor`, `model`, `status`, `purchaseDate`, `warrantyExpiry`, `assignedUserId`, `location`, `costCenter`, `notes`

Asset type values: `LAPTOP`, `DESKTOP`, `MONITOR`, `KEYBOARD`, `MOUSE`, `PHONE`, `TABLET`, `OTHER`

### GET /api/assets/{id}
**Authenticated.**

### PUT /api/assets/{id}
**ADMIN or TECHNICIAN only.** Full resource replacement.

### PATCH /api/assets/{id}/status
**Authenticated (ADMIN/TECHNICIAN enforced in service).**

Request body:
```json
{ "status": "UNDER_REPAIR", "notes": "Keyboard stopped working" }
```

Valid transitions:
```
AVAILABLE   → ASSIGNED, UNDER_REPAIR, RETIRED
ASSIGNED    → AVAILABLE, UNDER_REPAIR, RECLAIMED, LOST
UNDER_REPAIR→ AVAILABLE, RETIRED
RECLAIMED   → AVAILABLE, RETIRED
LOST        → RECLAIMED, RETIRED
RETIRED     → (none — terminal)
```

Invalid transitions return HTTP 409.

### POST /api/assets/{id}/assign
**Authenticated (ADMIN/TECHNICIAN enforced in service).**

Request body:
```json
{ "userId": 3, "notes": "Assigned laptop for onboarding" }
```

Asset must be in `AVAILABLE` status. Transitions to `ASSIGNED` and writes asset history.

### GET /api/assets/{id}/history
**Authenticated.** Returns list of `AssetHistory` events in reverse chronological order.

---

## Service Requests

### POST /api/requests
**Authenticated.** Any logged-in user can submit a request.

Request body:
```json
{
  "requestTypeId": 1,
  "title": "Need new laptop",
  "description": "My current laptop is 5 years old and cannot run the required tools.",
  "priority": "HIGH",
  "assetId": null,
  "dueDate": "2024-02-01"
}
```

Priority values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

Initial status is determined by the request type's `approvalRequired` flag:
- `true` → `PENDING_APPROVAL`
- `false` → `IN_PROGRESS`

### GET /api/requests/my
**Authenticated.** Returns the calling user's own requests (paginated).

Query params: `page`, `size`, `sort`

### GET /api/requests/queue
**TECHNICIAN, MANAGER, or ADMIN only.** All requests, filterable by status.

Query params: `status` (repeatable), `page`, `size`, `sort`

### GET /api/requests/{id}
**Authenticated.** EMPLOYEE can only view their own requests (403 otherwise).

### PATCH /api/requests/{id}/status
**TECHNICIAN or ADMIN only.**

Request body:
```json
{ "status": "IN_PROGRESS" }
```

### POST /api/requests/{id}/comments
**Authenticated.** EMPLOYEE can only comment on their own requests.

Request body:
```json
{ "body": "The replacement device has been dispatched." }
```

---

## Request Types

### GET /api/request-types
**Authenticated.** Returns all active request types.

Response `data` (array):
```json
[
  { "id": 1, "name": "New Device", "description": "...", "approvalRequired": true },
  { "id": 2, "name": "Repair", "description": "...", "approvalRequired": false }
]
```

---

## Approvals

### POST /api/requests/{id}/approve
**MANAGER only.** Advances request to `IN_PROGRESS` and notifies requester.

Request body:
```json
{ "comment": "Approved — within budget" }
```

### POST /api/requests/{id}/reject
**MANAGER only.** Sets request to `REJECTED` and notifies requester.

Request body:
```json
{ "comment": "Denied — budget freeze in effect" }
```

### GET /api/approvals/pending
**MANAGER only.** Returns paginated list of pending approval steps assigned to the calling manager.

### GET /api/requests/{id}/approvals
**Authenticated.** Returns the approval history for a request.

---

## Attachments

### POST /api/requests/{id}/attachments
**Authenticated (requester or TECHNICIAN).** Multipart file upload.

Form field: `file`

### GET /api/requests/{id}/attachments
**Authenticated.** Returns list of attachment metadata for a request.

### GET /api/attachments/{id}/download
**Authenticated.** Streams the file as a download response.

---

## Notifications

### GET /api/notifications
**Authenticated.** Returns the calling user's notifications, unread first (paginated).

### GET /api/notifications/unread-count
**Authenticated.** Returns `{ "count": 3 }`.

### PATCH /api/notifications/{id}/read
**Authenticated.** Marks one notification as read.

### PATCH /api/notifications/read-all
**Authenticated.** Marks all of the calling user's notifications as read.

---

## Dashboard

All dashboard endpoints are restricted to **ADMIN, TECHNICIAN, or MANAGER**. EMPLOYEEs receive 403.

### GET /api/dashboard/summary
Returns open request counts by status and asset counts by status.

### GET /api/dashboard/request-trends
Returns request counts grouped by month for the last 6 months.

### GET /api/dashboard/assets-by-status
Returns asset counts per status.

### GET /api/dashboard/sla-aging
Returns requests grouped by days past `due_date` (0–3 days, 4–7 days, 7+ days).

---

## Admin — Users

All endpoints below are **ADMIN only**.

### GET /api/admin/users
Paginated list of all users.

### GET /api/admin/users/{id}
Single user by ID.

### POST /api/admin/users
Create a user. Body: `name`, `email`, `employeeCode`, `password`, `role`, `departmentId`, `managerId`.

### PUT /api/admin/users/{id}
Update a user. Body: `name`, `email`, `employeeCode`, `role`, `departmentId`, `managerId`.

### PATCH /api/admin/users/{id}/deactivate
Soft-deactivates a user (sets `active = false`).

---

## Admin — Request Types

All endpoints below are **ADMIN only**.

### GET /api/admin/request-types
List all request types (active and inactive).

### POST /api/admin/request-types
Create a request type. Body: `name`, `description`, `approvalRequired`.

### PUT /api/admin/request-types/{id}
Update a request type.

### PATCH /api/admin/request-types/{id}/toggle-active
Toggles the `active` flag.

---

## Admin — Audit Logs

### GET /api/admin/audit-logs
**ADMIN only.** Paginated audit log.

Query params: `entityType`, `actorId`, `page`, `size`
