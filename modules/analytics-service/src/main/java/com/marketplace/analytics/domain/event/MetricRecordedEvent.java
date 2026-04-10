package com.marketplace.analytics.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when a metric is recorded to a view.
 */
@Getter
public class MetricRecordedEvent implements DomainEvent {

    private final String viewId;
    private final String metricName;
    private final double value;
    private final Instant occurredAt;
    private final EventMetadata metadata;

    public MetricRecordedEvent(String viewId, String metricName, double value, Instant occurredAt) {
        this.viewId = viewId;
        this.metricName = metricName;
        this.value = value;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            viewId,
            "AnalyticsView",
            Map.of(
                "metric", metricName,
                "value", value
            )
        );
    }

    @Override
    public String getEventType() {
        return "MetricRecordedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getAggregateId() {
        return viewId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }
}
