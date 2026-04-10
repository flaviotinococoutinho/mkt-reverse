package com.marketplace.sourcing.application.usecase;

import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.strategy.SourcingContext;
import com.marketplace.sourcing.domain.valueobject.BuyerContext;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventSettings;
import com.marketplace.sourcing.domain.valueobject.SourcingEventTimeline;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateSourcingEventUseCase {

    private final SourcingEventRepository sourcingEventRepository;
    private final SourcingContext sourcingContext;
    private final IdGenerator idGenerator;

    @Transactional
    public SourcingEventId execute(
        String tenantId,
        String buyerOrganizationId,
        String buyerContactId,
        String buyerContactName,
        String buyerContactPhone,
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
            buyerContactId,
            buyerContactName,
            buyerContactPhone
        );

        Instant now = Instant.now();
        // MVP: Default timeline logic
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

        // Execute specific strategy logic
        sourcingContext.execute(event);

        event.publish(Instant.now());
        sourcingEventRepository.save(event);

        return event.getId();
    }
}
