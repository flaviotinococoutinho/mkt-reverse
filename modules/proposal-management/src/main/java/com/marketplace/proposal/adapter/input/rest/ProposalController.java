package com.marketplace.proposal.adapter.input.rest;

import com.marketplace.proposal.application.dto.request.SubmitProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.SubmitProposalUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST Controller for Proposal operations.
 * 
 * Endpoints:
 * - POST /api/v1/proposals - Submit new proposal
 * - GET /api/v1/proposals/{id} - Get proposal by ID
 * - GET /api/v1/opportunities/{opportunityId}/proposals - Get proposals for opportunity
 * - PUT /api/v1/proposals/{id} - Update proposal
 * - DELETE /api/v1/proposals/{id}/withdraw - Withdraw proposal
 * - POST /api/v1/proposals/{id}/accept - Accept proposal
 * - POST /api/v1/proposals/{id}/reject - Reject proposal
 * 
 * Follows:
 * - RESTful principles
 * - Reactive programming (Spring WebFlux)
 * - Bean Validation
 * - Hexagonal Architecture (Adapter)
 */
@RestController
@RequestMapping("/api/v1/proposals")
public class ProposalController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProposalController.class);
    
    private final SubmitProposalUseCase submitProposalUseCase;
    
    public ProposalController(SubmitProposalUseCase submitProposalUseCase) {
        this.submitProposalUseCase = submitProposalUseCase;
    }
    
    /**
     * Submits a new proposal.
     * 
     * @param request submit proposal request
     * @return proposal response
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProposalResponse> submitProposal(@Valid @RequestBody SubmitProposalRequest request) {
        logger.info("Received request to submit proposal for opportunityId: {}", request.opportunityId());
        
        return submitProposalUseCase.execute(request)
            .doOnSuccess(response -> 
                logger.info("Proposal submitted successfully: proposalId={}", response.proposalId())
            )
            .doOnError(error -> 
                logger.error("Failed to submit proposal: {}", error.getMessage())
            );
    }
    
    /**
     * Gets proposal by ID.
     * 
     * @param proposalId proposal identifier
     * @return proposal response
     */
    @GetMapping(
        value = "/{proposalId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ProposalResponse> getProposal(@PathVariable Long proposalId) {
        logger.info("Received request to get proposal: proposalId={}", proposalId);
        
        // TODO: Implement GetProposalUseCase
        return Mono.empty();
    }
    
    /**
     * Gets all proposals for an opportunity.
     * 
     * @param opportunityId opportunity identifier
     * @return flux of proposal responses
     */
    @GetMapping(
        value = "/opportunity/{opportunityId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ProposalResponse> getProposalsForOpportunity(@PathVariable Long opportunityId) {
        logger.info("Received request to get proposals for opportunityId: {}", opportunityId);
        
        // TODO: Implement FindProposalsByOpportunityUseCase
        return Mono.empty();
    }
    
    /**
     * Updates proposal.
     * 
     * @param proposalId proposal identifier
     * @param request update proposal request
     * @return proposal response
     */
    @PutMapping(
        value = "/{proposalId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ProposalResponse> updateProposal(
        @PathVariable Long proposalId,
        @Valid @RequestBody SubmitProposalRequest request
    ) {
        logger.info("Received request to update proposal: proposalId={}", proposalId);
        
        // TODO: Implement UpdateProposalUseCase
        return Mono.empty();
    }
    
    /**
     * Withdraws proposal.
     * 
     * @param proposalId proposal identifier
     * @return void
     */
    @DeleteMapping("/{proposalId}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> withdrawProposal(@PathVariable Long proposalId) {
        logger.info("Received request to withdraw proposal: proposalId={}", proposalId);
        
        // TODO: Implement WithdrawProposalUseCase
        return Mono.empty();
    }
    
    /**
     * Accepts proposal.
     * 
     * @param proposalId proposal identifier
     * @return proposal response
     */
    @PostMapping(
        value = "/{proposalId}/accept",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ProposalResponse> acceptProposal(@PathVariable Long proposalId) {
        logger.info("Received request to accept proposal: proposalId={}", proposalId);
        
        // TODO: Implement AcceptProposalUseCase
        return Mono.empty();
    }
    
    /**
     * Rejects proposal.
     * 
     * @param proposalId proposal identifier
     * @return void
     */
    @PostMapping("/{proposalId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> rejectProposal(@PathVariable Long proposalId) {
        logger.info("Received request to reject proposal: proposalId={}", proposalId);
        
        // TODO: Implement RejectProposalUseCase
        return Mono.empty();
    }
}
