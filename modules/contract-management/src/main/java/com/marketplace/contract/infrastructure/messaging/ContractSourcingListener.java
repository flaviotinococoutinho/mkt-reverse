package com.marketplace.contract.infrastructure.messaging;

import com.marketplace.contract.application.ContractApplicationService;
import com.marketplace.sourcing.application.service.SourcingEventApplicationService;
import com.marketplace.sourcing.domain.event.SourcingEventStatusChangedEvent;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ContractSourcingListener {

    private static final Logger log = LoggerFactory.getLogger(ContractSourcingListener.class);

    private final ContractApplicationService contractService;
    private final SourcingEventApplicationService sourcingService;

    public ContractSourcingListener(ContractApplicationService contractService, SourcingEventApplicationService sourcingService) {
        this.contractService = contractService;
        this.sourcingService = sourcingService;
    }

    @EventListener
    public void onSourcingEventStatusChanged(SourcingEventStatusChangedEvent event) {
        // Only interested when an event is AWARDED
        if (SourcingEventStatus.AWARDED.name().equals(event.getMetadata().getProperty("newStatus"))) {
            log.info("Sourcing Event {} awarded. Creating draft contract...", event.getAggregateId());

            try {
                // Fetch full sourcing event details
                SourcingEvent sourcingEvent = sourcingService.getEvent(event.getAggregateId(), null);

                contractService.createContractFromAward(
                        sourcingEvent.getBuyerContext().getTenantId(),
                        sourcingEvent.getId().asString(),
                        sourcingEvent.getBuyerContext().getOrganizationId(),
                        sourcingEvent.getAwardedSupplierId(),
                        sourcingEvent.getAwardedAmount()
                );

                log.info("Draft contract created successfully for Sourcing Event {}.", event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to create contract for Sourcing Event {}: {}", event.getAggregateId(), e.getMessage());
            }
        }
    }
}
