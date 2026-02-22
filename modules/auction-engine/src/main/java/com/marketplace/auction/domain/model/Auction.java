package com.marketplace.auction.domain.model;

import com.marketplace.auction.domain.event.AuctionCancelledEvent;
import com.marketplace.auction.domain.event.AuctionCompletedEvent;
import com.marketplace.auction.domain.event.AuctionStartedEvent;
import com.marketplace.auction.domain.event.BidSubmittedEvent;
import com.marketplace.auction.domain.valueobject.AuctionId;
import com.marketplace.auction.domain.valueobject.AuctionRules;
import com.marketplace.auction.domain.valueobject.AuctionStatus;
import com.marketplace.auction.domain.valueobject.AuctionType;
import com.marketplace.auction.domain.valueobject.Bid;
import com.marketplace.auction.domain.valueobject.Bid.BidType;
import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Auction aggregate responsible for real-time bidding lifecycle.
 */
@Entity
@Table(name = "AUC_AUCTIONS", indexes = {
    @Index(name = "idx_auc_tenant", columnList = "tenant_id"),
    @Index(name = "idx_auc_status", columnList = "status"),
    @Index(name = "idx_auc_sourcing_event", columnList = "sourcing_event_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends AggregateRoot<AuctionId> {

    @EmbeddedId
    private AuctionId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "sourcing_event_id", nullable = false, length = 36)
    private String sourcingEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", nullable = false, length = 20)
    private AuctionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuctionStatus status;

    @Column(name = "scheduled_start_at", nullable = false)
    private Instant scheduledStartAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "starting_price", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "starting_currency", length = 3))
    })
    private Money startingPrice;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "reserve_price", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "reserve_currency", length = 3))
    })
    private Money reservePrice;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "current_best_bid", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "current_best_currency", length = 3))
    })
    private Money currentBestBid;

    @Embedded
    private AuctionRules rules;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "AUC_AUCTION_BIDS", joinColumns = @JoinColumn(name = "auction_id"))
    @OrderBy("submittedAt ASC")
    private List<Bid> bids = new ArrayList<>();

    @Column(name = "extensions_used", nullable = false)
    private int extensionsUsed;

    @Column(name = "winning_supplier_id", length = 36)
    private String winningSupplierId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "winning_bid_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "winning_bid_currency", length = 3))
    })
    private Money winningBidAmount;

    @Column(name = "last_bid_at")
    private Instant lastBidAt;

    private Auction(
        AuctionId id,
        String tenantId,
        String sourcingEventId,
        AuctionType type,
        AuctionStatus status,
        Instant scheduledStartAt,
        Money startingPrice,
        Money reservePrice,
        AuctionRules rules
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.sourcingEventId = sourcingEventId;
        this.type = type;
        this.status = status;
        this.scheduledStartAt = scheduledStartAt;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.rules = rules;
    }

    public static Auction schedule(
        String tenantId,
        String sourcingEventId,
        AuctionType type,
        Instant scheduledStartAt,
        Money startingPrice,
        Money reservePrice,
        AuctionRules rules
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(sourcingEventId, "sourcingEventId is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(scheduledStartAt, "scheduledStartAt is required");
        Objects.requireNonNull(startingPrice, "startingPrice is required");
        Objects.requireNonNull(rules, "rules is required");

        Auction auction = new Auction(
            AuctionId.generate(),
            tenantId.trim(),
            sourcingEventId.trim(),
            type,
            AuctionStatus.SCHEDULED,
            scheduledStartAt,
            startingPrice,
            reservePrice,
            rules
        );

        auction.addDomainEvent(new AuctionStartedEvent(auction.getId().asString(), false, scheduledStartAt));
        auction.markAsCreated();
        return auction;
    }

    public void start(Instant reference) {
        ensureStatus(AuctionStatus.SCHEDULED);
        this.status = AuctionStatus.ACTIVE;
        this.startedAt = reference != null ? reference : Instant.now();
        this.endsAt = rules.getAutoExtendMinutes() > 0
            ? this.startedAt.plus(Duration.ofMinutes(rules.getAutoExtendMinutes()))
            : this.startedAt.plus(Duration.ofMinutes(15));
        addDomainEvent(new AuctionStartedEvent(id.asString(), true, startedAt));
        markAsUpdated();
    }

    public void pause() {
        ensureStatus(AuctionStatus.ACTIVE);
        this.status = AuctionStatus.PAUSED;
        markAsUpdated();
    }

    public void resume() {
        ensureStatus(AuctionStatus.PAUSED);
        this.status = AuctionStatus.ACTIVE;
        markAsUpdated();
    }

    public Bid submitBid(String supplierId, Money amount, BidType bidType) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("supplierId cannot be blank");
        }
        Objects.requireNonNull(amount, "amount is required");
        ensureStatus(AuctionStatus.ACTIVE);

        if (!bids.isEmpty() && !amount.getCurrency().equals(currentCurrency().getCurrency())) {
            throw new IllegalArgumentException("Bid currency must match auction currency");
        }
        if (rules.getMaxBidsPerSupplier() > 0) {
            long count = bids.stream().filter(b -> b.getSupplierId().equals(supplierId)).count();
            if (count >= rules.getMaxBidsPerSupplier()) {
                throw new IllegalStateException("Supplier reached maximum number of bids");
            }
        }
        if (lastBidAt != null && Instant.now().isBefore(lastBidAt.plusSeconds(rules.getSilencePeriodSeconds()))) {
            throw new IllegalStateException("Bidder must wait silence period before new bid");
        }

        Money bestBefore = currentBestBid != null ? currentBestBid : startingPrice;
        if (!isBidBetter(amount, bestBefore)) {
            throw new IllegalArgumentException("Bid amount does not improve current standing");
        }
        if (reservePrice != null && amount.isGreaterThan(reservePrice) && type != AuctionType.DUTCH && bids.size() > 1) {
            throw new IllegalArgumentException("Bid must be below reserve price for reverse auctions");
        }

        Bid bid = Bid.create(supplierId.trim(), amount, BidType.PROXY.equals(bidType), bidType);
        bids.add(bid);
        
        // Update current standing
        this.currentBestBid = amount;
        this.winningSupplierId = supplierId.trim();
        this.winningBidAmount = amount;
        this.lastBidAt = Instant.now();

        recalculateRanking();
        
        // Find the newly ranked bid to return it correctly
        Bid rankedBid = bids.stream()
            .filter(b -> b.getBidId().equals(bid.getBidId()))
            .findFirst()
            .orElse(bid);

        if (shouldAutoExtend()) {
            this.endsAt = endsAt.plus(Duration.ofMinutes(rules.getAutoExtendMinutes()));
            this.extensionsUsed++;
        }

        addDomainEvent(new BidSubmittedEvent(
            id.asString(),
            bid.getBidId().asString(),
            supplierId.trim(),
            amount,
            bids.size()
        ));
        markAsUpdated();
        return rankedBid;
    }

    public void complete() {
        if (status.isTerminal()) {
            return;
        }
        if (winningSupplierId == null) {
            throw new IllegalStateException("Auction cannot complete without winning bid");
        }
        this.status = AuctionStatus.COMPLETED;
        this.completedAt = Instant.now();
        addDomainEvent(new AuctionCompletedEvent(
            id.asString(),
            winningSupplierId,
            winningBidAmount,
            completedAt
        ));
        markAsUpdated();
    }

    public void cancel(String reason) {
        if (status.isTerminal()) {
            return;
        }
        this.status = AuctionStatus.CANCELLED;
        addDomainEvent(new AuctionCancelledEvent(id.asString(), reason, Instant.now()));
        markAsUpdated();
    }

    private void recalculateRanking() {
        List<Bid> ordered = bids.stream()
            .sorted(Comparator.comparing(Bid::getAmount, (a, b) -> compareMoney(a, b, type)))
            .collect(Collectors.toList());

        for (int i = 0; i < ordered.size(); i++) {
            Bid original = ordered.get(i);
            Bid updated = original.withRank(i + 1);
            int index = bids.indexOf(original);
            bids.set(index, updated);
        }
    }

    private int compareMoney(Money a, Money b, AuctionType type) {
        return switch (type) {
            case REVERSE, HYBRID, SEALED -> a.compareTo(b);
            case DUTCH -> b.compareTo(a);
        };
    }

    private boolean isBidBetter(Money candidate, Money current) {
        if (type == AuctionType.DUTCH) {
            return candidate.isGreaterThan(current);
        }
        if (current == startingPrice) {
            return candidate.isLessThan(current) || candidate.equals(current);
        }
        Money threshold = current.subtract(rules.getMinimumDecrement());
        return candidate.isLessThan(threshold) || candidate.equals(threshold);
    }

    private Money currentCurrency() {
        return currentBestBid != null ? currentBestBid : startingPrice;
    }

    private boolean shouldAutoExtend() {
        return rules.shouldAutoExtend(extensionsUsed) && endsAt.minusSeconds(60).isBefore(Instant.now());
    }

    private void ensureStatus(AuctionStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Auction must be in status " + expected + " but was " + status);
        }
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("Auction id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (sourcingEventId == null || sourcingEventId.trim().isEmpty()) {
            throw new IllegalStateException("sourcingEventId cannot be null");
        }
        if (type == null) {
            throw new IllegalStateException("type cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
        if (startingPrice == null) {
            throw new IllegalStateException("startingPrice cannot be null");
        }
        if (rules == null) {
            throw new IllegalStateException("rules cannot be null");
        }
    }
}
