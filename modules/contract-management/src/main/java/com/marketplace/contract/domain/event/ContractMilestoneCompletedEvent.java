package com.marketplace.contract.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a contract milestone is completed.
 */
@Getter
public class ContractMilestoneCompletedEvent implements DomainEvent {

    private final String contractId;
    private final String milestoneName;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public ContractMilestoneCompletedEvent(String contractId, String milestoneName, Instant occurredAt) {
        this.contractId = contractId;
        this.milestoneName = milestoneName;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            contractId,
            "Contract",
            Map.of(
                "milestoneName", milestoneName
            )
        );
    }

    @Override
    public String getEventType() {
        return "ContractMilestoneCompletedEvent";
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
