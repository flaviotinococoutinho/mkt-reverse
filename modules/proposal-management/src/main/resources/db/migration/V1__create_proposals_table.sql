-- Migration: Create proposals table
-- Author: System
-- Date: 2024-01-15
-- Description: Creates proposals table for C2B marketplace

-- Create proposals table
CREATE TABLE IF NOT EXISTS proposals (
    -- Primary key using Snowflake ID
    id BIGINT PRIMARY KEY,
    
    -- Foreign keys
    opportunity_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- Price information
    price_amount DECIMAL(19, 4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    
    -- Delivery time information
    delivery_days INTEGER NOT NULL,
    delivery_hours INTEGER NOT NULL,
    estimated_delivery_date TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Proposal details
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    
    -- Attachments and specifications (JSONB for flexibility)
    attachments JSONB DEFAULT '[]'::jsonb,
    specifications JSONB DEFAULT '{}'::jsonb,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP WITH TIME ZONE,
    accepted_at TIMESTAMP WITH TIME ZONE,
    
    -- Constraints
    CONSTRAINT check_price_positive CHECK (price_amount > 0),
    CONSTRAINT check_delivery_days_non_negative CHECK (delivery_days >= 0),
    CONSTRAINT check_delivery_hours_valid CHECK (delivery_hours >= 0 AND delivery_hours < 24),
    CONSTRAINT check_description_not_empty CHECK (LENGTH(description) >= 50),
    CONSTRAINT check_status_valid CHECK (status IN (
        'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'NEGOTIATING',
        'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'CANCELLED', 'COMPLETED'
    ))
);

-- Create indexes

-- Index for finding proposals by opportunity
CREATE INDEX idx_proposals_opportunity 
ON proposals(opportunity_id);

-- Index for finding proposals by company
CREATE INDEX idx_proposals_company 
ON proposals(company_id);

-- Index for finding proposals by tenant
CREATE INDEX idx_proposals_tenant 
ON proposals(tenant_id);

-- Index for finding proposals by status
CREATE INDEX idx_proposals_status 
ON proposals(status);

-- Composite index for opportunity + status queries
CREATE INDEX idx_proposals_opportunity_status 
ON proposals(opportunity_id, status);

-- Composite index for company + status queries
CREATE INDEX idx_proposals_company_status 
ON proposals(company_id, status);

-- Index for finding active proposals
CREATE INDEX idx_proposals_active 
ON proposals(status) 
WHERE status IN ('SUBMITTED', 'UNDER_REVIEW', 'NEGOTIATING', 'ACCEPTED');

-- Index for sorting by creation date
CREATE INDEX idx_proposals_created_at 
ON proposals(created_at DESC);

-- Index for sorting by submission date
CREATE INDEX idx_proposals_submitted_at 
ON proposals(submitted_at DESC) 
WHERE submitted_at IS NOT NULL;

-- GIN index for searching in attachments
CREATE INDEX idx_proposals_attachments 
ON proposals USING GIN (attachments);

-- GIN index for searching in specifications
CREATE INDEX idx_proposals_specifications 
ON proposals USING GIN (specifications);

-- Create function for automatic updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic updated_at
CREATE TRIGGER trigger_proposals_updated_at
    BEFORE UPDATE ON proposals
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation

COMMENT ON TABLE proposals IS 'Proposals submitted by companies for consumer opportunities';

COMMENT ON COLUMN proposals.id IS 'Unique identifier using Snowflake ID algorithm';
COMMENT ON COLUMN proposals.opportunity_id IS 'Reference to the opportunity this proposal is for';
COMMENT ON COLUMN proposals.company_id IS 'Reference to the company submitting the proposal';
COMMENT ON COLUMN proposals.tenant_id IS 'Tenant identifier for multi-tenancy';

COMMENT ON COLUMN proposals.price_amount IS 'Proposed price amount';
COMMENT ON COLUMN proposals.price_currency IS 'Currency code (ISO 4217)';

COMMENT ON COLUMN proposals.delivery_days IS 'Estimated delivery time in days';
COMMENT ON COLUMN proposals.delivery_hours IS 'Additional delivery time in hours (0-23)';
COMMENT ON COLUMN proposals.estimated_delivery_date IS 'Calculated estimated delivery date';

COMMENT ON COLUMN proposals.description IS 'Detailed proposal description (minimum 50 characters)';
COMMENT ON COLUMN proposals.status IS 'Current status of the proposal';

COMMENT ON COLUMN proposals.attachments IS 'Array of attachment URLs (JSONB)';
COMMENT ON COLUMN proposals.specifications IS 'Additional specifications (JSONB)';

COMMENT ON COLUMN proposals.created_at IS 'Timestamp when proposal was created';
COMMENT ON COLUMN proposals.updated_at IS 'Timestamp when proposal was last updated';
COMMENT ON COLUMN proposals.submitted_at IS 'Timestamp when proposal was submitted';
COMMENT ON COLUMN proposals.accepted_at IS 'Timestamp when proposal was accepted';
