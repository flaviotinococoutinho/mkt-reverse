package com.marketplace.shared.infrastructure.events;

import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper component to publish and clear domain events from aggregates.
 */
@Component
@RequiredArgsConstructor
public class DomainEvents {

    private final DomainEventPublisher publisher;

    public void publishFrom(AggregateRoot<?> aggregate) {
        if (aggregate == null || aggregate.getDomainEvents().isEmpty()) {
            return;
        }
        publisher.publishAll(aggregate.getDomainEvents());
        aggregate.clearDomainEvents();
    }
}
