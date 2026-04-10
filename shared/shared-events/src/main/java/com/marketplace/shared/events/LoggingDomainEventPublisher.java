package com.marketplace.shared.events;

import com.marketplace.shared.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight publisher that simply logs events. Handy as a default bean
 * in environments where a full event bus is not configured yet.
 */
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        log.info("[domain-event] type={} aggregate={} occurredAt={}", 
            event.getEventType(), event.getAggregateId(), event.getOccurredAt());
    }
}
