package com.marketplace.gateway.search;

import com.marketplace.gateway.api.SourcingMvpController;
import com.marketplace.shared.paging.PageResult;

/**
 * Search read-model for supplier directory. Implementations may be backed by OpenSearch.
 *
 * If unavailable, API should fall back to Postgres (source of truth).
 */
public interface OpportunitySearchClient {

    PageResult<SourcingMvpController.SourcingEventView> search(OpportunitySearchRequest request);
}

