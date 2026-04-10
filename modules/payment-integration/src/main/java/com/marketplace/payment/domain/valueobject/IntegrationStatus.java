package com.marketplace.payment.domain.valueobject;

/**
 * Lifecycle states for payment connectors.
 */
public enum IntegrationStatus {
    PENDING,
    ACTIVE,
    DISABLED,
    ERROR;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
