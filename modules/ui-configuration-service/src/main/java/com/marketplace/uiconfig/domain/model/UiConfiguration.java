package com.marketplace.uiconfig.domain.model;

import com.marketplace.uiconfig.domain.valueobject.ConfigurationScope;
import com.marketplace.uiconfig.domain.valueobject.ConfigurationType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root for UI Configuration.
 * Manages form templates, field definitions, and UI customizations.
 * Follows DDD principles and Object Calisthenics.
 */
public final class UiConfiguration {
    
    private final Long id;
    private final Long tenantId;
    private final String configurationKey;
    private final ConfigurationType type;
    private final ConfigurationScope scope;
    private final String name;
    private final String description;
    private final List<FieldDefinition> fields;
    private final String templateContent;
    private final int version;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
    
    private UiConfiguration(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.configurationKey = validateConfigurationKey(builder.configurationKey);
        this.type = validateType(builder.type);
        this.scope = validateScope(builder.scope);
        this.name = validateName(builder.name);
        this.description = builder.description;
        this.fields = Collections.unmodifiableList(new ArrayList<>(builder.fields));
        this.templateContent = builder.templateContent;
        this.version = builder.version;
        this.active = builder.active;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new version of this configuration with updated fields.
     * 
     * @param updatedFields new field definitions
     * @return new UiConfiguration with incremented version
     */
    public UiConfiguration createNewVersion(List<FieldDefinition> updatedFields) {
        return builder()
            .from(this)
            .fields(updatedFields)
            .version(version + 1)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Activates this configuration.
     * 
     * @return new UiConfiguration with active status
     */
    public UiConfiguration activate() {
        if (active) {
            return this;
        }
        
        return builder()
            .from(this)
            .active(true)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Deactivates this configuration.
     * 
     * @return new UiConfiguration with inactive status
     */
    public UiConfiguration deactivate() {
        if (!active) {
            return this;
        }
        
        return builder()
            .from(this)
            .active(false)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Checks if this configuration is tenant-specific.
     * 
     * @return true if tenant-specific
     */
    public boolean isTenantSpecific() {
        return tenantId != null;
    }
    
    /**
     * Checks if this configuration is a form template.
     * 
     * @return true if type is FORM
     */
    public boolean isFormTemplate() {
        return type == ConfigurationType.FORM;
    }
    
    /**
     * Checks if this configuration is a notification template.
     * 
     * @return true if type is NOTIFICATION
     */
    public boolean isNotificationTemplate() {
        return type == ConfigurationType.NOTIFICATION;
    }
    
    public Long id() {
        return id;
    }
    
    public Long tenantId() {
        return tenantId;
    }
    
    public String configurationKey() {
        return configurationKey;
    }
    
    public ConfigurationType type() {
        return type;
    }
    
    public ConfigurationScope scope() {
        return scope;
    }
    
    public String name() {
        return name;
    }
    
    public String description() {
        return description;
    }
    
    public List<FieldDefinition> fields() {
        return fields;
    }
    
    public String templateContent() {
        return templateContent;
    }
    
    public int version() {
        return version;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Instant createdAt() {
        return createdAt;
    }
    
    public Instant updatedAt() {
        return updatedAt;
    }
    
    private String validateConfigurationKey(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("Configuration key cannot be blank");
        }
        
        if (containsInvalidCharacters(key)) {
            throw new IllegalArgumentException(
                "Configuration key must contain only lowercase letters, numbers, dots, and hyphens"
            );
        }
        
        return key;
    }
    
    private ConfigurationType validateType(ConfigurationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Configuration type cannot be null");
        }
        return type;
    }
    
    private ConfigurationScope validateScope(ConfigurationScope scope) {
        if (scope == null) {
            throw new IllegalArgumentException("Configuration scope cannot be null");
        }
        return scope;
    }
    
    private String validateName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Configuration name cannot be blank");
        }
        return name;
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private boolean containsInvalidCharacters(String key) {
        return !key.matches("^[a-z0-9.-]+$");
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotUiConfiguration(other)) {
            return false;
        }
        
        UiConfiguration that = (UiConfiguration) other;
        return Objects.equals(id, that.id);
    }
    
    private boolean isNotUiConfiguration(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format(
            "UiConfiguration{id=%d, key=%s, type=%s, version=%d}",
            id, configurationKey, type, version
        );
    }
    
    /**
     * Builder for UiConfiguration.
     */
    public static final class Builder {
        private Long id;
        private Long tenantId;
        private String configurationKey;
        private ConfigurationType type;
        private ConfigurationScope scope;
        private String name;
        private String description;
        private List<FieldDefinition> fields = new ArrayList<>();
        private String templateContent;
        private int version = 1;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;
        
        private Builder() {
        }
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder configurationKey(String configurationKey) {
            this.configurationKey = configurationKey;
            return this;
        }
        
        public Builder type(ConfigurationType type) {
            this.type = type;
            return this;
        }
        
        public Builder scope(ConfigurationScope scope) {
            this.scope = scope;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder addField(FieldDefinition field) {
            this.fields.add(field);
            return this;
        }
        
        public Builder fields(List<FieldDefinition> fields) {
            this.fields = new ArrayList<>(fields);
            return this;
        }
        
        public Builder templateContent(String templateContent) {
            this.templateContent = templateContent;
            return this;
        }
        
        public Builder version(int version) {
            this.version = version;
            return this;
        }
        
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder from(UiConfiguration config) {
            this.id = config.id;
            this.tenantId = config.tenantId;
            this.configurationKey = config.configurationKey;
            this.type = config.type;
            this.scope = config.scope;
            this.name = config.name;
            this.description = config.description;
            this.fields = new ArrayList<>(config.fields);
            this.templateContent = config.templateContent;
            this.version = config.version;
            this.active = config.active;
            this.createdAt = config.createdAt;
            this.updatedAt = config.updatedAt;
            return this;
        }
        
        public UiConfiguration build() {
            return new UiConfiguration(this);
        }
    }
}
