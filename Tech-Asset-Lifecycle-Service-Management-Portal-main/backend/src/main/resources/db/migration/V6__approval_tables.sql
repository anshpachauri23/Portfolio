-- V6: Approval workflow tables

CREATE TABLE approval_steps (
    id              BIGSERIAL PRIMARY KEY,
    request_id      BIGINT       NOT NULL REFERENCES service_requests(id) ON DELETE CASCADE,
    approver_id     BIGINT       NOT NULL REFERENCES users(id),
    decision        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (decision IN ('PENDING', 'APPROVED', 'REJECTED')),
    comment         TEXT,
    decided_at      TIMESTAMPTZ,
    sequence_order  INT          NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_approval_steps_request   ON approval_steps(request_id);
CREATE INDEX idx_approval_steps_approver  ON approval_steps(approver_id);
CREATE INDEX idx_approval_steps_decision  ON approval_steps(decision);
