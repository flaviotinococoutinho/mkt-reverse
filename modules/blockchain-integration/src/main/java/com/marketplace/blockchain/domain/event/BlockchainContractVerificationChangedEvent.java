package com.marketplace.blockchain.domain.event;

import com.marketplace.blockchain.domain.valueobject.VerificationStatus;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when contract verification status changes.
 */
@Getter
public class BlockchainContractVerificationChangedEvent implements DomainEvent {

    private final String contractId;
    private final VerificationStatus status;
    private final Instant occurredAt;
    private final String reason;
    private final EventMetadata metadata;

    public BlockchainContractVerificationChangedEvent(String contractId, VerificationStatus status, Instant occurredAt, String reason) {
        this.contractId = contractId;
        this.status = status;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.reason = reason;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            contractId,
            "BlockchainContract",
            Map.of(
                "status", status != null ? status.name() : "UNKNOWN",
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "BlockchainContractVerificationChangedEvent";
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
