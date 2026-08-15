package com.marketplace.agreement.application.port;

import com.marketplace.agreement.domain.model.Agreement;

/**
 * Deterministic idempotency keys for escrow commands.
 *
 * The agreement state machine allows each money-moving operation at most once
 * per agreement, so a key derived from (operation, agreementId) is stable
 * across retries: if the process dies between the PSP call and the local
 * commit, the retry re-sends the SAME key and the PSP deduplicates instead of
 * charging or paying twice.
 */
public final class EscrowIdempotency {

    private EscrowIdempotency() {
    }

    public static String fundKey(Agreement agreement) {
        return key("fund", agreement);
    }

    public static String releaseKey(Agreement agreement) {
        return key("release", agreement);
    }

    public static String refundKey(Agreement agreement) {
        return key("refund", agreement);
    }

    public static String resolveKey(Agreement agreement) {
        return key("resolve", agreement);
    }

    private static String key(String operation, Agreement agreement) {
        return "escrow-" + operation + "-" + agreement.getId().asString();
    }
}
