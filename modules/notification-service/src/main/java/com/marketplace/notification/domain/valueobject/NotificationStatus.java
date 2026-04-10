package com.marketplace.notification.domain.valueobject;

/**
 * Notification lifecycle states.
 */
public enum NotificationStatus {
    QUEUED,
    SENT,
    DELIVERED,
    FAILED,
    CANCELLED,
    SUPPRESSED;

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == CANCELLED || this == SUPPRESSED;
    }
}
