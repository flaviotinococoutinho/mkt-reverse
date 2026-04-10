package com.marketplace.sourcing.infrastructure.persistence;

import com.marketplace.shared.paging.PageResult;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public class JpaSourcingEventRepository implements SourcingEventRepository {

    private final SpringDataSourcingEventJpaRepository jpa;

    public JpaSourcingEventRepository(SpringDataSourcingEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<SourcingEvent> findById(SourcingEventId id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<SourcingEvent> findById(String id) {
        return jpa.findById(SourcingEventId.of(id));
    }

    @Override
    public List<SourcingEvent> findByStatus(SourcingEventStatus status) {
        return jpa.findByStatus(status);
    }

    @Override
    public List<SourcingEvent> findActiveByBuyer(String tenantId, String buyerOrganizationId) {
        return jpa.findActiveByBuyer(tenantId, buyerOrganizationId);
    }

    @Override
    public List<SourcingEvent> findPendingEvaluation(Instant reference) {
        return jpa.findPendingEvaluation(reference);
    }

    @Override
    public List<SourcingEvent> findExpirableEvents(Instant reference) {
        return jpa.findExpirableEvents(reference);
    }

    @Override
    public List<SourcingEvent> findBySupplierParticipation(String supplierId, Set<SourcingEventStatus> statuses) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return List.of();
        }
        Set<SourcingEventStatus> effectiveStatuses = (statuses == null || statuses.isEmpty()) ? null : statuses;
        return jpa.findBySupplierParticipation(supplierId.trim(), effectiveStatuses);
    }

    @Override
    public PageResult<SourcingEvent> search(String tenantId, SourcingEventStatus status, Integer mccCategoryCode, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));
        var result = jpa.search(tenantId, status, mccCategoryCode, PageRequest.of(safePage, safeSize));
        return new PageResult<>(result.getContent(), safePage, safeSize, result.getTotalElements());
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
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));

        var pageable = PageRequest.of(safePage, safeSize, Sort.by(resolveSort(sortBy, sortDir)));
        var statuses = EnumSet.of(SourcingEventStatus.PUBLISHED, SourcingEventStatus.IN_PROGRESS);

        String q = (query == null || query.trim().isEmpty()) ? null : query.trim();
        String qLower = q == null ? null : q.toLowerCase();
        String sup = (supplierId == null || supplierId.trim().isEmpty()) ? null : supplierId.trim();

        String vis = (visibility == null || visibility.trim().isEmpty()) ? "ALL" : visibility.trim().toUpperCase();
        boolean openOnly = "OPEN".equals(vis);
        boolean inviteOnly = "INVITE_ONLY".equals(vis);

        // Keep DB-side filtering stable across environments/dialects.
        // Text search is applied in-memory to avoid JPQL lower(...) issues on non-text columns.
        var result = jpa.searchOpportunitiesForSupplier(tenantId, statuses, mccCategoryCode, pageable);

        Stream<SourcingEvent> filtered = result.getContent().stream().filter(event -> {
            Set<String> invited = event.getInvitedSupplierIds();
            boolean isOpen = invited == null || invited.isEmpty();
            boolean isInvited = sup != null && invited != null && invited.contains(sup);

            boolean matchesQuery = true;
            if (qLower != null) {
                String title = event.getTitle() == null ? "" : event.getTitle().toLowerCase();
                String productName = (event.getProductSpecification() == null || event.getProductSpecification().getProductName() == null)
                    ? ""
                    : event.getProductSpecification().getProductName().toLowerCase();
                matchesQuery = title.contains(qLower) || productName.contains(qLower);
            }

            if (!matchesQuery) {
                return false;
            }

            if (openOnly) {
                return isOpen;
            }

            if (inviteOnly) {
                return !isOpen && isInvited;
            }

            return sup == null || isOpen || isInvited;
        });

        var filteredItems = filtered.toList();
        return new PageResult<>(filteredItems, safePage, safeSize, result.getTotalElements());
    }

    private Sort.Order resolveSort(String sortBy, String sortDir) {
        String by = sortBy == null ? "PUBLICATION_AT" : sortBy.trim().toUpperCase();
        String dir = sortDir == null ? "DESC" : sortDir.trim().toUpperCase();

        Sort.Direction direction = "ASC".equals(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (by) {
            case "TITLE" -> new Sort.Order(direction, "title");
            case "DEADLINE" -> new Sort.Order(direction, "timeline.submissionDeadline");
            case "PUBLICATION_AT" -> new Sort.Order(direction, "timeline.publicationAt");
            default -> new Sort.Order(Sort.Direction.DESC, "timeline.publicationAt");
        };
    }

    @Override
    public SourcingEvent save(SourcingEvent event) {
        return jpa.save(event);
    }

    @Override
    public void delete(SourcingEvent event) {
        jpa.delete(event);
    }
}
