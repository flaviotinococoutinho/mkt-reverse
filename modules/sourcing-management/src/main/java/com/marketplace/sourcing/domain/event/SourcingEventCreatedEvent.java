package com.marketplace.sourcing.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a sourcing event is created.
 */
public class SourcingEventCreatedEvent implements DomainEvent {

    private final String aggregateId;
    private final String title;
    private final SourcingEventType eventType;
    private final SourcingEventStatus status;
    private final String buyerOrganizationId;
    private final Instant submissionDeadline;
    private final EventMetadata metadata;

    public SourcingEventCreatedEvent(
        String aggregateId,
        String title,
        SourcingEventType eventType,
        SourcingEventStatus status,
        String buyerOrganizationId,
        Instant submissionDeadline
    ) {
        this.aggregateId = aggregateId;
        this.title = title;
        this.eventType = eventType;
        this.status = status;
        this.buyerOrganizationId = buyerOrganizationId;
        this.submissionDeadline = submissionDeadline;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            aggregateId,
            "SourcingEvent",
            Map.of(
                "title", title,
                "eventType", eventType.name(),
                "status", status.name(),
                "buyerOrganizationId", buyerOrganizationId,
                "submissionDeadline", submissionDeadline != null ? submissionDeadline.toString() : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "SourcingEventCreatedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return metadata.getOccurredAt();
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }

    public Map<String, Object> getPayload() {
        return Map.of(
            "eventId", aggregateId,
            "title", title,
            "eventType", eventType.name(),
            "status", status.name(),
            "buyerOrganizationId", buyerOrganizationId,
            "submissionDeadline", submissionDeadline != null ? submissionDeadline.toString() : null,
            "occurredAt", getOccurredAt().toString()
        );
    }
}
