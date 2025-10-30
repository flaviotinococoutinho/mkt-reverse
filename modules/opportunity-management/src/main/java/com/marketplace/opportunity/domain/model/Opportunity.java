package com.marketplace.opportunity.domain.model;

import com.marketplace.opportunity.domain.exception.InvalidOpportunityStateException;
import com.marketplace.opportunity.domain.valueobject.Money;
import com.marketplace.opportunity.domain.valueobject.OpportunityId;
import com.marketplace.opportunity.domain.valueobject.OpportunityStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Opportunity Aggregate Root.
 * Represents a consumer's demand in the reverse marketplace.
 * 
 * Follows DDD principles:
 * - Aggregate Root with strong consistency boundary
 * - Encapsulates business rules
 * - Emits domain events
 * - Immutable operations return new instances
 * 
 * Follows Object Calisthenics:
 * - One level of indentation
 * - No ELSE keyword
 * - Wrap primitives in Value Objects
 * - Small entities
 */
public final class Opportunity {
    
    private final OpportunityId id;
    private final Long consumerId;
    private final Long tenantId;
    private final String title;
    private final String description;
    private final String category;
    private final Money budget;
    private final Instant deadline;
    private final OpportunityStatus status;
    private final List<String> attachments;
    private final OpportunitySpecification specification;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<Object> domainEvents;
    
