package com.marketplace.notification.infrastructure.persistence;

import com.marketplace.notification.domain.model.Notification;
import com.marketplace.notification.domain.valueobject.NotificationId;
import com.marketplace.notification.domain.valueobject.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataNotificationJpaRepository extends JpaRepository<Notification, NotificationId> {

    List<Notification> findByStatus(NotificationStatus status);

    @Query("select n from Notification n where n.scheduledAt is not null and n.scheduledAt < :ref")
    List<Notification> findScheduledBefore(@Param("ref") Instant reference);

    @Query("select n from Notification n join n.recipients r where r.recipientId = :recipientId order by n.queuedAt desc")
    List<Notification> findByRecipientId(@Param("recipientId") String recipientId);
}
