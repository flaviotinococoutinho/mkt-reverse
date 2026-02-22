package com.marketplace.sourcing.application.usecase;

import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.sourcing.domain.valueobject.CategoryAttributeSchema;
import com.marketplace.sourcing.domain.valueobject.MccCategory;
import com.marketplace.sourcing.domain.valueobject.OfferCondition;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SpecAttribute;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import com.marketplace.sourcing.domain.valueobject.ShippingMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmitSupplierResponseUseCase {

    private final SourcingEventRepository sourcingEventRepository;
    private final SupplierResponseRepository supplierResponseRepository;
    private final IdGenerator idGenerator;

    @Transactional
    public SupplierResponseId execute(
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
        SourcingEvent event = sourcingEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sourcing event not found: " + eventId));

        if (!event.acceptsResponses()) {
            throw new IllegalStateException("Event is not accepting responses");
        }

        // HARD normalization: proposal attributes must follow the event's category schema
        Integer mccCode = event.getProductSpecification() != null ? event.getProductSpecification().getMccCategoryCode() : null;
        if (mccCode != null) {
            var category = MccCategory.requireFromCode(mccCode);
            CategoryAttributeSchema.validate(category, attributes);
        } else if (attributes != null && !attributes.isEmpty()) {
            throw new IllegalArgumentException("Event has no mccCategoryCode; cannot accept proposal attributes");
        }

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
}
