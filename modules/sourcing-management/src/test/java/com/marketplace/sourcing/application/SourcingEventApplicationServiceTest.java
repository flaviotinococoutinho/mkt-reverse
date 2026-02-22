package com.marketplace.sourcing.application;

import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.application.port.input.SourcingEventUseCases;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.marketplace.sourcing.TestSourcingManagementApplication.class)
@ActiveProfiles("test")
class SourcingEventApplicationServiceTest {

    @Autowired
    private SourcingEventUseCases service;

    @Autowired
    private SourcingEventRepository sourcingEventRepository;

    @Test
    void create_publish_submit_and_accept_flow_works() {
        var spec = ProductSpecification.of(
            "Peça X",
            "Compatível com modelo Y",
            "part",
            "unit",
            1L
        ).withMccCategoryCode(5533);

        var eventId = service.createAndPublishEvent(
            "tenant-1",
            "org-1",
            "contact-1",
            "Comprador",
            "+5527999999999",
            null,
            "Quero a peça X",
            "Detalhes do pedido",
            SourcingEventType.RFQ,
            spec,
            Instant.now().plusSeconds(3600),
            Money.of(500, CurrencyCode.BRL)
        );

        var event = service.getEvent(eventId.asString());
        assertThat(event.getId().asString()).isEqualTo(eventId.asString());
        assertThat(event.getStatus().name()).isEqualTo("PUBLISHED");

        var responseId = service.submitResponse(
            eventId.asString(),
            "supplier-1",
            Money.of(200, CurrencyCode.BRL),
            "Tenho a peça em estoque",
            3,
            3,
            com.marketplace.sourcing.domain.valueobject.OfferCondition.USED,
            com.marketplace.sourcing.domain.valueobject.ShippingMode.PICKUP,
            java.util.List.of(new com.marketplace.sourcing.domain.valueobject.SpecAttribute(
                "voltage",
                com.marketplace.sourcing.domain.valueobject.SpecAttributeType.VOLTAGE,
                "V",
                220
            ))
        );

        var responses = service.listResponses(eventId.asString());
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId().asString()).isEqualTo(responseId.asString());

        service.acceptResponse(eventId.asString(), responseId.asString());

        var updatedEvent = service.getEvent(eventId.asString());
        assertThat(updatedEvent.getStatus().name()).isEqualTo("AWARDED");
        assertThat(updatedEvent.getAwardedSupplierId()).isEqualTo("supplier-1");

        var updatedResponses = service.listResponses(eventId.asString());
        assertThat(updatedResponses.getFirst().getStatus().name()).isEqualTo("ACCEPTED");
    }

    @Test
    void findBySupplierParticipation_includes_events_with_supplier_response() {
        var spec = ProductSpecification.of(
            "Peça X",
            "Compatível com modelo Y",
            "part",
            "unit",
            1L
        ).withMccCategoryCode(5533);

        var eventId = service.createAndPublishEvent(
            "tenant-1",
            "org-1",
            "contact-1",
            "Comprador",
            "+5527999999999",
            null,
            "Quero a peça X",
            "Detalhes do pedido",
            SourcingEventType.RFQ,
            spec,
            Instant.now().plusSeconds(3600),
            Money.of(500, CurrencyCode.BRL)
        );

        var responseId = service.submitResponse(
            eventId.asString(),
            "supplier-1",
            Money.of(200, CurrencyCode.BRL),
            "Tenho a peça em estoque",
            3,
            3,
            com.marketplace.sourcing.domain.valueobject.OfferCondition.USED,
            com.marketplace.sourcing.domain.valueobject.ShippingMode.PICKUP,
            java.util.List.of()
        );

        assertThat(responseId).isNotNull();
        service.acceptResponse(eventId.asString(), responseId.asString());

        var statuses = EnumSet.of(SourcingEventStatus.PUBLISHED, SourcingEventStatus.IN_PROGRESS, SourcingEventStatus.AWARDED);
        var participating = sourcingEventRepository.findBySupplierParticipation("supplier-1", statuses);

        assertThat(participating)
            .extracting(e -> e.getId().asString())
            .contains(eventId.asString());
    }
}
