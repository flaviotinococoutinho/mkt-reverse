package com.marketplace.auction.infrastructure.messaging;

import com.marketplace.auction.application.usecase.ScheduleAuctionUseCase;
import com.marketplace.auction.domain.valueobject.AuctionRules;
import com.marketplace.auction.domain.valueobject.AuctionType;
import com.marketplace.sourcing.application.service.SourcingEventApplicationService;
import com.marketplace.sourcing.domain.event.SourcingEventStatusChangedEvent;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.marketplace.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionSourcingListener {

    private final ScheduleAuctionUseCase scheduleAuctionUseCase;
    private final SourcingEventApplicationService sourcingService;

    @EventListener
    public void onSourcingEventStatusChanged(SourcingEventStatusChangedEvent event) {
        // Only interested when an event is PUBLISHED
        if (isPublished(event)) {
            handlePublishedEvent(event.getAggregateId());
        }
    }

    private boolean isPublished(SourcingEventStatusChangedEvent event) {
        String newStatus = (String) event.getMetadata().getProperty("newStatus");
        return SourcingEventStatus.PUBLISHED.name().equals(newStatus);
    }

    private void handlePublishedEvent(String eventId) {
        try {
            SourcingEvent sourcingEvent = sourcingService.getEvent(eventId, null);

            if (sourcingEvent.getEventType() == SourcingEventType.REVERSE_AUCTION) {
                log.info("Sourcing Event {} is a Reverse Auction. Scheduling auction engine...", eventId);
                scheduleAuction(sourcingEvent);
            }
        } catch (Exception e) {
            log.error("Failed to schedule auction for Sourcing Event {}", eventId, e);
        }
    }

    private void scheduleAuction(SourcingEvent event) {
        // Map Sourcing params to Auction params
        // For MVP, we use default rules
        AuctionRules rules = AuctionRules.create(
            Money.of(BigDecimal.TEN, event.getEstimatedBudget().getCurrency()), // min decrement (BigDecimal enforced)
            2, // autoExtendMinutes
            3, // maxExtensions
            true, // allowProxyBids
            0, // maxBidsPerSupplier (0 = unlimited)
            "LOWEST_BID", // tieBreakerStrategy
            10 // silencePeriodSeconds
        );

        scheduleAuctionUseCase.execute(
            event.getBuyerContext().getTenantId(),
            event.getId().asString(),
            AuctionType.REVERSE, // Mapping REVERSE_AUCTION -> REVERSE
            Instant.now(), // Scheduled start: NOW for simplicity in MVP flow
            event.getEstimatedBudget(), // Starting Price = Max Budget
            null, // Reserve Price (optional)
            rules
        );
        
        log.info("Auction scheduled successfully for Sourcing Event {}", event.getId().asString());
    }
}
