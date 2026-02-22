package com.marketplace.analytics.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Metric value captured for analytics dashboards.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricValue implements Serializable {

    @Column(name = "metric_name", nullable = false, length = 120)
    private String name;

    @Column(name = "metric_value", nullable = false)
    private double value;

    @Column(name = "metric_unit", length = 20)
    private String unit;

    public static MetricValue of(String name, double value, String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be blank");
        }
        return new MetricValue(name.trim(), value, unit != null ? unit.trim() : null);
    }

    public MetricValue increment(double delta) {
        return new MetricValue(name, value + delta, unit);
    }
}
