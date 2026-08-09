package com.marketplace.agreement.domain.model;

import com.marketplace.agreement.domain.event.AgreementStatusChangedEvent;
import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.AgreementStatus;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import com.marketplace.shared.domain.model.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Contract & settlement aggregate ("agreement" bounded context).
 *
 * Compliance-by-design (docs/compliance/guardrails.md):
 * - The acceptance snapshot (proposal + schema + terms) is immutable and
 *   hashed — it is the contractual object; disputes become field checks.
 * - The contract is formed at acceptance but only becomes EFFECTIVE when the
 *   escrow is funded (suspensive condition, CC art. 125). The seller never
 *   ships without money retained at the PSP.
 * - The platform NEVER holds funds: escrow lives at an authorized PSP and this
 *   aggregate only tracks references and release triggers.
 * - Tracking code is mandatory before any release; the buyer address is only
 *   revealed after funding (enforced at the API layer).
 */
@Entity
@Table(name = "AGR_AGREEMENTS", indexes = {
    @Index(name = "idx_agr_event", columnList = "event_id"),
    @Index(name = "idx_agr_status", columnList = "status"),
    @Index(name = "idx_agr_buyer", columnList = "buyer_id"),
    @Index(name = "idx_agr_supplier", columnList = "supplier_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agreement extends AggregateRoot<AgreementId> {

    @EmbeddedId
    private AgreementId id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "response_id", nullable = false, length = 36)
    private String responseId;

    @Column(name = "buyer_id", nullable = false, length = 64)
    private String buyerId;

    @Column(name = "supplier_id", nullable = false, length = 64)
    private String supplierId;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AgreementStatus status;

    /** Immutable acceptance snapshot: proposal + spec + terms, canonical JSON. */
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    /** SHA-256 (hex) of the snapshot — the probative anchor of the contract. */
    @Column(name = "snapshot_hash", nullable = false, length = 64)
    private String snapshotHash;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "funding_deadline", nullable = false)
    private Instant fundingDeadline;

    @Column(name = "funded_at")
    private Instant fundedAt;

    @Column(name = "shipping_deadline")
    private Instant shippingDeadline;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "inspection_deadline")
    private Instant inspectionDeadline;

    @Column(name = "closed_at")
    private Instant closedAt;

    /** Reference of the escrow at the authorized PSP (never funds held locally). */
    @Column(name = "escrow_reference", length = 100)
    private String escrowReference;

    @Column(name = "dispute_reason", columnDefinition = "TEXT")
    private String disputeReason;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    private Agreement(
        AgreementId id,
        String tenantId,
        String eventId,
        String responseId,
        String buyerId,
        String supplierId,
        long priceCents,
        String currency,
        String snapshotJson,
        String snapshotHash,
        Instant acceptedAt,
        Instant fundingDeadline
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.eventId = eventId;
        this.responseId = responseId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.priceCents = priceCents;
        this.currency = currency;
        this.snapshotJson = snapshotJson;
        this.snapshotHash = snapshotHash;
        this.acceptedAt = acceptedAt;
        this.fundingDeadline = fundingDeadline;
        this.status = AgreementStatus.PENDING_FUNDING;
        transitionEvent(null, AgreementStatus.PENDING_FUNDING, "opened from acceptance");
    }

    /**
     * Opens the agreement from an accepted proposal.
     *
     * @param maxTicketCents kickoff escrow ceiling — above it the platform
     *                       declares itself unavailable instead of carrying
     *                       unpriced risk (guardrail).
     */
    public static Agreement open(
        AgreementId id,
        String tenantId,
        String eventId,
        String responseId,
        String buyerId,
        String supplierId,
        long priceCents,
        String currency,
        String snapshotJson,
        String snapshotHash,
        Instant acceptedAt,
        Duration fundingWindow,
        long maxTicketCents
    ) {
        Objects.requireNonNull(id, "id is required");
        requireText(tenantId, "tenantId");
        requireText(eventId, "eventId");
        requireText(responseId, "responseId");
        requireText(buyerId, "buyerId");
        requireText(supplierId, "supplierId");
        requireText(snapshotJson, "snapshotJson");
        requireText(snapshotHash, "snapshotHash");
        if (priceCents <= 0) {
            throw new IllegalArgumentException("priceCents must be positive");
        }
        if (maxTicketCents > 0 && priceCents > maxTicketCents) {
            throw new IllegalStateException(
                "Ticket above the kickoff escrow ceiling (" + maxTicketCents
                    + " cents) — the platform declares itself unavailable for this amount");
        }
        Instant accepted = acceptedAt != null ? acceptedAt : Instant.now();
        Duration window = fundingWindow != null ? fundingWindow : Duration.ofHours(48);

        return new Agreement(
            id, tenantId, eventId, responseId, buyerId, supplierId,
            priceCents,
            currency != null ? currency : "BRL",
            snapshotJson, snapshotHash,
            accepted,
            accepted.plus(window)
        );
    }

    /** Escrow funded at the PSP: the contract becomes effective. */
    public void fund(String escrowReference, Instant now, Duration shippingWindow) {
        requireStatus(AgreementStatus.PENDING_FUNDING, "fund");
        requireText(escrowReference, "escrowReference");
        Instant ref = now != null ? now : Instant.now();
        if (ref.isAfter(fundingDeadline)) {
            throw new IllegalStateException("Funding deadline has elapsed; the agreement lapsed");
        }
        this.escrowReference = escrowReference;
        this.fundedAt = ref;
        this.shippingDeadline = ref.plus(shippingWindow != null ? shippingWindow : Duration.ofDays(7));
        transition(AgreementStatus.FUNDED, "escrow funded at PSP: " + escrowReference);
    }

    /** Seller shipped — tracking code is mandatory (release precondition). */
    public void ship(String trackingCode, Instant now) {
        requireStatus(AgreementStatus.FUNDED, "ship");
        requireText(trackingCode, "trackingCode");
        this.trackingCode = trackingCode.trim();
        this.shippedAt = now != null ? now : Instant.now();
        transition(AgreementStatus.SHIPPED, "shipped, tracking=" + this.trackingCode);
    }

    /** Delivery confirmed — the inspection window starts. */
    public void markDelivered(Instant now, Duration inspectionWindow) {
        requireStatus(AgreementStatus.SHIPPED, "markDelivered");
        Instant ref = now != null ? now : Instant.now();
        this.deliveredAt = ref;
        this.inspectionDeadline = ref.plus(inspectionWindow != null ? inspectionWindow : Duration.ofHours(72));
        transition(AgreementStatus.DELIVERED, "delivered; inspection window until " + this.inspectionDeadline);
    }

    /**
     * Releases the escrow to the seller: either the buyer confirmed, or the
     * inspection window elapsed without a dispute (automatic release).
     */
    public void release(Instant now, boolean buyerConfirmed) {
        requireStatus(AgreementStatus.DELIVERED, "release");
        Instant ref = now != null ? now : Instant.now();
        if (!buyerConfirmed && inspectionDeadline != null && !ref.isAfter(inspectionDeadline)) {
            throw new IllegalStateException("Inspection window still open; only the buyer can release now");
        }
        this.closedAt = ref;
        transition(AgreementStatus.RELEASED,
            buyerConfirmed ? "released by buyer confirmation" : "auto-released after inspection window");
    }

    /** Buyer opens a dispute within the inspection window. */
    public void openDispute(String reason, Instant now) {
        requireStatus(AgreementStatus.DELIVERED, "openDispute");
        requireText(reason, "reason");
        Instant ref = now != null ? now : Instant.now();
        if (inspectionDeadline != null && ref.isAfter(inspectionDeadline)) {
            throw new IllegalStateException("Inspection window has elapsed; dispute can no longer be opened");
        }
        this.disputeReason = reason.trim();
        transition(AgreementStatus.DISPUTED, "dispute opened");
    }

    /** ODR decision executes the escrow (does not block access to the Judiciary). */
    public void resolve(ResolutionOutcome outcome, String note, Instant now) {
        requireStatus(AgreementStatus.DISPUTED, "resolve");
        Objects.requireNonNull(outcome, "outcome is required");
        this.resolutionNote = note;
        this.closedAt = now != null ? now : Instant.now();
        AgreementStatus target = switch (outcome) {
            case REFUND -> AgreementStatus.RESOLVED_REFUNDED;
            case PARTIAL -> AgreementStatus.RESOLVED_PARTIAL;
            case RELEASE -> AgreementStatus.RESOLVED_RELEASED;
        };
        transition(target, "dispute resolved: " + outcome);
    }

    /** Funding deadline elapsed without payment — contract dissolves. */
    public void lapse(Instant now) {
        requireStatus(AgreementStatus.PENDING_FUNDING, "lapse");
        Instant ref = now != null ? now : Instant.now();
        if (!ref.isAfter(fundingDeadline)) {
            throw new IllegalStateException("Funding deadline has not elapsed yet");
        }
        this.closedAt = ref;
        transition(AgreementStatus.LAPSED, "funding deadline elapsed");
    }

    /** Seller failed to ship within the deadline — full refund + penalty. */
    public void markSellerDefault(Instant now) {
        requireStatus(AgreementStatus.FUNDED, "markSellerDefault");
        Instant ref = now != null ? now : Instant.now();
        if (shippingDeadline == null || !ref.isAfter(shippingDeadline)) {
            throw new IllegalStateException("Shipping deadline has not elapsed yet");
        }
        this.closedAt = ref;
        transition(AgreementStatus.SELLER_DEFAULTED, "seller failed to ship in time");
    }

    /** Mutual cancellation, only before the contract becomes effective. */
    public void cancel(Instant now) {
        requireStatus(AgreementStatus.PENDING_FUNDING, "cancel");
        this.closedAt = now != null ? now : Instant.now();
        transition(AgreementStatus.CANCELLED, "cancelled by mutual agreement before funding");
    }

    public boolean isParty(String userId) {
        return userId != null && (userId.equals(buyerId) || userId.equals(supplierId));
    }

    private void requireStatus(AgreementStatus expected, String operation) {
        if (this.status != expected) {
            throw new IllegalStateException(
                "Cannot " + operation + " an agreement in status " + this.status + " (requires " + expected + ")");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private void transition(AgreementStatus to, String detail) {
        AgreementStatus from = this.status;
        this.status = to;
        transitionEvent(from, to, detail);
        markAsUpdated();
    }

    private void transitionEvent(AgreementStatus from, AgreementStatus to, String detail) {
        addDomainEvent(new AgreementStatusChangedEvent(
            id != null ? id.asString() : "",
            from,
            to,
            detail
        ));
    }

    @Override
    public void validate() {
        if (id == null) throw new IllegalStateException("id is required");
        if (status == null) throw new IllegalStateException("status is required");
        if (snapshotHash == null || snapshotHash.isBlank()) throw new IllegalStateException("snapshotHash is required");
    }
}
