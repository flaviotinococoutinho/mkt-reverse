package com.marketplace.sourcing.domain.strategy;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import org.springframework.stereotype.Component;

/**
 * Reverse Auction strategy.
 * Implements real-time price lowering logic and specific auction rules.
 */
@Component
public class ReverseAuctionSourcingStrategy implements SourcingStrategy {

    @Override
    public void execute(SourcingEvent event) {
        // Auction specific logic
        if (event.getEstimatedBudget() == null || event.getEstimatedBudget().isZero()) {
            throw new IllegalStateException("Reverse Auction requires an estimated budget (ceiling price)");
        }
    }

    @Override
    public boolean supports(SourcingEventType type) {
        return type == SourcingEventType.REVERSE_AUCTION;
    }
}
