package com.marketplace.user.domain.valueobject;

import lombok.Getter;

/**
 * User Type Enumeration
 * 
 * Represents the different types of users in the marketplace system.
 * Each type has specific roles, permissions, and business capabilities.
 * 
 * Design principles:
 * - Clear business meaning
 * - Extensible for future user types
 * - Rich behavior with business logic
 */
@Getter
public enum UserType {
    
    /**
     * Buyer - Companies or individuals looking to purchase goods/services
     * Can create sourcing events, RFQs, and participate in reverse auctions
     */
    BUYER("Buyer", "Companies or individuals seeking to purchase goods or services", true, false),
    
    /**
     * Supplier - Companies or individuals providing goods/services
     * Can respond to sourcing events, submit proposals, and participate in auctions
     */
    SUPPLIER("Supplier", "Companies or individuals providing goods or services", false, true),
    
    /**
     * Hybrid - Users who can act as both buyers and suppliers
     * Full access to both buyer and supplier functionalities
     */
    HYBRID("Hybrid", "Users who can act as both buyers and suppliers", true, true),
    
    /**
     * Admin - System administrators with full access
     * Can manage users, moderate disputes, and access all system functions
     */
    ADMIN("Administrator", "System administrators with full platform access", true, true);

    private final String displayName;
    private final String description;
    private final boolean canBuy;
    private final boolean canSell;

    UserType(String displayName, String description, boolean canBuy, boolean canSell) {
        this.displayName = displayName;
        this.description = description;
        this.canBuy = canBuy;
        this.canSell = canSell;
    }

    /**
     * Checks if this user type can create sourcing events
     */
    public boolean canCreateSourcingEvents() {
        return canBuy;
    }

    /**
     * Checks if this user type can respond to sourcing events
     */
    public boolean canRespondToSourcingEvents() {
        return canSell;
    }

    /**
     * Checks if this user type can participate in auctions as a bidder
     */
    public boolean canBidInAuctions() {
        return canSell;
    }

    /**
     * Checks if this user type can create auctions
     */
    public boolean canCreateAuctions() {
        return canBuy;
    }

    /**
     * Checks if this user type has administrative privileges
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if this user type is purely a buyer
     */
    public boolean isPureBuyer() {
        return this == BUYER;
    }

    /**
     * Checks if this user type is purely a supplier
     */
    public boolean isPureSupplier() {
        return this == SUPPLIER;
    }

    /**
     * Checks if this user type can act in both roles
     */
    public boolean isHybrid() {
        return canBuy && canSell && this != ADMIN;
    }

    /**
     * Gets the primary role for this user type
     */
    public String getPrimaryRole() {
        return switch (this) {
            case BUYER -> "BUYER";
            case SUPPLIER -> "SUPPLIER";
            case HYBRID -> "HYBRID";
            case ADMIN -> "ADMIN";
        };
    }

    /**
     * Gets all available roles for this user type
     */
    public String[] getAvailableRoles() {
        return switch (this) {
            case BUYER -> new String[]{"BUYER", "USER"};
            case SUPPLIER -> new String[]{"SUPPLIER", "USER"};
            case HYBRID -> new String[]{"BUYER", "SUPPLIER", "USER"};
            case ADMIN -> new String[]{"ADMIN", "BUYER", "SUPPLIER", "USER"};
        };
    }

    /**
     * Determines the required KYC level for this user type
     */
    public KycLevel getRequiredKycLevel() {
        return switch (this) {
            case BUYER -> KycLevel.BASIC;
            case SUPPLIER -> KycLevel.ENHANCED;
            case HYBRID -> KycLevel.ENHANCED;
            case ADMIN -> KycLevel.FULL;
        };
    }

    /**
     * Gets the maximum transaction limit for this user type (in USD)
     */
    public long getMaxTransactionLimit() {
        return switch (this) {
            case BUYER -> 1_000_000L; // $1M
            case SUPPLIER -> 1_000_000L; // $1M
            case HYBRID -> 5_000_000L; // $5M
            case ADMIN -> Long.MAX_VALUE; // No limit
        };
    }

    /**
     * Checks if this user type requires enhanced verification
     */
    public boolean requiresEnhancedVerification() {
        return this == SUPPLIER || this == HYBRID || this == ADMIN;
    }

    /**
     * Gets the default notification preferences for this user type
     */
    public String[] getDefaultNotificationTypes() {
        return switch (this) {
            case BUYER -> new String[]{
                "SOURCING_EVENT_RESPONSES", 
                "AUCTION_UPDATES", 
                "CONTRACT_UPDATES",
                "SYSTEM_ANNOUNCEMENTS"
            };
            case SUPPLIER -> new String[]{
                "NEW_SOURCING_EVENTS", 
                "AUCTION_INVITATIONS", 
                "BID_UPDATES",
                "CONTRACT_UPDATES",
                "SYSTEM_ANNOUNCEMENTS"
            };
            case HYBRID -> new String[]{
                "NEW_SOURCING_EVENTS",
                "SOURCING_EVENT_RESPONSES", 
                "AUCTION_UPDATES",
                "AUCTION_INVITATIONS", 
                "BID_UPDATES",
                "CONTRACT_UPDATES",
                "SYSTEM_ANNOUNCEMENTS"
            };
            case ADMIN -> new String[]{
                "ALL_NOTIFICATIONS",
                "SYSTEM_ALERTS",
                "SECURITY_ALERTS",
                "DISPUTE_NOTIFICATIONS"
            };
        };
    }

    /**
     * Creates a UserType from string (case-insensitive)
     */
    public static UserType fromString(String userType) {
        if (userType == null || userType.trim().isEmpty()) {
            throw new IllegalArgumentException("User type cannot be null or empty");
        }

        try {
            return UserType.valueOf(userType.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    /**
     * Checks if the given string is a valid user type
     */
    public static boolean isValid(String userType) {
        try {
            fromString(userType);
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

