package com.marketplace.analytics.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Time range used for analytics aggregation windows.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeRange implements Serializable {

    @Column(name = "range_start", nullable = false)
    private Instant start;

    @Column(name = "range_end", nullable = false)
    private Instant end;

    public static TimeRange of(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Time range bounds cannot be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Time range end must be after start");
        }
        return new TimeRange(start, end);
    }

    public boolean contains(Instant instant) {
        return !instant.isBefore(start) && instant.isBefore(end);
    }
}
