package com.marketplace.user.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.user.domain.valueobject.PersonalInfo;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * User Profile Updated Domain Event
 * 
 * Fired when a user's profile information is updated.
 * Triggers profile synchronization, audit logging, and notification processes.
 * 
 * Design principles:
 * - Immutable event data
 * - Before/after state tracking
 * - Privacy-aware (no sensitive data)
 * - Implements shared DomainEvent contract
 */
@Getter
public class UserProfileUpdatedEvent implements DomainEvent {

    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final Instant occurredOn;
    private final Long aggregateVersion;
    private final String correlationId;
    
    private final String userId;
    private final PersonalInfo oldPersonalInfo;
    private final PersonalInfo newPersonalInfo;
    private final EventMetadata metadata;

    public UserProfileUpdatedEvent(
        String userId, 
        PersonalInfo oldPersonalInfo, 
        PersonalInfo newPersonalInfo,
        Long aggregateVersion
    ) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = userId;
        this.aggregateType = "User";
        this.occurredOn = Instant.now();
        this.aggregateVersion = aggregateVersion != null ? aggregateVersion : 0L;
        this.correlationId = null;
        
        this.userId = userId;
        this.oldPersonalInfo = oldPersonalInfo;
        this.newPersonalInfo = newPersonalInfo;
        this.metadata = EventMetadata.of(Map.of(
            "oldDisplayName", oldPersonalInfo != null ? oldPersonalInfo.getDisplayName() : "",
            "newDisplayName", newPersonalInfo != null ? newPersonalInfo.getDisplayName() : "",
            "nameChanged", hasNameChanged()
        ));
    }

    /**
     * Checks if the name was changed
     */
    public boolean hasNameChanged() {
        if (oldPersonalInfo == null || newPersonalInfo == null) {
            return true;
        }
        return !oldPersonalInfo.getFullName().equals(newPersonalInfo.getFullName());
    }

    /**
     * Checks if the display name was changed
     */
    public boolean hasDisplayNameChanged() {
        if (oldPersonalInfo == null || newPersonalInfo == null) {
            return true;
        }
        return !oldPersonalInfo.getDisplayName().equals(newPersonalInfo.getDisplayName());
    }

    /**
     * Gets the event payload for serialization
     */
    public Map<String, Object> getPayload() {
        return Map.of(
            "userId", userId,
            "changes", Map.of(
                "nameChanged", hasNameChanged(),
                "displayNameChanged", hasDisplayNameChanged(),
                "oldDisplayName", oldPersonalInfo != null ? oldPersonalInfo.getDisplayName() : "",
                "newDisplayName", newPersonalInfo != null ? newPersonalInfo.getDisplayName() : ""
            ),
            "occurredOn", occurredOn.toString()
        );
    }

    /**
     * Gets the routing key for message broker
     */
    public String getRoutingKey() {
        return "user.profile.updated";
    }

    /**
     * Gets the event priority (for message broker)
     */
    public int getPriority() {
        return 3; // Lower priority than creation events
    }

    /**
     * Checks if this event should trigger notifications
     */
    public boolean shouldTriggerNotifications() {
        return hasNameChanged(); // Only notify on significant changes
    }

    /**
     * Gets the notification channels for this event
     */
    public String[] getNotificationChannels() {
        return new String[]{"audit", "analytics"};
    }

    @Override
    public String toString() {
        return "UserProfileUpdatedEvent{" +
               "eventId=" + eventId +
               ", userId='" + userId + '\'' +
               ", nameChanged=" + hasNameChanged() +
               ", displayNameChanged=" + hasDisplayNameChanged() +
               ", occurredOn=" + occurredOn +
               '}';
    }
}
