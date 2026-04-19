package com.marketplace.sourcing.application;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notification service for sending alerts.
 * In production, this would integrate with WebSocket, FCM, etc.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send notification when an opportunity matches an alert.
     */
    public void sendOpportunityMatch(String userId, SourcingEvent event) {
        log.info("Sending opportunity match notification to user: {} for event: {}", 
                userId, event.getTitle());
        
        // TODO: In production, implement:
        // 1. WebSocket for real-time
        // 2. Push notification (FCM)
        // 3. Email (SendGrid/SES)
        
        // For now, just log the notification
    }

    /**
     * Send notification when proposal is accepted.
     */
    public void sendProposalAccepted(String supplierId, String eventTitle) {
        log.info("Sending proposal accepted notification to supplier: {} for event: {}",
                supplierId, eventTitle);
    }

    /**
     * Send notification when new message in negotiation.
     */
    public void sendNewMessage(String userId, String message) {
        log.info("Sending new message notification to user: {}", userId);
    }
}