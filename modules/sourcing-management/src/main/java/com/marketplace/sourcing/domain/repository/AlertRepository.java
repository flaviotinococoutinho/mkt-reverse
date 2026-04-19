package com.marketplace.sourcing.domain.repository;

import com.marketplace.sourcing.domain.model.OpportunityAlert;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Opportunity Alerts.
 * Provides persistence operations for user alerts.
 */
public interface AlertRepository {

    /**
     * Save an alert to the repository.
     */
    OpportunityAlert save(OpportunityAlert alert);

    /**
     * Find an alert by its ID.
     */
    Optional<OpportunityAlert> findById(String id);

    /**
     * Find all alerts for a specific user.
     */
    List<OpportunityAlert> findByUserId(String userId);

    /**
     * Find active alerts that match a given event.
     * Used by the matching service.
     */
    List<OpportunityAlert> findMatching(OpportunityAlert alert);

    /**
     * Delete an alert by its ID.
     */
    void delete(String id);

    /**
     * Check if an alert exists.
     */
    boolean existsById(String id);
}