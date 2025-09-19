package com.marketplace.sourcing.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * Sourcing Event Type Enumeration
 * 
 * Represents different types of sourcing events in the marketplace.
 * Each type has specific rules, workflows, and business capabilities.
 * 
 * Design principles:
 * - Clear business meaning
 * - Extensible for future event types
 * - Rich behavior with business logic
 * - Workflow-aware configurations
 */
@Getter
public enum SourcingEventType {
    
    /**
     * Request for Quotation (RFQ) - Traditional procurement process
     * Buyers request quotes from multiple suppliers for specific requirements
     */
    RFQ("Request for Quotation", "Traditional procurement with quote requests", 
        true, false, false, 30, 5),
    
    /**
     * Request for Proposal (RFP) - Complex procurement with detailed proposals
     * Buyers request comprehensive proposals including technical and commercial aspects
     */
    RFP("Request for Proposal", "Complex procurement requiring detailed proposals", 
        true, false, true, 60, 10),
    
    /**
     * Reverse Auction - Competitive bidding with price reduction
     * Real-time auction where suppliers compete by lowering prices
     */
    REVERSE_AUCTION("Reverse Auction", "Real-time competitive bidding with price reduction", 
                   false, true, false, 7, 3),
    
    /**
     * Dutch Auction - Price starts high and decreases over time
     * Automated price reduction until a supplier accepts the offer
     */
    DUTCH_AUCTION("Dutch Auction", "Automated price reduction until acceptance", 
                 false, true, false, 3, 1),
    
    /**
     * Sealed Bid - Confidential single-round bidding
     * Suppliers submit sealed bids without knowing competitors' offers
     */
    SEALED_BID("Sealed Bid", "Confidential single-round bidding process", 
              true, false, false, 14, 3),
    
    /**
     * Open Tender - Public procurement with transparent process
     * Open to all qualified suppliers with public evaluation criteria
     */
    OPEN_TENDER("Open Tender", "Public procurement with transparent evaluation", 
               true, false, true, 45, 7),
    
    /**
     * Negotiation - Direct negotiation with selected suppliers
     * One-on-one negotiations with pre-qualified suppliers
     */
    NEGOTIATION("Negotiation", "Direct negotiation with selected suppliers", 
               false, false, true, 21, 2),
    
    /**
     * Framework Agreement - Long-term supply arrangement
     * Establishes terms for multiple future purchases
     */
    FRAMEWORK_AGREEMENT("Framework Agreement", "Long-term supply arrangement setup", 
                       true, false, true, 90, 14);

    private final String displayName;
    private final String description;
    private final boolean allowsMultipleRounds;
    private final boolean isRealTime;
    private final boolean requiresDetailedProposal;
    private final int defaultDurationDays;
    private final int minimumDurationDays;

    SourcingEventType(String displayName, String description, boolean allowsMultipleRounds, 
                     boolean isRealTime, boolean requiresDetailedProposal, 
                     int defaultDurationDays, int minimumDurationDays) {
        this.displayName = displayName;
        this.description = description;
        this.allowsMultipleRounds = allowsMultipleRounds;
        this.isRealTime = isRealTime;
        this.requiresDetailedProposal = requiresDetailedProposal;
        this.defaultDurationDays = defaultDurationDays;
        this.minimumDurationDays = minimumDurationDays;
    }

    /**
     * Checks if this event type supports real-time bidding
     */
    public boolean supportsRealTimeBidding() {
        return isRealTime;
    }

    /**
     * Checks if this event type allows multiple bidding rounds
     */
    public boolean supportsMultipleRounds() {
        return allowsMultipleRounds;
    }

    /**
     * Checks if this event type requires detailed technical proposals
     */
    public boolean requiresTechnicalProposals() {
        return requiresDetailedProposal;
    }

    /**
     * Checks if this event type supports automatic evaluation
     */
    public boolean supportsAutomaticEvaluation() {
        return this == REVERSE_AUCTION || this == DUTCH_AUCTION || this == SEALED_BID;
    }

    /**
     * Checks if this event type requires manual evaluation
     */
    public boolean requiresManualEvaluation() {
        return this == RFP || this == OPEN_TENDER || this == FRAMEWORK_AGREEMENT;
    }

    /**
     * Checks if this event type supports negotiation phase
     */
    public boolean supportsNegotiation() {
        return this == RFP || this == NEGOTIATION || this == FRAMEWORK_AGREEMENT;
    }

    /**
     * Gets the maximum allowed duration in days
     */
    public int getMaxDurationDays() {
        return switch (this) {
            case REVERSE_AUCTION, DUTCH_AUCTION -> 7;
            case RFQ, SEALED_BID -> 60;
            case NEGOTIATION -> 30;
            case RFP, OPEN_TENDER -> 120;
            case FRAMEWORK_AGREEMENT -> 180;
        };
    }

    /**
     * Gets the required supplier qualification level
     */
    public String getRequiredQualificationLevel() {
        return switch (this) {
            case REVERSE_AUCTION, DUTCH_AUCTION -> "BASIC";
            case RFQ, SEALED_BID -> "STANDARD";
            case NEGOTIATION -> "ENHANCED";
            case RFP, OPEN_TENDER, FRAMEWORK_AGREEMENT -> "PREMIUM";
        };
    }

