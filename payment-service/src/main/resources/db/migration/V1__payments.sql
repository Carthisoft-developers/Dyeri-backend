CREATE TABLE IF NOT EXISTS payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL,
    client_id           UUID NOT NULL,
    amount              NUMERIC(10,3) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'TND',
    provider            VARCHAR(50),
    provider_payment_id VARCHAR(255),
    status              VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS cook_payouts (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cook_id      UUID NOT NULL,
    order_id     UUID NOT NULL,
    gross_amount NUMERIC(10,3),
    platform_fee NUMERIC(10,3),
    net_amount   NUMERIC(10,3),
    paid_at      TIMESTAMPTZ
);
