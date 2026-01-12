package com.marketplace.proposal.domain.command;

import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.shared.domain.valueobject.Money;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command to submit a new proposal.
 * 
 * Immutable command following Command Pattern.
 * Encapsulates all data needed to submit a proposal.
 */
public record SubmitProposalCommand(
    Long opportunityId,
    Long companyId,
    Long tenantId,
    Money price,
    DeliveryTime deliveryTime,
    String description,
    List<String> attachments,
    Map<String, Object> specifications
) {
    
    public SubmitProposalCommand {
        Objects.requireNonNull(opportunityId, "Opportunity ID cannot be null");
        Objects.requireNonNull(companyId, "Company ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        Objects.requireNonNull(deliveryTime, "Delivery time cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        
        // Defensive copies for mutable collections
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
        specifications = specifications != null ? Map.copyOf(specifications) : Map.of();
    }
}
