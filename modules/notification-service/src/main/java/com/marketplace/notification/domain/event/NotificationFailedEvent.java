package com.marketplace.notification.domain.event;

import com.marketplace.notification.domain.valueobject.NotificationChannel;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when notification delivery fails.
 */
@Getter
public class NotificationFailedEvent implements DomainEvent {

    private final String notificationId;
    private final NotificationChannel channel;
    private final String errorCode;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public NotificationFailedEvent(String notificationId, NotificationChannel channel, String errorCode, Instant occurredAt) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.errorCode = errorCode;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            notificationId,
            "Notification",
            Map.of(
                "channel", channel != null ? channel.name() : "UNKNOWN",
                "errorCode", errorCode != null ? errorCode : ""
            )
        );
    }

    @Override
    public String getEventType() {
        return "NotificationFailedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
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
