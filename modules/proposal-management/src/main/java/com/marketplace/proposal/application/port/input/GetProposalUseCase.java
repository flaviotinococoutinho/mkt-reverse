package com.marketplace.proposal.application.port.input;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import reactor.core.publisher.Mono;

/**
 * Use Case for retrieving a proposal by ID.
 */
public interface GetProposalUseCase {
    Mono<ProposalResponse> execute(Long proposalId);
}
