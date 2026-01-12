package com.marketplace.opportunity.adapter.output.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.model.OpportunitySpecification;
import com.marketplace.opportunity.domain.valueobject.Money;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpportunityEntityMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpportunityEntityMapper mapper = new OpportunityEntityMapper(objectMapper);

    @Test
    void mapsBetweenDomainAndEntity() {
        Opportunity opportunity = Opportunity.builder()
            .id(OpportunityId.of(99L))
            .consumerId(11L)
            .tenantId(22L)
            .title("Enterprise integration project")
            .description("Need integration with existing ERP and CRM systems with full documentation.")
            .category("Integration")
            .budget(Money.of(BigDecimal.valueOf(5000), Currency.getInstance("USD")))
            .deadline(Instant.now().plusSeconds(7200))
            .status(OpportunityStatus.DRAFT)
            .attachments(List.of("https://files.example.com/spec.pdf"))
            .specification(OpportunitySpecification.withTemplate(Map.of("erp", "SAP"), "erp-template"))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        OpportunityEntity entity = mapper.toEntity(opportunity);

        assertThat(entity.id()).isEqualTo(99L);
        assertThat(entity.attachments()).containsExactly("https://files.example.com/spec.pdf");
        assertThat(entity.templateKey()).isEqualTo("erp-template");

        Opportunity mapped = mapper.toDomain(entity);

        assertThat(mapped.id()).isEqualTo(opportunity.id());
        assertThat(mapped.specification().templateKey()).isEqualTo("erp-template");
        assertThat(mapped.specification().all()).containsEntry("erp", "SAP");
    }

    @Test
    void deserializeAttachmentsReturnsEmptyListOnInvalidJson() {
        List<String> attachments = mapper.deserializeAttachments("{invalid-json");

        assertThat(attachments).isEmpty();
    }
}
