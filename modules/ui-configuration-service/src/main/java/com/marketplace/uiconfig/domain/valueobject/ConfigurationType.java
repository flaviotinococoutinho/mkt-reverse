package com.marketplace.uiconfig.domain.valueobject;

/**
 * Type of UI configuration.
 */
public enum ConfigurationType {
    
    /**
     * Form template with field definitions.
     */
    FORM,
    
    /**
     * Notification template (email, SMS, push, in-app).
     */
    NOTIFICATION,
    
    /**
     * Dashboard layout configuration.
     */
    DASHBOARD,
    
    /**
     * Page layout configuration.
     */
    PAGE_LAYOUT,
    
    /**
     * Widget configuration.
     */
    WIDGET,
    
    /**
     * Theme configuration (colors, fonts, etc).
     */
    THEME
}
