package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.request.UpdateProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.UpdateProposalUseCase;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.shared.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Use Case implementation for updating proposals.
 *
 * Orchestrates:
 * - Request validation and mapping
 * - Proposal retrieval
 * - Domain validation and updating
 * - Proposal persistence
 * - Response creation
 *
 * Follows:
 * - Hexagonal Architecture
 * - Single Responsibility Principle
 * - Dependency Inversion Principle
 */
@Service
public class UpdateProposalUseCaseImpl implements UpdateProposalUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProposalUseCaseImpl.class);

    private final ProposalRepository proposalRepository;

    public UpdateProposalUseCaseImpl(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Override
    public Mono<ProposalResponse> execute(Long proposalId, UpdateProposalRequest request) {
        return proposalRepository.findById(ProposalId.of(proposalId))
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Proposal not found"
            )))
            .map(proposal -> updateProposal(proposal, request))
            .flatMap(proposalRepository::save)
            .map(ProposalResponse::fromDomain)
            .doOnSuccess(response -> logSuccess(response))
            .doOnError(error -> logError(proposalId, error));
    }

    private Proposal updateProposal(Proposal proposal, UpdateProposalRequest request) {
        Money price = Money.of(request.priceAmount(), request.priceCurrency());
        DeliveryTime deliveryTime = DeliveryTime.fromDaysAndHours(
            request.deliveryDays(),
            request.deliveryHours()
        );

        proposal.update(price, deliveryTime, request.description());
        logger.debug("Proposal {} updated in memory successfully", proposal.getProposalId().value());
        return proposal;
    }

    private void logSuccess(ProposalResponse response) {
        logger.info(
            "Proposal updated successfully: proposalId={}, opportunityId={}, companyId={}",
            response.proposalId(),
            response.opportunityId(),
            response.companyId()
        );
    }

    private void logError(Long proposalId, Throwable error) {
        logger.error(
            "Failed to update proposal for proposalId={}: {}",
            proposalId,
            error.getMessage(),
            error
        );
    }
}
