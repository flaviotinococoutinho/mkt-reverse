package com.marketplace.auction.domain.model;

import com.marketplace.auction.domain.valueobject.*;
import com.marketplace.auction.domain.valueobject.Bid.BidType;
import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuctionTest {

    @Test
    void shouldFollowReverseAuctionLifecycle() {
        Money startingPrice = Money.of(1000, CurrencyCode.BRL);
        Money decrement = Money.of(10, CurrencyCode.BRL);
        AuctionRules rules = AuctionRules.create(
            decrement,
            5,
            3,
            true,
            0,
            "LOWEST_BID",
            0
        );

        Auction auction = Auction.schedule(
            "tenant-1",
            "event-1",
            AuctionType.REVERSE,
            Instant.now().plus(1, ChronoUnit.HOURS),
            startingPrice,
            Money.of(500, CurrencyCode.BRL),
            rules
        );

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);

        auction.start(Instant.now());
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.ACTIVE);

        // First bid
        Bid bid1 = auction.submitBid("supplier-1", Money.of(990, CurrencyCode.BRL), BidType.MANUAL);
        assertThat(auction.getCurrentBestBid()).isEqualTo(Money.of(990, CurrencyCode.BRL));
        assertThat(bid1).isNotNull();
        assertThat(bid1.getRank()).isEqualTo(1);

        // Second bid (better)
        auction.submitBid("supplier-2", Money.of(950, CurrencyCode.BRL), BidType.MANUAL);
        assertThat(auction.getCurrentBestBid()).isEqualTo(Money.of(950, CurrencyCode.BRL));

        // Invalid bid (not improving enough)
        assertThatThrownBy(() -> auction.submitBid("supplier-3", Money.of(945, CurrencyCode.BRL), BidType.MANUAL))
            .isInstanceOf(IllegalArgumentException.class);

        auction.complete();
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.COMPLETED);
        assertThat(auction.getWinningSupplierId()).isEqualTo("supplier-2");
    }
}
