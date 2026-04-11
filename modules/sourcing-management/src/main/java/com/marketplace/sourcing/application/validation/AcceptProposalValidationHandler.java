package com.marketplace.sourcing.application.validation;

import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;

/**
 * Accept Proposal Validation Handler
 * 
 * Validates all business rules before a buyer can accept a proposal:
 * - Proposal must exist
 * - Only event owner can accept
 * - Proposal must be in SUBMITTED status
 * - Event must not already have an awarded proposal
 * - Event must not be cancelled
 * 
 * Implements Object Calisthenics:
 * - Small focused methods
 * - No switch/case
 * - Value objects
 */
@RequiredArgsConstructor
public non-sealed class AcceptProposalValidationHandler implements ValidationHandler {

    private static final String PROPOSAL_NOT_FOUND = "Proposal not found";
    private static final String NOT_OWNER = "Only event owner can accept proposals";
    private static final String NOT_SUBMITTED = "Only SUBMITTED proposals can be accepted";
    private static final String ALREADY_AWARDED = "Event already has an awarded proposal";
    private static final String EVENT_CANCELLED = "Event has been cancelled";

    private final ProposalRepository proposalRepository;

    @Override
    public String getErrorMessage() {
        return "Accept proposal validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return findProposal(ctx)
                .map(this::validateOwnership)
                .orElse(ValidationResult.invalid(PROPOSAL_NOT_FOUND, "proposalId"));
    }

    private ValidationResult findProposal(ValidationContext ctx) {
        return ctx.dataAs(Proposal.class)
                .or(() -> ctx.eventId()
                        .flatMap(id -> proposalRepository.findById(id).orElse(null))
                        .map(ValidationResult::valid)
                        .orElse(ValidationResult.invalid(PROPOSAL_NOT_FOUND)));
    }

    private ValidationResult validateOwnership(Proposal proposal) {
        return isEventOwner(proposal)
                ? validateProposalStatus(proposal)
                : ValidationResult.invalid(NOT_OWNER, "proposalId");
    }

    private ValidationResult validateProposalStatus(Proposal proposal) {
        return proposal.isSubmitted()
                ? validateEventNotAwarded(proposal)
                : ValidationResult.invalid(NOT_SUBMITTED, "status");
    }

    private ValidationResult validateEventNotAwarded(Proposal proposal) {
        return proposal.getEvent().hasAwardedProposal()
                ? ValidationResult.invalid(ALREADY_AWARDED, "eventId")
                : validateEventNotCancelled(proposal);
    }

    private ValidationResult validateEventNotCancelled(Proposal proposal) {
        return !proposal.getEvent().isCancelled()
                ? ValidationResult.valid()
                : ValidationResult.invalid(EVENT_CANCELLED, "eventId");
    }

    private boolean isEventOwner(Proposal proposal) {
        return currentBuyerId()
                .equals(proposal.getEvent().getBuyerId());
    }

    private String currentBuyerId() {
        return validationContextData()
                .flatMap(ValidationContext::buyerId)
                .orElseThrow(() -> new IllegalStateException("Buyer ID not found"));
    }

    private ValidationContext validationContextData() {
        // Would get from security context in real implementation
        return ValidationContext.empty();
    }
}