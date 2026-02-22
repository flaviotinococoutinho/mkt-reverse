package com.marketplace.auction.domain.valueobject;

/**
 * Supported auction models.
 */
public enum AuctionType {
    REVERSE,
    DUTCH,
    SEALED,
    HYBRID;

    public boolean supportsAutoExtension() {
        return this == REVERSE || this == HYBRID;
    }

    public boolean requiresBiddingRounds() {
        return this == HYBRID || this == SEALED;
    }
}
