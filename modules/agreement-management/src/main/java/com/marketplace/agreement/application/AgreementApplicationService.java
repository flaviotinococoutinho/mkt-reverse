package com.marketplace.agreement.application;

import com.marketplace.agreement.application.port.EscrowGateway;
import com.marketplace.agreement.application.port.EscrowIdempotency;
import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.repository.AgreementRepository;
import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import com.marketplace.shared.events.DomainEventPublisher;
import com.marketplace.shared.id.IdGenerator;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service of the "agreement" context (contract & settlement).
 *
 * Kickoff limits are injected from configuration and documented in
 * docs/compliance/guardrails.md — they only go up by explicit decision:
 * - escrow ticket ceiling (default R$ 3.000)
 * - funding window (default 48h), shipping window (default 7d),
 *   delivery window (default 15d), inspection window (default 72h)
 *
 * Money-path invariants:
 * - the domain is validated BEFORE any PSP command (a JPA rollback does not
 *   undo money movements);
 * - every PSP command carries a deterministic idempotency key so retries
 *   after a crash never move money twice;
 * - scheduler sweeps run one transaction PER agreement — a poison item is
 *   skipped and retried on the next tick instead of reverting the whole
 *   batch after escrow commands were already emitted.
 */
@Service
public class AgreementApplicationService {

    private final AgreementRepository agreementRepository;
    private final SourcingEventRepository sourcingEventRepository;
    private final SupplierResponseRepository supplierResponseRepository;
    private final EscrowGateway escrowGateway;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    private final long maxTicketCents;
    private final Duration fundingWindow;
    private final Duration shippingWindow;
    private final Duration deliveryWindow;
    private final Duration inspectionWindow;

    public AgreementApplicationService(
        AgreementRepository agreementRepository,
        SourcingEventRepository sourcingEventRepository,
        SupplierResponseRepository supplierResponseRepository,
        EscrowGateway escrowGateway,
        IdGenerator idGenerator,
        ObjectMapper objectMapper,
        DomainEventPublisher domainEventPublisher,
        @Value("${marketplace.escrow.max-ticket-cents:300000}") long maxTicketCents,
        @Value("${marketplace.escrow.funding-window-hours:48}") long fundingWindowHours,
        @Value("${marketplace.escrow.shipping-window-days:7}") long shippingWindowDays,
        @Value("${marketplace.escrow.delivery-window-days:15}") long deliveryWindowDays,
        @Value("${marketplace.escrow.inspection-window-hours:72}") long inspectionWindowHours
    ) {
        this.agreementRepository = agreementRepository;
        this.sourcingEventRepository = sourcingEventRepository;
        this.supplierResponseRepository = supplierResponseRepository;
        this.escrowGateway = escrowGateway;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
        this.domainEventPublisher = domainEventPublisher;
        this.maxTicketCents = maxTicketCents;
        this.fundingWindow = Duration.ofHours(fundingWindowHours);
        this.shippingWindow = Duration.ofDays(shippingWindowDays);
        this.deliveryWindow = Duration.ofDays(deliveryWindowDays);
        this.inspectionWindow = Duration.ofHours(inspectionWindowHours);
    }

    /**
     * Opens the agreement right after a proposal is accepted (same transaction
     * as the acceptance when called from the acceptance coordinator).
     * The acceptance snapshot is canonical JSON + SHA-256 — the contractual
     * object that turns disputes into field checks.
     */
    @Transactional
    public Agreement openFromAcceptance(String eventId, String responseId) {
        if (agreementRepository.existsByResponseId(responseId)) {
            throw new IllegalStateException("An agreement already exists for response " + responseId);
        }

        SourcingEvent event = sourcingEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sourcing event not found: " + eventId));
        SupplierResponse response = supplierResponseRepository.findById(SupplierResponseId.of(responseId))
            .orElseThrow(() -> new IllegalArgumentException("Response not found: " + responseId));

        String snapshotJson = buildSnapshotJson(event, response);
        String snapshotHash = sha256Hex(snapshotJson);

        long priceCents = response.getOfferAmount() != null
            ? response.getOfferAmount().getAmount().movePointRight(2).longValueExact()
            : 0L;
        String currency = response.getOfferAmount() != null
            ? response.getOfferAmount().getCurrency().name()
            : "BRL";

        Agreement agreement = Agreement.open(
            AgreementId.of(idGenerator.nextId()),
            event.getBuyerContext().getTenantId(),
            eventId,
            responseId,
            event.getBuyerContext().getContactId(),
            response.getSupplierId(),
            priceCents,
            currency,
            snapshotJson,
            snapshotHash,
            Instant.now(),
            fundingWindow,
            maxTicketCents
        );

        return saveAndPublish(agreement);
    }

