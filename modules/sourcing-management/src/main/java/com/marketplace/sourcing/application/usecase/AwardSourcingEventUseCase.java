package com.marketplace.sourcing.application.usecase;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AwardSourcingEventUseCase {

    private final SourcingEventRepository sourcingEventRepository;
    private final SupplierResponseRepository supplierResponseRepository;

    @Transactional
    public void execute(String eventId, String responseId) {
        SourcingEvent event = sourcingEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sourcing event not found: " + eventId));

        SupplierResponse response = supplierResponseRepository.findById(SupplierResponseId.of(responseId))
            .orElseThrow(() -> new IllegalArgumentException("Response not found: " + responseId));

        if (!response.getEventId().equals(SourcingEventId.of(eventId))) {
            throw new IllegalArgumentException("Response does not belong to event");
        }

        // Handle state transition for awarding
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
