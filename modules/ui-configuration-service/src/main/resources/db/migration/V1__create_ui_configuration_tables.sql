-- Migration: Create UI configuration tables
-- Author: System
-- Date: 2024-01-15
-- Description: Creates tables for Service-Driven UI and notification templates

-- Create notification templates table
CREATE TABLE IF NOT EXISTS notification_templates (
    -- Primary key using Snowflake ID
    id BIGINT PRIMARY KEY,
    
    -- Multi-tenancy
    tenant_id BIGINT NOT NULL,
    
    -- Template identification
    template_key VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
    
    -- Template content (FreeMarker)
    subject_template TEXT,
    body_template TEXT NOT NULL,
    
    -- Versioning
    version INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    
    -- Metadata (JSON)
    metadata JSONB DEFAULT '{}'::jsonb,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_channel_valid CHECK (channel IN (
        'EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WHATSAPP'
    )),
    CONSTRAINT check_version_positive CHECK (version > 0),
    CONSTRAINT unique_template_per_tenant UNIQUE (tenant_id, template_key, channel, locale, version)
);

-- Create UI configurations table
CREATE TABLE IF NOT EXISTS ui_configurations (
    -- Primary key using Snowflake ID
    id BIGINT PRIMARY KEY,
    
    -- Multi-tenancy
    tenant_id BIGINT NOT NULL,
    
    -- Configuration identification
    configuration_key VARCHAR(100) NOT NULL,
    configuration_type VARCHAR(50) NOT NULL,
    scope VARCHAR(50) NOT NULL,
    
    -- Configuration content (JSON)
    configuration_data JSONB NOT NULL,
    
    -- Versioning
    version INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_type_valid CHECK (configuration_type IN (
        'FORM', 'DASHBOARD', 'WORKFLOW', 'THEME', 'VALIDATION'
    )),
    CONSTRAINT check_scope_valid CHECK (scope IN (
        'GLOBAL', 'TENANT', 'ROLE', 'USER'
    )),
    CONSTRAINT check_version_positive CHECK (version > 0),
    CONSTRAINT unique_config_per_tenant UNIQUE (tenant_id, configuration_key, version)
);

-- Create field definitions table
CREATE TABLE IF NOT EXISTS field_definitions (
    -- Primary key using Snowflake ID
    id BIGINT PRIMARY KEY,
    
    -- Foreign key to UI configuration
    ui_configuration_id BIGINT NOT NULL REFERENCES ui_configurations(id) ON DELETE CASCADE,
    
    -- Field information
    field_key VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    label VARCHAR(200) NOT NULL,
    placeholder VARCHAR(200),
    default_value TEXT,
    
    -- Validation rules (JSON)
    validation_rules JSONB DEFAULT '[]'::jsonb,
    
    -- Field options (for SELECT, RADIO, etc.)
    options JSONB DEFAULT '[]'::jsonb,
    
    -- Display order
    display_order INTEGER NOT NULL DEFAULT 0,
    required BOOLEAN NOT NULL DEFAULT false,
    visible BOOLEAN NOT NULL DEFAULT true,
    
    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_field_type_valid CHECK (field_type IN (
        'TEXT', 'NUMBER', 'EMAIL', 'PHONE', 'DATE', 'DATETIME',
        'SELECT', 'MULTISELECT', 'RADIO', 'CHECKBOX', 'TEXTAREA',
        'FILE', 'IMAGE', 'CURRENCY', 'PERCENTAGE'
    ))
);

-- Create indexes

-- Notification templates indexes
CREATE INDEX idx_notification_templates_tenant 
ON notification_templates(tenant_id);

CREATE INDEX idx_notification_templates_key 
ON notification_templates(template_key);

CREATE INDEX idx_notification_templates_channel 
ON notification_templates(channel);

CREATE INDEX idx_notification_templates_active 
ON notification_templates(active) 
WHERE active = true;

CREATE INDEX idx_notification_templates_tenant_key 
ON notification_templates(tenant_id, template_key, channel, locale) 
WHERE active = true;

-- UI configurations indexes
CREATE INDEX idx_ui_configurations_tenant 
ON ui_configurations(tenant_id);

CREATE INDEX idx_ui_configurations_key 
ON ui_configurations(configuration_key);

CREATE INDEX idx_ui_configurations_type 
ON ui_configurations(configuration_type);

CREATE INDEX idx_ui_configurations_active 
ON ui_configurations(active) 
WHERE active = true;

CREATE INDEX idx_ui_configurations_tenant_key 
ON ui_configurations(tenant_id, configuration_key) 
WHERE active = true;

-- GIN index for configuration data search
CREATE INDEX idx_ui_configurations_data 
ON ui_configurations USING GIN (configuration_data);

-- Field definitions indexes
CREATE INDEX idx_field_definitions_config 
ON field_definitions(ui_configuration_id);

CREATE INDEX idx_field_definitions_order 
ON field_definitions(ui_configuration_id, display_order);

-- Create triggers for automatic updated_at

CREATE TRIGGER trigger_notification_templates_updated_at
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_ui_configurations_updated_at
    BEFORE UPDATE ON ui_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_field_definitions_updated_at
    BEFORE UPDATE ON field_definitions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments

COMMENT ON TABLE notification_templates IS 'Templates for transactional and async notifications using Apache FreeMarker';
COMMENT ON TABLE ui_configurations IS 'Service-Driven UI configurations for dynamic forms and dashboards';
COMMENT ON TABLE field_definitions IS 'Field definitions for dynamic forms';
