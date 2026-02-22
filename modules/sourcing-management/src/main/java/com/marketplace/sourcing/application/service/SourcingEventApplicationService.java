package com.marketplace.sourcing.application.service;

import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.paging.PageResult;
import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.application.port.input.SourcingEventUseCases;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.sourcing.domain.valueobject.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Default implementation of the Sourcing Event Use Cases.
 */
@Service
public class SourcingEventApplicationService implements SourcingEventUseCases {

    private final SourcingEventRepository sourcingEventRepository;
    private final SupplierResponseRepository supplierResponseRepository;
    private final com.marketplace.sourcing.domain.repository.OpportunitySearchRepository opportunitySearchRepository;
    private final IdGenerator idGenerator;

    public SourcingEventApplicationService(
        SourcingEventRepository sourcingEventRepository,
        SupplierResponseRepository supplierResponseRepository,
        com.marketplace.sourcing.domain.repository.OpportunitySearchRepository opportunitySearchRepository,
        IdGenerator idGenerator
    ) {
        this.sourcingEventRepository = sourcingEventRepository;
        this.supplierResponseRepository = supplierResponseRepository;
        this.opportunitySearchRepository = opportunitySearchRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public SourcingEventId createAndPublishEvent(
        String tenantId,
        String buyerOrganizationId,
        String buyerContactId,
        String buyerContactName,
        String buyerContactPhone,
        String buyerContactEmail,
        String title,
        String description,
        SourcingEventType type,
        ProductSpecification specification,
        Instant submissionDeadline,
        Money estimatedBudget
    ) {
        BuyerContext buyerContext = BuyerContext.of(
            tenantId,
            buyerOrganizationId,
            null,
            buyerContactId,
            buyerContactName,
            buyerContactPhone,
            buyerContactEmail
        );

        Instant now = Instant.now();
        SourcingEventTimeline timeline = SourcingEventTimeline.create(
            now,
            now,
            submissionDeadline,
            submissionDeadline.plusSeconds(3600),
            submissionDeadline.plusSeconds(3600 * 24),
            null,
            submissionDeadline.plusSeconds(3600 * 24 * 2L),
            1
        );
        SourcingEvent event = SourcingEvent.create(
            SourcingEventId.of(idGenerator.nextId()),
            buyerContext,
            title,
            description,
            type != null ? type : SourcingEventType.RFQ,
            specification,
            timeline,
            SourcingEventSettings.defaults(),
            estimatedBudget != null ? estimatedBudget : Money.zero(CurrencyCode.BRL),
            null
        );
        event.publish(Instant.now());

        sourcingEventRepository.save(event);
        return event.getId();
    }

    @Override
    public SourcingEvent getEvent(String eventId) {
        return sourcingEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sourcing event not found: " + eventId));
    }

    @Override
    public PageResult<SourcingEvent> searchEvents(String tenantId, SourcingEventStatus status, Integer mccCategoryCode, int page, int size) {
        return sourcingEventRepository.search(tenantId, status, mccCategoryCode, page, size);
    }

    @Override
    public PageResult<SourcingEvent> searchOpportunitiesForSupplier(
        String tenantId,
        String supplierId,
        Integer mccCategoryCode,
        String query,
        String visibility,
        String sortBy,
        String sortDir,
        int page,
        int size
    ) {
        var searchResult = opportunitySearchRepository.search(
            tenantId, supplierId, mccCategoryCode, query, page, size
        );

        if (searchResult.totalElements() > 0) {
            return searchResult;
        }

        // Fallback for local/test environments where OpenSearch can be unavailable.
        // Keeps supplier opportunity discovery functional for MVP.
        return sourcingEventRepository.searchOpportunitiesForSupplier(
            tenantId,
            supplierId,
            mccCategoryCode,
            query,
            visibility,
            sortBy,
            sortDir,
            page,
            size
        );
    }

    @Override
    public List<SupplierResponse> listResponses(String eventId) {
        return supplierResponseRepository.findByEventId(eventId);
    }

    @Override
    @Transactional
    public SupplierResponseId submitResponse(
        String eventId,
        String supplierId,
        Money offerAmount,
        String message,
        Integer leadTimeDays,
        Integer warrantyMonths,
        OfferCondition condition,
        ShippingMode shippingMode,
        List<SpecAttribute> attributes
    ) {
        SourcingEvent event = getEvent(eventId);
        if (!event.acceptsResponses()) {
            throw new IllegalStateException("Event is not accepting responses");
        }

        // Delegate logic to domain
        event.validateProposalAttributes(attributes);

        SupplierResponse response = SupplierResponse.submit(
            SupplierResponseId.of(idGenerator.nextId()),
            SourcingEventId.of(eventId),
            supplierId,
            offerAmount,
            message,
            leadTimeDays,
            warrantyMonths,
            condition,
            shippingMode,
            attributes
        );

        supplierResponseRepository.save(response);
        event.registerResponse();
        sourcingEventRepository.save(event);

        return response.getId();
    }

    @Override
    @Transactional
    public void acceptResponse(String eventId, String responseId) {
        SourcingEvent event = getEvent(eventId);
        SupplierResponse response = supplierResponseRepository.findById(SupplierResponseId.of(responseId))
            .orElseThrow(() -> new IllegalArgumentException("Response not found: " + responseId));

        if (!response.getEventId().equals(SourcingEventId.of(eventId))) {
            throw new IllegalArgumentException("Response does not belong to event");
        }

        // MVP: model acceptance as an award. To satisfy the domain state machine,
        // we transition to evaluation first when necessary.
        if (event.getStatus() == SourcingEventStatus.PUBLISHED || event.getStatus() == SourcingEventStatus.IN_PROGRESS) {
            event.closeSubmissions(Instant.now());
            event.beginEvaluation(Instant.now());
        }

        response.accept(Instant.now());
        supplierResponseRepository.save(response);

        event.award(response.getSupplierId(), response.getOfferAmount(), Instant.now());
        sourcingEventRepository.save(event);
    }
}
