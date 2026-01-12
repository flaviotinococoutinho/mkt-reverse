package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.request.SubmitProposalRequest;
import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.service.ProposalValidationChain;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitProposalUseCaseImplTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProposalValidationChain validationChain;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Proposal> proposalCaptor;

    private SubmitProposalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitProposalUseCaseImpl(proposalRepository, validationChain, idGenerator);
    }

    @Test
    void executeCreatesProposalAndReturnsResponse() {
        when(idGenerator.nextId()).thenReturn(1001L);
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        SubmitProposalRequest request = new SubmitProposalRequest(
            55L,
            BigDecimal.valueOf(1200),
            "USD",
            5,
            4,
            "Detailed proposal covering requirements, timeline, and delivery milestones for the project.",
            List.of("https://files.example.com/portfolio.pdf"),
            Map.of("stack", "Spring")
        );

        StepVerifier.create(useCase.execute(request))
            .assertNext(response -> {
                assertThat(response).isInstanceOf(ProposalResponse.class);
                assertThat(response.opportunityId()).isEqualTo(request.opportunityId());
                assertThat(response.companyId()).isEqualTo(1L);
            })
            .verifyComplete();

        verify(validationChain).validate(any());
        verify(proposalRepository).save(proposalCaptor.capture());
        assertThat(proposalCaptor.getValue().getAttachments())
            .containsExactly("https://files.example.com/portfolio.pdf");
        assertThat(proposalCaptor.getValue().getSpecifications())
            .containsEntry("stack", "Spring");
    }
}
