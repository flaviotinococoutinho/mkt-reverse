package com.marketplace.auction.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when an auction is cancelled.
 */
@Getter
public class AuctionCancelledEvent implements DomainEvent {

    private final String auctionId;
    private final String reason;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public AuctionCancelledEvent(String auctionId, String reason, Instant occurredAt) {
        this.auctionId = auctionId;
        this.reason = reason;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            auctionId,
            "Auction",
            Map.of(
                "reason", reason != null ? reason : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "AuctionCancelledEvent";
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
