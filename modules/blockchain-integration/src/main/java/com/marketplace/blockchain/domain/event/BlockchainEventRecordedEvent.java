package com.marketplace.blockchain.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a blockchain ledger event is recorded locally.
 */
@Getter
public class BlockchainEventRecordedEvent implements DomainEvent {

    private final String contractId;
    private final String eventName;
    private final Long blockNumber;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public BlockchainEventRecordedEvent(String contractId, String eventName, Long blockNumber, Instant occurredAt) {
        this.contractId = contractId;
        this.eventName = eventName;
        this.blockNumber = blockNumber;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            contractId,
            "BlockchainContract",
            Map.of(
                "event", eventName,
                "blockNumber", blockNumber
            )
        );
    }

    @Override
    public String getEventType() {
        return "BlockchainEventRecordedEvent";
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
