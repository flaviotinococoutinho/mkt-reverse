package com.marketplace.auction.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.shared.valueobject.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a new bid is submitted in an auction.
 */
@Getter
public class BidSubmittedEvent implements DomainEvent {

    private final String auctionId;
    private final String bidId;
    private final String supplierId;
    private final Money amount;
    private final int totalBids;
    private final EventMetadata metadata;

    public BidSubmittedEvent(String auctionId, String bidId, String supplierId, Money amount, int totalBids) {
        this.auctionId = auctionId;
        this.bidId = bidId;
        this.supplierId = supplierId;
        this.amount = amount;
        this.totalBids = totalBids;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            auctionId,
            "Auction",
            Map.of(
                "bidId", bidId,
                "supplierId", supplierId,
                "amount", amount.getAmount(),
                "currency", amount.getCurrency().name(),
                "totalBids", totalBids
            )
        );
    }

    @Override
    public String getEventType() {
        return "BidSubmittedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return metadata.getOccurredAt();
    }

    @Override
    public String getAggregateId() {
        return auctionId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
