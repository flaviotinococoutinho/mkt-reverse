package com.marketplace.sourcing.application.port.input;

import com.marketplace.shared.paging.PageResult;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.valueobject.*;

import java.time.Instant;
import java.util.List;

public interface SourcingEventUseCases {

    SourcingEventId createAndPublishEvent(
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
    );

    SourcingEvent getEvent(String eventId);

    PageResult<SourcingEvent> searchEvents(String tenantId, SourcingEventStatus status, Integer mccCategoryCode, int page, int size);

    PageResult<SourcingEvent> searchOpportunitiesForSupplier(
        String tenantId,
        String supplierId,
        Integer mccCategoryCode,
        String query,
        String visibility,
        String sortBy,
        String sortDir,
        int page,
        int size
    );

    List<SupplierResponse> listResponses(String eventId);

    SupplierResponseId submitResponse(
        String eventId,
        String supplierId,
        Money offerAmount,
        String message,
        Integer leadTimeDays,
        Integer warrantyMonths,
        OfferCondition condition,
        ShippingMode shippingMode,
        List<SpecAttribute> attributes
    );

    void acceptResponse(String eventId, String responseId);
}
