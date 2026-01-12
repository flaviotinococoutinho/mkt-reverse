package com.marketplace.opportunity.adapter.input.rest;

import com.marketplace.opportunity.application.dto.request.CreateOpportunityRequest;
import com.marketplace.opportunity.application.dto.response.OpportunityResponse;
import com.marketplace.opportunity.application.port.input.CreateOpportunityUseCase;
import com.marketplace.opportunity.domain.command.CreateOpportunityCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST Controller for opportunity management.
 * 
 * Input Adapter in Hexagonal Architecture:
 * - Receives HTTP requests
 * - Validates input
 * - Converts DTOs to commands
 * - Delegates to use cases
 * - Converts domain models to DTOs
 * 
 * Follows RESTful principles:
 * - Resource-based URLs
 * - HTTP verbs
 * - Status codes
 * - HATEOAS (future)
 * 
 * Reactive implementation using Spring WebFlux.
 */
@RestController
@RequestMapping("/api/v1/opportunities")
public class OpportunityController {
    
    private static final Logger logger = LoggerFactory.getLogger(OpportunityController.class);
    
    private final CreateOpportunityUseCase createOpportunityUseCase;
    
    public OpportunityController(CreateOpportunityUseCase createOpportunityUseCase) {
        this.createOpportunityUseCase = createOpportunityUseCase;
    }
    
    /**
     * Creates a new opportunity.
     * 
     * POST /api/v1/opportunities
     * 
     * @param request create opportunity request
     * @return created opportunity response
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OpportunityResponse> createOpportunity(
        @Valid @RequestBody CreateOpportunityRequest request
    ) {
        logger.info("Received request to create opportunity: title={}", request.title());
        
        return Mono.just(request)
            .map(this::toCommand)
            .flatMap(createOpportunityUseCase::execute)
            .map(OpportunityResponse::from)
            .doOnSuccess(response -> logSuccess(response))
            .doOnError(error -> logError(request, error));
    }
    
    private CreateOpportunityCommand toCommand(CreateOpportunityRequest request) {
        return new CreateOpportunityCommand(
            request.consumerId(),
            request.tenantId(),
            request.title(),
            request.description(),
            request.category(),
            request.budgetAmount(),
            request.currencyCode(),
            request.deadline(),
            request.attachments(),
            request.specifications(),
            request.templateKey()
        );
    }
    
    private void logSuccess(OpportunityResponse response) {
        logger.info(
            "Opportunity created successfully: id={}, title={}",
            response.id(),
            response.title()
        );
    }
    
    private void logError(CreateOpportunityRequest request, Throwable error) {
        logger.error(
            "Failed to create opportunity: title={}, error={}",
            request.title(),
            error.getMessage(),
            error
        );
    }
}
