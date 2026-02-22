package com.marketplace.gateway.search;

public record OpportunitySearchRequest(
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
}
