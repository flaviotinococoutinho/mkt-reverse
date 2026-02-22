package com.marketplace.sourcing.infrastructure.search;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDocument {
    public static final String INDEX_NAME = "opportunities";

    private String id;
    private String title;
    private String description;
    private String status;
    private Integer mccCategoryCode;
    private Instant publicationDate;
    private Instant submissionDeadline;
    private String buyerTenantId;
    
    // Flattened location for search
    private String buyerLocation; 

    public static OpportunityDocument from(SourcingEvent event) {
        return OpportunityDocument.builder()
            .id(event.getId().asString())
            .title(event.getTitle())
            .description(event.getDescription())
            .status(event.getStatus().name())
            .mccCategoryCode(event.getProductSpecification().getMccCategoryCode())
            .publicationDate(event.getTimeline().getPublicationAt())
            .submissionDeadline(event.getTimeline().getSubmissionDeadline())
            .buyerTenantId(event.getBuyerContext().getTenantId())
            // In a real scenario we would fetch address, for now just placeholder or context data
            .buyerLocation("Unknown") 
            .build();
    }
}
