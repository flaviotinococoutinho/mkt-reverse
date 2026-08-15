package com.marketplace.agreement.domain.repository;

import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.AgreementStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AgreementRepository {

    Optional<Agreement> findById(AgreementId id);

    List<Agreement> findByEventId(String eventId);

    /** Agreements in a given status whose deadline column elapsed (scheduler use). */
    List<Agreement> findPendingFundingExpired(Instant reference);

    List<Agreement> findFundedShippingExpired(Instant reference);

    List<Agreement> findShippedDeliveryExpired(Instant reference);

    List<Agreement> findDeliveredInspectionExpired(Instant reference);

    boolean existsByResponseId(String responseId);

    Agreement save(Agreement agreement);

    default AgreementStatus statusOf(AgreementId id) {
        return findById(id).map(Agreement::getStatus).orElse(null);
    }
}
