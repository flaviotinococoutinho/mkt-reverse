package com.marketplace.proposal.domain.command;

import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.shared.domain.valueobject.Money;

import java.util.Objects;

/**
 * Command to update an existing proposal.
 * 
 * Immutable command following Command Pattern.
 */
public record UpdateProposalCommand(
    ProposalId proposalId,
    Money price,
    DeliveryTime deliveryTime,
    String description
) {
    
    public UpdateProposalCommand {
        Objects.requireNonNull(proposalId, "Proposal ID cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        Objects.requireNonNull(deliveryTime, "Delivery time cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
    }
}
