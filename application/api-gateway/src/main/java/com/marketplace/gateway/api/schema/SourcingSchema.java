package com.marketplace.gateway.api.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * JSON Schema definitions for API contracts.
 * 
 * Following JsonSchema specification (draft-07):
 * - Clear type definitions
 * - Validation constraints
 * - Documentation (description)
 * - Required fields marked
 * 
 * Object Calisthenics:
 * - Immutable records
 * - No magic numbers
 * - Named constants
 */
public sealed interface SourcingSchema 
        permits SourcingSchema.CreateEventRequest,
                SourcingSchema.CreateEventResponse,
                SourcingSchema.UpdateEventRequest,
                SourcingSchema.SubmitProposalRequest,
                SourcingSchema.SubmitProposalResponse,
                SourcingSchema.EventView,
                SourcingSchema.ProposalView,
                SourcingSchema.ErrorResponse {

    /** Request to create a new sourcing event. */
    record CreateEventRequest(
        
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
        @JsonProperty("title")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        @JsonProperty("description")
        String description,

        @NotNull(message = "Event type is required")
        @JsonProperty("type")
        String eventType,

        @Min(value = 174, message = "Invalid MCC category code")
        @Max(value = 891, message = "Invalid MCC category code")
        @JsonProperty("mcc_category_code")
        Integer mccCategoryCode,

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name too long")
        @JsonProperty("product_name")
        String productName,

        @Size(max = 2000, message = "Product description too long")
        @JsonProperty("product_description")
        String productDescription,

        @NotBlank(message = "Unit of measure is required")
        @JsonProperty("unit_of_measure")
        String unitOfMeasure,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 1000000, message = "Quantity cannot exceed 1,000,000")
        @JsonProperty("quantity_required")
        Integer quantityRequired,

        @Min(value = 1, message = "Validity must be at least 1 hour")
        @Max(value = 8760, message = "Validity cannot exceed 1 year (8760 hours)")
        @JsonProperty("valid_for_hours")
        Integer validForHours,

        @Min(value = 0, message = "Budget cannot be negative")
        @JsonProperty("estimated_budget_cents")
        Long estimatedBudgetCents,

        @JsonProperty("attributes")
        List<SpecAttributeSchema> attributes,

        @JsonProperty("buyer_contact_name")
        String buyerContactName,

        @JsonProperty("buyer_contact_phone")
        String buyerContactPhone,

        @JsonProperty("buyer_contact_email")
        String buyerContactEmail
    ) implements SourcingSchema {

        public static final String TYPE_RFQ = "RFQ";
        public static final String TYPE_REVERSE_AUCTION = "REVERSE_AUCTION";
        public static final String TYPE_MARKETPLACE = "MARKETPLACE";

        public static final Set<String> VALID_TYPES = Set.of(
                TYPE_RFQ, TYPE_REVERSE_AUCTION, TYPE_MARKETPLACE
        );

        public static final Set<String> VALID_MCC_CODES = Set.of(
                174, 275, 553, 521, 571, 501, 581, 504, 821, 829
        );
    }

    /** Response after creating an event. */
    record CreateEventResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("status")
        String status,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("expires_at")
        Instant expiresAt
    ) implements SourcingSchema {}

    /** Request to update an event. */
    record UpdateEventRequest(
        @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
        @JsonProperty("title")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        @JsonProperty("description")
        String description
    ) implements SourcingSchema {}

    /** Request to submit a proposal. */
    record SubmitProposalRequest(

        @NotBlank(message = "Supplier ID is required")
        @JsonProperty("supplier_id")
        String supplierId,

        @NotBlank(message = "Supplier organization ID is required")
        @JsonProperty("supplier_organization_id")
        String supplierOrganizationId,

        @NotNull(message = "Offer amount is required")
        @Min(value = 1, message = "Offer must be greater than zero")
        @JsonProperty("offer_cents")
        Integer offerCents,

        @Min(value = 1, message = "Lead time must be at least 1 day")
        @JsonProperty("lead_time_days")
        Integer leadTimeDays,

        @JsonProperty("warranty_months")
        Integer warrantyMonths,

        @JsonProperty("condition")
        String condition,

        @JsonProperty("shipping_mode")
        String shippingMode,

        @Size(max = 5000, message = "Message too long")
        @JsonProperty("message")
        String message,

        @JsonProperty("attributes")
        List<SpecAttributeSchema> attributes
    ) implements SourcingSchema {

        public static final Set<String> VALID_CONDITIONS = Set.of(
                "NEW", "REFURBISHED", "USED", "SAMPLE"
        );

        public static final Set<String> VALID_SHIPPING_MODES = Set.of(
                "EXWORKS", "FOB", "CIF", "DDP"
        );
    }

    /** Response after submitting a proposal. */
    record SubmitProposalResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("status")
        String status,

        @JsonProperty("submitted_at")
        Instant submittedAt
    ) implements SourcingSchema {}

    /** Event view for listings. */
    record EventView(
        @JsonProperty("id")
        String id,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("status")
        String status,

        @JsonProperty("event_type")
        String eventType,

        @JsonProperty("mcc_category_code")
        Integer mccCategoryCode,

        @JsonProperty("quantity_required")
        Integer quantityRequired,

        @JsonProperty("unit_of_measure")
        String unitOfMeasure,

        @JsonProperty("published_at")
        Instant publishedAt,

        @JsonProperty("expires_at")
        Instant expiresAt,

        @JsonProperty("proposals_count")
        Integer proposalsCount,

        @JsonProperty("lowest_offer_cents")
        Integer lowestOfferCents
    ) implements SourcingSchema {}

    /** Proposal view for listings. */
    record ProposalView(
        @JsonProperty("id")
        String id,

        @JsonProperty("supplier_id")
        String supplierId,

        @JsonProperty("supplier_name")
        String supplierName,

        @JsonProperty("offer_cents")
        Integer offerCents,

        @JsonProperty("lead_time_days")
        Integer leadTimeDays,

        @JsonProperty("status")
        String status,

        @JsonProperty("submitted_at")
        Instant submittedAt,

        @JsonProperty("accepted_at")
        Instant acceptedAt
    ) implements SourcingSchema {}

    /** Error response. */
    record ErrorResponse(
        @JsonProperty("error")
        String error,

        @JsonProperty("message")
        String message,

        @JsonProperty("field")
        String field,

        @JsonProperty("details")
        List<FieldError> details
    ) implements SourcingSchema {

        public record FieldError(
                @JsonProperty("field")
                String field,
                @JsonProperty("message")
                String message
        ) {}

        public static ErrorResponse of(String error, String message) {
            return new ErrorResponse(error, message, null, List.of());
        }

        public static ErrorResponse validation(String message, String field) {
            return new ErrorResponse("VALIDATION_ERROR", message, field, List.of());
        }
    }

    /** Specification attribute. */
    record SpecAttributeSchema(
        @NotBlank
        @JsonProperty("name")
        String name,

        @JsonProperty("value")
        String value,

        @JsonProperty("type")
        String type
    ) {}
}