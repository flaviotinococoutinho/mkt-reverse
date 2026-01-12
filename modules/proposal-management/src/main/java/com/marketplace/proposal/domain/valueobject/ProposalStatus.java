package com.marketplace.proposal.domain.valueobject;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Rich ENUM representing proposal status with behavior.
 * 
 * Implements Strategy Pattern:
 * - Each status knows its valid transitions
 * - Encapsulates status-specific behavior
 * - Validates state transitions
 * 
 * Follows Object Calisthenics:
 * - No ELSE statements
 * - Tell, Don't Ask
 * - Behavior encapsulated in ENUM
 */
public enum ProposalStatus {
    
    DRAFT {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return newStatus == SUBMITTED || newStatus == CANCELLED;
        }
        
        @Override
        public boolean canBeEdited() {
            return true;
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
    },
    
    SUBMITTED {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return newStatus == UNDER_REVIEW || 
                   newStatus == REJECTED || 
                   newStatus == WITHDRAWN;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
    },
    
    UNDER_REVIEW {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return newStatus == ACCEPTED || 
                   newStatus == REJECTED || 
                   newStatus == NEGOTIATING;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
    },
    
    NEGOTIATING {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return newStatus == ACCEPTED || 
                   newStatus == REJECTED || 
                   newStatus == UNDER_REVIEW;
        }
        
        @Override
        public boolean canBeEdited() {
            return true;
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
    },
    
    ACCEPTED {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return newStatus == COMPLETED || newStatus == CANCELLED;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return true;
        }
        
        @Override
        public boolean isFinal() {
            return false;
        }
    },
    
    REJECTED {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
        @Override
        public boolean isFinal() {
            return true;
        }
    },
    
    WITHDRAWN {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
        @Override
        public boolean isFinal() {
            return true;
        }
    },
    
    CANCELLED {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
        @Override
        public boolean isFinal() {
            return true;
        }
    },
    
    COMPLETED {
        @Override
        public boolean canTransitionTo(ProposalStatus newStatus) {
            return false;
        }
        
        @Override
        public boolean canBeEdited() {
            return false;
        }
        
        @Override
        public boolean isActive() {
            return false;
        }
        
        @Override
        public boolean isFinal() {
            return true;
        }
    };
    
    /**
     * Checks if transition to new status is valid.
     * 
     * @param newStatus target status
     * @return true if transition is valid
     */
    public abstract boolean canTransitionTo(ProposalStatus newStatus);
    
    /**
     * Checks if proposal can be edited in this status.
     * 
     * @return true if editable
     */
    public abstract boolean canBeEdited();
    
    /**
     * Checks if status represents an active proposal.
     * 
     * @return true if active
     */
    public abstract boolean isActive();
    
    /**
     * Checks if status is final (no more transitions possible).
     * 
     * @return true if final
     */
    public boolean isFinal() {
        return false;
    }
    
    /**
     * Validates status transition.
     * 
     * @param newStatus target status
     * @throws IllegalStateException if transition is invalid
     */
    public void validateTransition(ProposalStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", this, newStatus)
            );
        }
    }
    
    /**
     * Returns all active statuses.
     * 
     * @return set of active statuses
     */
    public static Set<ProposalStatus> activeStatuses() {
        return EnumSet.of(SUBMITTED, UNDER_REVIEW, NEGOTIATING, ACCEPTED);
    }
    
    /**
     * Returns all final statuses.
     * 
     * @return set of final statuses
     */
    public static Set<ProposalStatus> finalStatuses() {
        return EnumSet.of(REJECTED, WITHDRAWN, CANCELLED, COMPLETED);
    }
}
