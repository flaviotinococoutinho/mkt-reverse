package com.marketplace.sourcing.domain.repository;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.shared.paging.PageResult;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Domain repository abstraction for sourcing events.
 */
public interface SourcingEventRepository {

    Optional<SourcingEvent> findById(SourcingEventId id);

    Optional<SourcingEvent> findById(String id);

    List<SourcingEvent> findByStatus(SourcingEventStatus status);

    List<SourcingEvent> findActiveByBuyer(String tenantId, String buyerOrganizationId);

    List<SourcingEvent> findPendingEvaluation(Instant reference);

    List<SourcingEvent> findExpirableEvents(Instant reference);

    List<SourcingEvent> findBySupplierParticipation(String supplierId, Set<SourcingEventStatus> statuses);

    /**
     * MVP search endpoint. All filters are optional.
     */
    PageResult<SourcingEvent> search(String tenantId, SourcingEventStatus status, Integer mccCategoryCode, int page, int size);

    /**
     * Marketplace-facing search for suppliers (dealers) to discover open opportunities.
     *
     * Rules (MVP):
     * - Only published/in-progress events are searchable
     * - If event has invited suppliers, supplier must be invited
     * - Optional filters for tenant, MCC category and free-text query
     */
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

    SourcingEvent save(SourcingEvent event);

    void delete(SourcingEvent event);
}
