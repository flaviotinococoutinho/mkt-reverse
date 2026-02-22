package com.marketplace.auction.infrastructure.messaging;

import com.marketplace.auction.application.usecase.ScheduleAuctionUseCase;
import com.marketplace.auction.domain.valueobject.AuctionRules;
import com.marketplace.auction.domain.valueobject.AuctionType;
import com.marketplace.sourcing.application.SourcingMvpService;
import com.marketplace.sourcing.domain.event.SourcingEventStatusChangedEvent;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionSourcingListener {

    private final ScheduleAuctionUseCase scheduleAuctionUseCase;
    private final SourcingMvpService sourcingService;

    @EventListener
    public void onSourcingEventStatusChanged(SourcingEventStatusChangedEvent event) {
        // Only interested when an event is PUBLISHED
        if (isPublished(event)) {
            handlePublishedEvent(event.getAggregateId());
        }
    }

    private boolean isPublished(SourcingEventStatusChangedEvent event) {
        String newStatus = (String) event.getMetadata().getAttributes().get("newStatus");
        return SourcingEventStatus.PUBLISHED.name().equals(newStatus);
    }

    private void handlePublishedEvent(String eventId) {
        try {
            SourcingEvent sourcingEvent = sourcingService.getEvent(eventId);

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
        AuctionRules rules = new AuctionRules(
            Duration.ofMinutes(5), // min duration
            Duration.ofMinutes(2), // extension window
            Money.of(BigDecimal.TEN, event.getEstimatedBudget().getCurrency()) // min decrement (BigDecimal enforced)
        );

        scheduleAuctionUseCase.execute(
            event.getBuyerContext().getTenantId(),
            event.getId().asString(),
            AuctionType.REVERSE, // Mapping REVERSE_AUCTION -> REVERSE
            event.getTimeline().getSubmissionDeadline(), // Start auction when submission deadline hits? Or immediately? 
                                                         // Usually Reverse Auction starts at a specific time. 
                                                         // Let's assume it starts when published for this MVP flow or respects a start date.
                                                         // Actually, sourcing timeline usually has "Submission Deadline". 
                                                         // For Auction, Submission Deadline might be the "Start of Live Bidding".
            Instant.now(), // Scheduled start: NOW for simplicity in MVP flow
            event.getEstimatedBudget(), // Starting Price = Max Budget
            null, // Reserve Price (optional)
            rules
        );
        
        log.info("Auction scheduled successfully for Sourcing Event {}", event.getId().asString());
    }
}
