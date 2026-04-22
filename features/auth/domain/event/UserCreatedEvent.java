package com.marketplace.user.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.user.domain.valueobject.UserType;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
 * - Implements shared DomainEvent contract
 */
@Getter
public class UserCreatedEvent implements DomainEvent {

    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final Instant occurredOn;
    private final Long aggregateVersion;
    private final String correlationId;
    
    private final String userId;
    private final String email;
    private final UserType userType;
    private final String displayName;
    private final EventMetadata metadata;

    public UserCreatedEvent(
        String userId, 
        String email, 
        UserType userType, 
        String displayName,
        Long aggregateVersion
    ) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = userId;
        this.aggregateType = "User";
        this.occurredOn = Instant.now();
        this.aggregateVersion = aggregateVersion != null ? aggregateVersion : 0L;
        this.correlationId = null;
        
        this.userId = userId;
        this.email = email;
        this.userType = userType;
        this.displayName = displayName;
        this.metadata = EventMetadata.of(Map.of(
            "userType", userType.name(),
            "email", email,
            "displayName", displayName
        ));
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
            "occurredOn", occurredOn.toString()
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
               "eventId=" + eventId +
               ", userId='" + userId + '\'' +
               ", email='" + email + '\'' +
               ", userType=" + userType +
               ", displayName='" + displayName + '\'' +
               ", occurredOn=" + occurredOn +
               '}';
    }
}
