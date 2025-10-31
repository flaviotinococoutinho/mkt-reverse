package com.marketplace.proposal.application.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for submitting a new proposal.
 * 
 * Immutable record with validation annotations.
 * Used for REST API request body.
 */
public record SubmitProposalRequest(
    
    @NotNull(message = "Opportunity ID is required")
    @Positive(message = "Opportunity ID must be positive")
    Long opportunityId,
    
    @NotNull(message = "Price amount is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    BigDecimal priceAmount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    String priceCurrency,
    
    @NotNull(message = "Delivery days is required")
    @Min(value = 0, message = "Delivery days cannot be negative")
    @Max(value = 365, message = "Delivery days cannot exceed 365")
    Integer deliveryDays,
    
    @NotNull(message = "Delivery hours is required")
    @Min(value = 0, message = "Delivery hours cannot be negative")
    @Max(value = 23, message = "Delivery hours must be less than 24")
    Integer deliveryHours,
    
    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    String description,
    
    @Size(max = 10, message = "Cannot have more than 10 attachments")
    List<String> attachments,
    
    Map<String, Object> specifications
) {
    
    public SubmitProposalRequest {
        // Defensive copies for mutable collections
        attachments = attachments != null ? List.copyOf(attachments) : List.of();
        specifications = specifications != null ? Map.copyOf(specifications) : Map.of();
    }
}
