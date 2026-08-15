package com.marketplace.sourcing.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Emitted on every sealed-proposal transition (SUBMITTED, ACCEPTED, REJECTED).
 *
 * "Your proposal was accepted" is the single most critical notification of the
 * product — a model that dies of latency cannot afford a winning seller who
 * never learns they won. This event feeds the outbox (probative trail) and the
 * in-app notification channel.
 */
public class SupplierResponseStatusChangedEvent implements DomainEvent {

    private final String aggregateId;
    private final String sourcingEventId;
    private final String supplierId;
    private final SupplierResponseStatus newStatus;
    private final SupplierResponseStatus previousStatus;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public SupplierResponseStatusChangedEvent(
        String aggregateId,
        String sourcingEventId,
        String supplierId,
        SupplierResponseStatus newStatus,
        SupplierResponseStatus previousStatus,
        Instant occurredAt
    ) {
        this.aggregateId = aggregateId;
        this.sourcingEventId = sourcingEventId;
        this.supplierId = supplierId;
        this.newStatus = newStatus;
        this.previousStatus = previousStatus;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            aggregateId,
            "SupplierResponse",
            Map.of(
                "sourcingEventId", sourcingEventId != null ? sourcingEventId : "",
                "supplierId", supplierId != null ? supplierId : "",
                "newStatus", newStatus != null ? newStatus.name() : "UNKNOWN",
                "previousStatus", previousStatus != null ? previousStatus.name() : "NONE"
            )
        );
    }

    @Override
    public String getEventType() {
        return "SupplierResponseStatusChangedEvent";
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

    public String getSourcingEventId() {
        return sourcingEventId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public SupplierResponseStatus getNewStatus() {
        return newStatus;
    }

    public SupplierResponseStatus getPreviousStatus() {
        return previousStatus;
    }
}
