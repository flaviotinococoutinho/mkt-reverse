package com.marketplace.proposal.application.port.input;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import reactor.core.publisher.Flux;

/**
 * Use Case for retrieving proposals by opportunity ID.
 */
public interface FindProposalsByOpportunityUseCase {
    Flux<ProposalResponse> execute(Long opportunityId);
}