    /**
     * Gets the minimum number of suppliers required
     */
    public int getMinimumSuppliersRequired() {
        return switch (this) {
            case NEGOTIATION -> 1;
            case DUTCH_AUCTION -> 2;
            case RFQ, SEALED_BID, REVERSE_AUCTION -> 3;
            case RFP, OPEN_TENDER, FRAMEWORK_AGREEMENT -> 5;
        };
    }

    /**
     * Gets the evaluation criteria weights
     */
    public EvaluationCriteria getDefaultEvaluationCriteria() {
        return switch (this) {
            case REVERSE_AUCTION, DUTCH_AUCTION -> 
                new EvaluationCriteria(80, 10, 5, 5); // Price-focused
            case RFQ, SEALED_BID -> 
                new EvaluationCriteria(60, 20, 10, 10); // Price-quality balance
            case NEGOTIATION -> 
                new EvaluationCriteria(40, 30, 15, 15); // Balanced approach
            case RFP, OPEN_TENDER, FRAMEWORK_AGREEMENT -> 
                new EvaluationCriteria(30, 40, 20, 10); // Quality-focused
        };
    }

    /**
     * Gets the allowed bid visibility settings
     */
    public Set<BidVisibility> getAllowedBidVisibilities() {
        return switch (this) {
            case SEALED_BID -> Set.of(BidVisibility.HIDDEN);
            case REVERSE_AUCTION -> Set.of(BidVisibility.RANKING_ONLY, BidVisibility.FULL);
            case DUTCH_AUCTION -> Set.of(BidVisibility.CURRENT_PRICE);
            case OPEN_TENDER -> Set.of(BidVisibility.SUMMARY_ONLY, BidVisibility.FULL);
            default -> Set.of(BidVisibility.HIDDEN, BidVisibility.RANKING_ONLY);
        };
    }

    /**
     * Gets the notification frequency for this event type
     */
    public NotificationFrequency getNotificationFrequency() {
        return switch (this) {
            case REVERSE_AUCTION, DUTCH_AUCTION -> NotificationFrequency.REAL_TIME;
            case RFQ, SEALED_BID -> NotificationFrequency.DAILY;
            case NEGOTIATION -> NotificationFrequency.ON_CHANGE;
            case RFP, OPEN_TENDER, FRAMEWORK_AGREEMENT -> NotificationFrequency.WEEKLY;
        };
    }

    /**
     * Checks if this event type supports extensions
     */
    public boolean supportsExtensions() {
        return this != DUTCH_AUCTION && this != REVERSE_AUCTION;
    }

    /**
     * Gets the maximum number of extensions allowed
     */
    public int getMaxExtensions() {
        return switch (this) {
            case REVERSE_AUCTION, DUTCH_AUCTION -> 0;
            case RFQ, SEALED_BID -> 2;
            case NEGOTIATION -> 3;
            case RFP, OPEN_TENDER, FRAMEWORK_AGREEMENT -> 5;
        };
    }

    /**
     * Gets the complexity score (1-10, higher = more complex)
     */
    public int getComplexityScore() {
        return switch (this) {
            case DUTCH_AUCTION -> 3;
            case REVERSE_AUCTION -> 4;
            case RFQ, SEALED_BID -> 5;
            case NEGOTIATION -> 7;
            case RFP -> 8;
            case OPEN_TENDER -> 9;
            case FRAMEWORK_AGREEMENT -> 10;
        };
    }

    /**
     * Creates a SourcingEventType from string (case-insensitive)
     */
    public static SourcingEventType fromString(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }

        try {
            return SourcingEventType.valueOf(eventType.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sourcing event type: " + eventType);
        }
    }

    /**
     * Checks if the given string is a valid event type
     */
    public static boolean isValid(String eventType) {
        try {
            fromString(eventType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Evaluation Criteria for different event types
     */
    public static class EvaluationCriteria {
        @Getter private final int priceWeight;
        @Getter private final int qualityWeight;
        @Getter private final int deliveryWeight;
        @Getter private final int serviceWeight;

        public EvaluationCriteria(int priceWeight, int qualityWeight, int deliveryWeight, int serviceWeight) {
            if (priceWeight + qualityWeight + deliveryWeight + serviceWeight != 100) {
                throw new IllegalArgumentException("Evaluation criteria weights must sum to 100");
            }
            this.priceWeight = priceWeight;
            this.qualityWeight = qualityWeight;
            this.deliveryWeight = deliveryWeight;
            this.serviceWeight = serviceWeight;
        }
    }

    /**
     * Bid Visibility options
     */
    public enum BidVisibility {
        HIDDEN, RANKING_ONLY, SUMMARY_ONLY, CURRENT_PRICE, FULL
    }

    /**
     * Notification Frequency options
     */
    public enum NotificationFrequency {
        REAL_TIME, ON_CHANGE, DAILY, WEEKLY
    }
}

