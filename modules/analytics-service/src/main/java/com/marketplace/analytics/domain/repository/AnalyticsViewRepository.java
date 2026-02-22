package com.marketplace.analytics.domain.repository;

import com.marketplace.analytics.domain.model.AnalyticsView;
import com.marketplace.analytics.domain.valueobject.AnalyticsViewId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for analytics views.
 */
public interface AnalyticsViewRepository {

    Optional<AnalyticsView> findById(AnalyticsViewId id);

    Optional<AnalyticsView> findById(String id);

    List<AnalyticsView> findByTenant(String tenantId);

    List<AnalyticsView> findDueForRefresh(Instant reference);

    AnalyticsView save(AnalyticsView view);

    void delete(AnalyticsView view);
}
