package com.marketplace.sourcing.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;

public interface SpringDataSourcingEventJpaRepository extends JpaRepository<SourcingEvent, SourcingEventId> {

    List<SourcingEvent> findByStatus(SourcingEventStatus status);

    @Query("select e from SourcingEvent e where e.buyerContext.tenantId = :tenantId and e.buyerContext.organizationId = :orgId and e.status in (com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.PUBLISHED, com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.IN_PROGRESS, com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.NEGOTIATION)")
    List<SourcingEvent> findActiveByBuyer(@Param("tenantId") String tenantId, @Param("orgId") String orgId);

    @Query("select e from SourcingEvent e where e.status = com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.SUBMISSION_CLOSED and e.timeline.submissionDeadline <= :ref")
    List<SourcingEvent> findPendingEvaluation(@Param("ref") Instant reference);

    @Query("select e from SourcingEvent e where e.status in (com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.PUBLISHED, com.marketplace.sourcing.domain.valueobject.SourcingEventStatus.IN_PROGRESS) and e.timeline.submissionDeadline <= :ref")
    List<SourcingEvent> findExpirableEvents(@Param("ref") Instant reference);

    @Query("""
        select e from SourcingEvent e
        where (:tenantId is null or e.buyerContext.tenantId = :tenantId)
          and (:status is null or e.status = :status)
          and (:mcc is null or e.productSpecification.mccCategoryCode = :mcc)
        order by e.timeline.publicationAt desc
        """)
    Page<SourcingEvent> search(
        @Param("tenantId") String tenantId,
        @Param("status") SourcingEventStatus status,
        @Param("mcc") Integer mcc,
        Pageable pageable
    );

    @Query("""
        select e from SourcingEvent e
        where (:tenantId is null or e.buyerContext.tenantId = :tenantId)
          and e.status in :statuses
          and (:mcc is null or e.productSpecification.mccCategoryCode = :mcc)
        """)
    Page<SourcingEvent> searchOpportunitiesForSupplier(
        @Param("tenantId") String tenantId,
        @Param("statuses") Set<SourcingEventStatus> statuses,
        @Param("mcc") Integer mcc,
        Pageable pageable
    );

    @Query("""
        select distinct e from SourcingEvent e
        where (:supplierId member of e.invitedSupplierIds
               or exists (
                   select r from SupplierResponse r
                   where r.eventId = e.id
                     and r.supplierId = :supplierId
               ))
          and (:statuses is null or e.status in :statuses)
        order by e.timeline.publicationAt desc
        """)
    List<SourcingEvent> findBySupplierParticipation(
        @Param("supplierId") String supplierId,
        @Param("statuses") Set<SourcingEventStatus> statuses
    );
}
