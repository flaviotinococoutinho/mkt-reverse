package com.marketplace.uiconfig.domain.model;

import com.marketplace.uiconfig.domain.valueobject.NotificationChannel;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate Root for Notification Templates.
 * Stores FreeMarker templates for transactional and async notifications.
 * Templates are stored in database for dynamic configuration.
 * 
 * Follows DDD principles and Object Calisthenics.
 */
public final class NotificationTemplate {
    
    private final Long id;
    private final Long tenantId;
    private final String templateKey;
    private final NotificationChannel channel;
    private final Locale locale;
    private final String subjectTemplate;
    private final String bodyTemplate;
    private final boolean transactional;
    private final int version;
    private final boolean active;
    private final Map<String, Object> metadata;
    private final Instant createdAt;
    private final Instant updatedAt;
    
    private NotificationTemplate(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.templateKey = validateTemplateKey(builder.templateKey);
        this.channel = validateChannel(builder.channel);
        this.locale = validateLocale(builder.locale);
        this.subjectTemplate = validateSubjectTemplate(builder.subjectTemplate, builder.channel);
        this.bodyTemplate = validateBodyTemplate(builder.bodyTemplate);
        this.transactional = builder.transactional;
        this.version = builder.version;
        this.active = builder.active;
        this.metadata = builder.metadata != null ? Map.copyOf(builder.metadata) : Map.of();
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new version of this template with updated content.
     * 
     * @param newSubjectTemplate updated subject template
     * @param newBodyTemplate updated body template
     * @return new NotificationTemplate with incremented version
     */
    public NotificationTemplate createNewVersion(String newSubjectTemplate, String newBodyTemplate) {
        return builder()
            .from(this)
            .subjectTemplate(newSubjectTemplate)
            .bodyTemplate(newBodyTemplate)
            .version(version + 1)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Activates this template.
     * 
     * @return new NotificationTemplate with active status
     */
    public NotificationTemplate activate() {
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
     * Deactivates this template.
     * 
     * @return new NotificationTemplate with inactive status
     */
    public NotificationTemplate deactivate() {
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
     * Checks if this template is tenant-specific.
     * 
     * @return true if tenant-specific
     */
    public boolean isTenantSpecific() {
        return tenantId != null;
    }
    
    /**
     * Checks if this template requires subject (EMAIL channel).
     * 
     * @return true if subject is required
     */
    public boolean requiresSubject() {
        return channel == NotificationChannel.EMAIL;
    }
    
    /**
     * Gets the unique identifier for this template (key + channel + locale).
     * 
     * @return unique identifier
     */
    public String uniqueIdentifier() {
        return String.format("%s:%s:%s", templateKey, channel, locale);
    }
    
    public Long id() {
        return id;
    }
    
    public Long tenantId() {
        return tenantId;
    }
    
    public String templateKey() {
        return templateKey;
    }
    
    public NotificationChannel channel() {
        return channel;
    }
    
    public Locale locale() {
        return locale;
    }
    
    public String subjectTemplate() {
        return subjectTemplate;
    }
    
    public String bodyTemplate() {
        return bodyTemplate;
    }
    
    public boolean isTransactional() {
        return transactional;
    }
    
    public int version() {
        return version;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Map<String, Object> metadata() {
        return metadata;
    }
    
    public Instant createdAt() {
        return createdAt;
    }
    
    public Instant updatedAt() {
        return updatedAt;
    }
    
    private String validateTemplateKey(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("Template key cannot be blank");
        }
        
        if (containsInvalidCharacters(key)) {
            throw new IllegalArgumentException(
                "Template key must contain only lowercase letters, numbers, dots, and hyphens"
            );
        }
        
        return key;
    }
    
    private NotificationChannel validateChannel(NotificationChannel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Notification channel cannot be null");
        }
        return channel;
    }
    
    private Locale validateLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }
        return locale;
    }
    
    private String validateSubjectTemplate(String subject, NotificationChannel channel) {
        if (channel == NotificationChannel.EMAIL && isBlank(subject)) {
            throw new IllegalArgumentException("Subject template is required for EMAIL channel");
        }
        return subject;
    }
    
    private String validateBodyTemplate(String body) {
        if (isBlank(body)) {
            throw new IllegalArgumentException("Body template cannot be blank");
        }
        return body;
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
        
        if (isNotNotificationTemplate(other)) {
            return false;
        }
        
        NotificationTemplate that = (NotificationTemplate) other;
        return Objects.equals(id, that.id);
    }
    
    private boolean isNotNotificationTemplate(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format(
            "NotificationTemplate{id=%d, key=%s, channel=%s, locale=%s, version=%d}",
            id, templateKey, channel, locale, version
        );
    }
    
    /**
     * Builder for NotificationTemplate.
     */
    public static final class Builder {
        private Long id;
        private Long tenantId;
        private String templateKey;
        private NotificationChannel channel;
        private Locale locale = new Locale("pt", "BR");
        private String subjectTemplate;
        private String bodyTemplate;
        private boolean transactional = false;
        private int version = 1;
        private boolean active = true;
        private Map<String, Object> metadata;
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
        
        public Builder templateKey(String templateKey) {
            this.templateKey = templateKey;
            return this;
        }
        
        public Builder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }
        
        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }
        
        public Builder subjectTemplate(String subjectTemplate) {
            this.subjectTemplate = subjectTemplate;
            return this;
        }
        
        public Builder bodyTemplate(String bodyTemplate) {
            this.bodyTemplate = bodyTemplate;
            return this;
        }
        
        public Builder transactional(boolean transactional) {
            this.transactional = transactional;
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
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
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
        
        public Builder from(NotificationTemplate template) {
            this.id = template.id;
            this.tenantId = template.tenantId;
            this.templateKey = template.templateKey;
            this.channel = template.channel;
            this.locale = template.locale;
            this.subjectTemplate = template.subjectTemplate;
            this.bodyTemplate = template.bodyTemplate;
            this.transactional = template.transactional;
            this.version = template.version;
            this.active = template.active;
            this.metadata = template.metadata;
            this.createdAt = template.createdAt;
            this.updatedAt = template.updatedAt;
            return this;
        }
        
        public NotificationTemplate build() {
            return new NotificationTemplate(this);
        }
    }
}
