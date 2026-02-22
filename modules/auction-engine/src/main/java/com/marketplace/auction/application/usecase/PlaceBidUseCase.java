package com.marketplace.auction.application.usecase;

import com.marketplace.auction.domain.model.Auction;
import com.marketplace.auction.domain.repository.AuctionRepository;
import com.marketplace.auction.domain.valueobject.AuctionId;
import com.marketplace.auction.domain.valueobject.Bid;
import com.marketplace.auction.domain.valueobject.Bid.BidType;
import com.marketplace.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceBidUseCase {

    private final AuctionRepository auctionRepository;

    @Transactional
    public Bid execute(String auctionId, String supplierId, Money amount, BidType bidType) {
        Auction auction = auctionRepository.findById(AuctionId.of(auctionId))
            .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + auctionId));

        Bid bid = auction.submitBid(supplierId, amount, bidType);
        
        auctionRepository.save(auction);
        
        return bid;
    }
}
