package com.marketplace.proposal.application.port.input;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import reactor.core.publisher.Mono;

/**
 * Use Case for accepting a proposal by ID.
 */
public interface AcceptProposalUseCase {
    Mono<ProposalResponse> execute(Long proposalId);
}
