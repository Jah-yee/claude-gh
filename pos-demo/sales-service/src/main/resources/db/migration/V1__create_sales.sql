CREATE TABLE sales.sale (
    id          UUID        PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL,
    tender_type VARCHAR(10) NOT NULL,
    total_cents BIGINT      NOT NULL
);
CREATE INDEX sale_created_at_idx ON sales.sale (created_at);

-- Name and price are snapshotted at checkout so receipts never change when the catalog does.
CREATE TABLE sales.sale_line (
    sale_id          UUID        NOT NULL REFERENCES sales.sale (id),
    line_no          INTEGER     NOT NULL,
    sku              VARCHAR(32) NOT NULL,
    name             VARCHAR(100) NOT NULL,
    unit_price_cents BIGINT      NOT NULL,
    quantity         INTEGER     NOT NULL,
    PRIMARY KEY (sale_id, line_no)
);

-- event_id makes the LowStock consumer idempotent.
CREATE TABLE sales.alert (
    id         BIGSERIAL   PRIMARY KEY,
    event_id   UUID        NOT NULL UNIQUE,
    sku        VARCHAR(32) NOT NULL,
    remaining  INTEGER     NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
