package com.marketplace.uiconfig.domain.valueobject;

/**
 * Represents the type of a form field in the UI configuration.
 * Each type defines the rendering behavior and validation rules.
 */
public enum FieldType {
    
    /**
     * Single-line text input.
     */
    TEXT,
    
    /**
     * Multi-line text area.
     */
    TEXTAREA,
    
    /**
     * Numeric input (integer or decimal).
     */
    NUMBER,
    
    /**
     * Email input with validation.
     */
    EMAIL,
    
    /**
     * Phone number input with formatting.
     */
    PHONE,
    
    /**
     * Date picker.
     */
    DATE,
    
    /**
     * Date and time picker.
     */
    DATETIME,
    
    /**
     * Single selection dropdown.
     */
    SELECT,
    
    /**
     * Multiple selection dropdown.
     */
    MULTISELECT,
    
    /**
     * Radio button group.
     */
    RADIO,
    
    /**
     * Checkbox group.
     */
    CHECKBOX,
    
    /**
     * Single checkbox (boolean).
     */
    BOOLEAN,
    
    /**
     * File upload.
     */
    FILE,
    
    /**
     * Image upload with preview.
     */
    IMAGE,
    
    /**
     * Currency input with formatting.
     */
    CURRENCY,
    
    /**
     * URL input with validation.
     */
    URL,
    
    /**
     * Rich text editor.
     */
    RICHTEXT,
    
    /**
     * JSON editor.
     */
    JSON,
    
    /**
     * Hidden field.
     */
    HIDDEN,
    
    /**
     * Read-only display field.
     */
    READONLY;
    
    /**
     * Checks if this field type requires options (SELECT, MULTISELECT, RADIO, CHECKBOX).
     * 
     * @return true if field type requires options
     */
    public boolean requiresOptions() {
        return this == SELECT 
            || this == MULTISELECT 
            || this == RADIO 
            || this == CHECKBOX;
    }
    
    /**
     * Checks if this field type supports file upload.
     * 
     * @return true if field type supports file upload
     */
    public boolean supportsFileUpload() {
        return this == FILE || this == IMAGE;
    }
    
    /**
     * Checks if this field type is a text-based input.
     * 
     * @return true if field type is text-based
     */
    public boolean isTextBased() {
        return this == TEXT 
            || this == TEXTAREA 
            || this == EMAIL 
            || this == URL 
            || this == RICHTEXT;
    }
    
    /**
     * Checks if this field type is numeric.
     * 
     * @return true if field type is numeric
     */
    public boolean isNumeric() {
        return this == NUMBER || this == CURRENCY;
    }
}
