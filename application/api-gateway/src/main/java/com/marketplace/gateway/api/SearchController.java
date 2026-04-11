package com.marketplace.gateway.api;

import com.marketplace.gateway.search.OpportunitySearchClient;
import com.marketplace.gateway.search.OpportunitySearchRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final OpportunitySearchClient searchClient;

    public SearchController(OpportunitySearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @GetMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> searchOpportunities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer mccCategoryCode,
            @RequestParam(required = false, defaultValue = "PUBLIC") String visibility,
            @RequestParam(required = false, defaultValue = "PUBLISHED") String status,
            @RequestParam(required = false, defaultValue = "PUBLISHED_AT") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        var request = new OpportunitySearchRequest(
                null, null, mccCategoryCode, q, visibility, sortBy, sortDir, page, size
        );

        var results = searchClient.search(request);

        return ResponseEntity.ok(Map.of(
                "items", results.items(),
                "total", results.total(),
                "page", results.page(),
                "size", results.size(),
                "totalPages", results.totalPages()
        ));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<Map<String, String>>> autocomplete(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        var results = searchClient.autocomplete(q, limit);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/facets/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategoryFacets() {
        var facets = searchClient.getCategoryFacets();

        return ResponseEntity.ok(facets);
    }
}