    @Transactional
    public Agreement fund(String agreementId, String userId, boolean isAdmin) {
        Agreement agreement = requireAgreement(agreementId);
        if (!isAdmin && !agreement.getBuyerId().equals(userId)) {
            throw new AccessDeniedToAgreementException("Only the buyer can fund the escrow");
        }
        Instant now = Instant.now();
        agreement.requireFundable(now);
        String reference = escrowGateway.fund(agreement, EscrowIdempotency.fundKey(agreement));
        agreement.fund(reference, now, shippingWindow);
        return saveAndPublish(agreement);
    }

    @Transactional
    public Agreement ship(String agreementId, String trackingCode, String userId, boolean isAdmin) {
        Agreement agreement = requireAgreement(agreementId);
        if (!isAdmin && !agreement.getSupplierId().equals(userId)) {
            throw new AccessDeniedToAgreementException("Only the seller of this agreement can register shipment");
        }
        agreement.ship(trackingCode, Instant.now(), deliveryWindow);
        return saveAndPublish(agreement);
    }

    /**
     * Delivery confirmation belongs to the BUYER (or admin/carrier webhook in
     * the future) — never the seller, or a fake tracking code plus the
     * inspection window would auto-release the escrow without merchandise.
     */
    @Transactional
    public Agreement markDelivered(String agreementId, String userId, boolean isAdmin) {
        Agreement agreement = requireAgreement(agreementId);
        if (!isAdmin && !agreement.getBuyerId().equals(userId)) {
            throw new AccessDeniedToAgreementException("Only the buyer can confirm delivery");
        }
        agreement.markDelivered(Instant.now(), inspectionWindow);
        return saveAndPublish(agreement);
    }

    @Transactional
    public Agreement release(String agreementId, String userId, boolean isAdmin) {
        Agreement agreement = requireAgreement(agreementId);
        boolean buyerConfirmed = agreement.getBuyerId().equals(userId);
        if (!isAdmin && !buyerConfirmed) {
            throw new AccessDeniedToAgreementException("Only the buyer can release the escrow before the window elapses");
        }
        agreement.release(Instant.now(), buyerConfirmed || isAdmin);
        escrowGateway.release(agreement, EscrowIdempotency.releaseKey(agreement));
        return saveAndPublish(agreement);
    }

    @Transactional
    public Agreement openDispute(String agreementId, String reason, String userId) {
        Agreement agreement = requireAgreement(agreementId);
        if (!agreement.getBuyerId().equals(userId)) {
            throw new AccessDeniedToAgreementException("Only the buyer can open a dispute");
        }
        agreement.openDispute(reason, Instant.now());
        return saveAndPublish(agreement);
    }

    /** ODR decision — admin only (enforced at the API layer). */
    @Transactional
    public Agreement resolve(String agreementId, ResolutionOutcome outcome, String note) {
        Agreement agreement = requireAgreement(agreementId);
        agreement.resolve(outcome, note, Instant.now());
        escrowGateway.resolve(agreement, outcome, EscrowIdempotency.resolveKey(agreement));
        return saveAndPublish(agreement);
    }

    public Agreement get(String agreementId, String userId, boolean isAdmin) {
        Agreement agreement = requireAgreement(agreementId);
        if (!isAdmin && !agreement.isParty(userId)) {
            throw new AccessDeniedToAgreementException("Agreement is only visible to its parties");
        }
        return agreement;
    }

    public List<Agreement> findByEvent(String eventId, String userId, boolean isAdmin) {
        return agreementRepository.findByEventId(eventId).stream()
            .filter(a -> isAdmin || a.isParty(userId))
            .toList();
    }

    // ── Scheduler entry points (system actions) ──────────────────────────
    //
    // The scheduler iterates candidate ids and calls the *One methods, one
    // transaction per agreement (the candidate listing itself is not
    // transactional). Each *One re-validates the state inside its own
    // transaction, so a candidate that changed between listing and
    // processing fails fast without touching the PSP.

    public List<String> findLapseCandidates(Instant reference) {
        return ids(agreementRepository.findPendingFundingExpired(reference));
    }

