package com.marketplace.sourcing.application.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validation Chain Handler - Chain of Responsibility Pattern
 * 
 * Object Calthenics Principles Applied:
 * - Small methods (validate method)
 * - Fewer than 20 chars per line (formatting)
 * - No conditionals in validate chain (method over if/else)
 * - No switch/case in business logic (polymorphic handlers)
 */
public sealed interface ValidationHandler 
        permits SubmitProposalValidationHandler, 
                AcceptProposalValidationHandler,
                CreateEventValidationHandler {

    String getErrorMessage();

    ValidationResult validate(ValidationContext context);

    default ValidationHandler and(ValidationHandler next) {
        return new CompositeValidationHandler(this, next);
    }

    static ValidationHandler compose(ValidationHandler... handlers) {
        var list = new ArrayList<ValidationHandler>();
        for (var h : handlers) {
            if (h != null) {
                list.add(h);
            }
        }
        return new CompositeValidationHandler(list.toArray(new ValidationHandler[0]));
    }
}

/**
 * Composite handler that runs all validations in sequence
 */
record CompositeValidationHandler(ValidationHandler[] handlers) 
        implements ValidationHandler {

    @Override
    public String getErrorMessage() {
        return "Validation failed";
    }

    @Override
    public ValidationResult validate(ValidationContext context) {
        for (var handler : handlers) {
            var result = handler.validate(context);
            if (!result.isValid()) {
                return result;
            }
        }
        return ValidationResult.valid();
    }
}

/**
 * Context containing all data needed for validation
 * Immutable value object following Object Calthenics
 */
record ValidationContext(
    String eventId,
    String buyerId,
    String supplierId,
    Object data,
    ValidationContext previousContext
) {
    static ValidationContext empty() {
        return new ValidationContext(null, null, null, null, null);
    }

    ValidationContext withEventId(String id) {
        return new ValidationContext(id, buyerId(), supplierId(), data(), this);
    }

    ValidationContext withBuyerId(String id) {
        return new ValidationContext(eventId(), id, supplierId(), data(), this);
    }

    ValidationContext withSupplierId(String id) {
        return new ValidationContext(eventId(), buyerId(), id, data(), this);
    }

    ValidationContext withData(Object obj) {
        return new ValidationContext(eventId(), buyerId(), supplierId(), obj, this);
    }

    Optional<String> eventId() {
        return Optional.ofNullable(eventId);
    }

    Optional<String> buyerId() {
        return Optional.ofNullable(buyerId);
    }

    Optional<String> supplierId() {
        return Optional.ofNullable(supplierId);
    }

    @SuppressWarnings("unchecked")
    <T> Optional<T> dataAs(Class<T> type) {
        if (type.isInstance(data())) {
            return Optional.of((T) data());
        }
        return Optional.empty();
    }
}

/**
 * Result of validation - Value Object
 */
record ValidationResult(boolean valid, String error, String field) {

    static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    static ValidationResult invalid(String error) {
        return new ValidationResult(false, error, null);
    }

    static ValidationResult invalid(String error, String field) {
        return new ValidationResult(false, error, field);
    }

    boolean isValid() {
        return valid;
    }

    String errorMessage() {
        return error;
    }

    String errorField() {
        return field;
    }
}