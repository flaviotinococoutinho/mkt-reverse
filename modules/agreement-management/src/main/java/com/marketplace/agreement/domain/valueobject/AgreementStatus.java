package com.marketplace.agreement.domain.valueobject;

/**
 * State machine of the buyer-seller agreement (docs/product/business-model.md §5).
 *
 * Happy path:
 *   PENDING_FUNDING → FUNDED → SHIPPED → DELIVERED → RELEASED
 *
 * Branches:
 *   DELIVERED → DISPUTED → RESOLVED_* (refund | partial | release)
 *   PENDING_FUNDING → LAPSED     (buyer never funded within the deadline)
 *   FUNDED → SELLER_DEFAULTED    (seller never shipped within the deadline)
 *   PENDING_FUNDING → CANCELLED  (mutual, before shipment)
 *
 * Legal anchors: the agreement is formed at acceptance but only becomes
 * effective when the escrow is funded (suspensive condition, CC art. 125);
 * release happens on delivery confirmation or after the inspection window
 * elapses without a dispute.
 */
public enum AgreementStatus {

    /** Contract formed at acceptance; awaiting escrow funding (24–48h). */
    PENDING_FUNDING,

    /** Escrow funded at the PSP — the contract is now effective; seller must ship. */
    FUNDED,

    /** Seller shipped with a validated tracking code. */
    SHIPPED,

    /** Carrier/buyer confirmed delivery; inspection window (72h) running. */
    DELIVERED,

    /** Escrow released to the seller (buyer confirmation or window elapsed). */
    RELEASED,

    /** Buyer opened a dispute within the inspection window. */
    DISPUTED,

    /** Dispute resolved with full refund to the buyer. */
    RESOLVED_REFUNDED,

    /** Dispute resolved with a partial split. */
    RESOLVED_PARTIAL,

    /** Dispute resolved releasing the escrow to the seller. */
    RESOLVED_RELEASED,

    /** Funding deadline elapsed — the contract dissolved without effect. */
    LAPSED,

    /** Seller did not ship in time — full refund plus penalty (reputational). */
    SELLER_DEFAULTED,

    /** Mutual cancellation before shipment. */
    CANCELLED;

    public boolean isTerminal() {
        return switch (this) {
            case RELEASED, RESOLVED_REFUNDED, RESOLVED_PARTIAL, RESOLVED_RELEASED,
                 LAPSED, SELLER_DEFAULTED, CANCELLED -> true;
            default -> false;
        };
    }
}
