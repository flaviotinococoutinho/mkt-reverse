package com.marketplace.sourcing.domain.strategy;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;

/**
 * Strategy pattern for different sourcing execution models (RFQ, Reverse Auction, etc.)
 */
public interface SourcingStrategy {
    
    /**
     * Executes the specific logic for this sourcing type.
     * Can include validation, state transition logic, or specific business rules.
     */
    void execute(SourcingEvent event);

    /**
     * Checks if this strategy supports the given sourcing type.
     */
    boolean supports(SourcingEventType type);
}
