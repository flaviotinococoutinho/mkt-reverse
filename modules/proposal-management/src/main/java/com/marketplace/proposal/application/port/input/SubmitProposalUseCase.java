package com.marketplace.proposal.application.port.input;

import com.marketplace.proposal.application.dto.request.SubmitProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import reactor.core.publisher.Mono;

/**
 * Input Port (Use Case Interface) for submitting proposals.
 * 
 * Defines contract for submitting a new proposal.
 * Implementation will be in application layer.
 * 
 * Follows Hexagonal Architecture:
 * - Port (interface) in application layer
 * - Implementation (use case) in application layer
 * - Adapter (controller) calls this port
 */
public interface SubmitProposalUseCase {
    
    /**
     * Submits a new proposal for an opportunity.
     * 
     * @param request submit proposal request
     * @return proposal response
     */
    Mono<ProposalResponse> execute(SubmitProposalRequest request);
}
