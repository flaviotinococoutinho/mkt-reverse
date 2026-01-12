package com.marketplace.opportunity.application.usecase;

import com.marketplace.opportunity.application.port.output.EventPublisher;
import com.marketplace.opportunity.application.port.output.OpportunityRepository;
import com.marketplace.opportunity.domain.command.CreateOpportunityCommand;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.shared.infrastructure.id.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOpportunityUseCaseImplTest {

    @Mock
    private OpportunityRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Opportunity> opportunityCaptor;

    private CreateOpportunityUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOpportunityUseCaseImpl(repository, eventPublisher, idGenerator);
    }

    @Test
    void executeBuildsOpportunityAndPersists() {
        when(idGenerator.nextId()).thenReturn(42L);
        when(repository.save(any(Opportunity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        CreateOpportunityCommand command = new CreateOpportunityCommand(
            10L,
            20L,
            "New mobile app development",
            "We need a new mobile app built with modern architecture and testing.",
            "Software",
            BigDecimal.valueOf(15000),
            "USD",
            Instant.now().plusSeconds(3600),
            List.of("https://files.example.com/brief.pdf"),
            Map.of("platform", "iOS"),
            "mobile-app-template"
        );

        StepVerifier.create(useCase.execute(command))
            .assertNext(opportunity -> {
                assertThat(opportunity.id().value()).isEqualTo(42L);
                assertThat(opportunity.title()).isEqualTo(command.title());
                assertThat(opportunity.budget().currencyCode()).isEqualTo("USD");
                assertThat(opportunity.attachments()).containsExactly("https://files.example.com/brief.pdf");
                assertThat(opportunity.specification().templateKey()).isEqualTo("mobile-app-template");
                assertThat(opportunity.specification().all()).containsEntry("platform", "iOS");
            })
            .verifyComplete();

        verify(repository).save(opportunityCaptor.capture());
        assertThat(opportunityCaptor.getValue().consumerId()).isEqualTo(command.consumerId());
        verify(eventPublisher, never()).publishAll(any());
    }
}
