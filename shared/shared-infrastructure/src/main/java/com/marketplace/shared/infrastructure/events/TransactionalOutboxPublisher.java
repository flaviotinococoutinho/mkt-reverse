package com.marketplace.shared.infrastructure.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.events.DomainEventPublisher;
import com.marketplace.shared.infrastructure.outbox.OutboxEvent;
import com.marketplace.shared.infrastructure.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists domain events to the outbox table within the same transaction
 * as the business logic. Also publishes to local Spring context for synchronous listeners.
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionalOutboxPublisher implements DomainEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }

        // 1. Save to Outbox (Persistence)
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                "Aggregate", // Ideally verify event metadata for aggregate type
                event.getAggregateId(),
                event.getEventType(),
                payload
            );
            outboxEventRepository.save(outboxEvent);
            log.debug("Event {} saved to outbox", event.getEventType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}", event.getEventType(), e);
            throw new RuntimeException("Event serialization failed", e);
        }

        // 2. Publish locally (Spring Memory) - optional, kept for existing tests/listeners
        // Note: Listeners should be aware they are in the same transaction unless @Transactional(REQUIRES_NEW)
        applicationEventPublisher.publishEvent(event);
    }
}
