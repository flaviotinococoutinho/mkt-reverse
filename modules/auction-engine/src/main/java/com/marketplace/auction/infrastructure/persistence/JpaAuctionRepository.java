package com.marketplace.auction.infrastructure.persistence;

import com.marketplace.auction.domain.model.Auction;
import com.marketplace.auction.domain.repository.AuctionRepository;
import com.marketplace.auction.domain.valueobject.AuctionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.marketplace.auction.domain.valueobject.AuctionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaAuctionRepository implements AuctionRepository {

    private final SpringDataAuctionJpaRepository jpaRepository;

    @Override
    public Optional<Auction> findById(AuctionId id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Auction> findById(String id) {
        return jpaRepository.findById(AuctionId.of(id));
    }

    @Override
    public List<Auction> findBySourcingEventId(String sourcingEventId) {
        return jpaRepository.findBySourcingEventId(sourcingEventId);
    }

    @Override
    public List<Auction> findActiveAuctions(String tenantId) {
        // Simple implementation for MVP
        return jpaRepository.findByStatus(AuctionStatus.ACTIVE).stream()
            .filter(a -> a.getTenantId().equals(tenantId))
            .toList();
    }

    @Override
    public List<Auction> findEndingSoon(Instant threshold) {
        return jpaRepository.findByStatus(AuctionStatus.ACTIVE).stream()
            .filter(a -> a.getEndsAt() != null && a.getEndsAt().isBefore(threshold))
            .toList();
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public Auction save(Auction auction) {
        return jpaRepository.save(auction);
    }

    @Override
    public void delete(Auction auction) {
        jpaRepository.delete(auction);
    }
}
