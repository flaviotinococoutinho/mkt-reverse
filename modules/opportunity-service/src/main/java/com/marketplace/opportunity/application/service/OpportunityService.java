package com.marketplace.opportunity.application.service;

import com.marketplace.opportunity.application.dto.BidResponse;
import com.marketplace.opportunity.application.dto.CreateBidRequest;
import com.marketplace.opportunity.application.dto.CreateMessageRequest;
import com.marketplace.opportunity.application.dto.CreateOpportunityRequest;
import com.marketplace.opportunity.application.dto.NegotiationMessageResponse;
import com.marketplace.opportunity.application.dto.OpportunityResponse;
import com.marketplace.opportunity.domain.BidStatus;
import com.marketplace.opportunity.domain.OpportunityStatus;
import com.marketplace.opportunity.domain.model.Bid;
import com.marketplace.opportunity.domain.model.NegotiationMessage;
import com.marketplace.opportunity.domain.model.Opportunity;
import com.marketplace.opportunity.domain.repository.BidRepository;
import com.marketplace.opportunity.domain.repository.NegotiationMessageRepository;
import com.marketplace.opportunity.domain.repository.OpportunityRepository;
import com.marketplace.opportunity.domain.specification.OpportunitySpecifications;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final BidRepository bidRepository;
    private final NegotiationMessageRepository negotiationMessageRepository;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              BidRepository bidRepository,
                              NegotiationMessageRepository negotiationMessageRepository) {
        this.opportunityRepository = opportunityRepository;
        this.bidRepository = bidRepository;
        this.negotiationMessageRepository = negotiationMessageRepository;
    }

    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request, String createdBy) {
        Opportunity opportunity = new Opportunity();
        opportunity.setTitle(request.getTitle());
        opportunity.setDescription(request.getDescription());
        opportunity.setCategory(request.getCategory());
        opportunity.setLocation(request.getLocation());
        opportunity.setBudgetMin(request.getBudgetMin());
        opportunity.setBudgetMax(request.getBudgetMax());
        opportunity.setCurrency(request.getCurrency());
        opportunity.setDeadline(request.getDeadline());
        opportunity.setStatus(OpportunityStatus.OPEN);
        opportunity.setCreatedBy(createdBy);
        Opportunity saved = opportunityRepository.save(opportunity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OpportunityResponse> search(String q, String category, String location, OpportunityStatus status, Pageable pageable) {
        Specification<Opportunity> spec = Specification.where(OpportunitySpecifications.hasStatus(status))
                .and(OpportunitySpecifications.hasCategory(category))
                .and(OpportunitySpecifications.hasLocation(location))
                .and(OpportunitySpecifications.search(q));
        return opportunityRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OpportunityResponse findById(UUID id) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        return toResponse(opp);
    }

    @Transactional
    public BidResponse createBid(UUID opportunityId, CreateBidRequest request, String proposerId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        if (opp.getStatus() == OpportunityStatus.CLOSED || opp.getStatus() == OpportunityStatus.CANCELLED) {
            throw new IllegalStateException("Opportunity is not open for bids");
        }
        Bid bid = new Bid();
        bid.setOpportunity(opp);
        bid.setProposerId(proposerId);
        bid.setAmount(request.getAmount());
        bid.setCurrency(request.getCurrency());
        bid.setLeadTimeDays(request.getLeadTimeDays());
        bid.setMessage(request.getMessage());
        bid.setStatus(BidStatus.PENDING);
        Bid saved = bidRepository.save(bid);
        opp.setStatus(OpportunityStatus.IN_NEGOTIATION);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> listBids(UUID opportunityId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        return bidRepository.findByOpportunity(opp).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BidResponse acceptBid(UUID opportunityId, UUID bidId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        if (!bid.getOpportunity().getId().equals(opportunityId)) {
            throw new IllegalArgumentException("Bid does not belong to the opportunity");
        }
        bid.setStatus(BidStatus.ACCEPTED);
        bid.setDecisionAt(OffsetDateTime.now());
        opp.setStatus(OpportunityStatus.AWARDED);
        return toResponse(bid);
    }

    @Transactional
    public BidResponse rejectBid(UUID opportunityId, UUID bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
        if (!bid.getOpportunity().getId().equals(opportunityId)) {
            throw new IllegalArgumentException("Bid does not belong to the opportunity");
        }
        bid.setStatus(BidStatus.REJECTED);
        bid.setDecisionAt(OffsetDateTime.now());
        return toResponse(bid);
    }

    @Transactional
    public OpportunityResponse closeOpportunity(UUID opportunityId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        opp.setStatus(OpportunityStatus.CLOSED);
        return toResponse(opp);
    }

    @Transactional
    public NegotiationMessageResponse addMessage(UUID opportunityId, CreateMessageRequest request, String authorId, UUID bidId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        NegotiationMessage msg = new NegotiationMessage();
        msg.setOpportunity(opp);
        msg.setAuthorId(authorId);
        msg.setContent(request.getContent());
        if (bidId != null) {
            Bid bid = bidRepository.findById(bidId)
                    .orElseThrow(() -> new EntityNotFoundException("Bid not found"));
            msg.setBid(bid);
        }
        NegotiationMessage saved = negotiationMessageRepository.save(msg);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NegotiationMessageResponse> listMessages(UUID opportunityId) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new EntityNotFoundException("Opportunity not found"));
        return negotiationMessageRepository.findByOpportunityOrderByCreatedAtAsc(opp)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OpportunityResponse toResponse(Opportunity opp) {
        OpportunityResponse r = new OpportunityResponse();
        r.setId(opp.getId());
        r.setTitle(opp.getTitle());
        r.setDescription(opp.getDescription());
        r.setCategory(opp.getCategory());
        r.setLocation(opp.getLocation());
        r.setBudgetMin(opp.getBudgetMin());
        r.setBudgetMax(opp.getBudgetMax());
        r.setCurrency(opp.getCurrency());
        r.setDeadline(opp.getDeadline());
        r.setStatus(opp.getStatus());
        r.setCreatedBy(opp.getCreatedBy());
        r.setCreatedAt(opp.getCreatedAt());
        r.setUpdatedAt(opp.getUpdatedAt());
        return r;
    }

    private BidResponse toResponse(Bid bid) {
        BidResponse r = new BidResponse();
        r.setId(bid.getId());
        r.setOpportunityId(bid.getOpportunity().getId());
        r.setProposerId(bid.getProposerId());
        r.setAmount(bid.getAmount());
        r.setCurrency(bid.getCurrency());
        r.setLeadTimeDays(bid.getLeadTimeDays());
        r.setMessage(bid.getMessage());
        r.setStatus(bid.getStatus());
        r.setCreatedAt(bid.getCreatedAt());
        r.setDecisionAt(bid.getDecisionAt());
        return r;
    }

    private NegotiationMessageResponse toResponse(NegotiationMessage msg) {
        NegotiationMessageResponse r = new NegotiationMessageResponse();
        r.setId(msg.getId());
        r.setOpportunityId(msg.getOpportunity().getId());
        r.setBidId(msg.getBid() != null ? msg.getBid().getId() : null);
        r.setAuthorId(msg.getAuthorId());
        r.setContent(msg.getContent());
        r.setCreatedAt(msg.getCreatedAt());
        return r;
    }
}
