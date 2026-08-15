package com.marketplace.notification.infrastructure.persistence;

import com.marketplace.notification.domain.model.Notification;
import com.marketplace.notification.domain.repository.NotificationRepository;
import com.marketplace.notification.domain.valueobject.NotificationId;
import com.marketplace.notification.domain.valueobject.NotificationStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaNotificationRepository implements NotificationRepository {

    private final SpringDataNotificationJpaRepository jpa;

    public JpaNotificationRepository(SpringDataNotificationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Notification> findById(String id) {
        return jpa.findById(NotificationId.of(id));
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return jpa.findByStatus(status);
    }

    @Override
    public List<Notification> findScheduledBefore(Instant reference) {
        return jpa.findScheduledBefore(reference);
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return jpa.findByRecipientId(recipientId);
    }

    @Override
    public Notification save(Notification notification) {
        return jpa.save(notification);
    }

    @Override
    public void delete(Notification notification) {
        jpa.delete(notification);
    }
}
