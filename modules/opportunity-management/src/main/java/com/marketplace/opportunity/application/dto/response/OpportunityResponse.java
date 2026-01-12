package com.marketplace.opportunity.application.dto.response;

import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for opportunity response.
 * 
 * Immutable record for sending opportunity data to clients.
 * Contains only necessary data for presentation layer.
 * 
 * Follows DTO pattern:
 * - Data transfer between layers
 * - No business logic
 * - Immutable
 * - Factory method from domain model
 */
public record OpportunityResponse(
    Long id,
    Long consumerId,
    Long tenantId,
    String title,
    String description,
    String category,
    BigDecimal budgetAmount,
    String currencyCode,
    Instant deadline,
    OpportunityStatus status,
    List<String> attachments,
    Map<String, Object> specifications,
    String templateKey,
    Instant createdAt,
    Instant updatedAt
) {
    
    /**
     * Creates response from domain model.
     * 
     * @param opportunity domain opportunity
     * @return opportunity response
     */
    public static OpportunityResponse from(Opportunity opportunity) {
        return new OpportunityResponse(
            opportunity.id().value(),
            opportunity.consumerId(),
            opportunity.tenantId(),
            opportunity.title(),
            opportunity.description(),
            opportunity.category(),
            opportunity.budget().amount(),
            opportunity.budget().currencyCode(),
            opportunity.deadline(),
            opportunity.status(),
            opportunity.attachments(),
            opportunity.specification().all(),
            opportunity.specification().templateKey(),
            opportunity.createdAt(),
            opportunity.updatedAt()
        );
    }
}
