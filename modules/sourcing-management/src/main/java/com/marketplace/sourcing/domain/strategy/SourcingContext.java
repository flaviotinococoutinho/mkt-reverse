package com.marketplace.sourcing.domain.strategy;

import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service that selects and executes the appropriate sourcing strategy.
 */
@Service
public class SourcingContext {

    private final List<SourcingStrategy> strategies;

    public SourcingContext(List<SourcingStrategy> strategies) {
        this.strategies = strategies;
    }

    public void execute(SourcingEvent event) {
        SourcingStrategy strategy = strategies.stream()
            .filter(s -> s.supports(event.getEventType()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No strategy found for type: " + event.getEventType()));
        
        strategy.execute(event);
    }
}
