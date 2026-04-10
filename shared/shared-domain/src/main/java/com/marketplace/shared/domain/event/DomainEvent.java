package com.marketplace.shared.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the system.
 * Domain events represent something important that happened in the domain.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public interface DomainEvent {

    /**
     * Gets the unique identifier for this event instance.
     */
    default UUID getEventId() {
        EventMetadata metadata = getMetadata();
        return metadata != null && metadata.getEventId() != null ? metadata.getEventId() : UUID.randomUUID();
    }

    /**
     * Gets the event type identifier.
     */
    String getEventType();

    /**
     * Gets the version of the event definition.
     */
    String getEventVersion();

    /**
     * Gets the timestamp when the event occurred.
     */
    Instant getOccurredAt();

    /**
     * Gets the identifier of the aggregate that raised this event.
     */
    String getAggregateId();

    /**
     * Gets the aggregate type associated with this event.
     */
    default String getAggregateType() {
        EventMetadata metadata = getMetadata();
        return metadata != null ? metadata.getAggregateType() : null;
    }

    /**
     * Gets the aggregate version at the time of the event.
     */
    default Long getAggregateVersion() {
        EventMetadata metadata = getMetadata();
        return metadata != null ? metadata.getAggregateVersion() : null;
    }

    /**
     * Gets the correlation identifier for tracing.
     */
    default String getCorrelationId() {
        EventMetadata metadata = getMetadata();
        return metadata != null ? metadata.getCorrelationId() : null;
    }

    /**
     * Gets the causation identifier for event sourcing.
     */
    default String getCausationId() {
        EventMetadata metadata = getMetadata();
        return metadata != null ? metadata.getCausationId() : null;
    }

    /**
     * Gets event metadata with additional context information.
     */
    EventMetadata getMetadata();
}
