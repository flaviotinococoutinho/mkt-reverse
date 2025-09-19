package com.marketplace.user.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * KYC (Know Your Customer) Level Enumeration
 * 
 * Represents different levels of customer verification required for compliance.
 * Each level has specific document requirements and transaction limits.
 * 
 * Design principles:
 * - Progressive verification levels
 * - Compliance with financial regulations
 * - Clear document requirements
 * - Transaction limit enforcement
 */
@Getter
public enum KycLevel {
    
    /**
     * No KYC - Basic registration only
     * Limited functionality, no financial transactions
     */
    NONE("None", "Basic registration without verification", 
         Set.of(), 0L, 0),
    
    /**
     * Basic KYC - Email and phone verification
     * Basic profile information, limited transactions
     */
    BASIC("Basic", "Email and phone verification with basic profile information",
          Set.of("email_verification", "phone_verification"), 10_000L, 365),
    
    /**
     * Enhanced KYC - Document verification
     * Identity documents required, higher transaction limits
     */
    ENHANCED("Enhanced", "Identity document verification with proof of address",
             Set.of("identity_document", "proof_of_address", "selfie_verification"), 
             100_000L, 730),
    
    /**
     * Full KYC - Complete verification
     * All documents, background checks, highest limits
     */
    FULL("Full", "Complete verification with background checks and enhanced due diligence",
         Set.of("identity_document", "proof_of_address", "proof_of_income", 
                "bank_statement", "selfie_verification", "video_verification"),
         1_000_000L, 1095);

    private final String displayName;
    private final String description;
    private final Set<String> requiredDocuments;
    private final long maxTransactionLimit; // in USD cents
    private final int maxValidityDays; // 0 = never expires

    KycLevel(String displayName, String description, Set<String> requiredDocuments, 
             long maxTransactionLimit, int maxValidityDays) {
        this.displayName = displayName;
        this.description = description;
        this.requiredDocuments = requiredDocuments;
        this.maxTransactionLimit = maxTransactionLimit;
        this.maxValidityDays = maxValidityDays;
    }

    /**
     * Checks if this level allows the specified transaction amount
     */
    public boolean allowsTransaction(long amountInCents) {
        return amountInCents <= maxTransactionLimit;
    }

