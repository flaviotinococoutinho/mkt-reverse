package com.marketplace.auction.domain.valueobject;

import com.marketplace.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Bid value object stored inside auction aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bid implements Serializable {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "bid_id", nullable = false))
    private BidId bidId;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "bid_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "bid_currency", length = 3))
    })
    private Money amount;

    @Column(name = "bid_submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "bid_rank")
    private Integer rank;

    @Column(name = "bid_proxy", nullable = false)
    private boolean proxy;

    @Enumerated(EnumType.STRING)
    @Column(name = "bid_type", length = 20)
    private BidType bidType;

    public enum BidType {
        MANUAL,
        PROXY,
        AUTO_DECREMENT
    }

    public static Bid create(String supplierId, Money amount, boolean proxy, BidType type) {
        return new Bid(
            BidId.generate(),
            supplierId,
            amount,
            Instant.now(),
            0,
            proxy,
            type != null ? type : (proxy ? BidType.PROXY : BidType.MANUAL)
        );
    }

    public Bid withRank(int rank) {
        return new Bid(bidId, supplierId, amount, submittedAt, rank, proxy, bidType);
    }

    public boolean isBetterThan(Bid other, AuctionType type) {
        if (other == null) {
            return true;
        }
        return switch (type) {
            case REVERSE, HYBRID, SEALED -> amount.isLessThan(other.amount);
            case DUTCH -> amount.isGreaterThan(other.amount);
        };
    }
}
