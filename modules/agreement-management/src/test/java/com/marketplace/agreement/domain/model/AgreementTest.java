package com.marketplace.agreement.domain.model;

import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.AgreementStatus;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgreementTest {

    private static final Duration FUNDING = Duration.ofHours(48);
    private static final Duration SHIPPING = Duration.ofDays(7);
    private static final Duration INSPECTION = Duration.ofHours(72);
    private static final long CEILING = 300_000L;

    private Agreement newAgreement() {
        return newAgreement(19_900L, Instant.now());
    }

    private Agreement newAgreement(long priceCents, Instant acceptedAt) {
        return Agreement.open(
            AgreementId.of(1L),
            "tenant-default",
            "111",
            "222",
            "buyer-user",
            "supplier-user",
            priceCents,
            "BRL",
            "{\"schemaVersion\":\"1.0\"}",
            "abc123",
            acceptedAt,
            FUNDING,
            CEILING
        );
    }

    @Test
    void opensPendingFundingWithDeadline() {
        Instant accepted = Instant.now();
        Agreement agreement = newAgreement(19_900L, accepted);

        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.PENDING_FUNDING);
        assertThat(agreement.getFundingDeadline()).isEqualTo(accepted.plus(FUNDING));
        assertThat(agreement.getDomainEvents()).isNotEmpty();
    }

    @Test
    void rejectsTicketAboveKickoffCeiling() {
        assertThatThrownBy(() -> newAgreement(CEILING + 1, Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ceiling");
    }

    @Test
    void happyPathReachesReleased() {
        Agreement agreement = newAgreement();

        agreement.fund("psp-ref-1", Instant.now(), SHIPPING);
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.FUNDED);
        assertThat(agreement.getEscrowReference()).isEqualTo("psp-ref-1");
        assertThat(agreement.getShippingDeadline()).isNotNull();

        agreement.ship("BR123456789", Instant.now());
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.SHIPPED);

        agreement.markDelivered(Instant.now(), INSPECTION);
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.DELIVERED);
        assertThat(agreement.getInspectionDeadline()).isNotNull();

        agreement.release(Instant.now(), true);
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.RELEASED);
        assertThat(agreement.getStatus().isTerminal()).isTrue();
    }

    @Test
    void cannotShipWithoutFunding() {
        Agreement agreement = newAgreement();

        assertThatThrownBy(() -> agreement.ship("BR1", Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PENDING_FUNDING");
    }

    @Test
    void shipRequiresTrackingCode() {
        Agreement agreement = newAgreement();
        agreement.fund("psp-ref", Instant.now(), SHIPPING);

        assertThatThrownBy(() -> agreement.ship("  ", Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("trackingCode");
    }

    @Test
    void fundingAfterDeadlineFails() {
        Agreement agreement = newAgreement(19_900L, Instant.now().minus(Duration.ofHours(72)));

        assertThatThrownBy(() -> agreement.fund("psp-ref", Instant.now(), SHIPPING))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("lapsed");
    }

    @Test
    void autoReleaseOnlyAfterInspectionWindow() {
        Agreement agreement = newAgreement();
        agreement.fund("psp-ref", Instant.now(), SHIPPING);
        agreement.ship("BR1", Instant.now());
        agreement.markDelivered(Instant.now(), INSPECTION);

        assertThatThrownBy(() -> agreement.release(Instant.now(), false))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Inspection window still open");

        agreement.release(agreement.getInspectionDeadline().plusSeconds(1), false);
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.RELEASED);
    }

    @Test
    void disputeWithinWindowThenResolveRefund() {
        Agreement agreement = newAgreement();
        agreement.fund("psp-ref", Instant.now(), SHIPPING);
        agreement.ship("BR1", Instant.now());
        agreement.markDelivered(Instant.now(), INSPECTION);

        agreement.openDispute("Item veio paralelo, proposta declarava original=true", Instant.now());
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.DISPUTED);

        agreement.resolve(ResolutionOutcome.REFUND, "Atributo divergente do snapshot", Instant.now());
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.RESOLVED_REFUNDED);
    }

    @Test
    void disputeAfterWindowFails() {
        Agreement agreement = newAgreement();
        agreement.fund("psp-ref", Instant.now(), SHIPPING);
        agreement.ship("BR1", Instant.now());
        agreement.markDelivered(Instant.now().minus(Duration.ofHours(100)), INSPECTION);

        assertThatThrownBy(() -> agreement.openDispute("tarde demais", Instant.now()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("elapsed");
    }

    @Test
    void lapsesWhenFundingDeadlineElapses() {
        Agreement agreement = newAgreement(19_900L, Instant.now().minus(Duration.ofHours(72)));

        agreement.lapse(Instant.now());
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.LAPSED);
    }

    @Test
    void sellerDefaultWhenShippingDeadlineElapses() {
        Agreement agreement = newAgreement();
        agreement.fund("psp-ref", Instant.now().minus(Duration.ofDays(10)).plus(FUNDING).minus(FUNDING), SHIPPING);
        // funded "now"; simulate reference after shipping deadline
        Instant afterDeadline = agreement.getShippingDeadline().plusSeconds(1);

        agreement.markSellerDefault(afterDeadline);
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.SELLER_DEFAULTED);
    }

    @Test
    void cancelOnlyBeforeFunding() {
        Agreement agreement = newAgreement();
        agreement.cancel(Instant.now());
        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.CANCELLED);

        Agreement funded = newAgreement();
        funded.fund("psp-ref", Instant.now(), SHIPPING);
        assertThatThrownBy(() -> funded.cancel(Instant.now()))
            .isInstanceOf(IllegalStateException.class);
    }
}
