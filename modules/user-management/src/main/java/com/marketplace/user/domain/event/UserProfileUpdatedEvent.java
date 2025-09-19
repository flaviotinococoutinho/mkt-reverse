package com.marketplace.user.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import com.marketplace.user.domain.valueobject.PersonalInfo;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

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
 */
@Getter
public class UserProfileUpdatedEvent implements DomainEvent {

    private final String userId;
    private final PersonalInfo oldPersonalInfo;
    private final PersonalInfo newPersonalInfo;
    private final EventMetadata metadata;

    public UserProfileUpdatedEvent(String userId, PersonalInfo oldPersonalInfo, PersonalInfo newPersonalInfo) {
        this.userId = userId;
        this.oldPersonalInfo = oldPersonalInfo;
        this.newPersonalInfo = newPersonalInfo;
        this.metadata = EventMetadata.create(
            "UserProfileUpdatedEvent",
            "1.0",
            Instant.now(),
            userId,
            Map.of(
                "oldDisplayName", oldPersonalInfo != null ? oldPersonalInfo.getDisplayName() : "",
                "newDisplayName", newPersonalInfo != null ? newPersonalInfo.getDisplayName() : "",
                "nameChanged", hasNameChanged()
            )
        );
    }

    @Override
    public String getEventType() {
        return "UserProfileUpdatedEvent";
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
            "occurredAt", getOccurredAt().toString()
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
               "userId='" + userId + '\'' +
               ", nameChanged=" + hasNameChanged() +
               ", displayNameChanged=" + hasDisplayNameChanged() +
               ", occurredAt=" + getOccurredAt() +
               '}';
    }
}

