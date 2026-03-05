package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.AcceptProposalUseCase;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Use Case implementation for accepting a proposal by ID.
 */
@Service
public class AcceptProposalUseCaseImpl implements AcceptProposalUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AcceptProposalUseCaseImpl.class);

    private final ProposalRepository proposalRepository;

    public AcceptProposalUseCaseImpl(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Override
    public Mono<ProposalResponse> execute(Long proposalId) {
        return Mono.fromCallable(() -> ProposalId.of(proposalId))
            .flatMap(proposalRepository::findById)
            .flatMap(proposal -> {
                try {
                    proposal.accept();
                    return proposalRepository.save(proposal);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            })
            .map(ProposalResponse::fromDomain)
            .doOnSuccess(response -> {
                if (response != null) {
                    logger.info("Proposal accepted successfully: proposalId={}", proposalId);
                }
            })
            .doOnError(error -> logger.error("Failed to accept proposal: proposalId={}, error={}", proposalId, error.getMessage(), error));
    }
}
