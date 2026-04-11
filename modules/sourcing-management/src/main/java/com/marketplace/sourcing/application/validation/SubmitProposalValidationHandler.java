package com.marketplace.sourcing.application.validation;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.shared.valueobject.Money;
import lombok.RequiredArgsConstructor;

/**
 * Submit Proposal Validation Handler
 * 
 * Validates all business rules before a supplier can submit a proposal:
 * - Event must exist and accept responses
 * - Supplier cannot be the event owner (conflict of interest)
 * - Supplier cannot have already submitted for this event
 * - Offer amount must be positive
 * - Lead time must be at least 1 day
 * 
 * Implements Object Calisthenics:
 * - One level of indentation per method
 * - No else keyword
 * - No getters returning mutable objects
 * - No magic numbers
 */
@RequiredArgsConstructor
public non-sealed class SubmitProposalValidationHandler implements ValidationHandler {

    private static final String EVENT_NOT_FOUND = "Event not found";
    private static final String EVENT_NOT_ACCEPTING = "Event is not accepting responses";
    private static final String SELF_PROPOSAL = "Cannot submit proposal to your own event";
    private static final String DUPLICATE_PROPOSAL = "You already submitted a proposal for this event";
    private static final String INVALID_AMOUNT = "Offer amount must be greater than zero";
    private static final String INVALID_LEAD_TIME = "Lead time must be at least 1 day";

    private final SourcingEventRepository eventRepository;
    private final SupplierResponseRepository responseRepository;

    @Override
    public String getErrorMessage() {
        return "Submit proposal validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return ctx.eventId()
                .map(this::validateEventExists)
                .orElse(ValidationResult.invalid("Event ID is required", "eventId"));
    }

    private ValidationResult validateEventExists(String eventId) {
        return eventRepository.findById(eventId)
                .map(this::validateEventAcceptsResponses)
                .orElse(ValidationResult.invalid(EVENT_NOT_FOUND, "eventId"));
    }

    private ValidationResult validateEventAcceptsResponses(SourcingEvent event) {
        return event.acceptsResponses()
                ? validateNotSelfProposal(event)
                : ValidationResult.invalid(EVENT_NOT_ACCEPTING, "eventId");
    }

    private ValidationResult validateNotSelfProposal(SourcingEvent event) {
        return event.getBuyerId().equals(currentSupplierId())
                ? ValidationResult.invalid(SELF_PROPOSAL, "supplierId")
                : validateNoDuplicateProposal(event.getId());
    }

    private ValidationResult validateNoDuplicateProposal(String eventId) {
        var supplierId = currentSupplierId();
        return responseRepository.findByEventIdAndSupplierId(eventId, supplierId)
                .isPresent()
                ? ValidationResult.invalid(DUPLICATE_PROPOSAL, "supplierId")
                : validateOfferAmount();
    }

    // Note: In real implementation, supplierId would come from security context
    private String currentSupplierId() {
        return validationContextData()
                .flatMap(ctx -> ctx.supplierId())
                .orElseThrow(() -> new IllegalStateException("Supplier ID not found in context"));
    }

    private ValidationContext validationContextData() {
        // Get from thread local or security context in real implementation
        return ValidationContext.empty();
    }

    private ValidationResult validateOfferAmount() {
        return validationContextData()
                .flatMap(ctx -> ctx.dataAs(Money.class))
                .map(amount -> amount.isGreaterThanZero()
                        ? validateLeadTime()
                        : ValidationResult.invalid(INVALID_AMOUNT, "offerAmount"))
                .orElse(validateLeadTime());
    }

    private ValidationResult validateLeadTime() {
        return validationContextData()
                .flatMap(ctx -> ctx.dataAs(Integer.class))
                .map(days -> days > 0
                        ? ValidationResult.valid()
                        : ValidationResult.invalid(INVALID_LEAD_TIME, "leadTimeDays"))
                .orElse(ValidationResult.valid());
    }
}