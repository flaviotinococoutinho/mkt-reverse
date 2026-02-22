package com.marketplace.supplier.domain.valueobject;

/**
 * Supplier compliance lifecycle.
 */
public enum ComplianceStatus {
    
    NOT_STARTED,
    IN_PROGRESS,
    APPROVED,
    REJECTED,
    EXPIRED;

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isPending() {
        return this == NOT_STARTED || this == IN_PROGRESS;
    }

    public boolean canRequestParticipation() {
        return this == APPROVED || this == IN_PROGRESS;
    }
}
