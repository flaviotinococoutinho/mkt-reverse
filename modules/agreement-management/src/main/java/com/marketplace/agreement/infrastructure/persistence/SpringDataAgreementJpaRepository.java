package com.marketplace.agreement.infrastructure.persistence;

import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.AgreementId;
import com.marketplace.agreement.domain.valueobject.AgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataAgreementJpaRepository extends JpaRepository<Agreement, AgreementId> {

    List<Agreement> findByEventId(String eventId);

    boolean existsByResponseId(String responseId);

    @Query("select a from Agreement a where a.status = :status and a.fundingDeadline < :ref")
    List<Agreement> findByStatusAndFundingDeadlineBefore(
        @Param("status") AgreementStatus status, @Param("ref") Instant reference);

    @Query("select a from Agreement a where a.status = :status and a.shippingDeadline < :ref")
    List<Agreement> findByStatusAndShippingDeadlineBefore(
        @Param("status") AgreementStatus status, @Param("ref") Instant reference);

    @Query("select a from Agreement a where a.status = :status and a.deliveryDeadline < :ref")
    List<Agreement> findByStatusAndDeliveryDeadlineBefore(
        @Param("status") AgreementStatus status, @Param("ref") Instant reference);

    @Query("select a from Agreement a where a.status = :status and a.inspectionDeadline < :ref")
    List<Agreement> findByStatusAndInspectionDeadlineBefore(
        @Param("status") AgreementStatus status, @Param("ref") Instant reference);
}
