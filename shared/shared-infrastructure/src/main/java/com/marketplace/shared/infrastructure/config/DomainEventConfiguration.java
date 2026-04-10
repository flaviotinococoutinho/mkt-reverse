package com.marketplace.shared.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.shared.events.DomainEventPublisher;
import com.marketplace.shared.events.LoggingDomainEventPublisher;
import com.marketplace.shared.infrastructure.events.TransactionalOutboxPublisher;
import com.marketplace.shared.infrastructure.outbox.OutboxEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Common infrastructure wiring reused by all Spring Boot services.
 */
@Configuration
@EnableJpaAuditing
@EnableScheduling
public class DomainEventConfiguration {

    @Bean
    @org.springframework.context.annotation.Primary
    public DomainEventPublisher domainEventPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new TransactionalOutboxPublisher(outboxEventRepository, objectMapper, applicationEventPublisher);
    }

    @Bean
    public LoggingDomainEventPublisher loggingDomainEventPublisher() {
        return new LoggingDomainEventPublisher();
    }
}
