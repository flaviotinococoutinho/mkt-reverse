package com.marketplace.blockchain.domain.event;

import com.marketplace.blockchain.domain.valueobject.BlockchainNetwork;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a smart contract is registered.
 */
@Getter
public class BlockchainContractRegisteredEvent implements DomainEvent {

    private final String contractId;
    private final BlockchainNetwork network;
    private final String contractAddress;
    private final EventMetadata metadata;

    public BlockchainContractRegisteredEvent(String contractId, BlockchainNetwork network, String contractAddress) {
        this.contractId = contractId;
        this.network = network;
        this.contractAddress = contractAddress;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            contractId,
            "BlockchainContract",
            Map.of(
                "network", network != null ? network.name() : "UNKNOWN",
                "contractAddress", contractAddress
            )
        );
    }

    @Override
    public String getEventType() {
        return "BlockchainContractRegisteredEvent";
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
        return contractId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
