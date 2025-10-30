package com.marketplace.uiconfig.domain.model;

import com.marketplace.uiconfig.domain.valueobject.FieldType;
import com.marketplace.uiconfig.domain.valueobject.ValidationRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a field definition in a form template.
 * Entity within the UiConfiguration aggregate.
 * Follows Object Calisthenics and Clean Code principles.
 */
public final class FieldDefinition {
    
    private final String name;
    private final String label;
    private final FieldType type;
    private final boolean required;
    private final String placeholder;
    private final String helpText;
    private final Object defaultValue;
    private final List<ValidationRule> validationRules;
    private final List<FieldOption> options;
    private final Map<String, Object> metadata;
    private final int displayOrder;
    
    private FieldDefinition(Builder builder) {
        this.name = validateName(builder.name);
        this.label = validateLabel(builder.label);
        this.type = validateType(builder.type);
        this.required = builder.required;
        this.placeholder = builder.placeholder;
        this.helpText = builder.helpText;
        this.defaultValue = builder.defaultValue;
        this.validationRules = Collections.unmodifiableList(new ArrayList<>(builder.validationRules));
        this.options = Collections.unmodifiableList(new ArrayList<>(builder.options));
        this.metadata = builder.metadata != null ? Map.copyOf(builder.metadata) : Map.of();
        this.displayOrder = builder.displayOrder;
        
        validateOptionsIfRequired();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String name() {
        return name;
    }
    
    public String label() {
        return label;
    }
    
    public FieldType type() {
        return type;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public String placeholder() {
        return placeholder;
    }
    
    public String helpText() {
        return helpText;
    }
    
    public Object defaultValue() {
        return defaultValue;
    }
    
    public List<ValidationRule> validationRules() {
        return validationRules;
    }
    
    public List<FieldOption> options() {
        return options;
    }
    
    public Map<String, Object> metadata() {
        return metadata;
    }
    
    public int displayOrder() {
        return displayOrder;
    }
    
    private String validateName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Field name cannot be blank");
        }
        
        if (containsInvalidCharacters(name)) {
            throw new IllegalArgumentException(
                "Field name must contain only letters, numbers, and underscores"
            );
        }
        
        return name;
    }
    
    private String validateLabel(String label) {
        if (isBlank(label)) {
            throw new IllegalArgumentException("Field label cannot be blank");
        }
        return label;
    }
    
    private FieldType validateType(FieldType type) {
        if (type == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }
        return type;
    }
    
    private void validateOptionsIfRequired() {
        if (type.requiresOptions() && options.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Field type %s requires options", type)
            );
        }
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private boolean containsInvalidCharacters(String name) {
        return !name.matches("^[a-zA-Z0-9_]+$");
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotFieldDefinition(other)) {
            return false;
        }
        
        FieldDefinition that = (FieldDefinition) other;
        return Objects.equals(name, that.name);
    }
    
    private boolean isNotFieldDefinition(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public String toString() {
        return String.format("FieldDefinition{name=%s, label=%s, type=%s}", name, label, type);
    }
    
    /**
     * Builder for FieldDefinition following the Builder pattern.
     */
    public static final class Builder {
        private String name;
        private String label;
        private FieldType type;
        private boolean required = false;
        private String placeholder;
        private String helpText;
        private Object defaultValue;
        private List<ValidationRule> validationRules = new ArrayList<>();
        private List<FieldOption> options = new ArrayList<>();
        private Map<String, Object> metadata;
        private int displayOrder = 0;
        
        private Builder() {
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder label(String label) {
            this.label = label;
            return this;
        }
        
        public Builder type(FieldType type) {
            this.type = type;
            return this;
        }
        
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }
        
        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }
        
        public Builder helpText(String helpText) {
            this.helpText = helpText;
            return this;
        }
        
        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder addValidationRule(ValidationRule rule) {
            this.validationRules.add(rule);
            return this;
        }
        
        public Builder validationRules(List<ValidationRule> rules) {
            this.validationRules = new ArrayList<>(rules);
            return this;
        }
        
        public Builder addOption(FieldOption option) {
            this.options.add(option);
            return this;
        }
        
        public Builder options(List<FieldOption> options) {
            this.options = new ArrayList<>(options);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder displayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }
        
        public FieldDefinition build() {
            return new FieldDefinition(this);
        }
    }
    
    /**
     * Represents an option for select/radio/checkbox fields.
     */
    public static final class FieldOption {
        private final String value;
        private final String label;
        private final boolean disabled;
        
        private FieldOption(String value, String label, boolean disabled) {
            this.value = validateValue(value);
            this.label = validateLabel(label);
            this.disabled = disabled;
        }
        
        public static FieldOption of(String value, String label) {
            return new FieldOption(value, label, false);
        }
        
        public static FieldOption disabled(String value, String label) {
            return new FieldOption(value, label, true);
        }
        
        public String value() {
            return value;
        }
        
        public String label() {
            return label;
        }
        
        public boolean isDisabled() {
            return disabled;
        }
        
        private String validateValue(String value) {
            if (isBlank(value)) {
                throw new IllegalArgumentException("Option value cannot be blank");
            }
            return value;
        }
        
        private String validateLabel(String label) {
            if (isBlank(label)) {
                throw new IllegalArgumentException("Option label cannot be blank");
            }
            return label;
        }
        
        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
        
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            
            if (isNotFieldOption(other)) {
                return false;
            }
            
            FieldOption that = (FieldOption) other;
            return Objects.equals(value, that.value);
        }
        
        private boolean isNotFieldOption(Object other) {
            return other == null || getClass() != other.getClass();
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
        
        @Override
        public String toString() {
            return String.format("FieldOption{value=%s, label=%s}", value, label);
        }
    }
}
