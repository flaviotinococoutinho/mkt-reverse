package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.request.SubmitProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.input.SubmitProposalUseCase;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.command.SubmitProposalCommand;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.service.ProposalValidationChain;
import com.marketplace.proposal.domain.valueobject.DeliveryTime;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.shared.domain.valueobject.Money;
import com.marketplace.shared.infrastructure.id.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Use Case implementation for submitting proposals.
 * 
 * Orchestrates:
 * - Request validation
 * - Command creation
 * - Domain validation
 * - Proposal creation
 * - Proposal persistence
 * - Event publishing
 * - Response creation
 * 
 * Follows:
 * - Hexagonal Architecture
 * - Single Responsibility Principle
 * - Dependency Inversion Principle
 */
@Service
public class SubmitProposalUseCaseImpl implements SubmitProposalUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmitProposalUseCaseImpl.class);
    
    private final ProposalRepository proposalRepository;
    private final ProposalValidationChain validationChain;
    private final SnowflakeIdGenerator idGenerator;
    
    public SubmitProposalUseCaseImpl(
        ProposalRepository proposalRepository,
        ProposalValidationChain validationChain,
        SnowflakeIdGenerator idGenerator
    ) {
        this.proposalRepository = proposalRepository;
        this.validationChain = validationChain;
        this.idGenerator = idGenerator;
    }
    
    @Override
    public Mono<ProposalResponse> execute(SubmitProposalRequest request) {
        return Mono.fromCallable(() -> createCommand(request))
            .doOnNext(this::validateCommand)
            .flatMap(this::createAndSaveProposal)
            .map(ProposalResponse::fromDomain)
            .doOnSuccess(response -> logSuccess(response))
            .doOnError(error -> logError(request, error));
    }
    
    private SubmitProposalCommand createCommand(SubmitProposalRequest request) {
        Long tenantId = getTenantIdFromContext();
        Long companyId = getCompanyIdFromContext();
        
        Money price = Money.of(request.priceAmount(), request.priceCurrency());
        DeliveryTime deliveryTime = DeliveryTime.fromDaysAndHours(
            request.deliveryDays(),
            request.deliveryHours()
        );
        
        return new SubmitProposalCommand(
            request.opportunityId(),
            companyId,
            tenantId,
            price,
            deliveryTime,
            request.description(),
            request.attachments(),
            request.specifications()
        );
    }
    
    private void validateCommand(SubmitProposalCommand command) {
        validationChain.validate(command);
        logger.debug("Proposal command validated successfully");
    }
    
    private Mono<Proposal> createAndSaveProposal(SubmitProposalCommand command) {
        return Mono.fromCallable(() -> createProposal(command))
            .doOnNext(Proposal::submit)
            .flatMap(proposalRepository::save);
    }
    
    private Proposal createProposal(SubmitProposalCommand command) {
        ProposalId proposalId = ProposalId.of(idGenerator.nextId());
        
        Proposal proposal = Proposal.create(
            proposalId,
            command.opportunityId(),
            command.companyId(),
            command.tenantId(),
            command.price(),
            command.deliveryTime(),
            command.description()
        );
        
        command.attachments().forEach(proposal::addAttachment);
        command.specifications().forEach(proposal::addSpecification);
        
        logger.info("Created proposal with ID: {}", proposalId);
        
        return proposal;
    }
    
    private Long getTenantIdFromContext() {
        String tenantId = MDC.get("tenantId");
        return tenantId != null ? Long.parseLong(tenantId) : 1L;
    }
    
    private Long getCompanyIdFromContext() {
        String userId = MDC.get("userId");
        // In real implementation, fetch company ID from user service
        return userId != null ? Long.parseLong(userId) : 1L;
    }
    
    private void logSuccess(ProposalResponse response) {
        logger.info(
            "Proposal submitted successfully: proposalId={}, opportunityId={}, companyId={}",
            response.proposalId(),
            response.opportunityId(),
            response.companyId()
        );
    }
    
    private void logError(SubmitProposalRequest request, Throwable error) {
        logger.error(
            "Failed to submit proposal for opportunityId={}: {}",
            request.opportunityId(),
            error.getMessage(),
            error
        );
    }
}
