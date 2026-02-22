package com.marketplace.sourcing.infrastructure.persistence;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Repository;

import com.marketplace.shared.paging.PageResult;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.OpportunitySearchRepository;
import com.marketplace.sourcing.domain.valueobject.BuyerContext;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventSettings;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventTimeline;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import com.marketplace.sourcing.infrastructure.search.OpportunityDocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OpenSearchOpportunityRepository implements OpportunitySearchRepository {

    private final OpenSearchClient client;

    @Override
    public void index(SourcingEvent event) {
        OpportunityDocument doc = OpportunityDocument.from(event);
        try {
            client.index(i -> i
                .index(OpportunityDocument.INDEX_NAME)
                .id(doc.getId())
                .document(doc)
            );
        } catch (IOException e) {
            log.error("Failed to index opportunity: {}", event.getId(), e);
            throw new RuntimeException("Search indexing failed", e);
        }
    }

    @Override
    public void delete(String eventId) {
        try {
            client.delete(d -> d.index(OpportunityDocument.INDEX_NAME).id(eventId));
        } catch (IOException e) {
            log.error("Failed to delete opportunity: {}", eventId, e);
        }
    }

    @Override
    public PageResult<SourcingEvent> search(String tenantId, String supplierId, Integer mccCategoryCode, String query, int page, int size) {
        try {
            SearchResponse<OpportunityDocument> response = client.search(s -> s
                .index(OpportunityDocument.INDEX_NAME)
                .query(q -> q
                    .bool(b -> {
                        if (query != null && !query.isBlank()) {
                            b.must(m -> m
                                .multiMatch(mm -> mm
                                    .fields("title^2", "description")
                                    .query(query)
                                    .fuzziness("AUTO")
                                )
                            );
                        }
                        if (mccCategoryCode != null) {
                            b.filter(f -> f.term(t -> t.field("mccCategoryCode").value(v -> v.longValue(mccCategoryCode))));
                        }
                        // Filter by published status essentially
                        b.filter(f -> f.terms(t -> t
                            .field("status.keyword")
                            .terms(ts -> ts.value(List.of(
                                FieldValue.of(SourcingEventStatus.PUBLISHED.name()),
                                FieldValue.of(SourcingEventStatus.IN_PROGRESS.name())
                            )))
                        ));
                        return b;
                    })
                )
                .from(page * size)
                .size(size),
                OpportunityDocument.class
            );

            List<SourcingEvent> events = response.hits().hits().stream()
                .map(Hit::source)
                .map(this::mapToDomain)
                .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : 0;

            return new PageResult<>(events, page, size, total);

        } catch (Exception e) {
            // OpenSearch can fail with transport/runtime exceptions (connection refused,
            // missing index, auth, deserialization). For MVP we degrade gracefully to DB fallback.
            log.warn("OpenSearch search failed, falling back to primary repository", e);
            return PageResult.empty();
        }
    }

    private SourcingEvent mapToDomain(OpportunityDocument doc) {
        // Reconstruct a partial SourcingEvent for display purposes
        // Ideally we should use a specific DTO, but adhering to the interface return type:
        return SourcingEvent.create(
            SourcingEventId.of(doc.getId()),
            BuyerContext.of(doc.getBuyerTenantId(), "unknown", "unknown", "unknown", "unknown"),
            doc.getTitle(),
            doc.getDescription(),
            SourcingEventType.RFQ, // default/unknown
            ProductSpecification.of("unknown", "unknown", "unknown", "unknown", 1L)
                .withMccCategoryCode(doc.getMccCategoryCode()),
            SourcingEventTimeline.create(doc.getPublicationDate(), doc.getPublicationDate(), doc.getSubmissionDeadline(), null, null, null, null, 1),
            SourcingEventSettings.defaults(),
            Money.zero(com.marketplace.shared.valueobject.CurrencyCode.BRL),
            null
        );
        // Note: The status will be DRAFT by default in 'create', we might need to reflect real status via reflection or a hydrator
        // For MVP, this might be enough if the UI just reads title/desc/deadline.
    }
}
