package com.marketplace.sourcing.application.validation;

import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.proposal.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;

/**
 * Factory for creating validation chains
 * 
 * Centralizes validation logic following Object Calisthenics:
 * - One concept per class
 * - No public fields
 * - Immutable value objects
 * - Small cohesive methods
 */
@RequiredArgsConstructor
public final class ValidationChainFactory {

    private final SourcingEventRepository eventRepository;
    private final SupplierResponseRepository responseRepository;
    private final ProposalRepository proposalRepository;

    /**
     * Creates validation chain for submitting a proposal
     */
    public ValidationHandler submitProposalHandler() {
        return ValidationHandler.compose(
                new SubmitProposalValidationHandler(
                        eventRepository,
                        responseRepository
                )
        );
    }

    /**
     * Creates validation chain for accepting a proposal
     */
    public ValidationHandler acceptProposalHandler() {
        return ValidationHandler.compose(
                new AcceptProposalValidationHandler(
                        proposalRepository
                )
        );
    }

    /**
     * Creates validation chain for creating an event
     */
    public ValidationHandler createEventHandler() {
        return ValidationHandler.compose(
                new CreateEventValidationHandler()
        );
    }

    /**
     * Creates composite handler for search/filter operations
     */
    public ValidationHandler searchHandler() {
        return ValidationHandler.compose(
                new SearchValidationHandler()
        );
    }

    /**
     * Validates a context using the appropriate handler
     */
    public ValidationResult validate(String operation, ValidationContext ctx) {
        return switch (operation) {
            case "SUBMIT_PROPOSAL" -> submitProposalHandler().validate(ctx);
            case "ACCEPT_PROPOSAL" -> acceptProposalHandler().validate(ctx);
            case "CREATE_EVENT" -> createEventHandler().validate(ctx);
            case "SEARCH" -> searchHandler().validate(ctx);
            default -> ValidationResult.valid();
        };
    }
}