-- V7: Immutable audit log table

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    VARCHAR(50)  NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    actor_id     BIGINT       REFERENCES users(id),
    before_json  JSONB,
    after_json   JSONB,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_entity     ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor      ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
