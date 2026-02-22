package com.marketplace.gateway.graphql;

import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.application.service.SourcingEventApplicationService;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.valueobject.OfferCondition;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.ShippingMode;
import com.marketplace.sourcing.domain.valueobject.SpecAttribute;
import com.marketplace.sourcing.domain.valueobject.SpecAttributeType;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Controller
public class SourcingGraphqlController {

    private final SourcingEventApplicationService service;

    public SourcingGraphqlController(SourcingEventApplicationService service) {
        this.service = service;
    }

    // --- Queries ---

    @QueryMapping
    public SourcingEventView sourcingEvent(@Argument String id) {
        SourcingEvent event = service.getEvent(id);
        return SourcingEventView.from(event);
    }

    @QueryMapping
    public List<SourcingEventView> sourcingEvents(
        @Argument String tenantId,
        @Argument String status,
        @Argument Integer mccCategoryCode,
        @Argument Integer page,
        @Argument Integer size
    ) {
        SourcingEventStatus parsedStatus = status != null ? SourcingEventStatus.valueOf(status) : null;
        int safePage = page != null ? page : 0;
        int safeSize = size != null ? size : 20;

        return service.searchEvents(tenantId, parsedStatus, mccCategoryCode, safePage, safeSize)
            .items()
            .stream()
            .map(SourcingEventView::from)
            .toList();
    }

    @QueryMapping
    public List<SourcingEventView> opportunitiesForSupplier(
        @Argument String supplierId,
        @Argument String tenantId,
        @Argument Integer mccCategoryCode,
        @Argument String q,
        @Argument String visibility,
        @Argument String sortBy,
        @Argument String sortDir,
        @Argument Integer page,
        @Argument Integer size
    ) {
        int safePage = page != null ? page : 0;
        int safeSize = size != null ? size : 20;

        return service.searchOpportunitiesForSupplier(tenantId, supplierId, mccCategoryCode, q, visibility, sortBy, sortDir, safePage, safeSize)
            .items()
            .stream()
            .map(SourcingEventView::from)
            .toList();
    }

    @QueryMapping
    public List<SupplierResponseView> sourcingEventResponses(@Argument String eventId) {
        return service.listResponses(eventId).stream().map(SupplierResponseView::from).toList();
    }

    // --- Mutations ---

    @MutationMapping
    public String createSourcingEvent(@Argument CreateSourcingEventInput input) {
        ProductSpecification spec = ProductSpecification.of(
            input.productName(),
            input.productDescription(),
            input.category(),
            input.unitOfMeasure(),
            input.quantityRequired()
        ).withMccCategoryCode(input.mccCategoryCode())
         .withAttributes(input.attributes() != null ? input.attributes().stream().map(SpecAttributeInput::toDomain).toList() : null);

        Instant deadline = Instant.now().plusSeconds(input.validForHours() * 3600L);

        Money budget = input.estimatedBudgetCents() != null
            ? Money.of(input.estimatedBudgetCents() / 100.0, CurrencyCode.BRL)
            : Money.zero(CurrencyCode.BRL);

        SourcingEventId id = service.createAndPublishEvent(
            input.tenantId(),
            input.buyerOrganizationId(),
            UUID.randomUUID().toString(),
            input.buyerContactName(),
            input.buyerContactPhone(),
            input.buyerContactEmail(),
            input.title(),
            input.description(),
            input.type() != null ? SourcingEventType.valueOf(input.type()) : SourcingEventType.RFQ,
            spec,
            deadline,
            budget
        );

        return id.asString();
    }

    @MutationMapping
    public String submitResponse(@Argument SubmitResponseInput input) {
        Money offer = Money.of(input.offerCents() / 100.0, CurrencyCode.BRL);

        SupplierResponseId responseId = service.submitResponse(
            input.eventId(),
            input.supplierId(),
            offer,
            input.message(),
            input.leadTimeDays(),
            input.warrantyMonths(),
            input.condition() != null ? OfferCondition.valueOf(input.condition()) : null,
            input.shippingMode() != null ? ShippingMode.valueOf(input.shippingMode()) : null,
            input.attributes() != null ? input.attributes().stream().map(SpecAttributeInput::toDomain).toList() : null
        );

        return responseId.asString();
    }

    @MutationMapping
    public boolean acceptResponse(@Argument String eventId, @Argument String responseId) {
        service.acceptResponse(eventId, responseId);
        return true;
    }

    // --- DTOs (GraphQL-facing) ---

    public record SpecAttributeInput(String key, String type, String unit, Object value) {
        SpecAttribute toDomain() {
            return new SpecAttribute(key, SpecAttributeType.valueOf(type), unit, value);
        }
    }

    public record CreateSourcingEventInput(
        String tenantId,
        String buyerOrganizationId,
        String buyerContactName,
        String buyerContactPhone,
        String buyerContactEmail,
        String title,
        String description,
        String type,
        Integer mccCategoryCode,
        String productName,
        String productDescription,
        String category,
        String unitOfMeasure,
        Long quantityRequired,
        List<SpecAttributeInput> attributes,
        int validForHours,
        Long estimatedBudgetCents
    ) {}

    public record SubmitResponseInput(
        String eventId,
        String supplierId,
        Long offerCents,
        Integer leadTimeDays,
        Integer warrantyMonths,
        String condition,
        String shippingMode,
        List<SpecAttributeInput> attributes,
        String message
    ) {}

    public record SpecAttributeView(String key, String type, String unit, Object value) {
        static SpecAttributeView from(SpecAttribute a) {
            return new SpecAttributeView(a.getKey(), a.getType().name(), a.getUnit(), a.getValue());
        }
    }

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
        List<SpecAttributeView> attributes,
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
                r.getAttributesList().stream().map(SpecAttributeView::from).toList(),
                r.getMessage()
            );
        }
    }
}
