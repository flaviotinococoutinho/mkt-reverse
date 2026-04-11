package com.marketplace.gateway.search;

import com.marketplace.gateway.api.SourcingMvpController;
import com.marketplace.shared.paging.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PostgresOpportunitySearchClient implements OpportunitySearchClient {

    private static final Logger log = LoggerFactory.getLogger(PostgresOpportunitySearchClient.class);

    private final JdbcTemplate jdbcTemplate;

    public PostgresOpportunitySearchClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResult<SourcingMvpController.SourcingEventView> search(OpportunitySearchRequest request) {
        String sql = """
            SELECT * FROM search_opportunities(
                tenantId => ?,
                p_query => ?,
                p_mcc_category_code => ?,
                p_visibility => ?,
                p_status => ?,
                p_sort_by => ?,
                p_sort_dir => ?,
                p_page => ?,
                p_size => ?
            )
            """;

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,
                    request.tenantId(),
                    request.query(),
                    request.mccCategoryCode(),
                    request.visibility(),
                    request.status(),
                    request.sortBy(),
                    request.sortDir(),
                    request.page(),
                    request.size()
            );

            long total = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_count")).longValue();

            List<SourcingMvpController.SourcingEventView> items = rows.stream()
                    .map(this::mapToSourcingEventView)
                    .toList();

            int totalPages = (int) Math.ceil((double) total / request.size());

            return new PageResult<>(
                    items,
                    request.page(),
                    request.size(),
                    total,
                    totalPages
            );
        } catch (Exception e) {
            log.warn("Full-text search function not available, falling back to basic query", e);
            return fallbackSearch(request);
        }
    }

    private PageResult<SourcingMvpController.SourcingEventView> fallbackSearch(OpportunitySearchRequest request) {
        String sql = """
            SELECT id, title, description, product_name, status, mcc_category_code, visibility, published_at
            FROM src_sourcing_events
            WHERE tenant_id = ?
              AND status = 'PUBLISHED'
              AND visibility = 'PUBLIC'
            ORDER BY published_at DESC
            LIMIT ? OFFSET ?
            """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,
                request.tenantId(),
                request.size(),
                request.page() * request.size()
        );

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM src_sourcing_events WHERE tenant_id = ? AND status = 'PUBLISHED' AND visibility = 'PUBLIC'",
                Long.class,
                request.tenantId()
        );

        List<SourcingMvpController.SourcingEventView> items = rows.stream()
                .map(this::mapToSourcingEventView)
                .toList();

        int totalPages = (int) Math.ceil((double) total / request.size());

        return new PageResult<>(
                items,
                request.page(),
                request.size(),
                total,
                totalPages
        );
    }

    @Override
    public List<Map<String, String>> autocomplete(String prefix, int limit) {
        try {
            return jdbcTemplate.query(
                    "SELECT * FROM search_opportunities_autocomplete(?, ?, ?)",
                    (rs, rowNum) -> Map.of(
                            "id", rs.getString("id"),
                            "title", rs.getString("title"),
                            "productName", rs.getString("product_name")
                    ),
                    null, prefix + "%", limit
            );
        } catch (Exception e) {
            log.warn("Autocomplete function not available", e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> getCategoryFacets() {
        try {
            return jdbcTemplate.query(
                    "SELECT * FROM get_opportunity_category_facets(?)",
                    (rs, rowNum) -> Map.of(
                            "mccCategoryCode", rs.getInt("mcc_category_code"),
                            "count", rs.getLong("count"),
                            "label", rs.getString("label")
                    ),
                    null
            );
        } catch (Exception e) {
            log.warn("Category facets function not available", e);
            return List.of();
        }
    }

    private SourcingMvpController.SourcingEventView mapToSourcingEventView(Map<String, Object> row) {
        return new SourcingMvpController.SourcingEventView(
                row.get("id").toString(),
                (String) row.get("title"),
                (String) row.get("description"),
                (String) row.get("product_name"),
                null, null, null, null,
                (String) row.get("status"),
                row.get("mcc_category_code") != null ? ((Number) row.get("mcc_category_code")).intValue() : null,
                (String) row.get("visibility"),
                row.get("published_at").toString(),
                null
        );
    }
}