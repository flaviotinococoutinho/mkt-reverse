package com.marketplace.sourcing.application.event;

import com.marketplace.sourcing.domain.event.SourcingEventCreatedEvent;
import com.marketplace.sourcing.domain.event.SourcingEventStatusChangedEvent;
import com.marketplace.sourcing.domain.event.SourcingEventUpdatedEvent;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.OpportunitySearchRepository;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SourcingEventIndexer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SourcingEventIndexer.class);

    private final SourcingEventRepository sourcingEventRepository;
    private final OpportunitySearchRepository opportunitySearchRepository;

    public SourcingEventIndexer(
        SourcingEventRepository sourcingEventRepository,
        OpportunitySearchRepository opportunitySearchRepository
    ) {
        this.sourcingEventRepository = sourcingEventRepository;
        this.opportunitySearchRepository = opportunitySearchRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventPublished(SourcingEventCreatedEvent event) {
        indexEvent(event.getAggregateId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStatusChanged(SourcingEventStatusChangedEvent event) {
        indexEvent(event.getAggregateId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventUpdated(SourcingEventUpdatedEvent event) {
        indexEvent(event.getAggregateId());
    }

    private void indexEvent(String eventId) {
        log.debug("Indexing sourcing event: {}", eventId);
        sourcingEventRepository.findById(eventId).ifPresentOrElse(
            opportunitySearchRepository::index,
            () -> log.warn("Attempted to index missing event: {}", eventId)
        );
    }
}
