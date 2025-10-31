package com.marketplace.proposal.domain.model;

import com.marketplace.proposal.domain.valueobject.*;
import com.marketplace.shared.domain.valueobject.Money;

import java.time.Instant;
import java.util.*;

/**
 * Proposal Aggregate Root.
 * 
 * Represents a company's proposal for a consumer's opportunity.
 * 
 * Business Rules:
 * - Proposal must be submitted before opportunity deadline
 * - Price must be positive
 * - Delivery time must be reasonable
 * - Only DRAFT proposals can be edited
 * - Status transitions must be valid
 * - Cannot withdraw after acceptance
 * 
 * Follows DDD principles:
 * - Aggregate Root (consistency boundary)
 * - Domain Events for state changes
 * - Invariants maintained
 * - Tell, Don't Ask
 */
public class Proposal {
    
    private final ProposalId proposalId;
    private final Long opportunityId;
    private final Long companyId;
    private final Long tenantId;
    
    private Money price;
    private DeliveryTime deliveryTime;
    private String description;
    private ProposalStatus status;
    
    private List<String> attachments;
    private Map<String, Object> specifications;
    
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant submittedAt;
    private Instant acceptedAt;
    
    private final List<Object> domainEvents;
    
    private Proposal(
        ProposalId proposalId,
        Long opportunityId,
        Long companyId,
        Long tenantId,
        Money price,
        DeliveryTime deliveryTime,
        String description
    ) {
        this.proposalId = Objects.requireNonNull(proposalId, "Proposal ID cannot be null");
        this.opportunityId = Objects.requireNonNull(opportunityId, "Opportunity ID cannot be null");
        this.companyId = Objects.requireNonNull(companyId, "Company ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.deliveryTime = Objects.requireNonNull(deliveryTime, "Delivery time cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        
        this.status = ProposalStatus.DRAFT;
        this.attachments = new ArrayList<>();
        this.specifications = new HashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.domainEvents = new ArrayList<>();
        
        validateDescription(description);
    }
    
    /**
     * Creates new Proposal in DRAFT status.
     * 
     * @param proposalId proposal identifier
     * @param opportunityId opportunity identifier
     * @param companyId company identifier
     * @param tenantId tenant identifier
     * @param price proposal price
     * @param deliveryTime delivery time estimation
     * @param description proposal description
     * @return new Proposal instance
     */
    public static Proposal create(
        ProposalId proposalId,
        Long opportunityId,
        Long companyId,
        Long tenantId,
        Money price,
        DeliveryTime deliveryTime,
        String description
    ) {
        return new Proposal(
            proposalId,
            opportunityId,
            companyId,
            tenantId,
            price,
            deliveryTime,
            description
        );
    }
    
    /**
     * Submits proposal for review.
     * 
     * @throws IllegalStateException if proposal cannot be submitted
     */
    public void submit() {
        ensureCanBeEdited();
        
        status.validateTransition(ProposalStatus.SUBMITTED);
        status = ProposalStatus.SUBMITTED;
        submittedAt = Instant.now();
        updatedAt = Instant.now();
        
        // Domain event would be added here
        // domainEvents.add(new ProposalSubmitted(proposalId, opportunityId, companyId));
    }
    
    /**
     * Updates proposal details.
     * 
     * @param newPrice new price
     * @param newDeliveryTime new delivery time
     * @param newDescription new description
     * @throws IllegalStateException if proposal cannot be edited
     */
    public void update(Money newPrice, DeliveryTime newDeliveryTime, String newDescription) {
        ensureCanBeEdited();
        
        Objects.requireNonNull(newPrice, "Price cannot be null");
        Objects.requireNonNull(newDeliveryTime, "Delivery time cannot be null");
        Objects.requireNonNull(newDescription, "Description cannot be null");
        
        validateDescription(newDescription);
        
        this.price = newPrice;
        this.deliveryTime = newDeliveryTime;
        this.description = newDescription;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Accepts proposal.
     * 
     * @throws IllegalStateException if proposal cannot be accepted
     */
    public void accept() {
        ensureIsActive();
        
        status.validateTransition(ProposalStatus.ACCEPTED);
        status = ProposalStatus.ACCEPTED;
        acceptedAt = Instant.now();
        updatedAt = Instant.now();
        
        // Domain event would be added here
        // domainEvents.add(new ProposalAccepted(proposalId, opportunityId, companyId));
    }
    
    /**
     * Rejects proposal.
     * 
     * @throws IllegalStateException if proposal cannot be rejected
     */
    public void reject() {
        ensureIsActive();
        
        status.validateTransition(ProposalStatus.REJECTED);
        status = ProposalStatus.REJECTED;
        updatedAt = Instant.now();
        
        // Domain event would be added here
        // domainEvents.add(new ProposalRejected(proposalId, opportunityId, companyId));
    }
    
    /**
     * Withdraws proposal.
     * 
     * @throws IllegalStateException if proposal cannot be withdrawn
     */
    public void withdraw() {
        if (status == ProposalStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot withdraw accepted proposal");
        }
        
        status.validateTransition(ProposalStatus.WITHDRAWN);
        status = ProposalStatus.WITHDRAWN;
        updatedAt = Instant.now();
        
        // Domain event would be added here
        // domainEvents.add(new ProposalWithdrawn(proposalId, opportunityId, companyId));
    }
    
    /**
     * Marks proposal as under review.
     */
    public void markAsUnderReview() {
        status.validateTransition(ProposalStatus.UNDER_REVIEW);
        status = ProposalStatus.UNDER_REVIEW;
        updatedAt = Instant.now();
    }
    
    /**
     * Marks proposal as negotiating.
     */
    public void markAsNegotiating() {
        status.validateTransition(ProposalStatus.NEGOTIATING);
        status = ProposalStatus.NEGOTIATING;
        updatedAt = Instant.now();
    }
    
    /**
     * Completes proposal.
     */
    public void complete() {
        if (status != ProposalStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted proposals can be completed");
        }
        
        status.validateTransition(ProposalStatus.COMPLETED);
        status = ProposalStatus.COMPLETED;
        updatedAt = Instant.now();
        
        // Domain event would be added here
        // domainEvents.add(new ProposalCompleted(proposalId, opportunityId, companyId));
    }
    
    /**
     * Adds attachment to proposal.
     * 
     * @param attachmentUrl attachment URL
     */
    public void addAttachment(String attachmentUrl) {
        ensureCanBeEdited();
        
        Objects.requireNonNull(attachmentUrl, "Attachment URL cannot be null");
        
        if (attachmentUrl.isBlank()) {
            throw new IllegalArgumentException("Attachment URL cannot be blank");
        }
        
        attachments.add(attachmentUrl);
        updatedAt = Instant.now();
    }
    
    /**
     * Adds specification to proposal.
     * 
     * @param key specification key
     * @param value specification value
     */
    public void addSpecification(String key, Object value) {
        ensureCanBeEdited();
        
        Objects.requireNonNull(key, "Specification key cannot be null");
        Objects.requireNonNull(value, "Specification value cannot be null");
        
        specifications.put(key, value);
        updatedAt = Instant.now();
    }
    
    /**
     * Checks if proposal is competitive compared to another.
     * 
     * @param other other proposal
     * @return true if this proposal is more competitive
     */
    public boolean isMoreCompetitiveThan(Proposal other) {
        // Lower price and faster delivery = more competitive
        boolean cheaperPrice = price.isLessThan(other.price);
        boolean fasterDelivery = deliveryTime.isFasterThan(other.deliveryTime);
        
        return cheaperPrice || (price.equals(other.price) && fasterDelivery);
    }
    
    private void ensureCanBeEdited() {
        if (!status.canBeEdited()) {
            throw new IllegalStateException(
                String.format("Proposal in status %s cannot be edited", status)
            );
        }
    }
    
    private void ensureIsActive() {
        if (!status.isActive()) {
            throw new IllegalStateException(
                String.format("Proposal in status %s is not active", status)
            );
        }
    }
    
    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        
        if (description.length() < 50) {
            throw new IllegalArgumentException("Description must be at least 50 characters");
        }
        
        if (description.length() > 5000) {
            throw new IllegalArgumentException("Description cannot exceed 5000 characters");
        }
    }
    
    // Getters
    
    public ProposalId getProposalId() {
        return proposalId;
    }
    
    public Long getOpportunityId() {
        return opportunityId;
    }
    
    public Long getCompanyId() {
        return companyId;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public DeliveryTime getDeliveryTime() {
        return deliveryTime;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ProposalStatus getStatus() {
        return status;
    }
    
    public List<String> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }
    
    public Map<String, Object> getSpecifications() {
        return Collections.unmodifiableMap(specifications);
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public Instant getSubmittedAt() {
        return submittedAt;
    }
    
    public Instant getAcceptedAt() {
        return acceptedAt;
    }
    
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
