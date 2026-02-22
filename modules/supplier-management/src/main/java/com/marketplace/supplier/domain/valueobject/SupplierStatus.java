package com.marketplace.supplier.domain.valueobject;

/**
 * Supplier lifecycle statuses.
 */
public enum SupplierStatus {
    
    ONBOARDING,
    PENDING_REVIEW,
    ACTIVE,
    SUSPENDED,
    BLACKLISTED,
    ARCHIVED;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == SUSPENDED || this == BLACKLISTED;
    }

    public boolean allowsMarketplaceParticipation() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(SupplierStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case ONBOARDING -> target == PENDING_REVIEW || target == ACTIVE || target == ARCHIVED;
            case PENDING_REVIEW -> target == ACTIVE || target == SUSPENDED || target == ARCHIVED;
            case ACTIVE -> target == SUSPENDED || target == BLACKLISTED || target == ARCHIVED;
            case SUSPENDED -> target == ACTIVE || target == BLACKLISTED || target == ARCHIVED;
            case BLACKLISTED -> target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
