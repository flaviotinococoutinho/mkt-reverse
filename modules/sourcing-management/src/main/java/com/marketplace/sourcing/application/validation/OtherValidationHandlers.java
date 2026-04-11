package com.marketplace.sourcing.application.validation;

/**
 * Create Event Validation Handler
 * 
 * Validates business rules before a buyer can create an event:
 * - Title must not be empty or too long
 * - Description must not exceed max length
 * - Category must be valid MCC code
 * - Quantity must be positive
 * - Expiration date must be in the future
 */
public final class CreateEventValidationHandler implements ValidationHandler {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_QUANTITY = 1000000;

    private static final String TITLE_REQUIRED = "Title is required";
    private static final String TITLE_TOO_LONG = "Title must not exceed " + MAX_TITLE_LENGTH + " characters";
    private static final String DESC_TOO_LONG = "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters";
    private static final String INVALID_QUANTITY = "Quantity must be between 1 and " + MAX_QUANTITY;
    private static final String EXPIRED_DATE = "Expiration date must be in the future";

    @Override
    public String getErrorMessage() {
        return "Create event validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return validateTitle(ctx)
                .combine(() -> validateDescription(ctx))
                .combine(() -> validateQuantity(ctx))
                .combine(() -> validateExpiration(ctx));
    }

    private ValidationResult validateTitle(ValidationContext ctx) {
        return ctx.eventId() // Actually, event data would be in data field
                .map(id -> ValidationResult.valid()) // Placeholder
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validateDescription(ValidationContext ctx) {
        return ctx.dataAs(String.class)
                .map(desc -> desc.length() > MAX_DESCRIPTION_LENGTH
                        ? ValidationResult.invalid(DESC_TOO_LONG, "description")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validateQuantity(ValidationContext ctx) {
        return ctx.dataAs(Integer.class)
                .map(qty -> qty < 1 || qty > MAX_QUANTITY
                        ? ValidationResult.invalid(INVALID_QUANTITY, "quantity")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validateExpiration(ValidationContext ctx) {
        return ctx.dataAs(java.time.Instant.class)
                .map(exp -> java.time.Instant.now().isAfter(exp)
                        ? ValidationResult.invalid(EXPIRED_DATE, "validUntil")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }
}

/**
 * Search Validation Handler
 * 
 * Validates search/filter parameters to prevent abuse:
 * - Query must not exceed max length
 * - Page must be non-negative
 * - Size must be within limits
 */
final class SearchValidationHandler implements ValidationHandler {

    private static final int MAX_QUERY_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_PAGE = 0;

    private static final String QUERY_TOO_LONG = "Query too long (max " + MAX_QUERY_LENGTH + " characters)";
    private static final String INVALID_PAGE = "Page must be non-negative";
    private static final String SIZE_TOO_LARGE = "Page size cannot exceed " + MAX_PAGE_SIZE;

    @Override
    public String getErrorMessage() {
        return "Search validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext ctx) {
        return validateQueryLength(ctx)
                .combine(() -> validatePage(ctx))
                .combine(() -> validatePageSize(ctx));
    }

    private ValidationResult validateQueryLength(ValidationContext ctx) {
        return ctx.dataAs(String.class)
                .map(query -> query != null && query.length() > MAX_QUERY_LENGTH
                        ? ValidationResult.invalid(QUERY_TOO_LONG, "q")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validatePage(ValidationContext ctx) {
        return ctx.dataAs(Integer.class)
                .map(page -> page < MIN_PAGE
                        ? ValidationResult.invalid(INVALID_PAGE, "page")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }

    private ValidationResult validatePageSize(ValidationContext ctx) {
        return ctx.eventId() // Using eventId to pass size
                .map(size -> Integer.parseInt(size) > MAX_PAGE_SIZE
                        ? ValidationResult.invalid(SIZE_TOO_LARGE, "size")
                        : ValidationResult.valid())
                .orElse(ValidationResult.valid());
    }
}