package com.marketplace.auction.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when an auction is scheduled or starts running.
 */
@Getter
public class AuctionStartedEvent implements DomainEvent {

    private final String auctionId;
    private final boolean live;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public AuctionStartedEvent(String auctionId, boolean live, Instant occurredAt) {
        this.auctionId = auctionId;
        this.live = live;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            auctionId,
            "Auction",
            Map.of(
                "live", live
            )
        );
    }

    @Override
    public String getEventType() {
        return "AuctionStartedEvent";
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
