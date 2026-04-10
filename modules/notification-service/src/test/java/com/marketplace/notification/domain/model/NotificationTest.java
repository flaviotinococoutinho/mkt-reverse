package com.marketplace.notification.domain.model;

import com.marketplace.notification.domain.valueobject.NotificationChannel;
import com.marketplace.notification.domain.valueobject.NotificationPriority;
import com.marketplace.notification.domain.valueobject.NotificationStatus;
import com.marketplace.notification.domain.valueobject.Recipient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class NotificationTest {

    private Notification newQueuedNotification() {
        return Notification.enqueue(
            "tenant-1",
            "welcome",
            "USER_WELCOME",
            NotificationChannel.EMAIL,
            NotificationPriority.NORMAL,
            "Bem-vindo",
            "Olá, bem-vindo à plataforma",
            "{\"name\":\"João\"}",
            Set.of(Recipient.email("user-1", "user@example.com", null)),
            Set.of(NotificationChannel.EMAIL),
            Instant.now()
        );
    }

    @Test
    void shouldQueueNotificationWithRecipients() {
        Notification notification = newQueuedNotification();

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.QUEUED);
        assertThat(notification.getRecipients()).hasSize(1);
        assertThat(notification.getPrimaryChannel()).isEqualTo(NotificationChannel.EMAIL);
    }

    @Test
    void shouldMarkSentAndDelivered() {
        Notification notification = newQueuedNotification();

        notification.markSent(NotificationChannel.EMAIL, "accepted");
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getAttempts()).hasSize(1);

        notification.markDelivered();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(notification.getDeliveredAt()).isNotNull();
    }

    @Test
    void shouldFailAndRecordAttempt() {
        Notification notification = newQueuedNotification();

        notification.markFailed(NotificationChannel.EMAIL, "SMTP_500", "timeout");

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getAttempts()).hasSize(1);
        assertThat(notification.getAttempts().get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
    }
}
