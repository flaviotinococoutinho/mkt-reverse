package com.marketplace.sourcing.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for Opportunity Alert.
 * Represents a user's saved search that notifies them of new matching opportunities.
 */
public class OpportunityAlert {

    private final String id;
    private final String userId;
    private final String tenantId;
    private final String name;
    private final List<String> eventTypes;
    private final List<Integer> mccCategoryCodes;
    private final Long minBudgetCents;
    private final Long maxBudgetCents;
    private final Integer minQuantity;
    private final boolean notifyPush;
    private final boolean notifyEmail;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    private OpportunityAlert(String id, String userId, String tenantId, String name,
            List<String> eventTypes, List<Integer> mccCategoryCodes,
            Long minBudgetCents, Long maxBudgetCents, Integer minQuantity,
            boolean notifyPush, boolean notifyEmail, boolean active,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.tenantId = tenantId;
        this.name = name;
        this.eventTypes = eventTypes;
        this.mccCategoryCodes = mccCategoryCodes;
        this.minBudgetCents = minBudgetCents;
        this.maxBudgetCents = maxBudgetCents;
        this.minQuantity = minQuantity;
        this.notifyPush = notifyPush;
        this.notifyEmail = notifyEmail;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory methods
    public static OpportunityAlert create(String userId, String tenantId, String name,
            List<String> eventTypes, List<Integer> mccCategoryCodes,
            Long minBudgetCents, Long maxBudgetCents, Integer minQuantity,
            boolean notifyPush, boolean notifyEmail) {
        
        var now = Instant.now();
        return new OpportunityAlert(
                UUID.randomUUID().toString(),
                userId,
                tenantId,
                name,
                eventTypes,
                mccCategoryCodes,
                minBudgetCents,
                maxBudgetCents,
                minQuantity,
                notifyPush,
                notifyEmail,
                true,  // Active by default
                now,
                now
        );
    }

    // Business methods
    public OpportunityAlert toggle() {
        return new OpportunityAlert(
                id, userId, tenantId, name, eventTypes, mccCategoryCodes,
                minBudgetCents, maxBudgetCents, minQuantity,
                notifyPush, notifyEmail, !active,
                createdAt, Instant.now()
        );
    }

    public OpportunityAlert update(List<String> eventTypes, List<Integer> mccCategoryCodes,
            Long minBudgetCents, Long maxBudgetCents, Integer minQuantity) {
        return new OpportunityAlert(
                id, userId, tenantId, this.name, eventTypes, mccCategoryCodes,
                minBudgetCents, maxBudgetCents, minQuantity,
                notifyPush, notifyEmail, active,
                createdAt, Instant.now()
        );
    }

    public boolean matches(SourcingEvent event) {
        // Check if event matches alert criteria
        if (!active) return false;
        
        // Check event type
        if (eventTypes != null && !eventTypes.isEmpty()) {
            if (!eventTypes.contains(event.getEventType())) {
                return false;
            }
        }
        
        // Check MCC category
        if (mccCategoryCodes != null && !mccCategoryCodes.isEmpty()) {
            if (event.getMccCategoryCode() == null || 
                !mccCategoryCodes.contains(event.getMccCategoryCode())) {
                return false;
            }
        }
        
        // Check budget range
        if (minBudgetCents != null && event.getEstimatedBudgetCents() < minBudgetCents) {
            return false;
        }
        if (maxBudgetCents != null && event.getEstimatedBudgetCents() > maxBudgetCents) {
            return false;
        }
        
        // Check quantity
        if (minQuantity != null && event.getQuantityRequired() < minQuantity) {
            return false;
        }
        
        return true;
    }

    // Getters (following Object Calisthenics - no mutable state)
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public List<String> getEventTypes() { return eventTypes; }
    public List<Integer> getMccCategoryCodes() { return mccCategoryCodes; }
    public Long getMinBudgetCents() { return minBudgetCents; }
    public Long getMaxBudgetCents() { return maxBudgetCents; }
    public Integer getMinQuantity() { return minQuantity; }
    public boolean isNotifyPush() { return notifyPush; }
    public boolean isNotifyEmail() { return notifyEmail; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}