package com.marketplace.sourcing.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Event emitted when sourcing event details, settings or timeline are updated.
 */
public class SourcingEventUpdatedEvent implements DomainEvent {

    private final String aggregateId;
    private final SourcingEventStatus status;
    private final Instant occurredAt;
    private final UpdateType updateType;
    private final EventMetadata metadata;

    public enum UpdateType {
        DETAILS,
        TIMELINE,
        SETTINGS,
        PARTICIPANTS
    }

    public SourcingEventUpdatedEvent(
        String aggregateId,
        SourcingEventStatus status,
        Instant occurredAt,
        UpdateType updateType
    ) {
        this(aggregateId, status, occurredAt != null ? occurredAt : Instant.now(), updateType, null);
    }

    public static SourcingEventUpdatedEvent timelineExtended(
        String aggregateId,
        SourcingEventStatus status,
        Instant newDeadline
    ) {
        Instant now = Instant.now();
        Map<String, Object> extraProps = Map.of("submissionDeadline", newDeadline != null ? newDeadline.toString() : "");
        EventMetadata metadata = buildMetadata(aggregateId, status, now, UpdateType.TIMELINE, extraProps);
        return new SourcingEventUpdatedEvent(aggregateId, status, now, UpdateType.TIMELINE, metadata);
    }

    private static EventMetadata buildMetadata(
        String aggregateId,
        SourcingEventStatus status,
        Instant occurredAt,
        UpdateType updateType,
        Map<String, Object> extraProps
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put("status", status != null ? status.name() : "UNKNOWN");
        props.put("updateType", updateType != null ? updateType.name() : "UNKNOWN");
        if (extraProps != null) {
            props.putAll(extraProps);
        }
        return EventMetadata.create(
            "SourcingEventUpdatedEvent",
            "1.0",
            occurredAt,
            aggregateId,
            "SourcingEvent",
            props
        );
    }

    private SourcingEventUpdatedEvent(
        String aggregateId,
        SourcingEventStatus status,
        Instant occurredAt,
        UpdateType updateType,
        EventMetadata metadata
    ) {
        this.aggregateId = aggregateId;
        this.status = status;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.updateType = updateType;
        this.metadata = metadata != null ? metadata : buildMetadata(aggregateId, status, this.occurredAt, updateType, null);
    }

    @Override
    public String getEventType() {
        return "SourcingEventUpdatedEvent";
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

    public Map<String, Object> getPayload() {
        return Map.of(
            "eventId", aggregateId,
            "status", status != null ? status.name() : null,
            "updateType", updateType != null ? updateType.name() : null,
            "occurredAt", occurredAt.toString()
        );
    }
}
