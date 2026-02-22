package com.marketplace.shared.domain.event;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Metadata associated with domain events. Implemented without Lombok to
 * simplify compilation across all modules.
 */
public class EventMetadata {

    private final UUID eventId;
    private final String eventType;
    private final String eventVersion;
    private final Instant occurredAt;
    private final String aggregateId;
    private final String aggregateType;
    private final Long aggregateVersion;
    private final String correlationId;
    private final String causationId;
    private final Map<String, Object> properties;
    private final String userId;
    private final String sessionId;
    private final String ipAddress;
    private final String userAgent;
    private final String source;

    public EventMetadata(
        UUID eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String aggregateId,
        String aggregateType,
        Long aggregateVersion,
        String correlationId,
        String causationId,
        Map<String, Object> properties,
        String userId,
        String sessionId,
        String ipAddress,
        String userAgent,
        String source
    ) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID();
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.aggregateVersion = aggregateVersion;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.properties = properties != null ? Collections.unmodifiableMap(properties) : Collections.emptyMap();
        this.userId = userId;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.source = source;
    }

    public static EventMetadata empty() {
        return new EventMetadata(UUID.randomUUID(), null, null, Instant.now(), null, null, null, null, null, Collections.emptyMap(), null, null, null, null, null);
    }

    public static EventMetadata of(Map<String, Object> properties) {
        return new EventMetadata(UUID.randomUUID(), null, null, Instant.now(), null, null, null, null, null, properties, null, null, null, null, null);
    }

    public static EventMetadata create(
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String aggregateId,
        Map<String, Object> properties
    ) {
        return create(eventType, eventVersion, occurredAt, aggregateId, null, properties);
    }

    public static EventMetadata create(
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String aggregateId,
        String aggregateType,
        Map<String, Object> properties
    ) {
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(eventVersion, "eventVersion is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        Objects.requireNonNull(aggregateId, "aggregateId is required");

        return new EventMetadata(
            UUID.randomUUID(),
            eventType,
            eventVersion,
            occurredAt,
            aggregateId,
            aggregateType,
            null,
            null,
            null,
            properties,
            null,
            null,
            null,
            null,
            null
        );
    }

    public EventMetadata withAggregateVersion(Long version) {
        return new EventMetadata(eventId, eventType, eventVersion, occurredAt, aggregateId, aggregateType, version,
            correlationId, causationId, properties, userId, sessionId, ipAddress, userAgent, source);
    }

    public EventMetadata withCorrelation(String correlationId, String causationId) {
        return new EventMetadata(eventId, eventType, eventVersion, occurredAt, aggregateId, aggregateType, aggregateVersion,
            correlationId, causationId, properties, userId, sessionId, ipAddress, userAgent, source);
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public Long getAggregateVersion() {
        return aggregateVersion;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getSource() {
        return source;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getStringProperty(String key) {
        Object value = getProperty(key);
        return value != null ? value.toString() : null;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
}
