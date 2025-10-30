package com.marketplace.opportunity.adapter.output.persistence;

import com.marketplace.opportunity.application.port.output.OpportunityRepository;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;
import org.slf4j.Logger;
import org.slf.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * R2DBC adapter for opportunity persistence.
 * 
 * Output Adapter in Hexagonal Architecture:
 * - Implements output port (OpportunityRepository)
 * - Handles database operations
 * - Converts between domain models and database entities
 * - Uses R2DBC for reactive database access
 * 
 * Follows Adapter Pattern:
 * - Adapts R2DBC to domain repository interface
 * - Isolates infrastructure concerns
 * - Allows easy replacement of persistence technology
 */
@Repository
public class R2dbcOpportunityRepositoryAdapter implements OpportunityRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(R2dbcOpportunityRepositoryAdapter.class);
    
    private final DatabaseClient databaseClient;
    private final OpportunityEntityMapper mapper;
    
    public R2dbcOpportunityRepositoryAdapter(
        DatabaseClient databaseClient,
        OpportunityEntityMapper mapper
    ) {
        this.databaseClient = databaseClient;
        this.mapper = mapper;
    }
    
    @Override
    public Mono<Opportunity> save(Opportunity opportunity) {
        logger.debug("Saving opportunity: id={}", opportunity.id());
        
        OpportunityEntity entity = mapper.toEntity(opportunity);
        
        return databaseClient.sql("""
            INSERT INTO opportunities (
                id, consumer_id, tenant_id, title, description, category,
                budget_amount, budget_currency, deadline, status,
                attachments, specifications, template_key,
                created_at, updated_at
            ) VALUES (
                :id, :consumerId, :tenantId, :title, :description, :category,
                :budgetAmount, :budgetCurrency, :deadline, :status,
                :attachments, :specifications, :templateKey,
                :createdAt, :updatedAt
            )
            ON CONFLICT (id) DO UPDATE SET
                title = EXCLUDED.title,
                description = EXCLUDED.description,
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at
            RETURNING *
            """)
            .bind("id", entity.id())
            .bind("consumerId", entity.consumerId())
            .bind("tenantId", entity.tenantId())
            .bind("title", entity.title())
            .bind("description", entity.description())
            .bind("category", entity.category())
            .bind("budgetAmount", entity.budgetAmount())
            .bind("budgetCurrency", entity.budgetCurrency())
            .bind("deadline", entity.deadline())
            .bind("status", entity.status().name())
            .bind("attachments", mapper.serializeAttachments(entity.attachments()))
            .bind("specifications", mapper.serializeSpecifications(entity.specifications()))
            .bind("templateKey", entity.templateKey())
            .bind("createdAt", entity.createdAt())
            .bind("updatedAt", entity.updatedAt())
            .map(mapper::fromRow)
            .one()
            .map(mapper::toDomain)
            .doOnSuccess(saved -> logger.debug("Opportunity saved: id={}", saved.id()))
            .doOnError(error -> logger.error("Failed to save opportunity: id={}", opportunity.id(), error));
    }
    
    @Override
    public Mono<Opportunity> findById(OpportunityId id) {
        logger.debug("Finding opportunity by id: {}", id);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities WHERE id = :id
            """)
            .bind("id", id.value())
            .map(mapper::fromRow)
            .one()
            .map(mapper::toDomain)
            .doOnSuccess(opportunity -> {
                if (opportunity != null) {
                    logger.debug("Opportunity found: id={}", id);
                } else {
                    logger.debug("Opportunity not found: id={}", id);
                }
            });
    }
    
    @Override
    public Mono<Opportunity> findByIdAndConsumerId(OpportunityId id, Long consumerId) {
        logger.debug("Finding opportunity by id and consumer: id={}, consumerId={}", id, consumerId);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE id = :id AND consumer_id = :consumerId
            """)
            .bind("id", id.value())
            .bind("consumerId", consumerId)
            .map(mapper::fromRow)
            .one()
            .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Opportunity> findByConsumerId(Long consumerId) {
        logger.debug("Finding opportunities by consumer: consumerId={}", consumerId);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE consumer_id = :consumerId
            ORDER BY created_at DESC
            """)
            .bind("consumerId", consumerId)
            .map(mapper::fromRow)
            .all()
            .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Opportunity> findPublishedByCategory(String category) {
        logger.debug("Finding published opportunities by category: {}", category);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE category = :category 
            AND status = :status
            ORDER BY created_at DESC
            """)
            .bind("category", category)
            .bind("status", OpportunityStatus.PUBLISHED.name())
            .map(mapper::fromRow)
            .all()
            .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Opportunity> findByStatus(OpportunityStatus status) {
        logger.debug("Finding opportunities by status: {}", status);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE status = :status
            ORDER BY created_at DESC
            """)
            .bind("status", status.name())
            .map(mapper::fromRow)
            .all()
            .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Opportunity> findByDeadlineBefore(Instant deadline) {
        logger.debug("Finding opportunities with deadline before: {}", deadline);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE deadline < :deadline
            AND status IN (:activeStatuses)
            ORDER BY deadline ASC
            """)
            .bind("deadline", deadline)
            .bind("activeStatuses", OpportunityStatus.activeStatuses()
                .stream()
                .map(Enum::name)
                .toList())
            .map(mapper::fromRow)
            .all()
            .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Opportunity> findPublishedByTenant(Long tenantId) {
        logger.debug("Finding published opportunities by tenant: {}", tenantId);
        
        return databaseClient.sql("""
            SELECT * FROM opportunities 
            WHERE tenant_id = :tenantId
            AND status = :status
            ORDER BY created_at DESC
            """)
            .bind("tenantId", tenantId)
            .bind("status", OpportunityStatus.PUBLISHED.name())
            .map(mapper::fromRow)
            .all()
            .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Void> deleteById(OpportunityId id) {
        logger.debug("Deleting opportunity: id={}", id);
        
        return databaseClient.sql("""
            DELETE FROM opportunities WHERE id = :id
            """)
            .bind("id", id.value())
            .then()
            .doOnSuccess(v -> logger.debug("Opportunity deleted: id={}", id));
    }
    
    @Override
    public Mono<Boolean> existsById(OpportunityId id) {
        return databaseClient.sql("""
            SELECT EXISTS(SELECT 1 FROM opportunities WHERE id = :id)
            """)
            .bind("id", id.value())
            .map(row -> row.get(0, Boolean.class))
            .one()
            .defaultIfEmpty(false);
    }
    
    @Override
    public Mono<Long> countByConsumerId(Long consumerId) {
        return databaseClient.sql("""
            SELECT COUNT(*) FROM opportunities WHERE consumer_id = :consumerId
            """)
            .bind("consumerId", consumerId)
            .map(row -> row.get(0, Long.class))
            .one()
            .defaultIfEmpty(0L);
    }
    
    @Override
    public Mono<Long> countByStatus(OpportunityStatus status) {
        return databaseClient.sql("""
            SELECT COUNT(*) FROM opportunities WHERE status = :status
            """)
            .bind("status", status.name())
            .map(row -> row.get(0, Long.class))
            .one()
            .defaultIfEmpty(0L);
    }
}
