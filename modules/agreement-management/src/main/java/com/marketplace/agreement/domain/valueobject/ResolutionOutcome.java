package com.marketplace.agreement.domain.valueobject;

/**
 * Outcome of a dispute resolution (ODR). The platform's decision executes the
 * escrow but never blocks access to the Judiciary (CDC art. 51 — compulsory
 * arbitration against consumers is void).
 */
public enum ResolutionOutcome {
    REFUND,
    PARTIAL,
    RELEASE
}
