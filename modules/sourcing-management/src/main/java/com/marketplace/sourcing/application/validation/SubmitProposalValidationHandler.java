package com.marketplace.sourcing.application.validation;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;

import java.util.Optional;

/**
 * Simplified Submit Proposal Validation using pipeline pattern.
 * 
 * Reduces complexity from ~35 to ~10 by using method chaining.
 * 
 * Object Calisthenics:
 * - Single level of indentation
 * - Small focused methods
 * - Immutable flow
 */
public class SubmitProposalValidationHandler implements ValidationHandler {

    private final SourcingEventRepository eventRepository;
    private final SupplierResponseRepository responseRepository;

    // Error messages as constants
    private static final String ERR_EVENT_NOT_FOUND = "Event not found";
    private static final String ERR_NOT_ACCEPTING = "Event not accepting responses";
    private static final String ERR_SELF_PROPOSAL = "Cannot submit to your own event";
    private static final String ERR_DUPLICATE = "Already submitted a proposal";
    private static final String ERR_INVALID_AMOUNT = "Offer must be greater than zero";

    public SubmitProposalValidationHandler(
            SourcingEventRepository eventRepository,
            SupplierResponseRepository responseRepository
    ) {
        this.eventRepository = eventRepository;
        this.responseRepository = responseRepository;
    }

    @Override
    public String getErrorMessage() {
        return "Submit proposal validation failed";
    }

    /**
     * Main validation method - reduced to simple pipeline
     */
    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return Optional.ofNullable(ctx.eventId().orElse(null))
                .flatMap(eventRepository::findById)
                .map(this::validateEvent)
                .orElse(ValidationResult.invalid(ERR_EVENT_NOT_FOUND, "eventId"));
    }

    private ValidationResult validateEvent(SourcingEvent event) {
        return validateAcceptsResponses(event)
                .combine(() -> validateNoSelfProposal(event))
                .combine(() -> validateNoDuplicate(event.getId()));
    }

    private ValidationResult validateAcceptsResponses(SourcingEvent event) {
        return event.acceptsResponses()
                ? ValidationResult.valid()
                : ValidationResult.invalid(ERR_NOT_ACCEPTING, "eventId");
    }

    private ValidationResult validateNoSelfProposal(SourcingEvent event) {
        var buyerId = "current-supplier-id"; // Would come from security context
        return event.getBuyerId().equals(buyerId)
                ? ValidationResult.invalid(ERR_SELF_PROPOSAL, "supplierId")
                : ValidationResult.valid();
    }

    private ValidationResult validateNoDuplicateProposal(String eventId, String supplierId) {
        return responseRepository.findByEventIdAndSupplierId(eventId, supplierId)
                .isPresent()
                ? ValidationResult.invalid(ERR_DUPLICATE, "supplierId")
                : ValidationResult.valid();
    }

    private ValidationResult validateNoDuplicate(String eventId) {
        var supplierId = "current-supplier-id";
        if (eventRepository.findById(eventId).isEmpty()) {
            return ValidationResult.valid();
        }
        return responseRepository.findByEventIdAndSupplierId(eventId, supplierId)
                .isPresent()
                ? ValidationResult.invalid(ERR_DUPLICATE, "supplierId")
                : ValidationResult.valid();
    }
}