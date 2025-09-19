package com.marketplace.user.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * User Status Enumeration
 * 
 * Represents the different states a user can be in throughout their lifecycle.
 * Controls access to system features and defines allowed state transitions.
 * 
 * Design principles:
 * - Clear state machine with defined transitions
 * - Business-meaningful states
 * - Security-aware (locked, suspended states)
 */
@Getter
public enum UserStatus {
    
    /**
     * Pending Verification - User registered but not yet verified
     * Cannot access most system features until verification is complete
     */
    PENDING_VERIFICATION("Pending Verification", "User registered but awaiting email and KYC verification", false, false),
    
    /**
     * Active - Fully verified and active user
     * Can access all system features according to their user type and roles
     */
    ACTIVE("Active", "Fully verified and active user with full system access", true, true),
    
    /**
     * Suspended - Temporarily suspended user
     * Access restricted due to policy violations or security concerns
     */
    SUSPENDED("Suspended", "User temporarily suspended due to policy violations", false, false),
    
    /**
     * Inactive - User deactivated their account
     * Account exists but user cannot log in or access features
     */
    INACTIVE("Inactive", "User has deactivated their account", false, false),
    
    /**
     * Locked - Account locked due to security issues
     * Temporary lock due to failed login attempts or security concerns
     */
    LOCKED("Locked", "Account temporarily locked due to security concerns", false, false),
    
    /**
     * Banned - Permanently banned user
     * Permanent restriction due to severe policy violations
     */
    BANNED("Banned", "User permanently banned from the platform", false, false);

    private final String displayName;
    private final String description;
    private final boolean canLogin;
    private final boolean canAccessFeatures;

    UserStatus(String displayName, String description, boolean canLogin, boolean canAccessFeatures) {
        this.displayName = displayName;
        this.description = description;
        this.canLogin = canLogin;
        this.canAccessFeatures = canAccessFeatures;
    }

    /**
     * Checks if the user can log into the system
     */
    public boolean allowsLogin() {
        return canLogin;
    }

    /**
     * Checks if the user can access system features
     */
    public boolean allowsFeatureAccess() {
        return canAccessFeatures;
    }

    /**
     * Checks if the user can create sourcing events
     */
    public boolean allowsSourcingEvents() {
        return this == ACTIVE;
    }

    /**
     * Checks if the user can participate in auctions
     */
    public boolean allowsAuctionParticipation() {
        return this == ACTIVE;
    }

    /**
     * Checks if the user can send/receive messages
     */
    public boolean allowsMessaging() {
        return this == ACTIVE;
    }

    /**
     * Checks if the user can update their profile
     */
    public boolean allowsProfileUpdates() {
        return this == ACTIVE || this == PENDING_VERIFICATION;
    }

    /**
     * Checks if this is a temporary status that can be resolved
     */
    public boolean isTemporary() {
        return this == PENDING_VERIFICATION || this == SUSPENDED || this == LOCKED;
    }

    /**
     * Checks if this is a permanent status
     */
    public boolean isPermanent() {
        return this == BANNED;
    }

    /**
     * Checks if the user is in a restricted state
     */
    public boolean isRestricted() {
        return !canAccessFeatures;
    }

    /**
     * Gets the valid transitions from this status
     */
    public Set<UserStatus> getValidTransitions() {
        return switch (this) {
            case PENDING_VERIFICATION -> Set.of(ACTIVE, SUSPENDED, BANNED);
            case ACTIVE -> Set.of(SUSPENDED, INACTIVE, LOCKED, BANNED);
            case SUSPENDED -> Set.of(ACTIVE, BANNED);
            case INACTIVE -> Set.of(ACTIVE, BANNED);
            case LOCKED -> Set.of(ACTIVE, SUSPENDED, BANNED);
            case BANNED -> Set.of(); // No transitions from banned
        };
    }

    /**
     * Checks if transition to another status is valid
     */
    public boolean canTransitionTo(UserStatus newStatus) {
        return getValidTransitions().contains(newStatus);
    }

    /**
     * Gets the reason code for this status (for logging/auditing)
     */
    public String getReasonCode() {
        return switch (this) {
            case PENDING_VERIFICATION -> "AWAITING_VERIFICATION";
            case ACTIVE -> "VERIFIED_ACTIVE";
            case SUSPENDED -> "POLICY_VIOLATION";
            case INACTIVE -> "USER_DEACTIVATED";
            case LOCKED -> "SECURITY_LOCK";
            case BANNED -> "PERMANENT_BAN";
        };
    }

    /**
     * Gets the priority level for this status (for sorting/filtering)
     */
    public int getPriority() {
        return switch (this) {
            case BANNED -> 1;
            case SUSPENDED -> 2;
            case LOCKED -> 3;
            case INACTIVE -> 4;
            case PENDING_VERIFICATION -> 5;
            case ACTIVE -> 6;
        };
    }

    /**
     * Gets the CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case ACTIVE -> "status-active";
            case PENDING_VERIFICATION -> "status-pending";
            case SUSPENDED -> "status-warning";
            case INACTIVE -> "status-inactive";
            case LOCKED -> "status-locked";
            case BANNED -> "status-danger";
        };
    }

    /**
     * Gets the icon name for UI display
     */
    public String getIconName() {
        return switch (this) {
            case ACTIVE -> "check-circle";
            case PENDING_VERIFICATION -> "clock";
            case SUSPENDED -> "pause-circle";
            case INACTIVE -> "minus-circle";
            case LOCKED -> "lock";
            case BANNED -> "x-circle";
        };
    }

    /**
     * Creates a UserStatus from string (case-insensitive)
     */
    public static UserStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("User status cannot be null or empty");
        }

        try {
            return UserStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user status: " + status);
        }
    }

    /**
     * Checks if the given string is a valid user status
     */
    public static boolean isValid(String status) {
        try {
            fromString(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets all active statuses (statuses that allow system access)
     */
    public static Set<UserStatus> getActiveStatuses() {
        return Set.of(ACTIVE);
    }

    /**
     * Gets all restricted statuses (statuses that restrict access)
     */
    public static Set<UserStatus> getRestrictedStatuses() {
        return Set.of(SUSPENDED, LOCKED, BANNED, INACTIVE);
    }

    /**
     * Gets all temporary statuses (can be changed)
     */
    public static Set<UserStatus> getTemporaryStatuses() {
        return Set.of(PENDING_VERIFICATION, SUSPENDED, LOCKED);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

