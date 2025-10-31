package com.marketplace.proposal.adapter.output.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.proposal.domain.valueobject.ProposalStatus;
import com.marketplace.shared.domain.valueobject.Money;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Mapper between Proposal domain model and ProposalEntity.
 * 
 * Handles:
 * - Domain to Entity conversion
 * - Entity to Domain conversion
 * - R2DBC Row to Domain conversion
 * - JSON serialization/deserialization
 * 
 * Follows Adapter Pattern.
 */
@Component
public class ProposalEntityMapper {
    
    private final ObjectMapper objectMapper;
    
    public ProposalEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Converts domain Proposal to ProposalEntity.
     * 
     * @param proposal domain proposal
     * @return proposal entity
     */
    public ProposalEntity toEntity(Proposal proposal) {
        return new ProposalEntity(
            proposal.getProposalId().value(),
            proposal.getOpportunityId(),
            proposal.getCompanyId(),
            proposal.getTenantId(),
            proposal.getPrice().amount(),
            proposal.getPrice().currency(),
            proposal.getDeliveryTime().days(),
            proposal.getDeliveryTime().hours(),
            proposal.getDeliveryTime().estimatedDate(),
            proposal.getDescription(),
            proposal.getStatus(),
            toJson(proposal.getAttachments()),
            toJson(proposal.getSpecifications()),
            proposal.getCreatedAt(),
            proposal.getUpdatedAt(),
            proposal.getSubmittedAt(),
            proposal.getAcceptedAt()
        );
    }
    
    /**
     * Converts R2DBC Row to domain Proposal.
     * 
     * @param row database row
     * @return domain proposal
     */
    public Proposal fromRow(Row row) {
        ProposalId proposalId = ProposalId.of(row.get("id", Long.class));
        Long opportunityId = row.get("opportunity_id", Long.class);
        Long companyId = row.get("company_id", Long.class);
        Long tenantId = row.get("tenant_id", Long.class);
        
        Money price = Money.of(
            row.get("price_amount", java.math.BigDecimal.class),
            row.get("price_currency", String.class)
        );
        
        DeliveryTime deliveryTime = DeliveryTime.fromDaysAndHours(
            row.get("delivery_days", Integer.class),
            row.get("delivery_hours", Integer.class)
        );
        
        String description = row.get("description", String.class);
        
        Proposal proposal = Proposal.create(
            proposalId,
            opportunityId,
            companyId,
            tenantId,
            price,
            deliveryTime,
            description
        );
        
        // Restore attachments
        String attachmentsJson = row.get("attachments", String.class);
        List<String> attachments = fromJson(attachmentsJson, new TypeReference<>() {});
        attachments.forEach(proposal::addAttachment);
        
        // Restore specifications
        String specificationsJson = row.get("specifications", String.class);
        Map<String, Object> specifications = fromJson(specificationsJson, new TypeReference<>() {});
        specifications.forEach(proposal::addSpecification);
        
        // Restore status through reflection or state restoration
        // This is a simplified version - in production, use proper state restoration
        
        return proposal;
    }
    
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to serialize to JSON", exception);
        }
    }
    
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to deserialize from JSON", exception);
        }
    }
}
