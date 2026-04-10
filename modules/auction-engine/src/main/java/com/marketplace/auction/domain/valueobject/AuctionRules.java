package com.marketplace.auction.domain.valueobject;

import com.marketplace.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Business rules governing auction execution.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionRules implements Serializable {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "rule_min_decrement", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "rule_currency", length = 3))
    })
    private Money minimumDecrement;

    @Column(name = "rule_auto_extend_minutes", nullable = false)
    private int autoExtendMinutes;

    @Column(name = "rule_max_extensions", nullable = false)
    private int maxExtensions;

    @Column(name = "rule_allow_proxy_bids", nullable = false)
    private boolean allowProxyBids;

    @Column(name = "rule_max_bids_per_supplier", nullable = false)
    private int maxBidsPerSupplier;

    @Column(name = "rule_tie_breaker", length = 50)
    private String tieBreakerStrategy;

    @Column(name = "rule_silence_seconds", nullable = false)
    private int silencePeriodSeconds;

    public static AuctionRules create(
        Money minimumDecrement,
        int autoExtendMinutes,
        int maxExtensions,
        boolean allowProxyBids,
        int maxBidsPerSupplier,
        String tieBreakerStrategy,
        int silencePeriodSeconds
    ) {
        if (minimumDecrement == null || minimumDecrement.isNegative() || minimumDecrement.isZero()) {
            throw new IllegalArgumentException("minimumDecrement must be positive");
        }
        if (autoExtendMinutes < 0 || autoExtendMinutes > 60) {
            throw new IllegalArgumentException("autoExtendMinutes must be between 0 and 60");
        }
        if (maxExtensions < 0 || maxExtensions > 5) {
            throw new IllegalArgumentException("maxExtensions must be between 0 and 5");
        }
        if (maxBidsPerSupplier < 0) {
            throw new IllegalArgumentException("maxBidsPerSupplier cannot be negative");
        }
        if (silencePeriodSeconds < 0 || silencePeriodSeconds > 600) {
            throw new IllegalArgumentException("silencePeriodSeconds must be between 0 and 600");
        }

        return new AuctionRules(
            minimumDecrement,
            autoExtendMinutes,
            maxExtensions,
            allowProxyBids,
            maxBidsPerSupplier,
            tieBreakerStrategy != null ? tieBreakerStrategy.trim().toUpperCase() : "LOWEST_BID",
            silencePeriodSeconds
        );
    }

    public boolean shouldAutoExtend(int extensionsUsed) {
        return autoExtendMinutes > 0 && extensionsUsed < maxExtensions;
    }
}
