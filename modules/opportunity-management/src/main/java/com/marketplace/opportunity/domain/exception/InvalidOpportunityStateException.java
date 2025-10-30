package com.marketplace.opportunity.domain.exception;

/**
 * Domain exception thrown when an invalid state transition is attempted on an Opportunity.
 * Part of the domain layer - no framework dependencies.
 */
public class InvalidOpportunityStateException extends RuntimeException {
    
    public InvalidOpportunityStateException(String message) {
        super(message);
    }
    
    public InvalidOpportunityStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
