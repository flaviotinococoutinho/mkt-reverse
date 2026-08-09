package com.marketplace.gateway.api;

import com.marketplace.agreement.application.AgreementApplicationService;
import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contract & settlement API (agreement context).
 *
 * Visibility is restricted to the agreement parties; the ODR resolution is
 * admin-only. The escrow itself lives at an authorized PSP — these endpoints
 * only command state transitions and release triggers.
 */
@RestController
@RequestMapping("/api/v1/agreements")
public class AgreementController {

    private final AgreementApplicationService service;

    public AgreementController(AgreementApplicationService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public AgreementView get(@PathVariable String id, Authentication auth) {
        return AgreementView.from(service.get(id, userId(auth), isAdmin(auth)));
    }

    @GetMapping
    public List<AgreementView> listByEvent(@RequestParam String eventId, Authentication auth) {
        return service.findByEvent(eventId, userId(auth), isAdmin(auth)).stream()
            .map(AgreementView::from)
            .toList();
    }

    /** Buyer funds the escrow at the PSP — the contract becomes effective. */
    @PostMapping("/{id}/fund")
    public AgreementView fund(@PathVariable String id, Authentication auth) {
        return AgreementView.from(service.fund(id, userId(auth), isAdmin(auth)));
    }

    /** Seller registers shipment; tracking code is mandatory. */
    @PostMapping("/{id}/ship")
    public AgreementView ship(
        @PathVariable String id,
        @Valid @RequestBody ShipRequest req,
        Authentication auth
    ) {
        return AgreementView.from(service.ship(id, req.trackingCode(), userId(auth), isAdmin(auth)));
    }

    /** Delivery confirmation (MVP: by a party; carrier webhook is roadmap). */
    @PostMapping("/{id}/deliver")
    public AgreementView deliver(@PathVariable String id, Authentication auth) {
        return AgreementView.from(service.markDelivered(id, userId(auth), isAdmin(auth)));
    }

    /** Buyer confirms and releases the escrow to the seller. */
    @PostMapping("/{id}/release")
    public AgreementView release(@PathVariable String id, Authentication auth) {
        return AgreementView.from(service.release(id, userId(auth), isAdmin(auth)));
    }

    /** Buyer opens a dispute within the inspection window. */
    @PostMapping("/{id}/dispute")
    public AgreementView dispute(
        @PathVariable String id,
        @Valid @RequestBody DisputeRequest req,
        Authentication auth
    ) {
        return AgreementView.from(service.openDispute(id, req.reason(), userId(auth)));
    }

    /** ODR decision — executes the escrow; does not close the judicial route. */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AgreementView resolve(
        @PathVariable String id,
        @Valid @RequestBody ResolveRequest req
    ) {
        return AgreementView.from(service.resolve(id, req.outcome(), req.note()));
    }

    private static String userId(Authentication auth) {
        return auth != null ? auth.getName() : null;
    }

    private static boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public record ShipRequest(@NotBlank String trackingCode) {}

    public record DisputeRequest(@NotBlank String reason) {}

    public record ResolveRequest(ResolutionOutcome outcome, String note) {}

    public record AgreementView(
        String id,
        String eventId,
        String responseId,
        String buyerId,
        String supplierId,
        long priceCents,
        String currency,
        String status,
        String snapshotHash,
        String trackingCode,
        String escrowReference,
        String fundingDeadline,
        String shippingDeadline,
        String inspectionDeadline,
        String disputeReason,
        String resolutionNote
    ) {
        static AgreementView from(Agreement a) {
            return new AgreementView(
                a.getId().asString(),
                a.getEventId(),
                a.getResponseId(),
                a.getBuyerId(),
                a.getSupplierId(),
                a.getPriceCents(),
                a.getCurrency(),
                a.getStatus().name(),
                a.getSnapshotHash(),
                a.getTrackingCode(),
                a.getEscrowReference(),
                a.getFundingDeadline() != null ? a.getFundingDeadline().toString() : null,
                a.getShippingDeadline() != null ? a.getShippingDeadline().toString() : null,
                a.getInspectionDeadline() != null ? a.getInspectionDeadline().toString() : null,
                a.getDisputeReason(),
                a.getResolutionNote()
            );
        }
    }

    /** Party-check failures from the application service map to 403. */
    @ExceptionHandler(AgreementApplicationService.AccessDeniedToAgreementException.class)
    public ResponseEntity<org.springframework.http.ProblemDetail> handleAccessDenied(
        AgreementApplicationService.AccessDeniedToAgreementException ex
    ) {
        var pd = org.springframework.http.ProblemDetail.forStatus(org.springframework.http.HttpStatus.FORBIDDEN);
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", "FORBIDDEN");
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(pd);
    }
}
