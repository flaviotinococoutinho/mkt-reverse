package com.marketplace.agreement.application.port;

import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;

/**
 * Port for the escrow held at an AUTHORIZED payment institution (PSP).
 *
 * Non-negotiable compliance rule (Lei 12.865/2013; Res. BCB 80/2021): the
 * platform NEVER holds third-party funds. Implementations integrate a PSP's
 * escrow/split API; the platform only commands release triggers.
 */
public interface EscrowGateway {

    /**
     * Requests the PSP to retain the agreement amount from the buyer.
     *
     * @return the PSP escrow reference to persist on the agreement
     */
    String fund(Agreement agreement);

    /** Releases the retained amount to the seller (minus the platform take). */
    void release(Agreement agreement);

    /** Refunds the retained amount to the buyer (lapse, default, refund). */
    void refund(Agreement agreement);

    /** Executes a dispute resolution split at the PSP. */
    void resolve(Agreement agreement, ResolutionOutcome outcome);
}
