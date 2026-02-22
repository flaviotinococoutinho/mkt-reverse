package com.marketplace.supplier.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.supplier.domain.valueobject.SupplierStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event raised when supplier status changes.
 */
@Getter
public class SupplierStatusChangedEvent implements DomainEvent {

    private final String supplierId;
    private final SupplierStatus previousStatus;
    private final SupplierStatus newStatus;
    private final String reason;
    private final EventMetadata metadata;

    public SupplierStatusChangedEvent(String supplierId, SupplierStatus previousStatus, SupplierStatus newStatus, String reason) {
        this.supplierId = supplierId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            supplierId,
            "Supplier",
            Map.of(
                "previousStatus", previousStatus != null ? previousStatus.name() : "NONE",
                "newStatus", newStatus != null ? newStatus.name() : "UNKNOWN",
                "blocked", newStatus != null && newStatus.isBlocked(),
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "SupplierStatusChangedEvent";
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
        return supplierId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }

    public boolean isActivation() {
        return newStatus != null && newStatus.isActive();
    }

    public boolean isBlocking() {
        return newStatus != null && newStatus.isBlocked();
    }
}
