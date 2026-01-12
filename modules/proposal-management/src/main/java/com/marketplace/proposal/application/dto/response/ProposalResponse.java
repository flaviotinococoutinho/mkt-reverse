package com.marketplace.proposal.application.dto.response;

import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.ProposalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for proposal response.
 * 
 * Immutable record for REST API response.
 * Contains all proposal information.
 */
public record ProposalResponse(
    Long proposalId,
    Long opportunityId,
    Long companyId,
    Long tenantId,
    BigDecimal priceAmount,
    String priceCurrency,
    Integer deliveryDays,
    Integer deliveryHours,
    String deliveryTimeDescription,
    String description,
    ProposalStatus status,
    List<String> attachments,
    Map<String, Object> specifications,
    Instant createdAt,
    Instant updatedAt,
    Instant submittedAt,
    Instant acceptedAt
) {
    
    /**
     * Creates response from domain model.
     * 
     * @param proposal domain proposal
     * @return proposal response
     */
    public static ProposalResponse fromDomain(Proposal proposal) {
        return new ProposalResponse(
            proposal.getProposalId().value(),
            proposal.getOpportunityId(),
            proposal.getCompanyId(),
            proposal.getTenantId(),
            proposal.getPrice().amount(),
            proposal.getPrice().currency(),
            proposal.getDeliveryTime().days(),
            proposal.getDeliveryTime().hours(),
            proposal.getDeliveryTime().toString(),
            proposal.getDescription(),
            proposal.getStatus(),
            proposal.getAttachments(),
            proposal.getSpecifications(),
            proposal.getCreatedAt(),
            proposal.getUpdatedAt(),
            proposal.getSubmittedAt(),
            proposal.getAcceptedAt()
        );
    }
}
