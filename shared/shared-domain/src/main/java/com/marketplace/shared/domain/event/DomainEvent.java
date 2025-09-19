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
     * Unique identifier for this event instance.
     */
    UUID getEventId();
    
    /**
     * The ID of the aggregate that raised this event.
     */
    String getAggregateId();
    
    /**
     * The type of the aggregate that raised this event.
     */
    String getAggregateType();
    
    /**
     * When this event occurred.
     */
    Instant getOccurredOn();
    
    /**
     * Version of the aggregate when this event was raised.
     */
    Long getAggregateVersion();
    
    /**
     * Optional correlation ID for tracing related events.
     */
    default String getCorrelationId() {
        return null;
    }
    
    /**
     * Optional causation ID for event sourcing.
     */
    default String getCausationId() {
        return null;
    }
    
    /**
     * Event metadata for additional context.
     */
    default EventMetadata getMetadata() {
        return EventMetadata.empty();
    }
}

