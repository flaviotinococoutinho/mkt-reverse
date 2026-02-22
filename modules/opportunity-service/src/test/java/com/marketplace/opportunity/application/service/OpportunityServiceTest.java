package com.marketplace.opportunity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.marketplace.opportunity.application.dto.CreateBidRequest;
import com.marketplace.opportunity.application.dto.CreateOpportunityRequest;
import com.marketplace.opportunity.domain.BidStatus;
import com.marketplace.opportunity.domain.model.Bid;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.repository.BidRepository;
import com.marketplace.opportunity.domain.repository.NegotiationMessageRepository;
import com.marketplace.opportunity.domain.repository.OpportunityRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private NegotiationMessageRepository negotiationMessageRepository;

    @InjectMocks
    private OpportunityService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOpportunity() {
        CreateOpportunityRequest req = new CreateOpportunityRequest();
        req.setTitle("Pedido de notebook");
        req.setDescription("Quero um notebook gamer");
        req.setCategory("eletronicos");

        Opportunity saved = new Opportunity();
        saved.setId(UUID.randomUUID());
        saved.setTitle(req.getTitle());
        saved.setDescription(req.getDescription());
        saved.setCategory(req.getCategory());

        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(saved);

        var resp = service.createOpportunity(req, "user-1");

        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getTitle()).isEqualTo("Pedido de notebook");
    }

    @Test
    void shouldCreateBid() {
        UUID oppId = UUID.randomUUID();
        Opportunity opp = new Opportunity();
        opp.setId(oppId);

        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));

        Bid savedBid = new Bid();
        savedBid.setId(UUID.randomUUID());
        savedBid.setOpportunity(opp);
        savedBid.setProposerId("seller-1");
        savedBid.setAmount(BigDecimal.valueOf(1000));
        savedBid.setStatus(BidStatus.PENDING);

        when(bidRepository.save(any(Bid.class))).thenReturn(savedBid);

        CreateBidRequest req = new CreateBidRequest();
        req.setAmount(BigDecimal.valueOf(1000));

        var resp = service.createBid(oppId, req, "seller-1");

        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getProposerId()).isEqualTo("seller-1");
    }
}
