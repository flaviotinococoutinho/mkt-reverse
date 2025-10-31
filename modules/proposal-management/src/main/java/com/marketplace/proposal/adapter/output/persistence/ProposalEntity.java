package com.marketplace.proposal.adapter.output.persistence;

import com.marketplace.proposal.domain.valueobject.ProposalStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity record for database representation of Proposal.
 * 
 * Immutable record matching database schema.
 * Separate from domain model to avoid coupling.
 */
public record ProposalEntity(
    Long id,
    Long opportunityId,
    Long companyId,
    Long tenantId,
    BigDecimal priceAmount,
    String priceCurrency,
    Integer deliveryDays,
    Integer deliveryHours,
    Instant estimatedDeliveryDate,
    String description,
    ProposalStatus status,
    String attachmentsJson,
    String specificationsJson,
    Instant createdAt,
    Instant updatedAt,
    Instant submittedAt,
    Instant acceptedAt
) {}
