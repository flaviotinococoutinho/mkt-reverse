package com.marketplace.contract.domain.valueobject;

/**
 * Contract lifecycle statuses.
 */
public enum ContractStatus {
    DRAFT,
    NEGOTIATION,
    PENDING_SIGNATURE,
    ACTIVE,
    SUSPENDED,
    TERMINATED,
    EXPIRED,
    RENEWED;

    public boolean isActive() {
        return this == ACTIVE || this == RENEWED;
    }

    public boolean isTerminal() {
        return this == TERMINATED || this == EXPIRED;
    }

    public boolean canTransitionTo(ContractStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case DRAFT -> target == NEGOTIATION || target == PENDING_SIGNATURE;
            case NEGOTIATION -> target == PENDING_SIGNATURE || target == DRAFT;
            case PENDING_SIGNATURE -> target == ACTIVE || target == NEGOTIATION;
            case ACTIVE -> target == SUSPENDED || target == TERMINATED || target == EXPIRED || target == RENEWED;
            case SUSPENDED -> target == ACTIVE || target == TERMINATED;
            case TERMINATED, EXPIRED, RENEWED -> false;
        };
    }
}
