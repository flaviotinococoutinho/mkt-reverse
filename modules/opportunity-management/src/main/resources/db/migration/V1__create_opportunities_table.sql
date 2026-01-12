-- Migration: Create opportunities table
-- Author: System
-- Date: 2024-01-15
-- Description: Creates the main opportunities table with all necessary columns and indexes

-- Create opportunities table
CREATE TABLE IF NOT EXISTS opportunities (
    -- Primary key using Snowflake ID
    id BIGINT PRIMARY KEY,
    
    -- Foreign keys
    consumer_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- Basic information
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    
    -- Budget information
    budget_amount DECIMAL(19, 4) NOT NULL,
    budget_currency CHAR(3) NOT NULL,
    
    -- Deadline
    deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Status (ENUM stored as VARCHAR)
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    
    -- Attachments (JSON array)
    attachments JSONB DEFAULT '[]'::jsonb,
    
    -- Specifications (JSON object)
    specifications JSONB DEFAULT '{}'::jsonb,
    
    -- Template key for Service-Driven UI
    template_key VARCHAR(100),
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_budget_positive CHECK (budget_amount > 0),
    CONSTRAINT check_deadline_future CHECK (deadline > created_at),
    CONSTRAINT check_status_valid CHECK (status IN (
        'DRAFT', 'PUBLISHED', 'IN_PROGRESS', 'COMPLETED', 
        'CANCELLED', 'EXPIRED', 'CLOSED'
    ))
);

-- Create indexes for performance

-- Index for consumer queries
CREATE INDEX idx_opportunities_consumer_id 
ON opportunities(consumer_id);

-- Index for tenant queries (multi-tenancy)
CREATE INDEX idx_opportunities_tenant_id 
ON opportunities(tenant_id);

-- Index for category filtering
CREATE INDEX idx_opportunities_category 
ON opportunities(category);

-- Index for status filtering
CREATE INDEX idx_opportunities_status 
ON opportunities(status);

-- Index for deadline queries
CREATE INDEX idx_opportunities_deadline 
ON opportunities(deadline);

-- Composite index for published opportunities by category
CREATE INDEX idx_opportunities_status_category 
ON opportunities(status, category) 
WHERE status = 'PUBLISHED';

-- Composite index for tenant + status queries
CREATE INDEX idx_opportunities_tenant_status 
ON opportunities(tenant_id, status);

-- GIN index for JSONB specifications search
CREATE INDEX idx_opportunities_specifications 
ON opportunities USING GIN (specifications);

-- GIN index for JSONB attachments search
CREATE INDEX idx_opportunities_attachments 
ON opportunities USING GIN (attachments);

-- Create function for automatic updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic updated_at
CREATE TRIGGER trigger_opportunities_updated_at
    BEFORE UPDATE ON opportunities
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE opportunities IS 'Main table for storing consumer purchase opportunities in the C2B marketplace';
COMMENT ON COLUMN opportunities.id IS 'Unique identifier using Snowflake ID algorithm';
COMMENT ON COLUMN opportunities.consumer_id IS 'Reference to the consumer who created the opportunity';
COMMENT ON COLUMN opportunities.tenant_id IS 'Tenant identifier for multi-tenancy support';
COMMENT ON COLUMN opportunities.title IS 'Short descriptive title of the opportunity';
COMMENT ON COLUMN opportunities.description IS 'Detailed description of what the consumer wants';
COMMENT ON COLUMN opportunities.category IS 'Category of the opportunity (e.g., electronics, fashion, collectibles)';
COMMENT ON COLUMN opportunities.budget_amount IS 'Maximum budget amount the consumer is willing to pay';
COMMENT ON COLUMN opportunities.budget_currency IS 'ISO 4217 currency code (e.g., BRL, USD, EUR)';
COMMENT ON COLUMN opportunities.deadline IS 'Deadline for companies to submit proposals';
COMMENT ON COLUMN opportunities.status IS 'Current status of the opportunity';
COMMENT ON COLUMN opportunities.attachments IS 'JSON array of attachment URLs';
COMMENT ON COLUMN opportunities.specifications IS 'JSON object with custom specifications based on category';
COMMENT ON COLUMN opportunities.template_key IS 'Key for Service-Driven UI template configuration';
COMMENT ON COLUMN opportunities.created_at IS 'Timestamp when the opportunity was created';
COMMENT ON COLUMN opportunities.updated_at IS 'Timestamp when the opportunity was last updated';
