-- V4: Service request tables

CREATE TABLE service_requests (
    id               BIGSERIAL PRIMARY KEY,
    request_number   VARCHAR(30)  NOT NULL UNIQUE,
    request_type_id  BIGINT       NOT NULL REFERENCES request_types(id),
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NOT NULL,
    requester_id     BIGINT       NOT NULL REFERENCES users(id),
    asset_id         BIGINT       REFERENCES assets(id) ON DELETE SET NULL,
    priority         VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                         CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status           VARCHAR(30)  NOT NULL DEFAULT 'SUBMITTED'
                         CHECK (status IN (
                             'DRAFT','SUBMITTED','PENDING_APPROVAL','APPROVED',
                             'IN_PROGRESS','WAITING_FOR_USER','COMPLETED','CLOSED','REJECTED'
                         )),
    assigned_to      BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    approval_required BOOLEAN     NOT NULL DEFAULT FALSE,
    due_date         DATE,
    closed_at        TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE request_comments (
    id         BIGSERIAL PRIMARY KEY,
    request_id BIGINT      NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES users(id),
    body       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_service_requests_requester ON service_requests(requester_id);
CREATE INDEX idx_service_requests_status    ON service_requests(status);
CREATE INDEX idx_request_comments_request   ON request_comments(request_id);
