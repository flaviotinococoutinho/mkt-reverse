package com.marketplace.uiconfig.domain.valueobject;

import java.util.Objects;

/**
 * Represents a validation rule for a form field.
 * Immutable value object following Object Calisthenics principles.
 */
public final class ValidationRule {
    
    private final ValidationType type;
    private final String value;
    private final String errorMessage;
    
    private ValidationRule(ValidationType type, String value, String errorMessage) {
        this.type = validateType(type);
        this.value = value;
        this.errorMessage = validateErrorMessage(errorMessage);
    }
    
    public static ValidationRule required(String errorMessage) {
        return new ValidationRule(ValidationType.REQUIRED, null, errorMessage);
    }
    
    public static ValidationRule minLength(int length, String errorMessage) {
        return new ValidationRule(
            ValidationType.MIN_LENGTH, 
            String.valueOf(length), 
            errorMessage
        );
    }
    
    public static ValidationRule maxLength(int length, String errorMessage) {
        return new ValidationRule(
            ValidationType.MAX_LENGTH, 
            String.valueOf(length), 
            errorMessage
        );
    }
    
    public static ValidationRule min(Number value, String errorMessage) {
        return new ValidationRule(
            ValidationType.MIN, 
            value.toString(), 
            errorMessage
        );
    }
    
    public static ValidationRule max(Number value, String errorMessage) {
        return new ValidationRule(
            ValidationType.MAX, 
            value.toString(), 
            errorMessage
        );
    }
    
    public static ValidationRule pattern(String regex, String errorMessage) {
        return new ValidationRule(ValidationType.PATTERN, regex, errorMessage);
    }
    
    public static ValidationRule email(String errorMessage) {
        return new ValidationRule(ValidationType.EMAIL, null, errorMessage);
    }
    
    public static ValidationRule url(String errorMessage) {
        return new ValidationRule(ValidationType.URL, null, errorMessage);
    }
    
    public static ValidationRule custom(String expression, String errorMessage) {
        return new ValidationRule(ValidationType.CUSTOM, expression, errorMessage);
    }
    
    public ValidationType type() {
        return type;
    }
    
    public String value() {
        return value;
    }
    
    public String errorMessage() {
        return errorMessage;
    }
    
    private ValidationType validateType(ValidationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Validation type cannot be null");
        }
        return type;
    }
    
    private String validateErrorMessage(String errorMessage) {
        if (isBlank(errorMessage)) {
            throw new IllegalArgumentException("Error message cannot be blank");
        }
        return errorMessage;
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotValidationRule(other)) {
            return false;
        }
        
        ValidationRule that = (ValidationRule) other;
        return Objects.equals(type, that.type)
            && Objects.equals(value, that.value)
            && Objects.equals(errorMessage, that.errorMessage);
    }
    
    private boolean isNotValidationRule(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, value, errorMessage);
    }
    
    @Override
    public String toString() {
        return String.format(
            "ValidationRule{type=%s, value=%s, errorMessage=%s}",
            type,
            value,
            errorMessage
        );
    }
    
    /**
     * Types of validation rules supported.
     */
    public enum ValidationType {
        REQUIRED,
        MIN_LENGTH,
        MAX_LENGTH,
        MIN,
        MAX,
        PATTERN,
        EMAIL,
        URL,
        CUSTOM
    }
}
