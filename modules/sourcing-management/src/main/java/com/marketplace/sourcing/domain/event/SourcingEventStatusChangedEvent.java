package com.marketplace.sourcing.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted whenever a sourcing event changes status.
 */
public class SourcingEventStatusChangedEvent implements DomainEvent {

    private final String aggregateId;
    private final SourcingEventStatus newStatus;
    private final SourcingEventStatus previousStatus;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public SourcingEventStatusChangedEvent(
        String aggregateId,
        SourcingEventStatus newStatus,
        SourcingEventStatus previousStatus,
        Instant occurredAt
    ) {
        this.aggregateId = aggregateId;
        this.newStatus = newStatus;
        this.previousStatus = previousStatus;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            aggregateId,
            "SourcingEvent",
            Map.of(
                "newStatus", newStatus != null ? newStatus.name() : "UNKNOWN",
                "previousStatus", previousStatus != null ? previousStatus.name() : "NONE",
                "isFinal", newStatus != null && newStatus.isFinal(),
                "isActive", newStatus != null && newStatus.isActive()
            )
        );
    }

    @Override
    public String getEventType() {
        return "SourcingEventStatusChangedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }

    public boolean isFinalTransition() {
        return newStatus != null && newStatus.isFinal();
    }

    public boolean isActivation() {
        return previousStatus != null && !previousStatus.isActive() && newStatus != null && newStatus.isActive();
    }

    public boolean isDeactivation() {
        return previousStatus != null && previousStatus.isActive() && (newStatus == null || !newStatus.isActive());
    }
}
