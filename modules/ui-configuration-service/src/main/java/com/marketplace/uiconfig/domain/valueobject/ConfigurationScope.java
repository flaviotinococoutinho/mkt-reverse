package com.marketplace.uiconfig.domain.valueobject;

/**
 * Scope of UI configuration application.
 */
public enum ConfigurationScope {
    
    /**
     * Global configuration (applies to all tenants).
     */
    GLOBAL,
    
    /**
     * Tenant-specific configuration.
     */
    TENANT,
    
    /**
     * Role-specific configuration (CONSUMER, COMPANY, ADMIN).
     */
    ROLE,
    
    /**
     * User-specific configuration.
     */
    USER,
    
    /**
     * Category-specific configuration (collectibles, automotive, fashion).
     */
    CATEGORY
}
