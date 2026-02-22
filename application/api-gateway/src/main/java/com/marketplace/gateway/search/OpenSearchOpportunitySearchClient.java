package com.marketplace.gateway.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.gateway.api.SourcingMvpController;
import com.marketplace.shared.paging.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;

/**
 * OpenSearch implementation using HTTP API.
 *
 * Notes:
 * - best-effort: if OpenSearch is down, caller should fallback to Postgres
 * - uses fuzzy multi_match with fuzziness=AUTO
 */
@Component
@ConditionalOnProperty(prefix = "marketplace.search.opensearch", name = "enabled", havingValue = "true")
public class OpenSearchOpportunitySearchClient implements OpportunitySearchClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String index;

    public OpenSearchOpportunitySearchClient(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        @Value("${marketplace.search.opensearch.url:http://localhost:9201}") String baseUrl,
        @Value("${marketplace.search.opensearch.index:opportunities}") String index
    ) {
        this(restClientBuilder.baseUrl(baseUrl).build(), objectMapper, index);
    }

    OpenSearchOpportunitySearchClient(RestClient restClient, ObjectMapper objectMapper, String index) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.index = index;
    }

    @Override
    public PageResult<SourcingMvpController.SourcingEventView> search(OpportunitySearchRequest request) {
        try {
            var body = buildSearchBody(request);

            String response = restClient
                .post()
                .uri("/{index}/_search", index)
                .body(body)
                .retrieve()
                .body(String.class);

            if (response == null || response.isBlank()) {
                return new PageResult<>(java.util.List.of(), request.page(), request.size(), 0);
            }

            JsonNode json = objectMapper.readTree(response);
            long total = json.path("hits").path("total").path("value").asLong(0);
            var hits = json.path("hits").path("hits");

            var items = new ArrayList<SourcingMvpController.SourcingEventView>();
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                items.add(new SourcingMvpController.SourcingEventView(
                    source.path("id").asText(),
                    source.path("status").asText(),
                    source.path("title").asText(),
                    source.path("description").isMissingNode() ? null : source.path("description").asText(null),
                    source.path("eventType").asText(),
                    source.path("tenantId").asText(),
                    source.path("buyerOrganizationId").asText(),
                    source.path("awardedSupplierId").isMissingNode() ? null : source.path("awardedSupplierId").asText(null)
                ));
            }

            return new PageResult<>(items, request.page(), request.size(), total);
        } catch (Exception e) {
            // bubble up: controller will fallback to Postgres
            throw new IllegalStateException("OpenSearch query failed", e);
        }
    }

    private JsonNode buildSearchBody(OpportunitySearchRequest req) {
        int from = Math.max(0, req.page()) * Math.max(1, req.size());

        // Filters
        var filter = objectMapper.createArrayNode();
        if (req.tenantId() != null) {
            filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("tenantId", req.tenantId())));
        }
        if (req.mccCategoryCode() != null) {
            filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("mccCategoryCode", req.mccCategoryCode())));
        }

        // Soft delete rule: exclude archived/closed
        filter.add(objectMapper.createObjectNode().set(
            "terms",
            objectMapper.createObjectNode().set(
                "status",
                objectMapper.createArrayNode().add("PUBLISHED").add("IN_PROGRESS").add("NEGOTIATION").add("SUBMISSION_CLOSED").add("UNDER_EVALUATION")
            )
        ));

        // visibility:
        // - OPEN => only open events (invite list empty)
        // - INVITE_ONLY => only invite-only events and supplier must be invited
        // - ALL => open + invite-only (if invited)
        String vis = req.visibility() == null ? "ALL" : req.visibility().trim().toUpperCase();

        if ("OPEN".equals(vis)) {
            filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("isInviteOnly", false)));
        } else if ("INVITE_ONLY".equals(vis)) {
            filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("isInviteOnly", true)));
            if (req.supplierId() != null) {
                filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("invitedSupplierIds", req.supplierId())));
            }
        } else {
            // ALL: for invite-only docs, supplier must be invited; for open docs, allow.
            if (req.supplierId() != null) {
                var should = objectMapper.createArrayNode();
                should.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("isInviteOnly", false)));
                should.add(objectMapper.createObjectNode().set(
                    "bool",
                    objectMapper.createObjectNode()
                        .set("filter", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("isInviteOnly", true)))
                            .add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().put("invitedSupplierIds", req.supplierId())))
                        )
                ));
                var shouldBool = objectMapper.createObjectNode();
                shouldBool.set("should", should);
                shouldBool.put("minimum_should_match", 1);

                filter.add(objectMapper.createObjectNode().set("bool", shouldBool));
            }
        }

        // Query
        JsonNode must;
        if (req.query() == null || req.query().trim().isEmpty()) {
            must = objectMapper.createObjectNode().set("match_all", objectMapper.createObjectNode());
        } else {
            must = objectMapper.createObjectNode().set(
                "multi_match",
                multiMatch(req.query().trim())
            );
        }

        var bool = objectMapper.createObjectNode();
        bool.set("filter", filter);
        bool.set("must", objectMapper.createArrayNode().add(must));

        var root = objectMapper.createObjectNode();
        root.put("from", from);
        root.put("size", Math.max(1, req.size()));
        root.set("query", objectMapper.createObjectNode().set("bool", bool));
        root.set("sort", objectMapper.createArrayNode().add(resolveSort(req)));
        return root;
    }

    private JsonNode resolveSort(OpportunitySearchRequest req) {
        String by = req.sortBy() == null ? "PUBLICATION_AT" : req.sortBy().trim().toUpperCase();
        String dir = req.sortDir() == null ? "DESC" : req.sortDir().trim().toUpperCase();
        String order = "ASC".equals(dir) ? "asc" : "desc";

        String field = switch (by) {
            case "TITLE" -> "title.keyword";
            case "DEADLINE" -> "submissionDeadline";
            case "PUBLICATION_AT" -> "publicationAt";
            default -> "publicationAt";
        };

        return objectMapper.createObjectNode().set(
            field,
            objectMapper.createObjectNode().put("order", order)
        );
    }

    private JsonNode multiMatch(String query) {
        var node = objectMapper.createObjectNode();
        node.put("query", query);
        node.set("fields", objectMapper.createArrayNode().add("title^3").add("description").add("productName^2"));
        node.put("fuzziness", "AUTO");
        node.put("operator", "and");
        return node;
    }
}
