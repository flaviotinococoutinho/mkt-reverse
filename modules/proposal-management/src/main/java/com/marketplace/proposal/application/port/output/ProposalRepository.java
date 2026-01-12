package com.marketplace.proposal.application.port.output;

import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.proposal.domain.valueobject.ProposalStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Output Port (Repository Interface) for Proposal.
 * 
 * Defines contract for proposal persistence.
 * Implementation will be in adapter layer.
 * 
 * Follows Hexagonal Architecture:
 * - Port defined in application layer
 * - Adapter implements in infrastructure layer
 * - Domain doesn't depend on infrastructure
 */
public interface ProposalRepository {
    
    /**
     * Saves proposal.
     * 
     * @param proposal proposal to save
     * @return saved proposal
     */
    Mono<Proposal> save(Proposal proposal);
    
    /**
     * Finds proposal by ID.
     * 
     * @param proposalId proposal identifier
     * @return proposal if found
     */
    Mono<Proposal> findById(ProposalId proposalId);
    
    /**
     * Finds all proposals for an opportunity.
     * 
     * @param opportunityId opportunity identifier
     * @return flux of proposals
     */
    Flux<Proposal> findByOpportunityId(Long opportunityId);
    
    /**
     * Finds all proposals by company.
     * 
     * @param companyId company identifier
     * @return flux of proposals
     */
    Flux<Proposal> findByCompanyId(Long companyId);
    
    /**
     * Finds proposals by status.
     * 
     * @param status proposal status
     * @return flux of proposals
     */
    Flux<Proposal> findByStatus(ProposalStatus status);
    
    /**
     * Finds proposals by opportunity and status.
     * 
     * @param opportunityId opportunity identifier
     * @param status proposal status
     * @return flux of proposals
     */
    Flux<Proposal> findByOpportunityIdAndStatus(Long opportunityId, ProposalStatus status);
    
    /**
     * Deletes proposal.
     * 
     * @param proposalId proposal identifier
     * @return completion signal
     */
    Mono<Void> deleteById(ProposalId proposalId);
    
    /**
     * Checks if proposal exists.
     * 
     * @param proposalId proposal identifier
     * @return true if exists
     */
    Mono<Boolean> existsById(ProposalId proposalId);
    
    /**
     * Counts proposals for opportunity.
     * 
     * @param opportunityId opportunity identifier
     * @return count of proposals
     */
    Mono<Long> countByOpportunityId(Long opportunityId);
}
