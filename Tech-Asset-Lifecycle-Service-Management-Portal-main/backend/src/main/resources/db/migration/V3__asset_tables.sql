-- V3: Asset tables

CREATE TABLE assets (
    id               BIGSERIAL PRIMARY KEY,
    asset_tag        VARCHAR(50)  NOT NULL UNIQUE,
    serial_number    VARCHAR(100),
    asset_type       VARCHAR(50)  NOT NULL,
    vendor           VARCHAR(100),
    model            VARCHAR(100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE'
                         CHECK (status IN ('AVAILABLE','ASSIGNED','UNDER_REPAIR','RECLAIMED','LOST','RETIRED')),
    purchase_date    DATE,
    warranty_expiry  DATE,
    assigned_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    location         VARCHAR(200),
    cost_center      VARCHAR(100),
    notes            TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE asset_history (
    id          BIGSERIAL PRIMARY KEY,
    asset_id    BIGINT      NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    event_type  VARCHAR(50) NOT NULL,
    from_status VARCHAR(20),
    to_status   VARCHAR(20),
    actor_id    BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    notes       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_asset_history_asset_id ON asset_history(asset_id);
CREATE INDEX idx_assets_status          ON assets(status);
CREATE INDEX idx_assets_assigned_user   ON assets(assigned_user_id);

-- Sequence used for generating service request numbers
CREATE SEQUENCE service_request_seq START 1000 INCREMENT 1;
