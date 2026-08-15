package com.marketplace.notification.domain.valueobject;

/**
 * Supported notification delivery channels.
 */
public enum NotificationChannel {
    /** In-app feed, consumed by the web-app via polling (MVP channel). */
    IN_APP,
    EMAIL,
    SMS,
    PUSH,
    WHATSAPP,
    SLACK,
    WEBHOOK;

    public boolean isRealTime() {
        return this == SMS || this == PUSH || this == WHATSAPP || this == SLACK;
    }
}
