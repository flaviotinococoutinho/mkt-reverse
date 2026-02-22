package com.marketplace.payment.domain.event;

import com.marketplace.payment.domain.valueobject.IntegrationStatus;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when payment connector status changes.
 */
@Getter
public class PaymentConnectorStatusChangedEvent implements DomainEvent {

    private final String connectorId;
    private final IntegrationStatus status;
    private final Instant occurredAt;
    private final String reason;
    private final EventMetadata metadata;

    public PaymentConnectorStatusChangedEvent(String connectorId, IntegrationStatus status, Instant occurredAt, String reason) {
        this.connectorId = connectorId;
        this.status = status;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.reason = reason;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            connectorId,
            "PaymentConnector",
            Map.of(
                "status", status != null ? status.name() : "UNKNOWN",
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "PaymentConnectorStatusChangedEvent";
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
        return connectorId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
