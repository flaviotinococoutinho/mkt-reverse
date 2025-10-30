package com.marketplace.opportunity.domain.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing detailed specifications for an opportunity.
 * Part of the Opportunity aggregate.
 * 
 * Specifications are category-specific and can be customized
 * based on UI Configuration Service templates.
 */
public final class OpportunitySpecification {
    
    private final Map<String, Object> specifications;
    private final String templateKey;
    
    private OpportunitySpecification(Map<String, Object> specifications, String templateKey) {
        this.specifications = validateSpecifications(specifications);
        this.templateKey = templateKey;
    }
    
    public static OpportunitySpecification of(Map<String, Object> specifications) {
        return new OpportunitySpecification(specifications, null);
    }
    
    public static OpportunitySpecification withTemplate(
        Map<String, Object> specifications,
        String templateKey
    ) {
        return new OpportunitySpecification(specifications, templateKey);
    }
    
    /**
     * Gets specification value by key.
     * 
     * @param key specification key
     * @return specification value or null if not found
     */
    public Object get(String key) {
        return specifications.get(key);
    }
    
    /**
     * Checks if specification contains key.
     * 
     * @param key specification key
     * @return true if contains key
     */
    public boolean has(String key) {
        return specifications.containsKey(key);
    }
    
    /**
     * Gets all specifications as immutable map.
     * 
     * @return immutable specifications map
     */
    public Map<String, Object> all() {
        return specifications;
    }
    
    /**
     * Gets template key used for this specification.
     * 
     * @return template key or null if not template-based
     */
    public String templateKey() {
        return templateKey;
    }
    
    /**
     * Checks if this specification is template-based.
     * 
     * @return true if template-based
     */
    public boolean isTemplateBased() {
        return templateKey != null;
    }
    
    private Map<String, Object> validateSpecifications(Map<String, Object> specifications) {
        if (specifications == null) {
            throw new IllegalArgumentException("Specifications cannot be null");
        }
        return Collections.unmodifiableMap(specifications);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotOpportunitySpecification(other)) {
            return false;
        }
        
        OpportunitySpecification that = (OpportunitySpecification) other;
        return Objects.equals(specifications, that.specifications)
            && Objects.equals(templateKey, that.templateKey);
    }
    
    private boolean isNotOpportunitySpecification(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(specifications, templateKey);
    }
    
    @Override
    public String toString() {
        return String.format(
            "OpportunitySpecification{templateKey=%s, specifications=%s}",
            templateKey,
            specifications
        );
    }
}
