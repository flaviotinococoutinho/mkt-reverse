package com.marketplace.opportunity.domain.valueobject;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Rich Enum representing opportunity status with embedded business logic.
 * 
 * Implements Strategy Pattern - each status knows its own behavior.
 * Follows Object Calisthenics and Clean Code principles.
 */
public enum OpportunityStatus {
    
    /**
     * Opportunity is being drafted by consumer.
     * Not visible to companies yet.
     */
    DRAFT {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(PUBLISHED, CANCELLED);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Draft";
        }
        
        @Override
        public String description() {
            return "Opportunity is being drafted and not yet published";
        }
    },
    
    /**
     * Opportunity is published and visible to companies.
     * Companies can submit proposals.
     */
    PUBLISHED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(UNDER_REVIEW, CANCELLED, EXPIRED);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Published";
        }
        
        @Override
        public String description() {
            return "Opportunity is published and accepting proposals";
        }
    },
    
    /**
     * Opportunity is under review.
     * Consumer is evaluating received proposals.
     */
    UNDER_REVIEW {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(ACCEPTED, PUBLISHED, CANCELLED);
        }
        
        @Override
        public boolean requiresReview() {
            return true;
        }
        
        @Override
        public String displayName() {
            return "Under Review";
        }
        
        @Override
        public String description() {
            return "Consumer is reviewing received proposals";
        }
    },
    
    /**
     * A proposal has been accepted.
     * Transaction is in progress.
     */
    ACCEPTED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.of(CLOSED, CANCELLED);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Accepted";
        }
        
        @Override
        public String description() {
            return "A proposal has been accepted and transaction is in progress";
        }
    },
    
    /**
     * Opportunity is closed successfully.
     * Transaction completed.
     */
    CLOSED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.noneOf(OpportunityStatus.class);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Closed";
        }
        
        @Override
        public String description() {
            return "Opportunity closed successfully with completed transaction";
        }
    },
    
    /**
     * Opportunity was cancelled by consumer.
     * No proposals were accepted.
     */
    CANCELLED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.noneOf(OpportunityStatus.class);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Cancelled";
        }
        
        @Override
        public String description() {
            return "Opportunity was cancelled by consumer";
        }
    },
    
    /**
     * Opportunity expired without acceptance.
     * Deadline passed without proposal selection.
     */
    EXPIRED {
        @Override
        public Set<OpportunityStatus> allowedTransitions() {
            return EnumSet.noneOf(OpportunityStatus.class);
        }
        
        @Override
        public boolean requiresReview() {
            return false;
        }
        
        @Override
        public String displayName() {
            return "Expired";
        }
        
        @Override
        public String description() {
            return "Opportunity expired after deadline without proposal acceptance";
        }
    };
    
    /**
     * Gets allowed status transitions from current status.
     * Template Method Pattern - each status defines its transitions.
     * 
     * @return set of allowed target statuses
     */
    public abstract Set<OpportunityStatus> allowedTransitions();
    
    /**
     * Checks if this status requires manual review.
     * 
     * @return true if requires review
     */
    public abstract boolean requiresReview();
    
    /**
     * Gets human-readable display name.
     * 
     * @return display name
     */
    public abstract String displayName();
    
    /**
     * Gets detailed description of this status.
     * 
     * @return description
     */
    public abstract String description();
    
    /**
     * Checks if opportunity accepts new proposals in this status.
     * 
     * @return true if proposals can be submitted
     */
    public boolean acceptsProposals() {
        return this == PUBLISHED;
    }
    
    /**
     * Checks if opportunity is in terminal state.
     * Terminal states cannot transition to other states.
     * 
     * @return true if terminal
     */
    public boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }
    
    /**
     * Checks if opportunity is active (not terminal).
     * 
     * @return true if active
     */
    public boolean isActive() {
        return !isTerminal();
    }
    
    /**
     * Checks if opportunity is visible to companies.
     * 
     * @return true if visible
     */
    public boolean isVisibleToCompanies() {
        return this == PUBLISHED || this == UNDER_REVIEW;
    }
    
    /**
     * Checks if transition to target status is valid.
     * 
     * @param targetStatus target status
     * @return true if transition is valid
     */
    public boolean canTransitionTo(OpportunityStatus targetStatus) {
        return allowedTransitions().contains(targetStatus);
    }
    
    /**
     * Validates transition to target status.
     * 
     * @param targetStatus target status
     * @throws IllegalStateException if transition is invalid
     */
    public void validateTransitionTo(OpportunityStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                String.format(
                    "Invalid status transition from %s to %s. Allowed transitions: %s",
                    this,
                    targetStatus,
                    allowedTransitions()
                )
            );
        }
    }
    
    /**
     * Gets predicate for filtering opportunities by this status.
     * Functional interface usage.
     * 
     * @return predicate for status filtering
     */
    public Predicate<OpportunityStatus> statusFilter() {
        return status -> status == this;
    }
    
    /**
     * Gets predicate for filtering active opportunities.
     * 
     * @return predicate for active status filtering
     */
    public static Predicate<OpportunityStatus> activeStatusFilter() {
        return OpportunityStatus::isActive;
    }
    
    /**
     * Gets predicate for filtering terminal opportunities.
     * 
     * @return predicate for terminal status filtering
     */
    public static Predicate<OpportunityStatus> terminalStatusFilter() {
        return OpportunityStatus::isTerminal;
    }
    
    /**
     * Gets predicate for filtering visible opportunities.
     * 
     * @return predicate for visible status filtering
     */
    public static Predicate<OpportunityStatus> visibleStatusFilter() {
        return OpportunityStatus::isVisibleToCompanies;
    }
    
    /**
     * Finds status by display name.
     * 
     * @param displayName display name
     * @return matching status or null
     */
    public static OpportunityStatus fromDisplayName(String displayName) {
        for (OpportunityStatus status : values()) {
            if (status.displayName().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * Gets all active statuses.
     * 
     * @return set of active statuses
     */
    public static Set<OpportunityStatus> activeStatuses() {
        return EnumSet.of(PUBLISHED, UNDER_REVIEW, ACCEPTED);
    }
    
    /**
     * Gets all terminal statuses.
     * 
     * @return set of terminal statuses
     */
    public static Set<OpportunityStatus> terminalStatuses() {
        return EnumSet.of(CLOSED, CANCELLED, EXPIRED);
    }
    
    /**
     * Gets all statuses visible to companies.
     * 
     * @return set of visible statuses
     */
    public static Set<OpportunityStatus> visibleStatuses() {
        return EnumSet.of(PUBLISHED, UNDER_REVIEW);
    }
}
