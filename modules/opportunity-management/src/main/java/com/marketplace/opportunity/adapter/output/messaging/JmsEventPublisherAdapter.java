package com.marketplace.opportunity.adapter.output.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.opportunity.application.port.output.EventPublisher;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * JMS adapter for publishing domain events to RabbitMQ.
 * 
 * Output Adapter in Hexagonal Architecture:
 * - Implements EventPublisher port
 * - Handles message serialization
 * - Manages JMS communication
 * - Adds distributed tracing context
 * 
 * Uses Spring JMS with RabbitMQ for reliable message delivery.
 * Reactive wrapper around blocking JMS operations.
 */
@Component
public class JmsEventPublisherAdapter implements EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(JmsEventPublisherAdapter.class);
    
    private static final String EXCHANGE_PREFIX = "marketplace.events.";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String EVENT_TYPE_HEADER = "X-Event-Type";
    
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    
    public JmsEventPublisherAdapter(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> publish(Object event) {
        return Mono.fromRunnable(() -> publishEvent(event))
            .subscribeOn(Schedulers.boundedElastic())
            .then()
            .doOnSuccess(v -> logPublishSuccess(event))
            .doOnError(error -> logPublishError(event, error));
    }
    
    @Override
    public Mono<Void> publishAll(Iterable<?> events) {
        return Flux.fromIterable(events)
            .flatMap(this::publish)
            .then();
    }
    
    private void publishEvent(Object event) {
        String destination = buildDestination(event);
        MessageCreator messageCreator = createMessage(event);
        
        jmsTemplate.send(destination, messageCreator);
        
        logger.debug(
            "Event published to JMS: destination={}, eventType={}",
            destination,
            event.getClass().getSimpleName()
        );
    }
    
    private String buildDestination(Object event) {
        String eventType = extractEventType(event);
        return EXCHANGE_PREFIX + eventType;
    }
    
    private String extractEventType(Object event) {
        String className = event.getClass().getSimpleName();
        return toSnakeCase(className);
    }
    
    private String toSnakeCase(String camelCase) {
        return camelCase
            .replaceAll("([a-z])([A-Z])", "$1_$2")
            .toLowerCase();
    }
    
    private MessageCreator createMessage(Object event) {
        return new EventMessageCreator(event);
    }
    
    private void logPublishSuccess(Object event) {
        logger.info(
            "Domain event published successfully: eventType={}",
            event.getClass().getSimpleName()
        );
    }
    
    private void logPublishError(Object event, Throwable error) {
        logger.error(
            "Failed to publish domain event: eventType={}, error={}",
            event.getClass().getSimpleName(),
            error.getMessage(),
            error
        );
    }
    
    /**
     * Message creator for domain events.
     * Adds tracing headers and serializes event to JSON.
     */
    private class EventMessageCreator implements MessageCreator {
        
        private final Object event;
        
        public EventMessageCreator(Object event) {
            this.event = event;
        }
        
        @Override
        public Message createMessage(Session session) throws JMSException {
            try {
                String json = serializeEvent(event);
                Message message = session.createTextMessage(json);
                
                addTracingHeaders(message);
                addEventTypeHeader(message);
                
                return message;
            } catch (Exception exception) {
                throw new JMSException("Failed to create message: " + exception.getMessage());
            }
        }
        
        private String serializeEvent(Object event) throws Exception {
            return objectMapper.writeValueAsString(event);
        }
        
        private void addTracingHeaders(Message message) throws JMSException {
            String traceId = MDC.get("traceId");
            String spanId = MDC.get("spanId");
            
            if (traceId != null) {
                message.setStringProperty(TRACE_ID_HEADER, traceId);
            }
            
            if (spanId != null) {
                message.setStringProperty(SPAN_ID_HEADER, spanId);
            }
        }
        
        private void addEventTypeHeader(Message message) throws JMSException {
            String eventType = event.getClass().getSimpleName();
            message.setStringProperty(EVENT_TYPE_HEADER, eventType);
        }
    }
}
