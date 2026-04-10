package com.marketplace.auction.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.shared.valueobject.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when an auction completes successfully.
 */
@Getter
public class AuctionCompletedEvent implements DomainEvent {

    private final String auctionId;
    private final String winningSupplierId;
    private final Money winningBid;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public AuctionCompletedEvent(String auctionId, String winningSupplierId, Money winningBid, Instant occurredAt) {
        this.auctionId = auctionId;
        this.winningSupplierId = winningSupplierId;
        this.winningBid = winningBid;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            auctionId,
            "Auction",
            Map.of(
                "winningSupplierId", winningSupplierId,
                "winningBid", winningBid != null ? winningBid.getAmount() : null,
                "currency", winningBid != null ? winningBid.getCurrency().name() : null
            )
        );
    }

    @Override
    public String getEventType() {
        return "AuctionCompletedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
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
