-- ============================================================
-- MIGRATION: Add concurrency and data integrity constraints
-- Author: mkt-reverse
-- Date: 2026-04-11
-- ============================================================

-- 1. Add optimistic locking version column
ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

COMMENT ON COLUMN src_sourcing_events.version IS 'Optimistic locking version';

-- 2. Add unique constraint to prevent duplicate proposals per supplier
-- Drop if exists and recreate
ALTER TABLE src_supplier_responses 
ADD CONSTRAINT uk_event_supplier UNIQUE (sourcing_event_id, supplier_id);

-- 3. Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_sourcing_events_tenant_status 
ON src_sourcing_events (tenant_id, status);

CREATE INDEX IF NOT EXISTS idx_sourcing_events_published_at 
ON src_sourcing_events (published_at DESC);

CREATE INDEX IF NOT EXISTS idx_supplier_responses_event_id 
ON src_supplier_responses (sourcing_event_id);

CREATE INDEX IF NOT EXISTS idx_supplier_responses_supplier_id 
ON src_supplier_responses (supplier_id);

-- 4. Add unique constraint for user email per tenant
ALTER TABLE usr_users 
ADD CONSTRAINT uk_user_email_tenant UNIQUE (tenant_id, email);

-- 5. Add constraint for valid MCC codes
-- This is informational - actual validation happens in application layer
COMMENT ON COLUMN src_sourcing_events.mcc_category_code IS 
'Valid MCC codes: 174 (Electronics), 275 (Apparel), 553 (Auto), 521 (Furniture), 571 (Real Estate), 501 (Medical), 581 (Food), 504 (Machinery), 821 (Services)';

-- 6. Add status transition tracking
ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMP;

ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS status_changed_by VARCHAR(255);

-- 7. Add audit timestamps (if not exists)
ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE src_sourcing_events 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE src_supplier_responses 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE src_supplier_responses 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 8. Add indexes for search performance (if not exists)
CREATE INDEX IF NOT EXISTS idx_sourcing_events_title_fts 
ON src_sourcing_events USING gin(to_tsvector('portuguese', coalesce(title, '')));

CREATE INDEX IF NOT EXISTS idx_sourcing_events_description_fts 
ON src_sourcing_events USING gin(to_tsvector('portuguese', coalesce(description, '')));