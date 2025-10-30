package com.marketplace.opportunity.application.port.output;

import reactor.core.publisher.Mono;

/**
 * Output Port (Driven Port) for publishing domain events.
 * 
 * Part of Hexagonal Architecture:
 * - Interface defined in application layer
 * - Implementation in adapter layer (RabbitMQ JMS)
 * - Decouples domain from messaging infrastructure
 * 
 * Events are published to message broker for:
 * - Inter-service communication
 * - Async processing
 * - Event sourcing
 * - Audit trail
 */
public interface EventPublisher {
    
    /**
     * Publishes a domain event to message broker.
     * 
     * @param event domain event to publish
     * @return completion signal
     */
    Mono<Void> publish(Object event);
    
    /**
     * Publishes multiple domain events in order.
     * 
     * @param events domain events to publish
     * @return completion signal
     */
    Mono<Void> publishAll(Iterable<?> events);
}
