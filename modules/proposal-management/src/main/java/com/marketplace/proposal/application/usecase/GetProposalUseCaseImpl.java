package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.GetProposalUseCase;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Use Case implementation for retrieving proposals by ID.
 */
@Service
public class GetProposalUseCaseImpl implements GetProposalUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetProposalUseCaseImpl.class);

    private final ProposalRepository proposalRepository;

    public GetProposalUseCaseImpl(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Override
    public Mono<ProposalResponse> execute(Long proposalId) {
        return Mono.fromCallable(() -> ProposalId.of(proposalId))
            .flatMap(proposalRepository::findById)
            .map(ProposalResponse::fromDomain)
            .doOnSuccess(response -> logSuccess(proposalId, response))
            .doOnError(error -> logError(proposalId, error));
    }

    private void logSuccess(Long proposalId, ProposalResponse response) {
        if (response == null) {
            logger.debug("Proposal not found: proposalId={}", proposalId);
            return;
        }

        logger.info(
            "Proposal retrieved successfully: proposalId={}, opportunityId={}",
            response.proposalId(),
            response.opportunityId()
        );
    }

    private void logError(Long proposalId, Throwable error) {
        logger.error(
            "Failed to retrieve proposal: proposalId={}, error={}",
            proposalId,
            error.getMessage(),
            error
        );
    }
}
