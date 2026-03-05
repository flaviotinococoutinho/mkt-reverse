package com.marketplace.proposal.application.usecase;

import com.marketplace.proposal.application.dto.response.ProposalResponse;
import com.marketplace.proposal.application.port.output.ProposalRepository;
import com.marketplace.proposal.domain.model.Proposal;
import com.marketplace.proposal.domain.valueobject.ProposalId;
import com.marketplace.proposal.domain.valueobject.ProposalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptProposalUseCaseImplTest {

    @Mock
    private ProposalRepository proposalRepository;

    private AcceptProposalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AcceptProposalUseCaseImpl(proposalRepository);
    }

    @Test
    void executeAcceptsProposalAndReturnsResponse() {
        Long proposalIdVal = 1001L;
        ProposalId proposalId = ProposalId.of(proposalIdVal);

        // Given a submitted proposal
        Proposal proposal = Proposal.builder()
            .proposalId(proposalId)
            .opportunityId(55L)
            .companyId(1L)
            .status(ProposalStatus.SUBMITTED)
            .price(BigDecimal.valueOf(1000))
            .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Mono.just(proposal));
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(useCase.execute(proposalIdVal))
            .assertNext(response -> {
                assertThat(response).isInstanceOf(ProposalResponse.class);
                assertThat(response.proposalId()).isEqualTo(proposalIdVal);
                assertThat(response.status()).isEqualTo(ProposalStatus.ACCEPTED.name());
            })
            .verifyComplete();

        verify(proposalRepository).findById(proposalId);
        verify(proposalRepository).save(any(Proposal.class));
    }

    @Test
    void executeReturnsErrorWhenProposalNotFound() {
        Long proposalIdVal = 1001L;
        ProposalId proposalId = ProposalId.of(proposalIdVal);

        when(proposalRepository.findById(proposalId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(proposalIdVal))
            .verifyComplete(); // Empty Mono returned

        verify(proposalRepository).findById(proposalId);
    }

    @Test
    void executeReturnsErrorWhenInvalidStateTransition() {
        Long proposalIdVal = 1001L;
        ProposalId proposalId = ProposalId.of(proposalIdVal);

        // Given a proposal that is already withdrawn
        Proposal proposal = Proposal.builder()
            .proposalId(proposalId)
            .opportunityId(55L)
            .companyId(1L)
            .status(ProposalStatus.WITHDRAWN)
            .price(BigDecimal.valueOf(1000))
            .build();

        when(proposalRepository.findById(proposalId)).thenReturn(Mono.just(proposal));

        StepVerifier.create(useCase.execute(proposalIdVal))
            .expectError(IllegalStateException.class)
            .verify();

        verify(proposalRepository).findById(proposalId);
    }
}
