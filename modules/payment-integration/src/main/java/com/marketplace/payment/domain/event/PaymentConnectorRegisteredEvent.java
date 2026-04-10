package com.marketplace.payment.domain.event;

import com.marketplace.payment.domain.valueobject.PaymentProvider;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a new payment connector is registered.
 */
@Getter
public class PaymentConnectorRegisteredEvent implements DomainEvent {

    private final String connectorId;
    private final PaymentProvider provider;
    private final boolean sandbox;
    private final EventMetadata metadata;

    public PaymentConnectorRegisteredEvent(String connectorId, PaymentProvider provider, boolean sandbox) {
        this.connectorId = connectorId;
        this.provider = provider;
        this.sandbox = sandbox;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            connectorId,
            "PaymentConnector",
            Map.of(
                "provider", provider != null ? provider.name() : "UNKNOWN",
                "sandbox", sandbox
            )
        );
    }

    @Override
    public String getEventType() {
        return "PaymentConnectorRegisteredEvent";
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
        return connectorId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
