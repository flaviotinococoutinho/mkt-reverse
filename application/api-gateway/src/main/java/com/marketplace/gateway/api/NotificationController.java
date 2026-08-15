package com.marketplace.gateway.api;

import com.marketplace.notification.application.NotificationApplicationService;
import com.marketplace.notification.domain.model.Notification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * In-app notification feed of the authenticated user (MVP: polling).
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationApplicationService notifications;

    public NotificationController(NotificationApplicationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationView> list(Authentication authentication) {
        return notifications.listFor(authentication.getName()).stream()
            .map(NotificationView::from)
            .toList();
    }

    @PostMapping("/{id}/read")
    public NotificationView markRead(@PathVariable String id, Authentication authentication) {
        return NotificationView.from(notifications.markRead(id, authentication.getName()));
    }

    @ExceptionHandler(NotificationApplicationService.NotificationAccessDeniedException.class)
    public ProblemDetail accessDenied(NotificationApplicationService.NotificationAccessDeniedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problem.setTitle("Forbidden");
        return problem;
    }

    public record NotificationView(
        String id,
        String type,
        String priority,
        String subject,
        String body,
        String payload,
        Instant createdAt,
        boolean read
    ) {
        static NotificationView from(Notification notification) {
            return new NotificationView(
                notification.getId().asString(),
                notification.getNotificationType(),
                notification.getPriority() != null ? notification.getPriority().name() : null,
                notification.getSubject(),
                notification.getBody(),
                notification.getPayload(),
                notification.getQueuedAt(),
                notification.getDeliveredAt() != null
            );
        }
    }
}
