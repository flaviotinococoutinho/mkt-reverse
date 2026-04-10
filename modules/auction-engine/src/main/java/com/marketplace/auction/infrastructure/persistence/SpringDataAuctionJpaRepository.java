package com.marketplace.auction.infrastructure.persistence;

import com.marketplace.auction.domain.model.Auction;
import com.marketplace.auction.domain.valueobject.AuctionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marketplace.auction.domain.valueobject.AuctionStatus;
import java.util.List;

@Repository
public interface SpringDataAuctionJpaRepository extends JpaRepository<Auction, AuctionId> {
    List<Auction> findBySourcingEventId(String sourcingEventId);
    List<Auction> findByStatus(AuctionStatus status);
}
