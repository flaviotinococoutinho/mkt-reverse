package com.marketplace.erp.domain.event;

import com.marketplace.erp.domain.valueobject.ErpSystem;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when ERP connector is registered.
 */
@Getter
public class ErpConnectorRegisteredEvent implements DomainEvent {

    private final String connectorId;
    private final ErpSystem system;
    private final boolean bidirectional;
    private final EventMetadata metadata;

    public ErpConnectorRegisteredEvent(String connectorId, ErpSystem system, boolean bidirectional) {
        this.connectorId = connectorId;
        this.system = system;
        this.bidirectional = bidirectional;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            connectorId,
            "ErpConnector",
            Map.of(
                "system", system != null ? system.name() : "UNKNOWN",
                "bidirectional", bidirectional
            )
        );
    }

    @Override
    public String getEventType() {
        return "ErpConnectorRegisteredEvent";
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
