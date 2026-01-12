package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.FindProposalsByOpportunityUseCase;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Use Case implementation for retrieving proposals by opportunity.
 */
@Service
public class FindProposalsByOpportunityUseCaseImpl implements FindProposalsByOpportunityUseCase {

    private static final Logger logger = LoggerFactory.getLogger(FindProposalsByOpportunityUseCaseImpl.class);

    private final ProposalRepository proposalRepository;

    public FindProposalsByOpportunityUseCaseImpl(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Override
    public Flux<ProposalResponse> execute(Long opportunityId) {
        return proposalRepository.findByOpportunityId(opportunityId)
            .map(ProposalResponse::fromDomain)
            .doOnComplete(() -> logComplete(opportunityId))
            .doOnError(error -> logError(opportunityId, error));
    }

    private void logComplete(Long opportunityId) {
        logger.info("Fetched proposals for opportunityId={}", opportunityId);
    }

    private void logError(Long opportunityId, Throwable error) {
        logger.error(
            "Failed to fetch proposals for opportunityId={}: {}",
            opportunityId,
            error.getMessage(),
            error
        );
    }
}
