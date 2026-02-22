package com.marketplace.sourcing.domain.model;

import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class SourcingEventTest {

    private SourcingEvent newDraftEvent() {
        Instant now = Instant.now();
        SourcingEventTimeline timeline = SourcingEventTimeline.create(
            now,
            now.plusSeconds(10),
            now.plus(Duration.ofHours(3)),
            now.plus(Duration.ofHours(4)),
            now.plus(Duration.ofHours(6)),
            now.plus(Duration.ofHours(8)),
            now.plus(Duration.ofHours(12)),
            2
        );

        return SourcingEvent.create(
            SourcingEventId.of(1L),
            BuyerContext.of("tenant-1", "buyer-123", "buyer-contact-1", "Maria Compras", "+5527999999999"),
            "Compra de notebooks",
            "Compra corporativa de 20 notebooks",
            SourcingEventType.RFQ,
            ProductSpecification.of("Notebook 15\"", "Intel i7", "Informatica", "UN", 20L),
            timeline,
            SourcingEventSettings.defaults(),
            Money.of(50000, CurrencyCode.BRL),
            Set.of("supplier-1", "supplier-2")
        );
    }

    @Test
    void shouldFollowHappyPathLifecycle() {
        SourcingEvent event = newDraftEvent();
        Instant reference = Instant.now();

        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.DRAFT);

        event.publish(reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.PUBLISHED);

        event.start(reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.IN_PROGRESS);

        event.closeSubmissions(reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.SUBMISSION_CLOSED);

        event.beginEvaluation(reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.UNDER_EVALUATION);

        event.beginNegotiation(reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.NEGOTIATION);

        event.award("supplier-1", Money.of(45000, CurrencyCode.BRL), reference);
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.AWARDED);
        assertThat(event.getAwardedSupplierId()).isEqualTo("supplier-1");
    }

    @Test
    void shouldAutoExtendSubmissionWhenLowParticipation() {
        SourcingEvent event = newDraftEvent();
        event.publish(Instant.now());

        Instant originalDeadline = event.getTimeline().getSubmissionDeadline();

        // first response -> below minimum participants (defaults=2) triggers extension of 60 minutes
        event.registerResponse();

        assertThat(event.getTimeline().getSubmissionDeadline())
            .isAfter(originalDeadline)
            .isEqualTo(originalDeadline.plus(Duration.ofMinutes(event.getSettings().getAutoExtendMinutes())));
    }

    @Test
    void shouldSuspendAndResume() {
        SourcingEvent event = newDraftEvent();
        event.publish(Instant.now());
        event.start(Instant.now());

        event.suspend("compliance check", Instant.now());
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.SUSPENDED);

        event.resume(Instant.now());
        assertThat(event.getStatus()).isEqualTo(SourcingEventStatus.IN_PROGRESS);
    }

    @Test
    void shouldValidateAwardAmountNotExcessive() {
        SourcingEvent event = newDraftEvent();
        event.publish(Instant.now());
        event.start(Instant.now());
        event.closeSubmissions(Instant.now());
        event.beginEvaluation(Instant.now());

        assertThatThrownBy(() -> event.award("supplier-1", Money.of(200000, CurrencyCode.BRL), Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("excessively higher");
    }
}
