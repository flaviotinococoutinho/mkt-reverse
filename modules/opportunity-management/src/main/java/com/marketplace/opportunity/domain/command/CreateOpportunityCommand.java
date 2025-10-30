package com.marketplace.opportunity.domain.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Command for creating a new opportunity.
 * 
 * Implements Command Pattern:
 * - Encapsulates request as an object
 * - Allows parameterization of clients
 * - Supports queuing and logging
 * - Enables undo/redo operations
 * 
 * Immutable record following modern Java practices.
 */
public record CreateOpportunityCommand(
    Long consumerId,
    Long tenantId,
    String title,
    String description,
    String category,
    BigDecimal budgetAmount,
    String currencyCode,
    Instant deadline,
    List<String> attachments,
    Map<String, Object> specifications,
    String templateKey
) {
    
    /**
     * Validates command data.
     * Fail-fast principle.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        validateConsumerId();
        validateTitle();
        validateDescription();
        validateCategory();
        validateBudget();
        validateDeadline();
    }
    
    private void validateConsumerId() {
        if (consumerId == null) {
            throw new IllegalArgumentException("Consumer ID is required");
        }
    }
    
    private void validateTitle() {
        if (isBlank(title)) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (title.length() < 10) {
            throw new IllegalArgumentException("Title must be at least 10 characters");
        }
        
        if (title.length() > 200) {
            throw new IllegalArgumentException("Title must not exceed 200 characters");
        }
    }
    
    private void validateDescription() {
        if (isBlank(description)) {
            throw new IllegalArgumentException("Description is required");
        }
        
        if (description.length() < 50) {
            throw new IllegalArgumentException("Description must be at least 50 characters");
        }
    }
    
    private void validateCategory() {
        if (isBlank(category)) {
            throw new IllegalArgumentException("Category is required");
        }
    }
    
    private void validateBudget() {
        if (budgetAmount == null) {
            throw new IllegalArgumentException("Budget amount is required");
        }
        
        if (budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        
        if (isBlank(currencyCode)) {
            throw new IllegalArgumentException("Currency code is required");
        }
    }
    
    private void validateDeadline() {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        
        if (deadline.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
