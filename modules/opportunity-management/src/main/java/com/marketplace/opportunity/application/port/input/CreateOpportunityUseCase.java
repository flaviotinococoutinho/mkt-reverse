package com.marketplace.opportunity.application.port.input;

import com.marketplace.opportunity.domain.command.CreateOpportunityCommand;
import com.marketplace.opportunity.domain.model.Opportunity;
import reactor.core.publisher.Mono;

/**
 * Input Port (Driving Port) for creating opportunities.
 * 
 * Part of Hexagonal Architecture:
 * - Defines contract for creating opportunities
 * - Independent of implementation details
 * - Used by adapters (REST, WebSocket, etc.)
 * 
 * Follows Interface Segregation Principle (ISP).
 */
public interface CreateOpportunityUseCase {
    
    /**
     * Creates a new opportunity from command.
     * 
     * @param command create opportunity command
     * @return created opportunity
     */
    Mono<Opportunity> execute(CreateOpportunityCommand command);
}
