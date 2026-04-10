package com.marketplace.payment.domain.valueobject;

/**
 * Status of an Escrow agreement.
 */
public enum EscrowStatus {
    DRAFT,
    CREATED,
    FUNDED,
    RELEASED,
    DISPUTED,
    CANCELLED
}
