CREATE TABLE inventory.product (
    sku                 VARCHAR(32)  PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    price_cents         BIGINT       NOT NULL CHECK (price_cents >= 0),
    stock               INTEGER      NOT NULL,
    low_stock_threshold INTEGER      NOT NULL DEFAULT 5
);

-- Idempotent consumer: one row per event already applied.
CREATE TABLE inventory.processed_events (
    event_id     UUID        PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
