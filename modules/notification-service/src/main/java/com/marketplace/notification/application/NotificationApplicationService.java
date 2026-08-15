package com.marketplace.notification.application;

import com.marketplace.notification.domain.model.Notification;
import com.marketplace.notification.domain.repository.NotificationRepository;
import com.marketplace.notification.domain.valueobject.NotificationChannel;
import com.marketplace.notification.domain.valueobject.NotificationPriority;
import com.marketplace.notification.domain.valueobject.Recipient;
import com.marketplace.shared.events.DomainEventPublisher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Application service of the notification context.
 *
 * MVP scope: the IN_APP channel, consumed by the web-app via polling on
 * GET /api/v1/notifications. Real-time channels (WebSocket/push/e-mail) plug
 * into the same aggregate later — the model already carries channels,
 * priorities and delivery attempts.
 */
@Service
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public NotificationApplicationService(
        NotificationRepository notificationRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /** Creates an in-app notification for one user, already marked as sent. */
    @Transactional
    public Notification notifyInApp(
        String tenantId,
        String recipientUserId,
        String notificationType,
        NotificationPriority priority,
        String subject,
        String body,
        String payload
    ) {
        Notification notification = Notification.enqueue(
            tenantId,
            null,
            notificationType,
            NotificationChannel.IN_APP,
            priority != null ? priority : NotificationPriority.NORMAL,
            subject,
            body,
            payload,
            Set.of(Recipient.inApp(recipientUserId)),
            Set.of(NotificationChannel.IN_APP),
            null
        );
        // The in-app feed is pulled by the client; persisting the row IS the send.
        notification.markSent(NotificationChannel.IN_APP, "in-app feed");
        Notification saved = notificationRepository.save(notification);
        domainEventPublisher.publishAll(saved.getDomainEvents());
        saved.clearDomainEvents();
        return saved;
    }

    /** Newest-first feed of the authenticated user. */
    public List<Notification> listFor(String userId) {
        return notificationRepository.findByRecipientId(userId);
    }

    /** Marks a notification as read ("delivered") — only by its recipient. */
    @Transactional
    public Notification markRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        boolean isRecipient = notification.getRecipients().stream()
            .anyMatch(r -> userId != null && userId.equals(r.getRecipientId()));
        if (!isRecipient) {
            throw new NotificationAccessDeniedException("Notification belongs to another user");
        }
        notification.markDelivered();
        Notification saved = notificationRepository.save(notification);
        domainEventPublisher.publishAll(saved.getDomainEvents());
        saved.clearDomainEvents();
        return saved;
    }

    /** Mapped to 403 at the API layer. */
    public static class NotificationAccessDeniedException extends RuntimeException {
        public NotificationAccessDeniedException(String message) {
            super(message);
        }
    }
}
