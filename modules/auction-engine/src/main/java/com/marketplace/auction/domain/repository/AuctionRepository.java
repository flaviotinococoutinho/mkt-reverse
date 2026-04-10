package com.marketplace.auction.domain.repository;

import com.marketplace.auction.domain.model.Auction;
import com.marketplace.auction.domain.valueobject.AuctionId;
import com.marketplace.auction.domain.valueobject.AuctionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for auction aggregates.
 */
public interface AuctionRepository {

    Optional<Auction> findById(AuctionId id);

    Optional<Auction> findById(String id);

    List<Auction> findBySourcingEventId(String sourcingEventId);

    List<Auction> findActiveAuctions(String tenantId);

    List<Auction> findEndingSoon(Instant threshold);

    List<Auction> findByStatus(AuctionStatus status);

    Auction save(Auction auction);

    void delete(Auction auction);
}
