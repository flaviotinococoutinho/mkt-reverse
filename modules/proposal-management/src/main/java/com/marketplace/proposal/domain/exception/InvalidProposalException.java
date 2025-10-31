package com.marketplace.proposal.domain.exception;

import java.util.List;

/**
 * Domain exception for invalid proposal.
 * 
 * Thrown when proposal validation fails.
 */
public class InvalidProposalException extends RuntimeException {
    
    private final List<String> errors;
    
    public InvalidProposalException(List<String> errors) {
        super("Proposal validation failed: " + String.join(", ", errors));
        this.errors = List.copyOf(errors);
    }
    
    public InvalidProposalException(String message) {
        super(message);
        this.errors = List.of(message);
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
