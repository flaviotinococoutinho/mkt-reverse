package com.marketplace.analytics.domain.model;

import com.marketplace.analytics.domain.event.AnalyticsViewRefreshedEvent;
import com.marketplace.analytics.domain.event.MetricRecordedEvent;
import com.marketplace.analytics.domain.valueobject.AnalyticsViewId;
import com.marketplace.analytics.domain.valueobject.MetricValue;
import com.marketplace.analytics.domain.valueobject.TimeRange;
import com.marketplace.shared.domain.model.AggregateRoot;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Analytics view aggregate representing a curated dashboard with metrics.
 */
@Entity
@Table(name = "ANA_ANALYTICS_VIEWS", indexes = {
    @Index(name = "idx_ana_tenant", columnList = "tenant_id"),
    @Index(name = "idx_ana_owner", columnList = "owner_id"),
    @Index(name = "idx_ana_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalyticsView extends AggregateRoot<AnalyticsViewId> {

    @EmbeddedId
    private AnalyticsViewId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ANA_VIEW_DIMENSIONS", joinColumns = @JoinColumn(name = "view_id"))
    @Column(name = "dimension", length = 60)
    private Set<String> dimensions = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ANA_VIEW_TAGS", joinColumns = @JoinColumn(name = "view_id"))
    @Column(name = "tag", length = 40)
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "ANA_VIEW_METRICS", joinColumns = @JoinColumn(name = "view_id"))
    @OrderColumn(name = "sequence")
    private List<MetricValue> metrics = new ArrayList<>();

    @Embedded
    private TimeRange timeRange;

    @Column(name = "real_time", nullable = false)
    private boolean realTime;

    @Column(name = "refresh_interval_minutes")
    private Integer refreshIntervalMinutes;

    @Column(name = "last_refreshed_at")
    private Instant lastRefreshedAt;

    @Column(name = "next_refresh_at")
    private Instant nextRefreshAt;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    @Column(name = "query_definition", columnDefinition = "TEXT")
    private String queryDefinition;

    private AnalyticsView(
        AnalyticsViewId id,
        String tenantId,
        String name,
        String description,
        Set<String> dimensions,
        Set<String> tags,
        TimeRange timeRange,
        boolean realTime,
        Integer refreshIntervalMinutes,
        String ownerId,
        String queryDefinition
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
        if (dimensions != null) {
            this.dimensions.addAll(dimensions);
        }
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.timeRange = timeRange;
        this.realTime = realTime;
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.ownerId = ownerId;
        this.queryDefinition = queryDefinition;
        this.lastRefreshedAt = Instant.now();
        this.nextRefreshAt = calculateNextRefresh();
    }

    public static AnalyticsView create(
        String tenantId,
        String name,
        String description,
        Set<String> dimensions,
        Set<String> tags,
        TimeRange timeRange,
        boolean realTime,
        Integer refreshIntervalMinutes,
        String ownerId,
        String queryDefinition
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(timeRange, "timeRange is required");

        AnalyticsView view = new AnalyticsView(
            AnalyticsViewId.generate(),
            tenantId.trim(),
            name.trim(),
            description != null ? description.trim() : null,
            dimensions,
            tags,
            timeRange,
            realTime,
            refreshIntervalMinutes,
            ownerId,
            queryDefinition
        );
        view.markAsCreated();
        return view;
    }

    public void recordMetric(MetricValue metric) {
        metrics.add(metric);
        addDomainEvent(new MetricRecordedEvent(id.asString(), metric.getName(), metric.getValue(), Instant.now()));
        markAsUpdated();
    }

    public void refresh(Instant reference) {
        this.lastRefreshedAt = reference != null ? reference : Instant.now();
        this.nextRefreshAt = calculateNextRefresh();
        addDomainEvent(new AnalyticsViewRefreshedEvent(id.asString(), lastRefreshedAt, metrics.size()));
        markAsUpdated();
    }

    private Instant calculateNextRefresh() {
        if (!realTime && (refreshIntervalMinutes == null || refreshIntervalMinutes <= 0)) {
            return null;
        }
        int interval = refreshIntervalMinutes != null ? refreshIntervalMinutes : 5;
        return lastRefreshedAt.plusSeconds(interval * 60L);
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("AnalyticsView id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("name cannot be null");
        }
        if (timeRange == null) {
            throw new IllegalStateException("timeRange cannot be null");
        }
    }

    public List<MetricValue> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    public Set<String> getDimensions() {
        return Collections.unmodifiableSet(dimensions);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }
}
