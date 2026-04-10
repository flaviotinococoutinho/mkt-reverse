package com.marketplace.notification.domain.repository;

import com.marketplace.notification.domain.model.Notification;
import com.marketplace.notification.domain.valueobject.NotificationId;
import com.marketplace.notification.domain.valueobject.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for notifications.
 */
public interface NotificationRepository {

    Optional<Notification> findById(NotificationId id);

    Optional<Notification> findById(String id);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findScheduledBefore(Instant reference);

    Notification save(Notification notification);

    void delete(Notification notification);
}