    private Opportunity(Builder builder) {
        this.id = builder.id;
        this.consumerId = validateConsumerId(builder.consumerId);
        this.tenantId = builder.tenantId;
        this.title = validateTitle(builder.title);
        this.description = validateDescription(builder.description);
        this.category = validateCategory(builder.category);
        this.budget = validateBudget(builder.budget);
        this.deadline = validateDeadline(builder.deadline);
        this.status = validateStatus(builder.status);
        this.attachments = Collections.unmodifiableList(new ArrayList<>(builder.attachments));
        this.specification = builder.specification;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : Instant.now();
        this.domainEvents = new ArrayList<>(builder.domainEvents);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Publishes this opportunity making it visible to companies.
     * 
     * @return new Opportunity with PUBLISHED status
     * @throws InvalidOpportunityStateException if cannot publish
     */
    public Opportunity publish() {
        validateCanPublish();
        
        Opportunity published = builder()
            .from(this)
            .status(OpportunityStatus.PUBLISHED)
            .updatedAt(Instant.now())
            .build();
        
        published.addDomainEvent(new OpportunityPublishedEvent(id, consumerId, title, category));
        
        return published;
    }
    
    /**
     * Moves opportunity to under review status.
     * 
     * @return new Opportunity with UNDER_REVIEW status
     * @throws InvalidOpportunityStateException if cannot review
     */
    public Opportunity moveToReview() {
        validateCanMoveToReview();
        
        return builder()
            .from(this)
            .status(OpportunityStatus.UNDER_REVIEW)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Accepts a proposal for this opportunity.
     * 
     * @param proposalId accepted proposal ID
     * @return new Opportunity with ACCEPTED status
     * @throws InvalidOpportunityStateException if cannot accept
     */
    public Opportunity acceptProposal(Long proposalId) {
        validateCanAcceptProposal();
        
        Opportunity accepted = builder()
            .from(this)
            .status(OpportunityStatus.ACCEPTED)
            .updatedAt(Instant.now())
            .build();
        
        accepted.addDomainEvent(new ProposalAcceptedEvent(id, proposalId, consumerId));
        
        return accepted;
    }
    
    /**
     * Closes this opportunity successfully.
     * 
     * @return new Opportunity with CLOSED status
     * @throws InvalidOpportunityStateException if cannot close
     */
    public Opportunity close() {
        validateCanClose();
        
        Opportunity closed = builder()
            .from(this)
            .status(OpportunityStatus.CLOSED)
            .updatedAt(Instant.now())
            .build();
        
        closed.addDomainEvent(new OpportunityClosedEvent(id, consumerId));
        
        return closed;
    }
    
    /**
     * Cancels this opportunity.
     * 
     * @return new Opportunity with CANCELLED status
     * @throws InvalidOpportunityStateException if cannot cancel
     */
    public Opportunity cancel() {
        validateCanCancel();
        
        Opportunity cancelled = builder()
            .from(this)
            .status(OpportunityStatus.CANCELLED)
            .updatedAt(Instant.now())
            .build();
        
        cancelled.addDomainEvent(new OpportunityCancelledEvent(id, consumerId));
        
        return cancelled;
    }
    
    /**
     * Marks opportunity as expired.
     * 
     * @return new Opportunity with EXPIRED status
     */
    public Opportunity expire() {
        return builder()
            .from(this)
            .status(OpportunityStatus.EXPIRED)
            .updatedAt(Instant.now())
            .build();
    }
    
    /**
     * Checks if opportunity has expired based on deadline.
     * 
     * @return true if expired
     */
    public boolean hasExpired() {
        return Instant.now().isAfter(deadline);
    }
    
    /**
     * Checks if opportunity belongs to consumer.
     * 
     * @param consumerId consumer ID to check
     * @return true if belongs to consumer
     */
    public boolean belongsToConsumer(Long consumerId) {
        return Objects.equals(this.consumerId, consumerId);
    }
    
    public OpportunityId id() {
        return id;
    }
    
    public Long consumerId() {
        return consumerId;
    }
    
    public Long tenantId() {
        return tenantId;
    }
    
    public String title() {
        return title;
    }
    
    public String description() {
        return description;
    }
    
    public String category() {
        return category;
    }
    
    public Money budget() {
        return budget;
    }
    
    public Instant deadline() {
        return deadline;
    }
    
    public OpportunityStatus status() {
        return status;
    }
    
    public List<String> attachments() {
        return attachments;
    }
    
    public OpportunitySpecification specification() {
        return specification;
    }
    
    public Instant createdAt() {
        return createdAt;
    }
    
    public Instant updatedAt() {
        return updatedAt;
    }
    
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }
    
    private void validateCanPublish() {
        if (!status.canTransitionTo(OpportunityStatus.PUBLISHED)) {
            throw new InvalidOpportunityStateException(
                String.format("Cannot publish opportunity in status: %s", status)
            );
        }
    }
    
    private void validateCanMoveToReview() {
        if (!status.canTransitionTo(OpportunityStatus.UNDER_REVIEW)) {
            throw new InvalidOpportunityStateException(
                String.format("Cannot move to review opportunity in status: %s", status)
            );
        }
    }
    
    private void validateCanAcceptProposal() {
        if (!status.canTransitionTo(OpportunityStatus.ACCEPTED)) {
            throw new InvalidOpportunityStateException(
                String.format("Cannot accept proposal for opportunity in status: %s", status)
            );
        }
    }
    
    private void validateCanClose() {
        if (!status.canTransitionTo(OpportunityStatus.CLOSED)) {
            throw new InvalidOpportunityStateException(
                String.format("Cannot close opportunity in status: %s", status)
            );
        }
    }
    
    private void validateCanCancel() {
        if (!status.canTransitionTo(OpportunityStatus.CANCELLED)) {
            throw new InvalidOpportunityStateException(
                String.format("Cannot cancel opportunity in status: %s", status)
            );
        }
    }
    
    private Long validateConsumerId(Long consumerId) {
        if (consumerId == null) {
            throw new IllegalArgumentException("Consumer ID cannot be null");
        }
        return consumerId;
    }
    
    private String validateTitle(String title) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        
        if (isTooShort(title, 10)) {
            throw new IllegalArgumentException("Title must be at least 10 characters");
        }
        
        if (isTooLong(title, 200)) {
            throw new IllegalArgumentException("Title must not exceed 200 characters");
        }
        
        return title;
    }
    
    private String validateDescription(String description) {
        if (isBlank(description)) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        
        if (isTooShort(description, 50)) {
            throw new IllegalArgumentException("Description must be at least 50 characters");
        }
        
        return description;
    }
    
