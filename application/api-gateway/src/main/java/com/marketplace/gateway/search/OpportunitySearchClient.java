package com.marketplace.gateway.search;

import com.marketplace.gateway.api.SourcingMvpController;
import com.marketplace.shared.paging.PageResult;

import java.util.List;
import java.util.Map;

/**
 * Search read-model for supplier directory. Implementations may be backed by OpenSearch.
 *
 * If unavailable, API should fall back to Postgres (source of truth).
 */
public interface OpportunitySearchClient {

    PageResult<SourcingMvpController.SourcingEventView> search(OpportunitySearchRequest request);

    /**
     * Typeahead suggestions for published, public opportunities.
     * Implementations without autocomplete support return an empty list.
     */
    default List<Map<String, String>> autocomplete(String tenantId, String prefix, int limit) {
        return List.of();
    }

    /**
     * Category (MCC) facets for filter sidebars.
     * Implementations without facet support return an empty list.
     */
    default List<Map<String, Object>> getCategoryFacets(String tenantId) {
        return List.of();
    }
}
