package com.marketplace.sourcing.domain.model;

import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.valueobject.BuyerContext;
import com.marketplace.sourcing.domain.valueobject.OfferCondition;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.ShippingMode;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventSettings;
import com.marketplace.sourcing.domain.valueobject.SourcingEventTimeline;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kickoff guardrails of the sealed-proposal mechanics
 * (docs/compliance/guardrails.md — Descoberta & Propostas).
 */
class SealedProposalGuardrailsTest {

    private SourcingEvent newPublishedEvent() {
        Instant now = Instant.now();
        SourcingEventTimeline timeline = SourcingEventTimeline.create(
            now, now,
            now.plusSeconds(3600 * 48),
            now.plusSeconds(3600 * 49),
            now.plusSeconds(3600 * 72),
            null,
            now.plusSeconds(3600 * 96),
            1
        );
        SourcingEvent event = SourcingEvent.create(
            SourcingEventId.of(1L),
            BuyerContext.of("tenant-default", "org-1", null, "buyer-1", "Comprador", "+5527999999999", null),
            "Quero item raro",
            "Detalhes",
            SourcingEventType.RFQ,
            ProductSpecification.of("Item", null, "part", "UN", 1),
            timeline,
            SourcingEventSettings.defaults(),
            Money.zero(CurrencyCode.BRL),
            null
        );
        event.publish(now);
        return event;
    }

    @Test
    void intentAcceptsAtMostSevenSealedProposals() {
        SourcingEvent event = newPublishedEvent();

        for (int i = 0; i < SourcingEvent.MAX_SEALED_PROPOSALS; i++) {
            event.registerResponse();
        }
        assertThat(event.getResponsesCount()).isEqualTo(SourcingEvent.MAX_SEALED_PROPOSALS);

        assertThatThrownBy(event::registerResponse)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("maximum of " + SourcingEvent.MAX_SEALED_PROPOSALS);
    }

    @Test
    void proposalHasDefault72hValidity() {
        SupplierResponse response = SupplierResponse.submit(
            SupplierResponseId.of(10L),
            SourcingEventId.of(1L),
            "supplier-1",
            Money.fromCents(19_900L, CurrencyCode.BRL),
            "Tenho em estoque",
            3, 1,
            OfferCondition.USED,
            ShippingMode.PICKUP,
            null
        );

        assertThat(response.getValidUntil()).isNotNull();
        assertThat(response.isExpired(Instant.now())).isFalse();
        assertThat(response.isExpired(Instant.now().plus(SupplierResponse.DEFAULT_VALIDITY).plusSeconds(60))).isTrue();
    }

    @Test
    void expiredProposalCannotBeAccepted() {
        SupplierResponse response = SupplierResponse.submit(
            SupplierResponseId.of(11L),
            SourcingEventId.of(1L),
            "supplier-1",
            Money.fromCents(19_900L, CurrencyCode.BRL),
            null,
            3, 1,
            OfferCondition.USED,
            ShippingMode.PICKUP,
            null,
            Instant.now().minusSeconds(60)
        );

        assertThatThrownBy(() -> response.accept(Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expired");
    }
}
