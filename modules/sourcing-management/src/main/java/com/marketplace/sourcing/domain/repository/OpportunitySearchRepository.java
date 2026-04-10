package com.marketplace.sourcing.domain.repository;

import com.marketplace.shared.paging.PageResult;
import com.marketplace.sourcing.domain.model.SourcingEvent;

public interface OpportunitySearchRepository {
    void index(SourcingEvent event);
    
    void delete(String eventId);

    PageResult<SourcingEvent> search(
        String tenantId,
        String supplierId, // for potential personalization/filtering
        Integer mccCategoryCode,
        String query,
        int page,
        int size
    );
}
