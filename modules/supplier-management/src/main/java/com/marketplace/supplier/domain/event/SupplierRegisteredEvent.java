package com.marketplace.supplier.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event published when a supplier completes registration.
 */
@Getter
public class SupplierRegisteredEvent implements DomainEvent {

    private final String supplierId;
    private final String legalName;
    private final String tenantId;
    private final String actorId;
    private final String taxIdentifier;
    private final EventMetadata metadata;

    public SupplierRegisteredEvent(
        String supplierId,
        String legalName,
        String tenantId,
        String actorId,
        String taxIdentifier
    ) {
        this.supplierId = supplierId;
        this.legalName = legalName;
        this.tenantId = tenantId;
        this.actorId = actorId;
        this.taxIdentifier = taxIdentifier;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            supplierId,
            "Supplier",
            Map.of(
                "legalName", legalName,
                "tenantId", tenantId,
                "actorId", actorId,
                "taxIdentifier", taxIdentifier
            )
        );
    }

    @Override
    public String getEventType() {
        return "SupplierRegisteredEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.1";
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
}
