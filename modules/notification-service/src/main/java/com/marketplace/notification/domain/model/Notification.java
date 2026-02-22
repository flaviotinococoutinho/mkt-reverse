package com.marketplace.notification.domain.model;

import com.marketplace.notification.domain.event.NotificationFailedEvent;
import com.marketplace.notification.domain.event.NotificationQueuedEvent;
import com.marketplace.notification.domain.event.NotificationSentEvent;
import com.marketplace.notification.domain.valueobject.DeliveryAttempt;
import com.marketplace.notification.domain.valueobject.NotificationChannel;
import com.marketplace.notification.domain.valueobject.NotificationId;
import com.marketplace.notification.domain.valueobject.NotificationPriority;
import com.marketplace.notification.domain.valueobject.NotificationStatus;
import com.marketplace.notification.domain.valueobject.Recipient;
import com.marketplace.shared.domain.model.AggregateRoot;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Notification aggregate representing an omni-channel message dispatch.
 */
@Entity
@Table(name = "NOT_NOTIFICATIONS", indexes = {
    @Index(name = "idx_not_tenant", columnList = "tenant_id"),
    @Index(name = "idx_not_status", columnList = "status"),
    @Index(name = "idx_not_template", columnList = "template_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends AggregateRoot<NotificationId> {

    @EmbeddedId
    private NotificationId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "template_code", length = 100)
    private String templateCode;

    @Column(name = "notification_type", length = 100)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_channel", nullable = false, length = 20)
    private NotificationChannel primaryChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "NOT_NOTIFICATION_RECIPIENTS", joinColumns = @JoinColumn(name = "notification_id"))
    private Set<Recipient> recipients = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "NOT_NOTIFICATION_CHANNELS", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "channel", length = 20)
    private Set<NotificationChannel> channels = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "NOT_NOTIFICATION_ATTEMPTS", joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "sequence")
    private List<DeliveryAttempt> attempts = new ArrayList<>();

    @Column(name = "queued_at", nullable = false)
    private Instant queuedAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 300)
    private String cancellationReason;

    private Notification(
        NotificationId id,
        String tenantId,
        String templateCode,
        String notificationType,
        NotificationChannel primaryChannel,
        NotificationPriority priority,
        String subject,
        String body,
        String payload,
        Set<Recipient> recipients,
        Set<NotificationChannel> channels,
        Instant scheduledAt
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.templateCode = templateCode;
        this.notificationType = notificationType;
        this.primaryChannel = primaryChannel;
        this.priority = priority;
        this.subject = subject;
        this.body = body;
        this.payload = payload;
        if (recipients != null) {
            this.recipients.addAll(recipients);
        }
        if (channels != null && !channels.isEmpty()) {
            this.channels.addAll(channels);
        } else {
            this.channels.add(primaryChannel);
        }
        this.scheduledAt = scheduledAt;
        this.status = NotificationStatus.QUEUED;
        this.queuedAt = Instant.now();
    }

    public static Notification enqueue(
        String tenantId,
        String templateCode,
        String notificationType,
        NotificationChannel primaryChannel,
        NotificationPriority priority,
        String subject,
        String body,
        String payload,
        Set<Recipient> recipients,
        Set<NotificationChannel> channels,
        Instant scheduledAt
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(primaryChannel, "primaryChannel is required");
        Objects.requireNonNull(priority, "priority is required");
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("Notification must have at least one recipient");
        }

        Notification notification = new Notification(
            NotificationId.generate(),
            tenantId.trim(),
            templateCode != null ? templateCode.trim() : null,
            notificationType != null ? notificationType.trim() : null,
            primaryChannel,
            priority,
            subject != null ? subject.trim() : null,
            body,
            payload,
            recipients,
            channels,
            scheduledAt
        );

        notification.addDomainEvent(new NotificationQueuedEvent(
            notification.getId().asString(),
            notification.getPrimaryChannel(),
            notification.getPriority(),
            notification.getRecipients().size()
        ));
        notification.markAsCreated();
        return notification;
    }

    public void markSent(NotificationChannel channel, String providerResponse) {
        if (status.isTerminal()) {
            return;
        }
        DeliveryAttempt attempt = DeliveryAttempt.of(attempts.size() + 1, channel, NotificationStatus.SENT, providerResponse, null);
        attempts.add(attempt);
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        addDomainEvent(new NotificationSentEvent(id.asString(), channel, sentAt));
        markAsUpdated();
    }

    public void markDelivered() {
        if (status.isTerminal()) {
            return;
        }
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        markAsUpdated();
    }

    public void markFailed(NotificationChannel channel, String errorCode, String response) {
        DeliveryAttempt attempt = DeliveryAttempt.of(attempts.size() + 1, channel, NotificationStatus.FAILED, response, errorCode);
        attempts.add(attempt);
        this.status = NotificationStatus.FAILED;
        addDomainEvent(new NotificationFailedEvent(id.asString(), channel, errorCode, Instant.now()));
        markAsUpdated();
    }

    public void cancel(String reason) {
        if (status.isTerminal()) {
            return;
        }
        this.status = NotificationStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.cancellationReason = reason;
        markAsUpdated();
    }

    public void suppress() {
        if (status.isTerminal()) {
            return;
        }
        this.status = NotificationStatus.SUPPRESSED;
        this.cancelledAt = Instant.now();
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("Notification id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (primaryChannel == null) {
            throw new IllegalStateException("primaryChannel cannot be null");
        }
        if (priority == null) {
            throw new IllegalStateException("priority cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
        if (recipients.isEmpty()) {
            throw new IllegalStateException("Notification must have recipients");
        }
    }

    public Set<Recipient> getRecipients() {
        return Collections.unmodifiableSet(recipients);
    }

    public Set<NotificationChannel> getChannels() {
        return Collections.unmodifiableSet(channels);
    }

    public List<DeliveryAttempt> getAttempts() {
        return Collections.unmodifiableList(attempts);
    }
}
