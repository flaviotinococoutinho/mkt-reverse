package com.marketplace.user.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.user.domain.valueobject.UserStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * User Status Changed Domain Event
 * 
 * Fired when a user's status changes (active, suspended, banned, etc.).
 * Triggers access control updates, notifications, and audit logging.
 * 
 * Design principles:
 * - Immutable event data
 * - State transition tracking
 * - Audit trail support
 * - Security-aware notifications
 */
@Getter
public class UserStatusChangedEvent implements DomainEvent {

    private final String userId;
    private final UserStatus oldStatus;
    private final UserStatus newStatus;
    private final String reason;
    private final EventMetadata metadata;

    public UserStatusChangedEvent(String userId, UserStatus oldStatus, UserStatus newStatus) {
        this(userId, oldStatus, newStatus, null);
    }

    public UserStatusChangedEvent(String userId, UserStatus oldStatus, UserStatus newStatus, String reason) {
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.metadata = EventMetadata.create(
            "UserStatusChangedEvent",
            "1.0",
            Instant.now(),
            userId,
            Map.of(
                "oldStatus", oldStatus.name(),
                "newStatus", newStatus.name(),
                "reason", reason != null ? reason : "",
                "isRestriction", isRestriction(),
                "isActivation", isActivation(),
                "severity", getSeverity()
            )
        );
    }

    @Override
    public String getEventType() {
        return "UserStatusChangedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return metadata.getOccurredAt();
    }

    @Override
    public String getAggregateId() {
        return userId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }

    /**
     * Checks if this is a restriction (user being limited)
     */
    public boolean isRestriction() {
        return oldStatus.allowsFeatureAccess() && !newStatus.allowsFeatureAccess();
    }

    /**
     * Checks if this is an activation (user gaining access)
     */
    public boolean isActivation() {
        return !oldStatus.allowsFeatureAccess() && newStatus.allowsFeatureAccess();
    }

    /**
     * Checks if this is a suspension
     */
    public boolean isSuspension() {
        return newStatus == UserStatus.SUSPENDED;
    }

    /**
     * Checks if this is a ban
     */
    public boolean isBan() {
        return newStatus == UserStatus.BANNED;
    }

    /**
     * Checks if this is a reactivation
     */
    public boolean isReactivation() {
        return newStatus == UserStatus.ACTIVE && oldStatus != UserStatus.PENDING_VERIFICATION;
    }

    /**
     * Gets the severity level of this status change
     */
    public String getSeverity() {
        if (isBan()) {
            return "CRITICAL";
        } else if (isSuspension()) {
            return "HIGH";
        } else if (isRestriction()) {
            return "MEDIUM";
        } else if (isActivation()) {
            return "LOW";
        } else {
            return "INFO";
        }
    }

    /**
     * Gets the event payload for serialization
     */
    public Map<String, Object> getPayload() {
        return Map.of(
            "userId", userId,
            "statusChange", Map.of(
                "from", oldStatus.name(),
                "to", newStatus.name(),
                "reason", reason != null ? reason : "",
                "isRestriction", isRestriction(),
                "isActivation", isActivation(),
                "severity", getSeverity()
            ),
            "occurredAt", getOccurredAt().toString()
        );
    }

    /**
     * Gets the routing key for message broker
     */
    public String getRoutingKey() {
        return "user.status.changed." + newStatus.name().toLowerCase();
    }

    /**
     * Gets the event priority (for message broker)
     */
    public int getPriority() {
        return switch (getSeverity()) {
            case "CRITICAL" -> 10; // Highest priority
            case "HIGH" -> 8;
            case "MEDIUM" -> 5;
            case "LOW" -> 3;
            default -> 1;
        };
    }

    /**
     * Checks if this event should trigger immediate notifications
     */
    public boolean shouldTriggerNotifications() {
        return isRestriction() || isActivation();
    }

    /**
     * Gets the notification channels for this event
     */
    public String[] getNotificationChannels() {
        if (isBan() || isSuspension()) {
            return new String[]{"email", "sms", "push", "audit", "security", "admin"};
        } else if (isActivation()) {
            return new String[]{"email", "push", "audit", "analytics"};
        } else {
            return new String[]{"audit", "analytics"};
        }
    }

    /**
     * Gets the notification template for this event
     */
    public String getNotificationTemplate() {
        return switch (newStatus) {
            case ACTIVE -> "user_activated";
            case SUSPENDED -> "user_suspended";
            case BANNED -> "user_banned";
            case INACTIVE -> "user_deactivated";
            case LOCKED -> "user_locked";
            default -> "user_status_changed";
        };
    }

    /**
     * Checks if this event requires admin attention
     */
    public boolean requiresAdminAttention() {
        return isBan() || isSuspension() || "CRITICAL".equals(getSeverity());
    }

    /**
     * Gets the admin alert level
     */
    public String getAdminAlertLevel() {
        return switch (getSeverity()) {
            case "CRITICAL" -> "URGENT";
            case "HIGH" -> "HIGH";
            case "MEDIUM" -> "NORMAL";
            default -> "INFO";
        };
    }

    @Override
    public String toString() {
        return "UserStatusChangedEvent{" +
               "userId='" + userId + '\'' +
               ", oldStatus=" + oldStatus +
               ", newStatus=" + newStatus +
               ", reason='" + reason + '\'' +
               ", severity='" + getSeverity() + '\'' +
               ", occurredAt=" + getOccurredAt() +
               '}';
    }
}

