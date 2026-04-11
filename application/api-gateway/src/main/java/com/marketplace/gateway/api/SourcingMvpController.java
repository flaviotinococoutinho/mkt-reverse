package com.marketplace.gateway.api;

import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.application.service.SourcingEventApplicationService;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.valueobject.OfferCondition;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.ShippingMode;
import com.marketplace.sourcing.domain.valueobject.SpecAttribute;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import com.marketplace.gateway.search.OpportunitySearchClient;
import com.marketplace.gateway.search.OpportunitySearchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SourcingMvpController {

    private final SourcingEventApplicationService service;
    private final ObjectProvider<OpportunitySearchClient> opportunitySearchClient;

    public SourcingMvpController(SourcingEventApplicationService service, ObjectProvider<OpportunitySearchClient> opportunitySearchClient) {
        this.service = service;
        this.opportunitySearchClient = opportunitySearchClient;
    }

    @GetMapping("/health")
    public Object health() {
        return new HealthResponse(true);
    }

    @PostMapping("/sourcing-events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_BUYER') or hasAuthority('ROLE_ADMIN')")
    public EntityModel<CreateSourcingEventResponse> create(@Valid @RequestBody CreateSourcingEventRequest req) {
        ProductSpecification spec = ProductSpecification.of(
            req.productName(),
            req.productDescription(),
            req.category(),
            req.unitOfMeasure(),
            req.quantityRequired()
        )
            .withMccCategoryCode(req.mccCategoryCode())
            .withAttributes(req.attributes());

        Instant deadline = Instant.now().plusSeconds(req.validForHours() * 3600L);

        Money budget = req.estimatedBudgetCents() != null
            ? Money.fromCents(req.estimatedBudgetCents(), CurrencyCode.BRL)
            : Money.zero(CurrencyCode.BRL);

        SourcingEventId id = service.createAndPublishEvent(
            req.tenantId() != null && !req.tenantId().isBlank() ? req.tenantId() : "tenant-default",
            req.buyerOrganizationId() != null && !req.buyerOrganizationId().isBlank() ? req.buyerOrganizationId() : "org-default",
            UUID.randomUUID().toString(),
            req.buyerContactName(),
            req.buyerContactPhone(),
            req.buyerContactEmail(),
            req.title(),
            req.description(),
            req.type() != null ? req.type() : SourcingEventType.RFQ,
            spec,
            deadline,
            budget
        );

        var body = new CreateSourcingEventResponse(id.asString());

        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(id.asString(), req.tenantId() != null && !req.tenantId().isBlank() ? req.tenantId() : "tenant-default")
        ).withSelfRel();

        Link responses = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(id.asString(), req.tenantId() != null && !req.tenantId().isBlank() ? req.tenantId() : "tenant-default")
        ).withRel("responses");

        return EntityModel.of(body, self, responses);
    }

    @GetMapping(value = "/sourcing-events", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<EntityModel<SourcingEventView>> list(
        @RequestParam(required = false) String tenantId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer mccCategoryCode,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var parsedStatus = status != null ? com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.valueOf(status) : null;

        var result = service.searchEvents(tenantId, parsedStatus, mccCategoryCode, page, size);

        var items = result.items().stream().map(e -> {
                var view = SourcingEventView.from(e);

                Link self = WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(view.id(), tenantId)
                ).withSelfRel();

                Link responses = WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(view.id(), tenantId)
                ).withRel("responses");

                return EntityModel.of(view, self, responses);
            }).toList();

        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class)
                .list(tenantId, status, mccCategoryCode, page, size)
        ).withSelfRel();

        var metadata = new PagedModel.PageMetadata(result.size(), result.page(), result.totalElements(), result.totalPages());
        return PagedModel.of(items, metadata, self);
    }

    /**
     * Supplier-facing directory endpoint for discovering published opportunities.
     *
     * MVP semantics:
     * - Only PUBLISHED / IN_PROGRESS events
     * - If event is invite-only, supplier must be invited; otherwise open to all
     */
    @GetMapping(value = "/opportunities", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<EntityModel<SourcingEventView>> listOpportunities(
        @RequestParam(required = false) String tenantId,
        @RequestParam(required = false) String supplierId,
        @RequestParam(required = false) Integer mccCategoryCode,
        @RequestParam(required = false, name = "q") String query,
        @RequestParam(required = false, defaultValue = "ALL") String visibility,
        @RequestParam(required = false, defaultValue = "PUBLICATION_AT") String sortBy,
        @RequestParam(required = false, defaultValue = "DESC") String sortDir,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var result = service.searchOpportunitiesForSupplier(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size);

        var items = result.items().stream().map(e -> {
                var view = SourcingEventView.from(e);

                Link self = WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(view.id(), tenantId)
                ).withSelfRel();

                Link responses = WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(view.id(), tenantId)
                ).withRel("responses");

                return EntityModel.of(view, self, responses);
            }).toList();

        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class)
                .listOpportunities(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size)
        ).withSelfRel();

        var metadata = new PagedModel.PageMetadata(result.size(), result.page(), result.totalElements(), result.totalPages());
        return PagedModel.of(items, metadata, self);
    }

    /**
     * Supplier directory search with fuzzy matching.
     *
     * If OpenSearch is enabled and available, uses it. Otherwise falls back to Postgres.
     */
    @GetMapping(value = "/opportunities/search", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<EntityModel<SourcingEventView>> searchOpportunities(
        @RequestParam(required = false) String tenantId,
        @RequestParam(required = false) String supplierId,
        @RequestParam(required = false) Integer mccCategoryCode,
        @RequestParam(required = false, name = "q") String query,
        @RequestParam(required = false, defaultValue = "ALL") String visibility,
        @RequestParam(required = false, defaultValue = "PUBLICATION_AT") String sortBy,
        @RequestParam(required = false, defaultValue = "DESC") String sortDir,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var client = opportunitySearchClient.getIfAvailable();

        com.marketplace.shared.paging.PageResult<SourcingEventView> result;
        if (client != null) {
            try {
                result = client.search(new OpportunitySearchRequest(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size));
            } catch (Exception e) {
                // fallback to Postgres
                result = service.searchOpportunitiesForSupplier(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size)
                    .map(SourcingEventView::from);
            }
        } else {
            result = service.searchOpportunitiesForSupplier(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size)
                .map(SourcingEventView::from);
        }

        var items = result.items().stream().map(view -> {
            Link self = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(view.id(), tenantId)
            ).withSelfRel();

            Link responses = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(view.id(), tenantId)
            ).withRel("responses");

            return EntityModel.of(view, self, responses);
        }).toList();

        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class)
                .searchOpportunities(tenantId, supplierId, mccCategoryCode, query, visibility, sortBy, sortDir, page, size)
        ).withSelfRel();

        var metadata = new PagedModel.PageMetadata(result.size(), result.page(), result.totalElements(), result.totalPages());
        return PagedModel.of(items, metadata, self);
    }

    @PatchMapping("/sourcing-events/{id}")
    public EntityModel<CreateSourcingEventResponse> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateSourcingEventRequest req
    ) {
        service.updateEvent(id, req.tenantId(), req.title(), req.description());
        
        var body = new CreateSourcingEventResponse(id);
        
        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(id, req.tenantId())
        ).withSelfRel();
        
        return EntityModel.of(body, self);
    }

    @GetMapping("/sourcing-events/{id}")
    public EntityModel<SourcingEventView> get(
        @PathVariable String id,
        @RequestParam(required = false) String tenantId
    ) {
        SourcingEvent event = service.getEvent(id, tenantId);
        var view = SourcingEventView.from(event);

        Link self = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(id, tenantId)
        ).withSelfRel();

        Link responses = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(id, tenantId)
        ).withRel("responses");

        return EntityModel.of(view, self, responses);
    }

    @GetMapping("/sourcing-events/{id}/responses")
    public List<EntityModel<SupplierResponseView>> listResponses(
        @PathVariable String id,
        @RequestParam(required = false) String tenantId
    ) {
        return service.listResponses(id).stream().map(r -> {
            var view = SupplierResponseView.from(r);
            Link self = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(id, tenantId)
            ).withRel("collection"); // This link is for the collection, not the individual response
            Link accept = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(SourcingMvpController.class).accept(id, view.id(), tenantId)
            ).withRel("accept");
            Link root = WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(SourcingMvpController.class).get(id, tenantId)
            ).withRel("event");
            return EntityModel.of(view, self, accept, root);
        }).toList();
    }

    @PostMapping("/sourcing-events/{id}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_SUPPLIER') or hasAuthority('ROLE_ADMIN')")
    public EntityModel<CreateSupplierResponseResponse> submitResponse(
        @PathVariable String id,
        @Valid @RequestBody CreateSupplierResponseRequest req
    ) {
        Money offer = Money.fromCents(req.offerCents(), CurrencyCode.BRL);
        SupplierResponseId responseId = service.submitResponse(
            id,
            req.supplierId(),
            req.supplierOrganizationId() != null ? req.supplierOrganizationId() : "org-default", // Fallback for MVP
            offer,
            req.message(),
            req.leadTimeDays(),
            req.warrantyMonths(),
            req.condition(),
            req.shippingMode(),
            req.attributes()
        );
        var body = new CreateSupplierResponseResponse(responseId.asString());

        Link responses = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).listResponses(id, null)
        ).withRel("responses");

        Link accept = WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(SourcingMvpController.class).accept(id, responseId.asString(), null)
        ).withRel("accept");

        return EntityModel.of(body, responses, accept);
    }

    @PostMapping("/sourcing-events/{eventId}/responses/{responseId}/accept")
    @PreAuthorize("@sourcingSecurityService.isEventOwner(#eventId, authentication.principal.id) or hasAuthority('ROLE_ADMIN')")
    public org.springframework.http.ResponseEntity<Void> accept(
        @PathVariable String eventId,
        @PathVariable String responseId,
        @RequestParam(required = false) String tenantId
    ) {
        service.acceptResponse(eventId, responseId, tenantId);
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    public record HealthResponse(boolean ok) {}

    public record CreateSourcingEventRequest(
        String tenantId,
        String buyerOrganizationId,
        @NotBlank String buyerContactName,
        @NotBlank @Size(max = 30) String buyerContactPhone,
        String buyerContactEmail,
        @NotBlank String title,
        String description,
        SourcingEventType type,

        // MCC-like category code (curated subset for MVP)
        Integer mccCategoryCode,

        @NotBlank String productName,
        String productDescription,
        String category,
        @NotBlank String unitOfMeasure,
        @NotNull @Min(1) Long quantityRequired,

        // Common typed attributes
        java.util.List<SpecAttribute> attributes,

        @Min(1) int validForHours,
        Long estimatedBudgetCents
    ) {}

    public record CreateSourcingEventResponse(String id) {}

    public record UpdateSourcingEventRequest(
        @NotBlank String tenantId,
        String title,
        String description
    ) {}

    public record CreateSupplierResponseRequest(
        @NotBlank String supplierId,
        String supplierOrganizationId,
        @NotNull @Min(1) Long offerCents,
        Integer leadTimeDays,
        Integer warrantyMonths,
        OfferCondition condition,
        ShippingMode shippingMode,
        java.util.List<SpecAttribute> attributes,
        String message
    ) {}

    public record CreateSupplierResponseResponse(String id) {}

    public record SourcingEventView(
        String id,
        String status,
        String title,
        String description,
        String eventType,
        String tenantId,
        String buyerOrganizationId,
        String awardedSupplierId
    ) {
        static SourcingEventView from(SourcingEvent e) {
            return new SourcingEventView(
                e.getId().asString(),
                e.getStatus().name(),
                e.getTitle(),
                e.getDescription(),
                e.getEventType().name(),
                e.getBuyerContext().getTenantId(),
                e.getBuyerContext().getOrganizationId(),
                e.getAwardedSupplierId()
            );
        }
    }

    public record SupplierResponseView(
        String id,
        String eventId,
        String supplierId,
        String status,
        long offerCents,
        String currency,
        Integer leadTimeDays,
        Integer warrantyMonths,
        String condition,
        String shippingMode,
        java.util.List<SpecAttribute> attributes,
        String message
    ) {
        static SupplierResponseView from(SupplierResponse r) {
            long cents = r.getOfferAmount() != null
                ? r.getOfferAmount().getAmount().movePointRight(2).longValue()
                : 0L;

            return new SupplierResponseView(
                r.getId().asString(),
                r.getEventId().asString(),
                r.getSupplierId(),
                r.getStatus().name(),
                cents,
                r.getOfferAmount() != null ? r.getOfferAmount().getCurrency().name() : "BRL",
                r.getLeadTimeDays(),
                r.getWarrantyMonths(),
                r.getCondition() != null ? r.getCondition().name() : null,
                r.getShippingMode() != null ? r.getShippingMode().name() : null,
                r.getAttributesList(),
                r.getMessage()
            );
        }
    }
}
