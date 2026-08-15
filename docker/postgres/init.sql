-- ============================================================
-- Init SQL - Schema e Configurações Iniciais
-- ============================================================

-- Habilitar extensão UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------
-- Users Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usr_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200),
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(20) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_tenant ON usr_users(tenant_id);
CREATE INDEX idx_users_email ON usr_users(email);

-- ----------------------------------------------------------
-- Organizations Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usr_organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    document_type VARCHAR(10),
    document_number VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orgs_tenant ON usr_organizations(tenant_id);

-- ----------------------------------------------------------
-- Sourcing Events Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS src_sourcing_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(50) NOT NULL,
    buyer_id UUID NOT NULL,
    buyer_organization_id UUID NOT NULL,
    buyer_contact_name VARCHAR(200),
    buyer_contact_phone VARCHAR(30),
    buyer_contact_email VARCHAR(255),
    
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    product_name VARCHAR(200),
    product_description TEXT,
    mcc_category_code INTEGER,
    unit_of_measure VARCHAR(20) DEFAULT 'un',
    quantity_required INTEGER DEFAULT 1,
    estimated_budget BIGINT,
    currency VARCHAR(10) DEFAULT 'BRL',
    
    status VARCHAR(30) DEFAULT 'DRAFT',
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_events_tenant_status ON src_sourcing_events(tenant_id, status);
CREATE INDEX idx_events_published ON src_sourcing_events(published_at DESC);
CREATE INDEX idx_events_mcc ON src_sourcing_events(mcc_category_code);

-- ----------------------------------------------------------
-- Supplier Responses Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS src_supplier_responses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sourcing_event_id UUID NOT NULL REFERENCES src_sourcing_events(id),
    supplier_id UUID NOT NULL,
    supplier_organization_id UUID NOT NULL,
    supplier_contact_name VARCHAR(200),
    supplier_contact_phone VARCHAR(30),
    supplier_contact_email VARCHAR(255),
    
    offer_cents INTEGER NOT NULL,
    lead_time_days INTEGER,
    warranty_months INTEGER,
    condition VARCHAR(50),
    shipping_mode VARCHAR(20),
    message TEXT,
    
    status VARCHAR(30) DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- Proposta selada é oferta vinculante com prazo (default 72h)
    valid_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_responses_event ON src_supplier_responses(sourcing_event_id);
CREATE INDEX idx_responses_supplier ON src_supplier_responses(supplier_id);
CREATE UNIQUE INDEX idx_responses_unique ON src_supplier_responses(sourcing_event_id, supplier_id);

-- ----------------------------------------------------------
-- Agreements Table (contrato & liquidação — contexto agreement)
-- O escrow vive no PSP autorizado; aqui só referências e gatilhos.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS agr_agreements (
    id BIGINT PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    -- UNIQUE: dois aceites simultâneos da mesma proposta são barrados pelo
    -- banco, não só pelo check-then-act da aplicação.
    response_id VARCHAR(36) NOT NULL UNIQUE,
    buyer_id VARCHAR(64) NOT NULL,
    supplier_id VARCHAR(64) NOT NULL,
    price_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    snapshot_json TEXT NOT NULL,
    snapshot_hash VARCHAR(64) NOT NULL,
    accepted_at TIMESTAMP NOT NULL,
    funding_deadline TIMESTAMP NOT NULL,
    funded_at TIMESTAMP,
    shipping_deadline TIMESTAMP,
    shipped_at TIMESTAMP,
    -- Prazo da transportadora: SHIPPED nunca é beco sem saída.
    delivery_deadline TIMESTAMP,
    tracking_code VARCHAR(100),
    delivered_at TIMESTAMP,
    inspection_deadline TIMESTAMP,
    closed_at TIMESTAMP,
    escrow_reference VARCHAR(100),
    dispute_reason TEXT,
    resolution_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_agr_event ON agr_agreements(event_id);
CREATE INDEX IF NOT EXISTS idx_agr_status ON agr_agreements(status);
CREATE INDEX IF NOT EXISTS idx_agr_buyer ON agr_agreements(buyer_id);
CREATE INDEX IF NOT EXISTS idx_agr_supplier ON agr_agreements(supplier_id);

-- ----------------------------------------------------------
-- Transactional Outbox (trilha probatória — shared-infrastructure)
-- Escrito na MESMA transação da mudança de negócio; o OutboxRelay
-- publica no RabbitMQ e marca processed.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS shr_outbox_events (
    id VARCHAR(255) PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_outbox_processed ON shr_outbox_events(processed);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON shr_outbox_events(created_at);

-- ----------------------------------------------------------
-- ShedLock (locks distribuídos: OutboxRelay + AgreementLifecycleScheduler)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);

-- ----------------------------------------------------------
-- Notifications (canal in-app do MVP — contexto notification)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS not_notifications (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    template_code VARCHAR(100),
    notification_type VARCHAR(100),
    primary_channel VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subject VARCHAR(200),
    body TEXT,
    payload TEXT,
    queued_at TIMESTAMP NOT NULL,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(300),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_not_tenant ON not_notifications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_not_status ON not_notifications(status);
CREATE INDEX IF NOT EXISTS idx_not_template ON not_notifications(template_code);

CREATE TABLE IF NOT EXISTS not_notification_recipients (
    notification_id UUID NOT NULL REFERENCES not_notifications(id),
    recipient_id VARCHAR(36),
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(30),
    recipient_locale VARCHAR(10),
    recipient_allow_email BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_allow_sms BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_allow_push BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_not_recipient ON not_notification_recipients(recipient_id);

CREATE TABLE IF NOT EXISTS not_notification_channels (
    notification_id UUID NOT NULL REFERENCES not_notifications(id),
    channel VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS not_notification_attempts (
    notification_id UUID NOT NULL REFERENCES not_notifications(id),
    sequence INTEGER,
    attempt_number INTEGER NOT NULL,
    attempt_channel VARCHAR(20) NOT NULL,
    attempt_at TIMESTAMP NOT NULL,
    attempt_status VARCHAR(20) NOT NULL,
    attempt_response VARCHAR(500),
    attempt_error_code VARCHAR(50)
);

-- ----------------------------------------------------------
-- Sys Config Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_config (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tables já existem apenas se não existirem
--CREATE TABLE IF NOT EXISTS sys_config (key VARCHAR(100), value TEXT, description TEXT, created_at TIMESTAMP, updated_at TIMESTAMP);