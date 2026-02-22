package com.marketplace.contract.domain.event;

import com.marketplace.contract.domain.valueobject.ContractType;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.shared.valueobject.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a contract is created.
 */
@Getter
public class ContractCreatedEvent implements DomainEvent {

    private final String contractId;
    private final String contractNumber;
    private final ContractType contractType;
    private final Money totalValue;
    private final EventMetadata metadata;

    public ContractCreatedEvent(String contractId, String contractNumber, ContractType contractType, Money totalValue) {
        this.contractId = contractId;
        this.contractNumber = contractNumber;
        this.contractType = contractType;
        this.totalValue = totalValue;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            contractId,
            "Contract",
            Map.of(
                "contractNumber", contractNumber,
                "contractType", contractType.name(),
                "totalValue", totalValue.getAmount(),
                "currency", totalValue.getCurrency().name()
            )
        );
    }

    @Override
    public String getEventType() {
        return "ContractCreatedEvent";
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
