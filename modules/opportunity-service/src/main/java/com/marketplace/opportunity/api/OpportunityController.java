package com.marketplace.opportunity.api;

import com.marketplace.opportunity.application.dto.BidResponse;
import com.marketplace.opportunity.application.dto.CreateBidRequest;
import com.marketplace.opportunity.application.dto.CreateMessageRequest;
import com.marketplace.opportunity.application.dto.CreateOpportunityRequest;
import com.marketplace.opportunity.application.dto.NegotiationMessageResponse;
import com.marketplace.opportunity.application.dto.OpportunityResponse;
import com.marketplace.opportunity.application.service.OpportunityService;
import com.marketplace.opportunity.domain.OpportunityStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @PostMapping
    public ResponseEntity<OpportunityResponse> create(@RequestBody @Valid CreateOpportunityRequest request,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String createdBy = (userId == null || userId.isBlank()) ? "anonymous" : userId;
        return ResponseEntity.ok(opportunityService.createOpportunity(request, createdBy));
    }

    @GetMapping
    public ResponseEntity<Page<OpportunityResponse>> search(@RequestParam(value = "q", required = false) String q,
                                                             @RequestParam(value = "category", required = false) String category,
                                                             @RequestParam(value = "location", required = false) String location,
                                                             @RequestParam(value = "status", required = false) OpportunityStatus status,
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OpportunityResponse> result = opportunityService.search(q, category, location, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(opportunityService.findById(id));
    }

    @PostMapping("/{id}/bids")
    public ResponseEntity<BidResponse> createBid(@PathVariable("id") UUID opportunityId,
                                                 @RequestBody @Valid CreateBidRequest request,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String proposerId = (userId == null || userId.isBlank()) ? request.getProposerId() : userId;
        if (proposerId == null || proposerId.isBlank()) {
            throw new IllegalArgumentException("Proposer id is required (header X-User-Id or body proposerId)");
        }
        return ResponseEntity.ok(opportunityService.createBid(opportunityId, request, proposerId));
    }

    @GetMapping("/{id}/bids")
    public ResponseEntity<List<BidResponse>> listBids(@PathVariable("id") UUID opportunityId) {
        return ResponseEntity.ok(opportunityService.listBids(opportunityId));
    }

    @PostMapping("/{id}/bids/{bidId}/accept")
    public ResponseEntity<BidResponse> acceptBid(@PathVariable("id") UUID opportunityId,
                                                 @PathVariable UUID bidId) {
        return ResponseEntity.ok(opportunityService.acceptBid(opportunityId, bidId));
    }

    @PostMapping("/{id}/bids/{bidId}/reject")
    public ResponseEntity<BidResponse> rejectBid(@PathVariable("id") UUID opportunityId,
                                                 @PathVariable UUID bidId) {
        return ResponseEntity.ok(opportunityService.rejectBid(opportunityId, bidId));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<OpportunityResponse> close(@PathVariable("id") UUID opportunityId) {
        return ResponseEntity.ok(opportunityService.closeOpportunity(opportunityId));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<NegotiationMessageResponse> addMessage(@PathVariable("id") UUID opportunityId,
                                                                 @RequestParam(value = "bidId", required = false) UUID bidId,
                                                                 @RequestBody @Valid CreateMessageRequest request,
                                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String author = (userId == null || userId.isBlank()) ? request.getAuthorId() : userId;
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author id is required (header X-User-Id or body authorId)");
        }
        return ResponseEntity.ok(opportunityService.addMessage(opportunityId, request, author, bidId));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<NegotiationMessageResponse>> listMessages(@PathVariable("id") UUID opportunityId) {
        return ResponseEntity.ok(opportunityService.listMessages(opportunityId));
    }
}
