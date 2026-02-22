package com.marketplace.sourcing.domain.strategy;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import org.springframework.stereotype.Component;

/**
 * Standard RFQ (Request for Quotation) strategy.
 * Focuses on sealed-bid or open-bid collection without dynamic auctioning logic.
 */
@Component
public class RfqSourcingStrategy implements SourcingStrategy {

    @Override
    public void execute(SourcingEvent event) {
        // RFQ specific logic could go here
        // For example, validating that it must have a submission deadline
        if (event.getTimeline().getSubmissionDeadline() == null) {
            throw new IllegalStateException("RFQ requires a submission deadline");
        }
    }

    @Override
    public boolean supports(SourcingEventType type) {
        return type == SourcingEventType.RFQ || type == SourcingEventType.RFP;
    }
}
