package com.marketplace.notification.domain.event;

import com.marketplace.notification.domain.valueobject.NotificationChannel;
import com.marketplace.notification.domain.valueobject.NotificationPriority;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a notification is queued for delivery.
 */
@Getter
public class NotificationQueuedEvent implements DomainEvent {

    private final String notificationId;
    private final NotificationChannel channel;
    private final NotificationPriority priority;
    private final int recipients;
    private final EventMetadata metadata;

    public NotificationQueuedEvent(String notificationId, NotificationChannel channel, NotificationPriority priority, int recipients) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.priority = priority;
        this.recipients = recipients;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            notificationId,
            "Notification",
            Map.of(
                "channel", channel != null ? channel.name() : "UNKNOWN",
                "priority", priority != null ? priority.name() : "UNKNOWN",
                "recipients", recipients
            )
        );
    }

    @Override
    public String getEventType() {
        return "NotificationQueuedEvent";
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
        return notificationId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
