package com.marketplace.notification.domain.valueobject;

/**
 * Supported notification delivery channels.
 */
public enum NotificationChannel {
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
