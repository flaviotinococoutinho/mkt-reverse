package com.marketplace.proposal.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing Proposal identifier.
 * 
 * Uses Snowflake ID for distributed unique identification.
 * Immutable and defined by its value, not identity.
 */
public record ProposalId(Long value) {
    
    public ProposalId {
        Objects.requireNonNull(value, "Proposal ID cannot be null");
        
        if (value <= 0) {
            throw new IllegalArgumentException("Proposal ID must be positive");
        }
    }
    
    public static ProposalId of(Long value) {
        return new ProposalId(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
