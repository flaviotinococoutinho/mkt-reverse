package com.marketplace.contract.domain.event;

import com.marketplace.contract.domain.valueobject.ContractStatus;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted whenever contract status changes.
 */
@Getter
public class ContractStatusChangedEvent implements DomainEvent {

    private final String contractId;
    private final ContractStatus newStatus;
    private final Instant occurredAt;
    private final String reason;
    private final EventMetadata metadata;

    public ContractStatusChangedEvent(String contractId, ContractStatus newStatus, Instant occurredAt, String reason) {
        this.contractId = contractId;
        this.newStatus = newStatus;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.reason = reason;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            contractId,
            "Contract",
            Map.of(
                "status", newStatus != null ? newStatus.name() : "UNKNOWN",
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "ContractStatusChangedEvent";
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
        return contractId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
