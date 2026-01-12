package com.marketplace.opportunity.application.usecase;

import com.marketplace.opportunity.application.port.input.CreateOpportunityUseCase;
import com.marketplace.opportunity.application.port.output.EventPublisher;
import com.marketplace.opportunity.application.port.output.OpportunityRepository;
import com.marketplace.opportunity.domain.command.CreateOpportunityCommand;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.model.OpportunitySpecification;
import com.marketplace.opportunity.domain.service.ValidationChain;
import com.marketplace.opportunity.domain.valueobject.Money;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.shared.infrastructure.id.SnowflakeIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Currency;

/**
 * Use Case implementation for creating opportunities.
 * 
 * Follows Hexagonal Architecture:
 * - Implements input port (driving port)
 * - Uses output ports (driven ports)
 * - Contains application logic
 * - Orchestrates domain objects
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Only creates opportunities
 * - Dependency Inversion: Depends on abstractions (ports)
 * - Interface Segregation: Uses specific ports
 * 
 * Reactive implementation using Project Reactor.
 */
@Service
public class CreateOpportunityUseCaseImpl implements CreateOpportunityUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateOpportunityUseCaseImpl.class);
    
    private final OpportunityRepository repository;
    private final EventPublisher eventPublisher;
    private final SnowflakeIdGenerator idGenerator;
    private final ValidationChain validationChain;
    
    public CreateOpportunityUseCaseImpl(
        OpportunityRepository repository,
        EventPublisher eventPublisher,
        SnowflakeIdGenerator idGenerator
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.idGenerator = idGenerator;
        this.validationChain = buildValidationChain();
    }
    
    @Override
    public Mono<Opportunity> execute(CreateOpportunityCommand command) {
        return Mono.just(command)
            .doOnNext(cmd -> logExecutionStart(cmd))
            .doOnNext(CreateOpportunityCommand::validate)
            .map(this::buildOpportunity)
            .flatMap(this::validateOpportunity)
            .flatMap(repository::save)
            .flatMap(this::publishDomainEvents)
            .doOnSuccess(opportunity -> logExecutionSuccess(opportunity))
            .doOnError(error -> logExecutionError(command, error));
    }
    
    private Opportunity buildOpportunity(CreateOpportunityCommand command) {
        OpportunityId id = generateOpportunityId();
        Money budget = createBudget(command);
        OpportunitySpecification specification = createSpecification(command);
        
        return Opportunity.builder()
            .id(id)
            .consumerId(command.consumerId())
            .tenantId(command.tenantId())
            .title(command.title())
            .description(command.description())
            .category(command.category())
            .budget(budget)
            .deadline(command.deadline())
            .attachments(command.attachments())
            .specification(specification)
            .build();
    }
    
    private OpportunityId generateOpportunityId() {
        Long id = idGenerator.nextId();
        return OpportunityId.of(id);
    }
    
    private Money createBudget(CreateOpportunityCommand command) {
        Currency currency = Currency.getInstance(command.currencyCode());
        return Money.of(command.budgetAmount(), currency);
    }
    
    private OpportunitySpecification createSpecification(CreateOpportunityCommand command) {
        if (hasTemplate(command)) {
            return OpportunitySpecification.withTemplate(
                command.specifications(),
                command.templateKey()
            );
        }
        return OpportunitySpecification.of(command.specifications());
    }
    
    private boolean hasTemplate(CreateOpportunityCommand command) {
        return command.templateKey() != null && !command.templateKey().isBlank();
    }
    
    private Mono<Opportunity> validateOpportunity(Opportunity opportunity) {
        return Mono.fromCallable(() -> validationChain.validate(opportunity))
            .flatMap(result -> {
                if (result.isValid()) {
                    return Mono.just(opportunity);
                }
                return Mono.error(
                    new IllegalArgumentException("Opportunity validation failed: " + result.firstError())
                );
            });
    }
    
    private Mono<Opportunity> publishDomainEvents(Opportunity opportunity) {
        if (hasNoDomainEvents(opportunity)) {
            return Mono.just(opportunity);
        }
        
        return eventPublisher.publishAll(opportunity.domainEvents())
            .doOnSuccess(v -> opportunity.clearDomainEvents())
            .thenReturn(opportunity);
    }
    
    private boolean hasNoDomainEvents(Opportunity opportunity) {
        return opportunity.domainEvents().isEmpty();
    }
    
    private ValidationChain buildValidationChain() {
        return new ValidationChain.Builder()
            .addTitleValidator()
            .addBudgetValidator()
            .addDeadlineValidator()
            .addCategoryValidator()
            .build();
    }
    
    private void logExecutionStart(CreateOpportunityCommand command) {
        MDC.put("consumerId", String.valueOf(command.consumerId()));
        MDC.put("tenantId", String.valueOf(command.tenantId()));
        
        logger.info(
            "Creating opportunity: title={}, category={}, budget={}",
            command.title(),
            command.category(),
            command.budgetAmount()
        );
    }
    
    private void logExecutionSuccess(Opportunity opportunity) {
        MDC.put("opportunityId", opportunity.id().toString());
        
        logger.info(
            "Opportunity created successfully: id={}, title={}, status={}",
            opportunity.id(),
            opportunity.title(),
            opportunity.status()
        );
        
        MDC.clear();
    }
    
    private void logExecutionError(CreateOpportunityCommand command, Throwable error) {
        logger.error(
            "Failed to create opportunity: title={}, error={}",
            command.title(),
            error.getMessage(),
            error
        );
        
        MDC.clear();
    }
}
