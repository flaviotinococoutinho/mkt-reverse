package com.marketplace.shared.events;

import com.marketplace.shared.domain.event.DomainEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory collector for domain events. Useful in unit tests where
 * we want to assert that aggregates emitted the expected events without
 * spinning up any messaging infrastructure.
 */
public class InMemoryDomainEventPublisher implements DomainEventPublisher {

    @Getter
    private final List<DomainEvent> publishedEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        if (event != null) {
            publishedEvents.add(event);
        }
    }

    public void clear() {
        publishedEvents.clear();
    }

    public List<DomainEvent> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(publishedEvents));
    }
}
