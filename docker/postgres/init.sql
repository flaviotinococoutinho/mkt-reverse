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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_responses_event ON src_supplier_responses(sourcing_event_id);
CREATE INDEX idx_responses_supplier ON src_supplier_responses(supplier_id);
CREATE UNIQUE INDEX idx_responses_unique ON src_supplier_responses(sourcing_event_id, supplier_id);

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