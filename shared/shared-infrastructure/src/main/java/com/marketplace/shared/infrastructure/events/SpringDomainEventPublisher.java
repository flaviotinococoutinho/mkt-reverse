package com.marketplace.shared.infrastructure.events;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Bridges domain events to Spring's {@link ApplicationEventPublisher} so
 * listeners can subscribe with {@code @EventListener}.
 */
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        applicationEventPublisher.publishEvent(event);
    }
}
