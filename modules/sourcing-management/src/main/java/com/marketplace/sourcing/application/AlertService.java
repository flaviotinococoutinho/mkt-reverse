package com.marketplace.sourcing.application;

import com.marketplace.sourcing.domain.model.OpportunityAlert;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for managing opportunity alerts.
 * Handles CRUD operations and matching of events to alerts.
 */
@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public AlertService(AlertRepository alertRepository, NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create a new alert for a user.
     */
    public String createAlert(CreateAlertRequest request, String userId, String tenantId) {
        log.info("Creating alert for user: {}", userId);

        var alert = OpportunityAlert.create(
                userId,
                tenantId,
                request.name(),
                request.eventTypes(),
                request.mccCategoryCodes(),
                request.minBudgetCents(),
                request.maxBudgetCents(),
                request.minQuantity(),
                request.notifyPush(),
                request.notifyEmail()
        );

        var saved = alertRepository.save(alert);
        log.info("Alert created: {}", saved.getId());

        return saved.getId();
    }

    /**
     * List all alerts for a user.
     */
    public List<OpportunityAlert> listUserAlerts(String userId) {
        log.debug("Listing alerts for user: {}", userId);
        return alertRepository.findByUserId(userId);
    }

    /**
     * Toggle an alert active/inactive.
     */
    public OpportunityAlert toggleAlert(String alertId, String userId) {
        log.info("Toggling alert: {} for user: {}", alertId, userId);

        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (!alert.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to modify this alert");
        }

        var updated = alert.toggle();
        return alertRepository.save(updated);
    }

    /**
     * Delete an alert.
     */
    public void deleteAlert(String alertId, String userId) {
        log.info("Deleting alert: {} for user: {}", alertId, userId);

        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        if (!alert.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this alert");
        }

        alertRepository.delete(alertId);
    }

    /**
     * Match a newly created event against all active alerts.
     * Called asynchronously when an event is published.
     */
    @Async
    @EventListener
    public void onEventCreated(SourcingEvent event) {
        log.info("Matching event {} against all alerts", event.getId());

        if (!event.isPublished()) {
            return; // Only match published events
        }

        // Find all active alerts that match this event
        var matcher = OpportunityAlert.createMatchingTemplate(
                event.getTenantId(),
                event.getEventType(),
                event.getMccCategoryCode(),
                event.getEstimatedBudgetCents(),
                event.getQuantityRequired()
        );

        var matchingAlerts = alertRepository.findMatching(matcher);

        log.info("Found {} matching alerts for event {}", matchingAlerts.size(), event.getId());

        // Send notifications to each user
        for (var alert : matchingAlerts) {
            if (!alert.getUserId().equals(event.getBuyerId())) {
                // Don't notify the event owner
                try {
                    notificationService.sendOpportunityMatch(alert.getUserId(), event);
                } catch (Exception e) {
                    log.error("Failed to send notification for alert {}: {}", 
                            alert.getId(), e.getMessage());
                }
            }
        }
    }

    // Request record
    public record CreateAlertRequest(
            String name,
            String[] eventTypes,
            Integer[] mccCategoryCodes,
            Long minBudgetCents,
            Long maxBudgetCents,
            Integer minQuantity,
            Boolean notifyPush,
            Boolean notifyEmail
    ) {}
}