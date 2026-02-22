package com.marketplace.shared.infrastructure.config;

import com.marketplace.shared.events.DomainEventPublisher;
import com.marketplace.shared.events.LoggingDomainEventPublisher;
import com.marketplace.shared.infrastructure.events.SpringDomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Common infrastructure wiring reused by all Spring Boot services.
 */
@Configuration
@EnableJpaAuditing
public class DomainEventConfiguration {

    @Bean
    @org.springframework.context.annotation.Primary
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        // Wrap Spring's publisher so domain events propagate to any @EventListener
        return new SpringDomainEventPublisher(applicationEventPublisher);
    }

    @Bean
    public LoggingDomainEventPublisher loggingDomainEventPublisher() {
        return new LoggingDomainEventPublisher();
    }
}
