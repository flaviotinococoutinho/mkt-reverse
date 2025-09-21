package com.marketplace.user.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.user.domain.valueobject.UserType;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * User Created Domain Event
 * 
 * Fired when a new user is created in the system.
 * Triggers downstream processes like welcome emails, analytics tracking, etc.
 * 
 * Design principles:
 * - Immutable event data
 * - Rich context information
 * - Backward compatible structure
 */
@Getter
public class UserCreatedEvent implements DomainEvent {

    private final String userId;
    private final String email;
    private final UserType userType;
    private final String displayName;
    private final EventMetadata metadata;

    public UserCreatedEvent(String userId, String email, UserType userType, String displayName) {
        this.userId = userId;
        this.email = email;
        this.userType = userType;
        this.displayName = displayName;
        this.metadata = EventMetadata.create(
            "UserCreatedEvent",
            "1.0",
            Instant.now(),
            userId,
            Map.of(
                "userType", userType.name(),
                "email", email,
                "displayName", displayName
            )
        );
    }

    @Override
    public String getEventType() {
        return "UserCreatedEvent";
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
     * Gets the event payload for serialization
     */
    public Map<String, Object> getPayload() {
        return Map.of(
            "userId", userId,
            "email", email,
            "userType", userType.name(),
            "displayName", displayName,
            "occurredAt", getOccurredAt().toString()
        );
    }

    /**
     * Gets the routing key for message broker
     */
    public String getRoutingKey() {
        return "user.created." + userType.name().toLowerCase();
    }

    /**
     * Gets the event priority (for message broker)
     */
    public int getPriority() {
        return 5; // Normal priority
    }

    /**
     * Checks if this event should trigger immediate notifications
     */
    public boolean shouldTriggerNotifications() {
        return true;
    }

    /**
     * Gets the notification channels for this event
     */
    public String[] getNotificationChannels() {
        return new String[]{"email", "analytics", "audit"};
    }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
               "userId='" + userId + '\'' +
               ", email='" + email + '\'' +
               ", userType=" + userType +
               ", displayName='" + displayName + '\'' +
               ", occurredAt=" + getOccurredAt() +
               '}';
    }
}

