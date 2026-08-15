package com.marketplace.agreement.infrastructure.persistence;

import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.repository.AgreementRepository;
import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.AgreementStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaAgreementRepository implements AgreementRepository {

    private final SpringDataAgreementJpaRepository jpa;

    public JpaAgreementRepository(SpringDataAgreementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Agreement> findById(AgreementId id) {
        return jpa.findById(id);
    }

    @Override
    public List<Agreement> findByEventId(String eventId) {
        return jpa.findByEventId(eventId);
    }

    @Override
    public List<Agreement> findPendingFundingExpired(Instant reference) {
        return jpa.findByStatusAndFundingDeadlineBefore(AgreementStatus.PENDING_FUNDING, reference);
    }

    @Override
    public List<Agreement> findFundedShippingExpired(Instant reference) {
        return jpa.findByStatusAndShippingDeadlineBefore(AgreementStatus.FUNDED, reference);
    }

    @Override
    public List<Agreement> findShippedDeliveryExpired(Instant reference) {
        return jpa.findByStatusAndDeliveryDeadlineBefore(AgreementStatus.SHIPPED, reference);
    }

    @Override
    public List<Agreement> findDeliveredInspectionExpired(Instant reference) {
        return jpa.findByStatusAndInspectionDeadlineBefore(AgreementStatus.DELIVERED, reference);
    }

    @Override
    public boolean existsByResponseId(String responseId) {
        return jpa.existsByResponseId(responseId);
    }

    @Override
    public Agreement save(Agreement agreement) {
        return jpa.save(agreement);
    }
}
