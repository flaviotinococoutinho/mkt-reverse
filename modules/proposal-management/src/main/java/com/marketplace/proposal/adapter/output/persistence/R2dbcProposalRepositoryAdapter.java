package com.marketplace.proposal.adapter.output.persistence;

import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.proposal.domain.valueobject.ProposalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2DBC Repository Adapter for Proposal.
 * 
 * Implements ProposalRepository port using R2DBC.
 * Provides reactive database access.
 * 
 * Follows:
 * - Hexagonal Architecture (Adapter)
 * - Reactive programming
 * - Repository Pattern
 */
@Repository
public class R2dbcProposalRepositoryAdapter implements ProposalRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(R2dbcProposalRepositoryAdapter.class);
    
    private final DatabaseClient databaseClient;
    private final ProposalEntityMapper entityMapper;
    
    public R2dbcProposalRepositoryAdapter(
        DatabaseClient databaseClient,
        ProposalEntityMapper entityMapper
    ) {
        this.databaseClient = databaseClient;
        this.entityMapper = entityMapper;
    }
    
    @Override
    public Mono<Proposal> save(Proposal proposal) {
        ProposalEntity entity = entityMapper.toEntity(proposal);
        
        String sql = """
            INSERT INTO proposals (
                id, opportunity_id, company_id, tenant_id,
                price_amount, price_currency,
                delivery_days, delivery_hours, estimated_delivery_date,
                description, status,
                attachments, specifications,
                created_at, updated_at, submitted_at, accepted_at
            ) VALUES (
                :id, :opportunityId, :companyId, :tenantId,
                :priceAmount, :priceCurrency,
                :deliveryDays, :deliveryHours, :estimatedDeliveryDate,
                :description, :status,
                :attachments::jsonb, :specifications::jsonb,
                :createdAt, :updatedAt, :submittedAt, :acceptedAt
            )
            ON CONFLICT (id) DO UPDATE SET
                price_amount = EXCLUDED.price_amount,
                price_currency = EXCLUDED.price_currency,
                delivery_days = EXCLUDED.delivery_days,
                delivery_hours = EXCLUDED.delivery_hours,
                estimated_delivery_date = EXCLUDED.estimated_delivery_date,
                description = EXCLUDED.description,
                status = EXCLUDED.status,
                attachments = EXCLUDED.attachments,
                specifications = EXCLUDED.specifications,
                updated_at = EXCLUDED.updated_at,
                submitted_at = EXCLUDED.submitted_at,
                accepted_at = EXCLUDED.accepted_at
            RETURNING *
            """;
        
        return databaseClient.sql(sql)
            .bind("id", entity.id())
            .bind("opportunityId", entity.opportunityId())
            .bind("companyId", entity.companyId())
            .bind("tenantId", entity.tenantId())
            .bind("priceAmount", entity.priceAmount())
            .bind("priceCurrency", entity.priceCurrency())
            .bind("deliveryDays", entity.deliveryDays())
            .bind("deliveryHours", entity.deliveryHours())
            .bind("estimatedDeliveryDate", entity.estimatedDeliveryDate())
            .bind("description", entity.description())
            .bind("status", entity.status().name())
            .bind("attachments", entity.attachmentsJson())
            .bind("specifications", entity.specificationsJson())
            .bind("createdAt", entity.createdAt())
            .bind("updatedAt", entity.updatedAt())
            .bind("submittedAt", entity.submittedAt())
            .bind("acceptedAt", entity.acceptedAt())
            .fetch()
            .one()
            .map(entityMapper::fromRow)
            .doOnSuccess(saved -> 
                logger.debug("Saved proposal: id={}", saved.getProposalId())
            )
            .doOnError(error -> 
                logger.error("Failed to save proposal: {}", error.getMessage())
            );
    }
    
    @Override
    public Mono<Proposal> findById(ProposalId proposalId) {
        String sql = "SELECT * FROM proposals WHERE id = :id";
        
        return databaseClient.sql(sql)
            .bind("id", proposalId.value())
            .fetch()
            .one()
            .map(entityMapper::fromRow)
            .doOnSuccess(proposal -> 
                logger.debug("Found proposal: id={}", proposalId)
            );
    }
    
    @Override
    public Flux<Proposal> findByOpportunityId(Long opportunityId) {
        String sql = "SELECT * FROM proposals WHERE opportunity_id = :opportunityId ORDER BY created_at DESC";
        
        return databaseClient.sql(sql)
            .bind("opportunityId", opportunityId)
            .fetch()
            .all()
            .map(entityMapper::fromRow)
            .doOnComplete(() -> 
                logger.debug("Found proposals for opportunityId: {}", opportunityId)
            );
    }
    
    @Override
    public Flux<Proposal> findByCompanyId(Long companyId) {
        String sql = "SELECT * FROM proposals WHERE company_id = :companyId ORDER BY created_at DESC";
        
        return databaseClient.sql(sql)
            .bind("companyId", companyId)
            .fetch()
            .all()
            .map(entityMapper::fromRow);
    }
    
    @Override
    public Flux<Proposal> findByStatus(ProposalStatus status) {
        String sql = "SELECT * FROM proposals WHERE status = :status ORDER BY created_at DESC";
        
        return databaseClient.sql(sql)
            .bind("status", status.name())
            .fetch()
            .all()
            .map(entityMapper::fromRow);
    }
    
    @Override
    public Flux<Proposal> findByOpportunityIdAndStatus(Long opportunityId, ProposalStatus status) {
        String sql = """
            SELECT * FROM proposals 
            WHERE opportunity_id = :opportunityId AND status = :status 
            ORDER BY created_at DESC
            """;
        
        return databaseClient.sql(sql)
            .bind("opportunityId", opportunityId)
            .bind("status", status.name())
            .fetch()
            .all()
            .map(entityMapper::fromRow);
    }
    
    @Override
    public Mono<Void> deleteById(ProposalId proposalId) {
        String sql = "DELETE FROM proposals WHERE id = :id";
        
        return databaseClient.sql(sql)
            .bind("id", proposalId.value())
            .fetch()
            .rowsUpdated()
            .then()
            .doOnSuccess(v -> 
                logger.debug("Deleted proposal: id={}", proposalId)
            );
    }
    
    @Override
    public Mono<Boolean> existsById(ProposalId proposalId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM proposals WHERE id = :id)";
        
        return databaseClient.sql(sql)
            .bind("id", proposalId.value())
            .fetch()
            .one()
            .map(row -> (Boolean) row.get("exists"))
            .defaultIfEmpty(false);
    }
    
    @Override
    public Mono<Long> countByOpportunityId(Long opportunityId) {
        String sql = "SELECT COUNT(*) as count FROM proposals WHERE opportunity_id = :opportunityId";
        
        return databaseClient.sql(sql)
            .bind("opportunityId", opportunityId)
            .fetch()
            .one()
            .map(row -> ((Number) row.get("count")).longValue())
            .defaultIfEmpty(0L);
    }
}
