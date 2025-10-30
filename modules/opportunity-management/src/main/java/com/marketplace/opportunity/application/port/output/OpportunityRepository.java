package com.marketplace.opportunity.application.port.output;

import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Output Port (Driven Port) for Opportunity persistence.
 * 
 * Part of Hexagonal Architecture:
 * - Interface defined in application layer
 * - Implementation in adapter layer (infrastructure)
 * - Domain doesn't depend on infrastructure
 * 
 * Uses reactive types (Mono/Flux) for non-blocking operations.
 */
public interface OpportunityRepository {
    
    /**
     * Saves an opportunity.
     * 
     * @param opportunity opportunity to save
     * @return saved opportunity
     */
    Mono<Opportunity> save(Opportunity opportunity);
    
    /**
     * Finds opportunity by ID.
     * 
     * @param id opportunity ID
     * @return opportunity if found
     */
    Mono<Opportunity> findById(OpportunityId id);
    
    /**
     * Finds opportunity by ID and consumer ID.
     * 
     * @param id opportunity ID
     * @param consumerId consumer ID
     * @return opportunity if found and belongs to consumer
     */
    Mono<Opportunity> findByIdAndConsumerId(OpportunityId id, Long consumerId);
    
    /**
     * Finds all opportunities by consumer ID.
     * 
     * @param consumerId consumer ID
     * @return flux of opportunities
     */
    Flux<Opportunity> findByConsumerId(Long consumerId);
    
    /**
     * Finds all published opportunities by category.
     * 
     * @param category category
     * @return flux of opportunities
     */
    Flux<Opportunity> findPublishedByCategory(String category);
    
    /**
     * Finds all opportunities by status.
     * 
     * @param status opportunity status
     * @return flux of opportunities
     */
    Flux<Opportunity> findByStatus(OpportunityStatus status);
    
    /**
     * Finds all opportunities with deadline before given instant.
     * 
     * @param deadline deadline instant
     * @return flux of opportunities
     */
    Flux<Opportunity> findByDeadlineBefore(Instant deadline);
    
    /**
     * Finds all published opportunities for a tenant.
     * 
     * @param tenantId tenant ID
     * @return flux of opportunities
     */
    Flux<Opportunity> findPublishedByTenant(Long tenantId);
    
    /**
     * Deletes an opportunity.
     * 
     * @param id opportunity ID
     * @return completion signal
     */
    Mono<Void> deleteById(OpportunityId id);
    
    /**
     * Checks if opportunity exists.
     * 
     * @param id opportunity ID
     * @return true if exists
     */
    Mono<Boolean> existsById(OpportunityId id);
    
    /**
     * Counts opportunities by consumer ID.
     * 
     * @param consumerId consumer ID
     * @return count of opportunities
     */
    Mono<Long> countByConsumerId(Long consumerId);
    
    /**
     * Counts opportunities by status.
     * 
     * @param status opportunity status
     * @return count of opportunities
     */
    Mono<Long> countByStatus(OpportunityStatus status);
}
