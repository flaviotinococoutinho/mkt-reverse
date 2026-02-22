package com.marketplace.analytics.domain.event;

import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted when an analytics view is refreshed.
 */
@Getter
public class AnalyticsViewRefreshedEvent implements DomainEvent {

    private final String viewId;
    private final Instant occurredAt;
    private final int metricsCount;
    private final EventMetadata metadata;

    public AnalyticsViewRefreshedEvent(String viewId, Instant occurredAt, int metricsCount) {
        this.viewId = viewId;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.metricsCount = metricsCount;
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            this.occurredAt,
            viewId,
            "AnalyticsView",
            Map.of(
                "metricsCount", metricsCount
            )
        );
    }

    @Override
    public String getEventType() {
        return "AnalyticsViewRefreshedEvent";
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
