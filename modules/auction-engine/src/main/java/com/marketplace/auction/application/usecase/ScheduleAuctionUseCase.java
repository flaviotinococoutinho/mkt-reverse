package com.marketplace.auction.application.usecase;

import com.marketplace.auction.domain.model.Auction;
import com.marketplace.auction.domain.repository.AuctionRepository;
import com.marketplace.auction.domain.valueobject.AuctionId;
import com.marketplace.auction.domain.valueobject.AuctionRules;
import com.marketplace.auction.domain.valueobject.AuctionType;
import com.marketplace.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ScheduleAuctionUseCase {

    private final AuctionRepository auctionRepository;

    @Transactional
    public AuctionId execute(
        String tenantId,
        String sourcingEventId,
        AuctionType type,
        Instant scheduledStartAt,
        Money startingPrice,
        Money reservePrice,
        AuctionRules rules
    ) {
        Auction auction = Auction.schedule(
            tenantId,
            sourcingEventId,
            type,
            scheduledStartAt,
            startingPrice,
            reservePrice,
            rules
        );

        auctionRepository.save(auction);
        
        return auction.getId();
    }
}
