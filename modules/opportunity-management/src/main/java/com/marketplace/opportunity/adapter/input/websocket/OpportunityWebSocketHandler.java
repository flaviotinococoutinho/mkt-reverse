package com.marketplace.opportunity.adapter.input.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.opportunity.application.port.output.OpportunityRepository;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time opportunity notifications.
 * 
 * Input Adapter in Hexagonal Architecture:
 * - Handles WebSocket connections
 * - Streams opportunity updates to clients
 * - Manages active subscriptions
 * 
 * Provides real-time updates for:
 * - New published opportunities
 * - Opportunity status changes
 * - Proposal submissions
 * 
 * Uses Spring WebFlux reactive WebSocket support.
 */
@Component
public class OpportunityWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OpportunityWebSocketHandler.class);
    
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
    private static final String HEARTBEAT_MESSAGE = "{\"type\":\"heartbeat\"}";
    
    private final OpportunityRepository opportunityRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> activeSessions;
    
    public OpportunityWebSocketHandler(
        OpportunityRepository opportunityRepository,
        ObjectMapper objectMapper
    ) {
        this.opportunityRepository = opportunityRepository;
        this.objectMapper = objectMapper;
        this.activeSessions = new ConcurrentHashMap<>();
    }
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        
        logger.info("WebSocket connection established: sessionId={}", sessionId);
        
        activeSessions.put(sessionId, session);
        
        Flux<WebSocketMessage> outputMessages = createOutputStream(session);
        
        return session.send(outputMessages)
            .doOnTerminate(() -> handleDisconnection(sessionId))
            .doOnError(error -> handleError(sessionId, error));
    }
    
    /**
     * Creates output stream of messages for the session.
     * Combines published opportunities stream with heartbeat.
     * 
     * @param session WebSocket session
     * @return flux of WebSocket messages
     */
    private Flux<WebSocketMessage> createOutputStream(WebSocketSession session) {
        Flux<WebSocketMessage> opportunityStream = streamPublishedOpportunities(session);
        Flux<WebSocketMessage> heartbeatStream = createHeartbeatStream(session);
        
        return Flux.merge(opportunityStream, heartbeatStream);
    }
    
    /**
     * Streams published opportunities to client.
     * 
     * @param session WebSocket session
     * @return flux of opportunity messages
     */
    private Flux<WebSocketMessage> streamPublishedOpportunities(WebSocketSession session) {
        return opportunityRepository.findByStatus(OpportunityStatus.PUBLISHED)
            .map(opportunity -> {
                try {
                    OpportunityNotification notification = OpportunityNotification.from(opportunity);
                    String json = objectMapper.writeValueAsString(notification);
                    return session.textMessage(json);
                } catch (Exception exception) {
                    logger.error("Failed to serialize opportunity notification", exception);
                    return session.textMessage("{\"type\":\"error\",\"message\":\"Serialization failed\"}");
                }
            })
            .doOnNext(message -> logger.debug("Sending opportunity notification via WebSocket"))
            .onErrorResume(error -> {
                logger.error("Error streaming opportunities", error);
                return Flux.empty();
            });
    }
    
    /**
     * Creates heartbeat stream to keep connection alive.
     * 
     * @param session WebSocket session
     * @return flux of heartbeat messages
     */
    private Flux<WebSocketMessage> createHeartbeatStream(WebSocketSession session) {
        return Flux.interval(HEARTBEAT_INTERVAL)
            .map(tick -> session.textMessage(HEARTBEAT_MESSAGE))
            .doOnNext(message -> logger.trace("Sending heartbeat"));
    }
    
    /**
     * Broadcasts message to all active sessions.
     * 
     * @param message message to broadcast
     */
    public void broadcast(String message) {
        activeSessions.values().forEach(session -> {
            session.send(Mono.just(session.textMessage(message)))
                .subscribe(
                    v -> logger.debug("Broadcast sent to session: {}", session.getId()),
                    error -> logger.error("Failed to broadcast to session: {}", session.getId(), error)
                );
        });
    }
    
    /**
     * Broadcasts opportunity event to all active sessions.
     * 
     * @param event opportunity event
     */
    public void broadcastOpportunityEvent(Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            broadcast(json);
            logger.info("Opportunity event broadcasted: eventType={}", event.getClass().getSimpleName());
        } catch (Exception exception) {
            logger.error("Failed to broadcast opportunity event", exception);
        }
    }
    
    private void handleDisconnection(String sessionId) {
        activeSessions.remove(sessionId);
        logger.info("WebSocket connection closed: sessionId={}, activeConnections={}", 
            sessionId, activeSessions.size());
    }
    
    private void handleError(String sessionId, Throwable error) {
        logger.error("WebSocket error: sessionId={}, error={}", 
            sessionId, error.getMessage(), error);
        activeSessions.remove(sessionId);
    }
    
    /**
     * DTO for opportunity notifications sent via WebSocket.
     */
    private record OpportunityNotification(
        String type,
        Long id,
        String title,
        String category,
        String budgetAmount,
        String currencyCode,
        String deadline,
        String status
    ) {
        public static OpportunityNotification from(com.marketplace.opportunity.domain.model.Opportunity opportunity) {
            return new OpportunityNotification(
                "opportunity_published",
                opportunity.id().value(),
                opportunity.title(),
                opportunity.category(),
                opportunity.budget().amount().toString(),
                opportunity.budget().currencyCode(),
                opportunity.deadline().toString(),
                opportunity.status().name()
            );
        }
    }
}
