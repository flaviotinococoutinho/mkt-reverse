package com.marketplace.opportunity.domain.service;

import com.marketplace.opportunity.domain.model.Opportunity;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of Responsibility for opportunity validation.
 * 
 * Implements Chain of Responsibility Pattern:
 * - Decouples sender from receiver
 * - Multiple handlers can process request
 * - Flexible and extensible validation pipeline
 * 
 * Follows Object Calisthenics and Clean Code principles.
 */
public abstract class ValidationChain {
    
    private ValidationChain nextChain;
    
    /**
     * Sets the next validator in the chain.
     * 
     * @param nextChain next validator
     * @return this validator for fluent API
     */
    public ValidationChain setNext(ValidationChain nextChain) {
        this.nextChain = nextChain;
        return nextChain;
    }
    
    /**
     * Validates opportunity and passes to next chain.
     * Template Method Pattern - defines algorithm skeleton.
     * 
     * @param opportunity opportunity to validate
     * @return validation result
     */
    public final ValidationResult validate(Opportunity opportunity) {
        ValidationResult result = doValidate(opportunity);
        
        if (hasFailedValidation(result)) {
            return result;
        }
        
        if (hasNextChain()) {
            return nextChain.validate(opportunity);
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Performs actual validation logic.
     * Hook method to be implemented by concrete validators.
     * 
     * @param opportunity opportunity to validate
     * @return validation result
     */
    protected abstract ValidationResult doValidate(Opportunity opportunity);
    
    private boolean hasFailedValidation(ValidationResult result) {
        return !result.isValid();
    }
    
    private boolean hasNextChain() {
        return nextChain != null;
    }
    
    /**
     * Result of validation.
     * Immutable value object.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = List.copyOf(errors);
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }
        
        public static ValidationResult failure(String error) {
            return new ValidationResult(false, List.of(error));
        }
        
        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> errors() {
            return errors;
        }
        
        public String firstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }
    
    /**
     * Validates opportunity title.
     */
    public static class TitleValidator extends ValidationChain {
        
        @Override
        protected ValidationResult doValidate(Opportunity opportunity) {
            String title = opportunity.title();
            
            if (containsProfanity(title)) {
                return ValidationResult.failure("Title contains inappropriate content");
            }
            
            if (containsSpam(title)) {
                return ValidationResult.failure("Title appears to be spam");
            }
            
            return ValidationResult.success();
        }
        
        private boolean containsProfanity(String title) {
            // Simplified - in production, use proper profanity filter
            return false;
        }
        
        private boolean containsSpam(String title) {
            // Simplified - in production, use spam detection
            return title.toLowerCase().contains("click here");
        }
    }
    
    /**
     * Validates opportunity budget.
     */
    public static class BudgetValidator extends ValidationChain {
        
        private static final int MINIMUM_BUDGET = 10;
        private static final int MAXIMUM_BUDGET = 1_000_000;
        
        @Override
        protected ValidationResult doValidate(Opportunity opportunity) {
            var budget = opportunity.budget();
            var amount = budget.amount().intValue();
            
            if (isBelowMinimum(amount)) {
                return ValidationResult.failure(
                    String.format("Budget must be at least %d", MINIMUM_BUDGET)
                );
            }
            
            if (isAboveMaximum(amount)) {
                return ValidationResult.failure(
                    String.format("Budget must not exceed %d", MAXIMUM_BUDGET)
                );
            }
            
            return ValidationResult.success();
        }
        
        private boolean isBelowMinimum(int amount) {
            return amount < MINIMUM_BUDGET;
        }
        
        private boolean isAboveMaximum(int amount) {
            return amount > MAXIMUM_BUDGET;
        }
    }
    
    /**
     * Validates opportunity deadline.
     */
    public static class DeadlineValidator extends ValidationChain {
        
        private static final long MINIMUM_HOURS = 24;
        private static final long MAXIMUM_DAYS = 90;
        
        @Override
        protected ValidationResult doValidate(Opportunity opportunity) {
            var deadline = opportunity.deadline();
            var now = java.time.Instant.now();
            var hoursUntilDeadline = java.time.Duration.between(now, deadline).toHours();
            
            if (isTooSoon(hoursUntilDeadline)) {
                return ValidationResult.failure(
                    String.format("Deadline must be at least %d hours in the future", MINIMUM_HOURS)
                );
            }
            
            if (isTooFar(hoursUntilDeadline)) {
                return ValidationResult.failure(
                    String.format("Deadline must not exceed %d days", MAXIMUM_DAYS)
                );
            }
            
            return ValidationResult.success();
        }
        
        private boolean isTooSoon(long hours) {
            return hours < MINIMUM_HOURS;
        }
        
        private boolean isTooFar(long hours) {
            return hours > (MAXIMUM_DAYS * 24);
        }
    }
    
    /**
     * Validates opportunity category.
     */
    public static class CategoryValidator extends ValidationChain {
        
        private static final List<String> ALLOWED_CATEGORIES = List.of(
            "collectibles",
            "automotive",
            "fashion",
            "technology",
            "services"
        );
        
        @Override
        protected ValidationResult doValidate(Opportunity opportunity) {
            String category = opportunity.category();
            
            if (!isAllowedCategory(category)) {
                return ValidationResult.failure(
                    String.format("Category '%s' is not allowed. Allowed categories: %s",
                        category, ALLOWED_CATEGORIES)
                );
            }
            
            return ValidationResult.success();
        }
        
        private boolean isAllowedCategory(String category) {
            return ALLOWED_CATEGORIES.contains(category.toLowerCase());
        }
    }
    
    /**
     * Builder for creating validation chain.
     * Fluent API for chain construction.
     */
    public static class Builder {
        private final List<ValidationChain> validators = new ArrayList<>();
        
        public Builder addTitleValidator() {
            validators.add(new TitleValidator());
            return this;
        }
        
        public Builder addBudgetValidator() {
            validators.add(new BudgetValidator());
            return this;
        }
        
        public Builder addDeadlineValidator() {
            validators.add(new DeadlineValidator());
            return this;
        }
        
        public Builder addCategoryValidator() {
            validators.add(new CategoryValidator());
            return this;
        }
        
        public Builder addCustomValidator(ValidationChain validator) {
            validators.add(validator);
            return this;
        }
        
        public ValidationChain build() {
            if (validators.isEmpty()) {
                throw new IllegalStateException("At least one validator must be added");
            }
            
            ValidationChain head = validators.get(0);
            ValidationChain current = head;
            
            for (int i = 1; i < validators.size(); i++) {
                current = current.setNext(validators.get(i));
            }
            
            return head;
        }
    }
}
