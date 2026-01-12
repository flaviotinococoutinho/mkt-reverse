package com.marketplace.proposal.adapter.output.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.shared.domain.valueobject.Money;
import io.r2dbc.spi.Row;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProposalEntityMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProposalEntityMapper mapper = new ProposalEntityMapper(objectMapper);

    @Test
    void toEntitySerializesCollections() throws Exception {
        Proposal proposal = Proposal.create(
            ProposalId.of(77L),
            44L,
            33L,
            22L,
            Money.of(BigDecimal.valueOf(900), "USD"),
            DeliveryTime.fromDaysAndHours(3, 5),
            "A detailed proposal describing the approach, deliverables, and timeline for delivery."
        );
        proposal.addAttachment("https://files.example.com/quote.pdf");
        proposal.addSpecification("service", "Design");

        ProposalEntity entity = mapper.toEntity(proposal);

        List<String> attachments = objectMapper.readValue(entity.attachmentsJson(), new TypeReference<>() {});
        Map<String, Object> specifications = objectMapper.readValue(entity.specificationsJson(), new TypeReference<>() {});

        assertThat(entity.id()).isEqualTo(77L);
        assertThat(attachments).containsExactly("https://files.example.com/quote.pdf");
        assertThat(specifications).containsEntry("service", "Design");
    }

    @Test
    void fromRowRestoresAttachmentsAndSpecifications() {
        Row row = Mockito.mock(Row.class);

        when(row.get("id", Long.class)).thenReturn(88L);
        when(row.get("opportunity_id", Long.class)).thenReturn(55L);
        when(row.get("company_id", Long.class)).thenReturn(66L);
        when(row.get("tenant_id", Long.class)).thenReturn(77L);
        when(row.get("price_amount", BigDecimal.class)).thenReturn(BigDecimal.valueOf(1500));
        when(row.get("price_currency", String.class)).thenReturn("USD");
        when(row.get("delivery_days", Integer.class)).thenReturn(2);
        when(row.get("delivery_hours", Integer.class)).thenReturn(4);
        when(row.get("description", String.class))
            .thenReturn("Proposal description with enough detail to satisfy validation length.");
        when(row.get("attachments", String.class)).thenReturn("[\"https://files.example.com/specs.pdf\"]");
        when(row.get("specifications", String.class)).thenReturn("{\"tool\":\"Figma\"}");

        Proposal proposal = mapper.fromRow(row);

        assertThat(proposal.getProposalId()).isEqualTo(ProposalId.of(88L));
        assertThat(proposal.getAttachments()).containsExactly("https://files.example.com/specs.pdf");
        assertThat(proposal.getSpecifications()).containsEntry("tool", "Figma");
    }
}