    private String validateCategory(String category) {
        if (isBlank(category)) {
            throw new IllegalArgumentException("Category cannot be blank");
        }
        return category;
    }
    
    private Money validateBudget(Money budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Budget cannot be null");
        }
        
        if (budget.isNegative()) {
            throw new IllegalArgumentException("Budget must be positive");
        }
        
        return budget;
    }
    
    private Instant validateDeadline(Instant deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline cannot be null");
        }
        
        if (isInPast(deadline)) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }
        
        return deadline;
    }
    
    private OpportunityStatus validateStatus(OpportunityStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return status;
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private boolean isTooShort(String value, int minLength) {
        return value.length() < minLength;
    }
    
    private boolean isTooLong(String value, int maxLength) {
        return value.length() > maxLength;
    }
    
    private boolean isInPast(Instant instant) {
        return instant.isBefore(Instant.now());
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (isNotOpportunity(other)) {
            return false;
        }
        
        Opportunity that = (Opportunity) other;
        return Objects.equals(id, that.id);
    }
    
    private boolean isNotOpportunity(Object other) {
        return other == null || getClass() != other.getClass();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format(
            "Opportunity{id=%s, title=%s, status=%s}",
            id, title, status
        );
    }
    
    /**
     * Builder for Opportunity.
     */
    public static final class Builder {
        private OpportunityId id;
        private Long consumerId;
        private Long tenantId;
        private String title;
        private String description;
        private String category;
        private Money budget;
        private Instant deadline;
        private OpportunityStatus status = OpportunityStatus.DRAFT;
        private List<String> attachments = new ArrayList<>();
        private OpportunitySpecification specification;
        private Instant createdAt;
        private Instant updatedAt;
        private List<Object> domainEvents = new ArrayList<>();
        
        private Builder() {
        }
        
        public Builder id(OpportunityId id) {
            this.id = id;
            return this;
        }
        
        public Builder consumerId(Long consumerId) {
            this.consumerId = consumerId;
            return this;
        }
        
        public Builder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder budget(Money budget) {
            this.budget = budget;
            return this;
        }
        
        public Builder deadline(Instant deadline) {
            this.deadline = deadline;
            return this;
        }
        
        public Builder status(OpportunityStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder addAttachment(String attachment) {
            this.attachments.add(attachment);
            return this;
        }
        
        public Builder attachments(List<String> attachments) {
            this.attachments = new ArrayList<>(attachments);
            return this;
        }
        
        public Builder specification(OpportunitySpecification specification) {
            this.specification = specification;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder from(Opportunity opportunity) {
            this.id = opportunity.id;
            this.consumerId = opportunity.consumerId;
            this.tenantId = opportunity.tenantId;
            this.title = opportunity.title;
            this.description = opportunity.description;
            this.category = opportunity.category;
            this.budget = opportunity.budget;
            this.deadline = opportunity.deadline;
            this.status = opportunity.status;
            this.attachments = new ArrayList<>(opportunity.attachments);
            this.specification = opportunity.specification;
            this.createdAt = opportunity.createdAt;
            this.updatedAt = opportunity.updatedAt;
            this.domainEvents = new ArrayList<>(opportunity.domainEvents);
            return this;
        }
        
        public Opportunity build() {
            return new Opportunity(this);
        }
    }
    
    // Domain Events (placeholder classes)
    public record OpportunityPublishedEvent(
        OpportunityId opportunityId,
        Long consumerId,
        String title,
        String category
    ) {}
    
    public record ProposalAcceptedEvent(
        OpportunityId opportunityId,
        Long proposalId,
        Long consumerId
    ) {}
    
    public record OpportunityClosedEvent(
        OpportunityId opportunityId,
        Long consumerId
    ) {}
    
    public record OpportunityCancelledEvent(
        OpportunityId opportunityId,
        Long consumerId
    ) {}
}
