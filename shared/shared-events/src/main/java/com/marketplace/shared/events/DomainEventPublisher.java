package com.marketplace.shared.events;

import com.marketplace.shared.domain.event.DomainEvent;

import java.util.Collection;

/**
 * Contract for publishing domain events to the infrastructure layer.
 * Concrete implementations may publish to Spring's ApplicationEventPublisher,
 * Kafka, or in-memory collectors for testing purposes.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);

    default void publishAll(Collection<? extends DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        events.forEach(this::publish);
    }
}
