package com.marketplace.opportunity.adapter.output.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.model.OpportunitySpecification;
import com.marketplace.opportunity.domain.valueobject.Money;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;
import io.r2dbc.spi.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting between domain model and database entity.
 * 
 * Follows Adapter Pattern:
 * - Adapts database representation to domain model
 * - Handles serialization/deserialization
 * - Isolates persistence concerns from domain
 * 
 * Uses Jackson for JSON serialization of complex types.
 */
@Component
public class OpportunityEntityMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(OpportunityEntityMapper.class);
    
    private final ObjectMapper objectMapper;
    
    public OpportunityEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Converts domain model to database entity.
     * 
     * @param opportunity domain opportunity
     * @return database entity
     */
    public OpportunityEntity toEntity(Opportunity opportunity) {
        return new OpportunityEntity(
            opportunity.id().value(),
            opportunity.consumerId(),
            opportunity.tenantId(),
            opportunity.title(),
            opportunity.description(),
            opportunity.category(),
            opportunity.budget().amount(),
            opportunity.budget().currencyCode(),
            opportunity.deadline(),
            opportunity.status(),
            opportunity.attachments(),
            opportunity.specification().all(),
            opportunity.specification().templateKey(),
            opportunity.createdAt(),
            opportunity.updatedAt()
        );
    }
    
    /**
     * Converts database entity to domain model.
     * 
     * @param entity database entity
     * @return domain opportunity
     */
    public Opportunity toDomain(OpportunityEntity entity) {
        OpportunityId id = OpportunityId.of(entity.id());
        Money budget = createMoney(entity.budgetAmount(), entity.budgetCurrency());
        OpportunitySpecification specification = createSpecification(
            entity.specifications(),
            entity.templateKey()
        );
        
        return Opportunity.builder()
            .id(id)
            .consumerId(entity.consumerId())
            .tenantId(entity.tenantId())
            .title(entity.title())
            .description(entity.description())
            .category(entity.category())
            .budget(budget)
            .deadline(entity.deadline())
            .status(entity.status())
            .attachments(entity.attachments())
            .specification(specification)
            .createdAt(entity.createdAt())
            .updatedAt(entity.updatedAt())
            .build();
    }
    
    /**
     * Converts R2DBC row to database entity.
     * 
     * @param row R2DBC row
     * @return database entity
     */
    public OpportunityEntity fromRow(Row row) {
        return new OpportunityEntity(
            row.get("id", Long.class),
            row.get("consumer_id", Long.class),
            row.get("tenant_id", Long.class),
            row.get("title", String.class),
            row.get("description", String.class),
            row.get("category", String.class),
            row.get("budget_amount", BigDecimal.class),
            row.get("budget_currency", String.class),
            row.get("deadline", Instant.class),
            OpportunityStatus.valueOf(row.get("status", String.class)),
            deserializeAttachments(row.get("attachments", String.class)),
            deserializeSpecifications(row.get("specifications", String.class)),
            row.get("template_key", String.class),
            row.get("created_at", Instant.class),
            row.get("updated_at", Instant.class)
        );
    }
    
    /**
     * Serializes attachments list to JSON string.
     * 
     * @param attachments attachments list
     * @return JSON string
     */
    public String serializeAttachments(List<String> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize attachments", exception);
            throw new RuntimeException("Failed to serialize attachments", exception);
        }
    }
    
    /**
     * Deserializes attachments from JSON string.
     * 
     * @param json JSON string
     * @return attachments list
     */
    public List<String> deserializeAttachments(String json) {
        if (isBlank(json)) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException exception) {
            logger.error("Failed to deserialize attachments: {}", json, exception);
            return List.of();
        }
    }
    
    /**
     * Serializes specifications map to JSON string.
     * 
     * @param specifications specifications map
     * @return JSON string
     */
    public String serializeSpecifications(Map<String, Object> specifications) {
        try {
            return objectMapper.writeValueAsString(specifications);
        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize specifications", exception);
            throw new RuntimeException("Failed to serialize specifications", exception);
        }
    }
    
    /**
     * Deserializes specifications from JSON string.
     * 
     * @param json JSON string
     * @return specifications map
     */
    public Map<String, Object> deserializeSpecifications(String json) {
        if (isBlank(json)) {
            return Map.of();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException exception) {
            logger.error("Failed to deserialize specifications: {}", json, exception);
            return Map.of();
        }
    }
    
    private Money createMoney(BigDecimal amount, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return Money.of(amount, currency);
    }
    
    private OpportunitySpecification createSpecification(
        Map<String, Object> specifications,
        String templateKey
    ) {
        if (hasTemplate(templateKey)) {
            return OpportunitySpecification.withTemplate(specifications, templateKey);
        }
        return OpportunitySpecification.of(specifications);
    }
    
    private boolean hasTemplate(String templateKey) {
        return templateKey != null && !templateKey.isBlank();
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
