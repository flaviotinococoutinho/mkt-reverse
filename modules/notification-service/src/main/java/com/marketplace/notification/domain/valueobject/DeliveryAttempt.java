package com.marketplace.notification.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Delivery attempt history for notifications.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeliveryAttempt implements Serializable {

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "attempt_at", nullable = false)
    private Instant attemptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "attempt_response", length = 500)
    private String providerResponse;

    @Column(name = "attempt_error_code", length = 50)
    private String errorCode;

    public static DeliveryAttempt of(int attemptNumber, NotificationChannel channel, NotificationStatus status, String response, String errorCode) {
        return new DeliveryAttempt(attemptNumber, channel, Instant.now(), status, response, errorCode);
    }
}
