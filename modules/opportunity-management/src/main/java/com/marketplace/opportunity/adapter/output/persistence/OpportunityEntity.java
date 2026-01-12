package com.marketplace.opportunity.adapter.output.persistence;

import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Database entity representation of Opportunity.
 * 
 * Immutable record for database persistence.
 * Separated from domain model to allow independent evolution.
 * 
 * Maps to 'opportunities' table in PostgreSQL.
 */
public record OpportunityEntity(
    Long id,
    Long consumerId,
    Long tenantId,
    String title,
    String description,
    String category,
    BigDecimal budgetAmount,
    String budgetCurrency,
    Instant deadline,
    OpportunityStatus status,
    List<String> attachments,
    Map<String, Object> specifications,
    String templateKey,
    Instant createdAt,
    Instant updatedAt
) {
}
