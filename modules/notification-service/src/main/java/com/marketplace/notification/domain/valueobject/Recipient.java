package com.marketplace.notification.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Locale;

/**
 * Recipient details and preferences for notifications.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Recipient implements Serializable {

    @Column(name = "recipient_id", length = 36)
    private String recipientId;

    @Column(name = "recipient_email", length = 255)
    private String email;

    @Column(name = "recipient_phone", length = 30)
    private String phone;

    @Column(name = "recipient_locale", length = 10)
    private String locale;

    @Column(name = "recipient_allow_email", nullable = false)
    private boolean allowEmail;

    @Column(name = "recipient_allow_sms", nullable = false)
    private boolean allowSms;

    @Column(name = "recipient_allow_push", nullable = false)
    private boolean allowPush;

    public static Recipient email(String recipientId, String email, Locale locale) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be blank");
        }
        return new Recipient(
            recipientId,
            email.trim().toLowerCase(),
            null,
            locale != null ? locale.toLanguageTag() : null,
            true,
            false,
            false
        );
    }

    public static Recipient sms(String recipientId, String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient phone cannot be blank");
        }
        return new Recipient(recipientId, null, phone.trim(), null, false, true, false);
    }

    public Recipient allowChannel(NotificationChannel channel, boolean allowed) {
        return switch (channel) {
            case EMAIL -> new Recipient(recipientId, email, phone, locale, allowed, allowSms, allowPush);
            case SMS, WHATSAPP -> new Recipient(recipientId, email, phone, locale, allowEmail, allowed, allowPush);
            case PUSH, SLACK -> new Recipient(recipientId, email, phone, locale, allowEmail, allowSms, allowed);
            case WEBHOOK -> this;
        };
    }
}
