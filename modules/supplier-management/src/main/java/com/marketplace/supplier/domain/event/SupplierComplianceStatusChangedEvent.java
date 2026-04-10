package com.marketplace.supplier.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.supplier.domain.valueobject.ComplianceStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when supplier compliance status changes.
 */
@Getter
public class SupplierComplianceStatusChangedEvent implements DomainEvent {

    private final String supplierId;
    private final ComplianceStatus newStatus;
    private final Instant occurredAt;
    private final String reason;
    private final EventMetadata metadata;

    public SupplierComplianceStatusChangedEvent(String supplierId, ComplianceStatus newStatus, Instant occurredAt) {
        this(supplierId, newStatus, occurredAt, null);
    }

    public SupplierComplianceStatusChangedEvent(String supplierId, ComplianceStatus newStatus, Instant occurredAt, String reason) {
        this.supplierId = supplierId;
        this.newStatus = newStatus;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.reason = reason;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            supplierId,
            "Supplier",
            Map.of(
                "newStatus", newStatus != null ? newStatus.name() : "UNKNOWN",
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "SupplierComplianceStatusChangedEvent";
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
        return supplierId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
