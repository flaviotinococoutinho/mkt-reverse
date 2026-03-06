package com.marketplace.contract.infrastructure.messaging;

import com.marketplace.contract.application.ContractApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Removed cross-module synchronous coupling.
// MVP: Contract creation is handled by listening to events with full payload or via API explicitly,
// not by querying sourcing-management DB objects directly from here, which breaks modularity without a client.
@Component
public class ContractSourcingListener {

    private static final Logger log = LoggerFactory.getLogger(ContractSourcingListener.class);

    private final ContractApplicationService contractService;

    public ContractSourcingListener(ContractApplicationService contractService) {
        this.contractService = contractService;
    }

    // TODO: Implement async event consumption with full payloads instead of fetching aggregate
}
