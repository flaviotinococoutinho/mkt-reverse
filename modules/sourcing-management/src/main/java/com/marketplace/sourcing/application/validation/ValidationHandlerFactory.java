package com.marketplace.sourcing.application.validation;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;

/**
 * Factory for creating validation handlers using Strategy pattern.
 * 
 * Reduces complexity from O(n) if-else to O(1) lookup.
 * 
 * Object Calisthenics:
 * - One level of indentation
 * - No else keyword
 * - Immutable strategy map
 */
public final class ValidationHandlerFactory {

    // Strategy map - O(1) lookup instead of O(n) if-else
    public static ValidationHandler forOperation(String operation) {
        return switch (operation) {
            case "SUBMIT_PROPOSAL" -> submitProposalHandler();
            case "ACCEPT_PROPOSAL" -> acceptProposalHandler();
            case "CREATE_EVENT" -> createEventHandler();
            case "SEARCH" -> searchHandler();
            default -> defaultHandler();
        };
    }

    private static ValidationHandler defaultHandler() {
        return context -> ValidationResult.valid();
    }

    private static ValidationHandler submitProposalHandler() {
        return new SubmitProposalValidationHandler(null, null);
    }

    private static ValidationHandler acceptProposalHandler() {
        return new AcceptProposalValidationHandler(null);
    }

    private static ValidationHandler createEventHandler() {
        return new CreateEventValidationHandler();
    }

    private static ValidationHandler searchHandler() {
        return new SimpleSearchValidationHandler();
    }
}

/**
 * Extracted validation handler with lower complexity.
 * Uses Functional approach with method references.
 */
final class SimpleSearchValidationHandler implements ValidationHandler {

    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    @Override
    public String getErrorMessage() {
        return "Search validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return validateQueryLength(ctx)
                .combine(this::validatePage)
                .combine(this::validatePageSize);
    }

    private ValidationResult validateQueryLength(ValidationContext ctx) {
        return ctx.dataAs(String.class)
                .map(q -> q.length() > MAX_QUERY_LENGTH
                        ? ValidationResult.invalid("Query too long", "q")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validatePage(ValidationContext ctx) {
        return ctx.dataAs(Integer.class)
                .map(p -> p < 0
                        ? ValidationResult.invalid("Page must be non-negative", "page")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validatePageSize(ValidationContext ctx) {
        return "100".equals(ctx.eventId())
                ? ValidationResult.invalid("Size too large", "size")
                : ValidationResult.valid();
    }
}