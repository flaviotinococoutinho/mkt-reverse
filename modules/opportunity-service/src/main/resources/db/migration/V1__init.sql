CREATE TABLE opportunities (
    id UUID PRIMARY KEY,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(120) NOT NULL,
    location VARCHAR(120),
    budget_min NUMERIC(15,2),
    budget_max NUMERIC(15,2),
    currency VARCHAR(3),
    deadline DATE,
    status VARCHAR(32) NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE bids (
    id UUID PRIMARY KEY,
    opportunity_id UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    proposer_id VARCHAR(64) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3),
    lead_time_days INT,
    message VARCHAR(2000),
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    decision_at TIMESTAMPTZ
);

CREATE INDEX idx_bids_opportunity ON bids(opportunity_id);

CREATE TABLE negotiation_messages (
    id UUID PRIMARY KEY,
    opportunity_id UUID NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    bid_id UUID REFERENCES bids(id) ON DELETE SET NULL,
    author_id VARCHAR(64) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_messages_opportunity ON negotiation_messages(opportunity_id);
