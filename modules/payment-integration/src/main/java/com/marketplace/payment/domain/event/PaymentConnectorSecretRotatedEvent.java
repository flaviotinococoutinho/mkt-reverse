package com.marketplace.payment.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when connector credentials are rotated.
 */
@Getter
public class PaymentConnectorSecretRotatedEvent implements DomainEvent {

    private final String connectorId;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public PaymentConnectorSecretRotatedEvent(String connectorId, Instant occurredAt) {
        this.connectorId = connectorId;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            connectorId,
            "PaymentConnector",
            Map.of()
        );
    }

    @Override
    public String getEventType() {
        return "PaymentConnectorSecretRotatedEvent";
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
