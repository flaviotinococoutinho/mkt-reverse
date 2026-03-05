package com.marketplace.proposal.application.port.input;

import com.marketplace.proposal.application.dto.request.UpdateProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import reactor.core.publisher.Mono;

/**
 * Input Port (Use Case Interface) for updating proposals.
 *
 * Defines contract for updating an existing proposal.
 * Implementation will be in application layer.
 *
 * Follows Hexagonal Architecture:
 * - Port (interface) in application layer
 * - Implementation (use case) in application layer
 * - Adapter (controller) calls this port
 */
public interface UpdateProposalUseCase {

    /**
     * Updates an existing proposal.
     *
     * @param proposalId proposal identifier
     * @param request update proposal request
     * @return proposal response
     */
    Mono<ProposalResponse> execute(Long proposalId, UpdateProposalRequest request);
}
