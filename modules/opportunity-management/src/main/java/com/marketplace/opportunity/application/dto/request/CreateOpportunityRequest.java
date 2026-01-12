package com.marketplace.opportunity.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for creating opportunity request.
 * 
 * Immutable record with Bean Validation annotations.
 * Used by REST controllers to receive data from clients.
 * 
 * Follows DTO pattern:
 * - Data transfer between layers
 * - Validation annotations
 * - No business logic
 * - Immutable
 */
public record CreateOpportunityRequest(
    
    @NotNull(message = "Consumer ID is required")
    @Positive(message = "Consumer ID must be positive")
    Long consumerId,
    
    @NotNull(message = "Tenant ID is required")
    @Positive(message = "Tenant ID must be positive")
    Long tenantId,
    
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    String description,
    
    @NotBlank(message = "Category is required")
    String category,
    
    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget must be greater than zero")
    BigDecimal budgetAmount,
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO 4217)")
    String currencyCode,
    
    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    Instant deadline,
    
    List<String> attachments,
    
    Map<String, Object> specifications,
    
    String templateKey
) {
    
    /**
     * Compact constructor for additional validation.
     */
    public CreateOpportunityRequest {
        if (attachments == null) {
            attachments = List.of();
        }
        
        if (specifications == null) {
            specifications = Map.of();
        }
    }
}