    /**
     * Checks if this level is higher than another level
     */
    public boolean isHigherThan(KycLevel other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * Checks if this level is lower than another level
     */
    public boolean isLowerThan(KycLevel other) {
        return this.ordinal() < other.ordinal();
    }

    /**
     * Checks if this level meets the minimum requirement
     */
    public boolean meetsMinimum(KycLevel minimum) {
        return this.ordinal() >= minimum.ordinal();
    }

    /**
     * Gets the next higher KYC level
     */
    public KycLevel getNextLevel() {
        KycLevel[] levels = KycLevel.values();
        int currentIndex = this.ordinal();
        
        if (currentIndex < levels.length - 1) {
            return levels[currentIndex + 1];
        }
        
        return this; // Already at highest level
    }

    /**
     * Gets the previous lower KYC level
     */
    public KycLevel getPreviousLevel() {
        KycLevel[] levels = KycLevel.values();
        int currentIndex = this.ordinal();
        
        if (currentIndex > 0) {
            return levels[currentIndex - 1];
        }
        
        return this; // Already at lowest level
    }

    /**
     * Gets the maximum transaction limit in USD
     */
    public double getMaxTransactionLimitUSD() {
        return maxTransactionLimit / 100.0;
    }

    /**
     * Gets the maximum daily transaction limit (typically 10% of max)
     */
    public long getMaxDailyTransactionLimit() {
        return maxTransactionLimit / 10;
    }

    /**
     * Gets the maximum monthly transaction limit (typically 50% of max)
     */
    public long getMaxMonthlyTransactionLimit() {
        return maxTransactionLimit / 2;
    }

    /**
     * Checks if this level requires document verification
     */
    public boolean requiresDocuments() {
        return !requiredDocuments.isEmpty();
    }

    /**
     * Checks if this level requires identity documents
     */
    public boolean requiresIdentityDocument() {
        return requiredDocuments.contains("identity_document");
    }

    /**
     * Checks if this level requires proof of address
     */
    public boolean requiresProofOfAddress() {
        return requiredDocuments.contains("proof_of_address");
    }

    /**
     * Checks if this level requires biometric verification
     */
    public boolean requiresBiometricVerification() {
        return requiredDocuments.contains("selfie_verification") || 
               requiredDocuments.contains("video_verification");
    }

    /**
     * Checks if this level requires financial documents
     */
    public boolean requiresFinancialDocuments() {
        return requiredDocuments.contains("proof_of_income") || 
               requiredDocuments.contains("bank_statement");
    }

    /**
     * Gets the estimated verification time in business days
     */
    public int getEstimatedVerificationDays() {
        return switch (this) {
            case NONE -> 0;
            case BASIC -> 1;
            case ENHANCED -> 3;
            case FULL -> 7;
        };
    }

    /**
     * Gets the verification fee in USD cents
     */
    public long getVerificationFeeCents() {
        return switch (this) {
            case NONE -> 0L;
            case BASIC -> 0L; // Free
            case ENHANCED -> 1000L; // $10
            case FULL -> 5000L; // $50
        };
    }

    /**
     * Gets the risk score (0-100, higher = more risk)
     */
    public int getRiskScore() {
        return switch (this) {
            case NONE -> 100;
            case BASIC -> 70;
            case ENHANCED -> 30;
            case FULL -> 10;
        };
    }

    /**
     * Gets the compliance rating
     */
    public String getComplianceRating() {
        return switch (this) {
            case NONE -> "NON_COMPLIANT";
            case BASIC -> "BASIC_COMPLIANT";
            case ENHANCED -> "ENHANCED_COMPLIANT";
            case FULL -> "FULLY_COMPLIANT";
        };
    }

    /**
     * Gets the allowed features for this KYC level
     */
    public Set<String> getAllowedFeatures() {
        return switch (this) {
            case NONE -> Set.of("profile_view", "basic_search");
            case BASIC -> Set.of("profile_view", "basic_search", "messaging", "small_transactions");
            case ENHANCED -> Set.of("profile_view", "basic_search", "messaging", "small_transactions", 
                                   "medium_transactions", "sourcing_events", "auction_participation");
            case FULL -> Set.of("profile_view", "basic_search", "messaging", "small_transactions",
                               "medium_transactions", "large_transactions", "sourcing_events", 
                               "auction_participation", "premium_features", "api_access");
        };
    }

    /**
     * Checks if this level allows a specific feature
     */
    public boolean allowsFeature(String feature) {
        return getAllowedFeatures().contains(feature);
    }

    /**
     * Gets the minimum KYC level required for a user type
     */
    public static KycLevel getMinimumForUserType(UserType userType) {
        return switch (userType) {
            case BUYER -> BASIC;
            case SUPPLIER -> ENHANCED;
            case HYBRID -> ENHANCED;
            case ADMIN -> FULL;
        };
    }

    /**
     * Gets the recommended KYC level for a transaction amount
     */
    public static KycLevel getRecommendedForAmount(long amountInCents) {
        if (amountInCents <= BASIC.maxTransactionLimit) {
            return BASIC;
        } else if (amountInCents <= ENHANCED.maxTransactionLimit) {
            return ENHANCED;
        } else {
            return FULL;
        }
    }

    /**
     * Creates a KycLevel from string (case-insensitive)
     */
    public static KycLevel fromString(String level) {
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("KYC level cannot be null or empty");
        }

        try {
            return KycLevel.valueOf(level.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid KYC level: " + level);
        }
    }

    /**
     * Checks if the given string is a valid KYC level
     */
    public static boolean isValid(String level) {
        try {
            fromString(level);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}

