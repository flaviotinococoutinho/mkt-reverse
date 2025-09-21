package com.marketplace.sourcing.domain.valueobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;

/**
 * Product Specification Value Object
 * 
 * Represents detailed product or service specifications for sourcing events.
 * Handles technical requirements, quality standards, and compliance criteria.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Rich specification management
 * - Flexible attribute system
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ProductSpecification implements Serializable {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "product_category", length = 100)
    private String category;

    @Column(name = "product_subcategory", length = 100)
    private String subcategory;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "quantity_required")
    private Long quantityRequired;

    @Column(name = "technical_specifications", columnDefinition = "TEXT")
    private String technicalSpecifications; // JSON string

    @Column(name = "quality_standards", columnDefinition = "TEXT")
    private String qualityStandards; // JSON string

    @Column(name = "compliance_requirements", columnDefinition = "TEXT")
    private String complianceRequirements; // JSON string

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON string of file references

    /**
     * Creates a ProductSpecification with basic information
     */
    public static ProductSpecification of(String productName, String description, 
                                        String category, String unitOfMeasure, Long quantityRequired) {
        validateBasicFields(productName, unitOfMeasure, quantityRequired);
        
        return new ProductSpecification(
            productName.trim(),
            description != null ? description.trim() : null,
            category != null ? category.trim() : null,
            null, // subcategory
            unitOfMeasure.trim(),
            quantityRequired,
            null, // technicalSpecifications
            null, // qualityStandards
            null, // complianceRequirements
            null  // attachments
        );
    }

    /**
     * Creates a complete ProductSpecification
     */
    public static ProductSpecification create(String productName, String description, String category, 
                                            String subcategory, String unitOfMeasure, Long quantityRequired,
                                            Map<String, Object> technicalSpecs, List<String> qualityStandards,
                                            List<String> complianceReqs, List<String> attachments) {
        validateBasicFields(productName, unitOfMeasure, quantityRequired);
        
        return new ProductSpecification(
            productName.trim(),
            description != null ? description.trim() : null,
            category != null ? category.trim() : null,
            subcategory != null ? subcategory.trim() : null,
            unitOfMeasure.trim(),
            quantityRequired,
            serializeToJson(technicalSpecs),
            serializeToJson(qualityStandards),
            serializeToJson(complianceReqs),
            serializeToJson(attachments)
        );
    }

    /**
     * Gets technical specifications as a map
     */
    public Map<String, Object> getTechnicalSpecificationsMap() {
        if (technicalSpecifications == null || technicalSpecifications.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(technicalSpecifications, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize technical specifications", e);
            return new HashMap<>();
        }
    }

    /**
     * Gets quality standards as a list
     */
    public List<String> getQualityStandardsList() {
        if (qualityStandards == null || qualityStandards.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(qualityStandards, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize quality standards", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets compliance requirements as a list
     */
    public List<String> getComplianceRequirementsList() {
        if (complianceRequirements == null || complianceRequirements.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(complianceRequirements, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize compliance requirements", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets attachments as a list
     */
    public List<String> getAttachmentsList() {
        if (attachments == null || attachments.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(attachments, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize attachments", e);
            return new ArrayList<>();
        }
    }

    /**
     * Adds a technical specification
     */
    public ProductSpecification addTechnicalSpecification(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Technical specification key cannot be null or empty");
        }
        
        Map<String, Object> currentSpecs = getTechnicalSpecificationsMap();
        currentSpecs.put(key.trim(), value);
        
        return new ProductSpecification(
            this.productName, this.description, this.category, this.subcategory,
            this.unitOfMeasure, this.quantityRequired,
            serializeToJson(currentSpecs), this.qualityStandards, 
            this.complianceRequirements, this.attachments
        );
    }

    /**
     * Adds a quality standard
     */
    public ProductSpecification addQualityStandard(String standard) {
        if (standard == null || standard.trim().isEmpty()) {
            throw new IllegalArgumentException("Quality standard cannot be null or empty");
        }
        
        List<String> currentStandards = getQualityStandardsList();
        if (!currentStandards.contains(standard.trim())) {
            currentStandards.add(standard.trim());
        }
        
        return new ProductSpecification(
            this.productName, this.description, this.category, this.subcategory,
            this.unitOfMeasure, this.quantityRequired, this.technicalSpecifications,
            serializeToJson(currentStandards), this.complianceRequirements, this.attachments
        );
    }

    /**
     * Adds a compliance requirement
     */
    public ProductSpecification addComplianceRequirement(String requirement) {
        if (requirement == null || requirement.trim().isEmpty()) {
            throw new IllegalArgumentException("Compliance requirement cannot be null or empty");
        }
        
        List<String> currentRequirements = getComplianceRequirementsList();
        if (!currentRequirements.contains(requirement.trim())) {
            currentRequirements.add(requirement.trim());
        }
        
        return new ProductSpecification(
            this.productName, this.description, this.category, this.subcategory,
            this.unitOfMeasure, this.quantityRequired, this.technicalSpecifications,
            this.qualityStandards, serializeToJson(currentRequirements), this.attachments
        );
    }

    /**
     * Adds an attachment
     */
    public ProductSpecification addAttachment(String attachmentReference) {
        if (attachmentReference == null || attachmentReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Attachment reference cannot be null or empty");
        }
        
        List<String> currentAttachments = getAttachmentsList();
        if (!currentAttachments.contains(attachmentReference.trim())) {
            currentAttachments.add(attachmentReference.trim());
        }
        
        return new ProductSpecification(
            this.productName, this.description, this.category, this.subcategory,
            this.unitOfMeasure, this.quantityRequired, this.technicalSpecifications,
            this.qualityStandards, this.complianceRequirements, serializeToJson(currentAttachments)
        );
    }

    /**
     * Updates the quantity required
     */
    public ProductSpecification withQuantity(Long newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        return new ProductSpecification(
            this.productName, this.description, this.category, this.subcategory,
            this.unitOfMeasure, newQuantity, this.technicalSpecifications,
            this.qualityStandards, this.complianceRequirements, this.attachments
        );
    }

    /**
     * Updates the description
     */
    public ProductSpecification withDescription(String newDescription) {
        return new ProductSpecification(
            this.productName, newDescription != null ? newDescription.trim() : null, 
            this.category, this.subcategory, this.unitOfMeasure, this.quantityRequired,
            this.technicalSpecifications, this.qualityStandards, 
            this.complianceRequirements, this.attachments
        );
    }

    /**
     * Checks if this specification has technical requirements
     */
    public boolean hasTechnicalSpecifications() {
        return !getTechnicalSpecificationsMap().isEmpty();
    }

    /**
     * Checks if this specification has quality standards
     */
    public boolean hasQualityStandards() {
        return !getQualityStandardsList().isEmpty();
    }

    /**
     * Checks if this specification has compliance requirements
     */
    public boolean hasComplianceRequirements() {
        return !getComplianceRequirementsList().isEmpty();
    }

    /**
     * Checks if this specification has attachments
     */
    public boolean hasAttachments() {
        return !getAttachmentsList().isEmpty();
    }

    /**
     * Gets the full category path
     */
    public String getFullCategory() {
        if (category == null) {
            return "";
        }
        if (subcategory == null) {
            return category;
        }
        return category + " > " + subcategory;
    }

    /**
     * Gets the specification completeness score (0-100)
     */
    public int getCompletenessScore() {
        int score = 0;
        
        // Basic information (40 points)
        if (productName != null && !productName.trim().isEmpty()) score += 10;
        if (description != null && !description.trim().isEmpty()) score += 10;
        if (category != null && !category.trim().isEmpty()) score += 10;
        if (quantityRequired != null && quantityRequired > 0) score += 10;
        
        // Technical details (30 points)
        if (hasTechnicalSpecifications()) score += 15;
        if (hasQualityStandards()) score += 15;
        
        // Compliance and documentation (30 points)
        if (hasComplianceRequirements()) score += 15;
        if (hasAttachments()) score += 15;
        
        return score;
    }

    /**
     * Validates basic required fields
     */
    private static void validateBasicFields(String productName, String unitOfMeasure, Long quantityRequired) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (productName.trim().length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters");
        }
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit of measure cannot be null or empty");
        }
        if (quantityRequired == null || quantityRequired <= 0) {
            throw new IllegalArgumentException("Quantity required must be positive");
        }
    }

    /**
     * Serializes an object to JSON string
     */
    private static String serializeToJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSpecification that = (ProductSpecification) o;
        return Objects.equals(productName, that.productName) &&
               Objects.equals(description, that.description) &&
               Objects.equals(category, that.category) &&
               Objects.equals(subcategory, that.subcategory) &&
               Objects.equals(unitOfMeasure, that.unitOfMeasure) &&
               Objects.equals(quantityRequired, that.quantityRequired) &&
               Objects.equals(technicalSpecifications, that.technicalSpecifications) &&
               Objects.equals(qualityStandards, that.qualityStandards) &&
               Objects.equals(complianceRequirements, that.complianceRequirements) &&
               Objects.equals(attachments, that.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, description, category, subcategory, 
                           unitOfMeasure, quantityRequired, technicalSpecifications, 
                           qualityStandards, complianceRequirements, attachments);
    }

    @Override
    public String toString() {
        return "ProductSpecification{" +
               "productName='" + productName + '\'' +
               ", category='" + getFullCategory() + '\'' +
               ", quantity=" + quantityRequired + " " + unitOfMeasure +
               ", completeness=" + getCompletenessScore() + "%" +
               '}';
    }
}

