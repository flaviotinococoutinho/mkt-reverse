package com.marketplace.notification.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identifier for notification aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public static NotificationId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationId value cannot be null");
        }
        return new NotificationId(value);
    }

    public static NotificationId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NotificationId value cannot be blank");
        }
        return new NotificationId(UUID.fromString(value.trim()));
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationId that = (NotificationId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
