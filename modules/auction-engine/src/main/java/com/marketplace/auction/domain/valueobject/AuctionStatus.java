package com.marketplace.auction.domain.valueobject;

/**
 * Auction lifecycle statuses.
 */
public enum AuctionStatus {
    SCHEDULED,
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED;

    public boolean isActive() {
        return this == ACTIVE || this == PAUSED;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == FAILED;
    }
}
