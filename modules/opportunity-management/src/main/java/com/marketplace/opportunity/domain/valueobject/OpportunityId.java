package com.marketplace.opportunity.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing an Opportunity identifier.
 * Uses Snowflake ID for distributed unique identification.
 * 
 * Immutable and follows Object Calisthenics principles.
 */
public final class OpportunityId {
    
    private final Long value;
    
    private OpportunityId(Long value) {
        this.value = validateValue(value);
    }
    
    public static OpportunityId of(Long value) {
        return new OpportunityId(value);
    }
    
    public Long value() {
        return value;
    }
    
    private Long validateValue(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("Opportunity ID cannot be null");
        }
        
        if (isNegative(value)) {
            throw new IllegalArgumentException("Opportunity ID must be positive");
        }
        
        return value;
    }
    
    private boolean isNegative(Long value) {
        return value <= 0;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotOpportunityId(other)) {
            return false;
        }
        
        OpportunityId that = (OpportunityId) other;
        return Objects.equals(value, that.value);
    }
    
    private boolean isNotOpportunityId(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