    public List<String> findSellerDefaultCandidates(Instant reference) {
        return ids(agreementRepository.findFundedShippingExpired(reference));
    }

    public List<String> findDeliveryOverdueCandidates(Instant reference) {
        return ids(agreementRepository.findShippedDeliveryExpired(reference));
    }

    public List<String> findAutoReleaseCandidates(Instant reference) {
        return ids(agreementRepository.findDeliveredInspectionExpired(reference));
    }

    @Transactional
    public void lapseOne(String agreementId, Instant reference) {
        Agreement agreement = requireAgreement(agreementId);
        agreement.lapse(reference);
        saveAndPublish(agreement);
    }

    @Transactional
    public void sellerDefaultOne(String agreementId, Instant reference) {
        Agreement agreement = requireAgreement(agreementId);
        agreement.markSellerDefault(reference);
        escrowGateway.refund(agreement, EscrowIdempotency.refundKey(agreement));
        saveAndPublish(agreement);
    }

    @Transactional
    public void deliveryOverdueOne(String agreementId, Instant reference) {
        Agreement agreement = requireAgreement(agreementId);
        agreement.markDeliveryOverdue(reference);
        escrowGateway.refund(agreement, EscrowIdempotency.refundKey(agreement));
        saveAndPublish(agreement);
    }

    @Transactional
    public void autoReleaseOne(String agreementId, Instant reference) {
        Agreement agreement = requireAgreement(agreementId);
        agreement.release(reference, false);
        escrowGateway.release(agreement, EscrowIdempotency.releaseKey(agreement));
        saveAndPublish(agreement);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Agreement saveAndPublish(Agreement agreement) {
        Agreement saved = agreementRepository.save(agreement);
        domainEventPublisher.publishAll(saved.getDomainEvents());
        saved.clearDomainEvents();
        return saved;
    }

    private static List<String> ids(List<Agreement> agreements) {
        return agreements.stream().map(a -> a.getId().asString()).toList();
    }

    private Agreement requireAgreement(String agreementId) {
        return agreementRepository.findById(AgreementId.of(agreementId))
            .orElseThrow(() -> new IllegalArgumentException("Agreement not found: " + agreementId));
    }

    private String buildSnapshotJson(SourcingEvent event, SupplierResponse response) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("schemaVersion", "1.0");
        snapshot.put("eventId", event.getId().asString());
        snapshot.put("eventTitle", event.getTitle());
        snapshot.put("tenantId", event.getBuyerContext().getTenantId());
        snapshot.put("buyerId", event.getBuyerContext().getContactId());

        Map<String, Object> spec = new LinkedHashMap<>();
        if (event.getProductSpecification() != null) {
            var ps = event.getProductSpecification();
            spec.put("productName", ps.getProductName());
            spec.put("mccCategoryCode", ps.getMccCategoryCode());
            spec.put("quantityRequired", ps.getQuantityRequired());
            spec.put("unitOfMeasure", ps.getUnitOfMeasure());
            spec.put("attributes", ps.getAttributesList());
        }
        snapshot.put("productSpecification", spec);

        Map<String, Object> proposal = new LinkedHashMap<>();
        proposal.put("responseId", response.getId().asString());
        proposal.put("supplierId", response.getSupplierId());
        proposal.put("offerAmount", response.getOfferAmount() != null ? response.getOfferAmount().getAmount() : null);
        proposal.put("offerCurrency", response.getOfferAmount() != null ? response.getOfferAmount().getCurrency().name() : null);
        proposal.put("condition", response.getCondition() != null ? response.getCondition().name() : null);
        proposal.put("shippingMode", response.getShippingMode() != null ? response.getShippingMode().name() : null);
        proposal.put("leadTimeDays", response.getLeadTimeDays());
        proposal.put("warrantyMonths", response.getWarrantyMonths());
        proposal.put("attributes", response.getAttributesList());
        proposal.put("submittedAt", response.getSubmittedAt() != null ? response.getSubmittedAt().toString() : null);
        snapshot.put("proposal", proposal);

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize acceptance snapshot", e);
        }
    }

    private static String sha256Hex(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Party check failure — mapped to 403 at the API layer. */
    public static class AccessDeniedToAgreementException extends RuntimeException {
        public AccessDeniedToAgreementException(String message) {
            super(message);
        }
    }
}
