package com.marketplace.analytics.domain.valueobject;

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
 * Identifier for analytics dashboard/view aggregates.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnalyticsViewId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static AnalyticsViewId generate() {
        return new AnalyticsViewId(UUID.randomUUID());
    }

    public static AnalyticsViewId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AnalyticsViewId value cannot be null");
        }
        return new AnalyticsViewId(value);
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyticsViewId that = (AnalyticsViewId) o;
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
