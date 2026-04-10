package com.marketplace.erp.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted after an ERP synchronization finishes.
 */
@Getter
public class ErpSyncCompletedEvent implements DomainEvent {

    private final String connectorId;
    private final boolean success;
    private final Instant occurredAt;
    private final String errorMessage;
    private final EventMetadata metadata;

    public ErpSyncCompletedEvent(String connectorId, boolean success, Instant occurredAt, String errorMessage) {
        this.connectorId = connectorId;
        this.success = success;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.errorMessage = errorMessage;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            connectorId,
            "ErpConnector",
            Map.of(
                "success", success,
                "error", errorMessage != null ? errorMessage : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "ErpSyncCompletedEvent";
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
