package com.marketplace.sourcing.domain.model;

import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.event.SourcingEventCreatedEvent;
import com.marketplace.sourcing.domain.event.SourcingEventStatusChangedEvent;
import com.marketplace.sourcing.domain.event.SourcingEventUpdatedEvent;
import com.marketplace.sourcing.domain.valueobject.BuyerContext;
import com.marketplace.sourcing.domain.valueobject.ProductSpecification;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SourcingEventSettings;
import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import com.marketplace.sourcing.domain.valueobject.SourcingEventTimeline;
import com.marketplace.sourcing.domain.valueobject.SourcingEventType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root representing a sourcing event (RFQ, RFP, auction, negotiation).
 * Encapsulates lifecycle management, participant invitations and awarding.
 */
@Entity
@Table(name = "SRC_SOURCING_EVENTS", indexes = {
    @Index(name = "idx_src_event_tenant", columnList = "tenant_id"),
    @Index(name = "idx_src_event_status", columnList = "status"),
    @Index(name = "idx_src_event_type", columnList = "event_type"),
    @Index(name = "idx_src_event_published", columnList = "published_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourcingEvent extends AggregateRoot<SourcingEventId> {

    @EmbeddedId
    private SourcingEventId id;

    @Embedded
    private BuyerContext buyerContext;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private SourcingEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SourcingEventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private SourcingEventStatus previousStatus;

    @Embedded
    private ProductSpecification productSpecification;

    @Embedded
    private SourcingEventTimeline timeline;

    @Embedded
    private SourcingEventSettings settings;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "estimated_budget_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "estimated_budget_currency", length = 3))
    })
    private Money estimatedBudget;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SRC_EVENT_INVITED_SUPPLIERS", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "supplier_id", nullable = false, length = 36)
    private Set<String> invitedSupplierIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "SRC_EVENT_SHORTLIST", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "supplier_id", nullable = false, length = 36)
    private Set<String> shortlistedSupplierIds = new HashSet<>();

    @Column(name = "responses_count", nullable = false)
    private int responsesCount;

    @Column(name = "questions_count", nullable = false)
    private int questionsCount;

    @Column(name = "attachments_count", nullable = false)
    private int attachmentsCount;

    @Column(name = "awarded_supplier_id", length = 36)
    private String awardedSupplierId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "awarded_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "awarded_currency", length = 3))
    })
    private Money awardedAmount;

    @Column(name = "awarded_at")
    private Instant awardedAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    private SourcingEvent(
        SourcingEventId id,
        BuyerContext buyerContext,
        String title,
        String description,
        SourcingEventType eventType,
        SourcingEventStatus status,
        ProductSpecification productSpecification,
        SourcingEventTimeline timeline,
        SourcingEventSettings settings,
        Money estimatedBudget,
        Set<String> invitedSupplierIds
    ) {
        this.id = id;
        this.buyerContext = buyerContext;
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.status = status;
        this.productSpecification = productSpecification;
        this.timeline = timeline;
        this.settings = settings;
        this.estimatedBudget = estimatedBudget;
        if (invitedSupplierIds != null) {
            this.invitedSupplierIds.addAll(invitedSupplierIds);
        }
    }

    public static SourcingEvent create(
        SourcingEventId id,
        BuyerContext buyerContext,
        String title,
        String description,
        SourcingEventType eventType,
        ProductSpecification productSpecification,
        SourcingEventTimeline timeline,
        SourcingEventSettings settings,
        Money estimatedBudget,
        Set<String> invitedSupplierIds
    ) {
        Objects.requireNonNull(buyerContext, "buyerContext is required");
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(productSpecification, "productSpecification is required");
        Objects.requireNonNull(timeline, "timeline is required");

        Objects.requireNonNull(id, "id is required");

        SourcingEvent event = new SourcingEvent(
            id,
            buyerContext,
            title.trim(),
            description != null ? description.trim() : null,
            eventType,
            SourcingEventStatus.DRAFT,
            productSpecification,
            timeline,
            settings != null ? settings : SourcingEventSettings.defaults(),
            estimatedBudget != null ? estimatedBudget : Money.zero(CurrencyCode.BRL),
            invitedSupplierIds
        );

        event.markAsCreated();
        event.addDomainEvent(new SourcingEventCreatedEvent(
            event.getId().asString(),
            event.getTitle(),
            event.getEventType(),
            event.getStatus(),
            event.getBuyerContext().getOrganizationId(),
            event.getTimeline().getSubmissionDeadline()
        ));
        return event;
    }

    public void publish(Instant reference) {
        ensureStatus(SourcingEventStatus.DRAFT);
        this.status = SourcingEventStatus.PUBLISHED;
        markStatusChange(reference);
    }

    public void start(Instant reference) {
        ensureStatus(SourcingEventStatus.PUBLISHED);
        this.status = SourcingEventStatus.IN_PROGRESS;
        markStatusChange(reference);
    }

    public void closeSubmissions(Instant reference) {
        if (!status.acceptsResponses()) {
            throw new IllegalStateException("Event is not accepting responses");
        }
        this.status = SourcingEventStatus.SUBMISSION_CLOSED;
        markStatusChange(reference);
    }

    public void beginEvaluation(Instant reference) {
        ensureStatus(SourcingEventStatus.SUBMISSION_CLOSED);
        this.status = SourcingEventStatus.UNDER_EVALUATION;
        markStatusChange(reference);
    }

    public void beginNegotiation(Instant reference) {
        ensureStatus(SourcingEventStatus.UNDER_EVALUATION);
        if (!timeline.hasNegotiationPhase()) {
            throw new IllegalStateException("Negotiation phase not configured for this event");
        }
        this.status = SourcingEventStatus.NEGOTIATION;
        markStatusChange(reference);
    }

    public void award(String supplierId, Money amount, Instant reference) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("supplierId cannot be null or empty");
        }
        if (amount != null && estimatedBudget != null && amount.isGreaterThan(estimatedBudget.multiply(2))) {
            throw new IllegalArgumentException("Award amount cannot be excessively higher than estimated budget");
        }
        if (!SourcingEventStatus.NEGOTIATION.equals(status) && !SourcingEventStatus.UNDER_EVALUATION.equals(status)) {
            throw new IllegalStateException("Event must be under evaluation or negotiation to award");
        }
        this.status = SourcingEventStatus.AWARDED;
        this.awardedSupplierId = supplierId.trim();
        this.awardedAmount = amount;
        this.awardedAt = reference != null ? reference : Instant.now();
        markStatusChange(reference);
    }

    public void cancel(String reason, Instant reference) {
        if (status.isFinal()) {
            throw new IllegalStateException("Cannot cancel an event in final status");
        }
        this.status = SourcingEventStatus.CANCELLED;
        this.cancellationReason = reason != null ? reason.trim() : null;
        markStatusChange(reference);
    }

    public void expire(Instant reference) {
        if (!timeline.isExpired(reference)) {
            throw new IllegalStateException("Event timeline has not expired yet");
        }
        this.status = SourcingEventStatus.EXPIRED;
        markStatusChange(reference);
    }

    public void suspend(String reason, Instant reference) {
        if (!status.isActive()) {
            throw new IllegalStateException("Only active events can be suspended");
        }
        this.previousStatus = status;
        this.status = SourcingEventStatus.SUSPENDED;
        this.cancellationReason = reason != null ? reason.trim() : null;
        markStatusChange(reference);
    }

    public void resume(Instant reference) {
        if (!SourcingEventStatus.SUSPENDED.equals(status)) {
            throw new IllegalStateException("Event is not suspended");
        }
        this.status = previousStatus != null ? previousStatus : SourcingEventStatus.DRAFT;
        this.previousStatus = null;
        markStatusChange(reference);
    }

    public void registerResponse() {
        if (!status.acceptsResponses()) {
            throw new IllegalStateException("Event is not accepting responses");
        }
        this.responsesCount++;
        if (settings.shouldAutoExtend(responsesCount) && timeline.canExtendSubmission(Duration.ofMinutes(settings.getAutoExtendMinutes()))) {
            this.timeline = timeline.extendSubmission(Duration.ofMinutes(settings.getAutoExtendMinutes()));
            addDomainEvent(SourcingEventUpdatedEvent.timelineExtended(id.asString(), status, timeline.getSubmissionDeadline()));
        }
    }

    public void registerQuestion() {
        this.questionsCount++;
    }

    public void incrementAttachments() {
        this.attachmentsCount++;
    }

    public void addInvitedSupplier(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("supplierId cannot be null or empty");
        }
        if (invitedSupplierIds.size() >= settings.getMaximumSuppliers()) {
            throw new IllegalStateException("Maximum number of invited suppliers reached");
        }
        invitedSupplierIds.add(supplierId.trim());
    }

    public void shortlistSupplier(String supplierId) {
        if (!invitedSupplierIds.contains(supplierId)) {
            throw new IllegalArgumentException("Supplier must be invited before being shortlisted");
        }
        shortlistedSupplierIds.add(supplierId);
    }

    public Set<String> getInvitedSupplierIds() {
        return Collections.unmodifiableSet(invitedSupplierIds);
    }

    public Set<String> getShortlistedSupplierIds() {
        return Collections.unmodifiableSet(shortlistedSupplierIds);
    }

    private void ensureStatus(SourcingEventStatus expected) {
        if (!expected.equals(status)) {
            throw new IllegalStateException("Event must be in status " + expected + " but was " + status);
        }
    }

    private void markStatusChange(Instant reference) {
        addDomainEvent(new SourcingEventStatusChangedEvent(
            id.asString(),
            status,
            previousStatus,
            reference != null ? reference : Instant.now()
        ));
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("SourcingEvent id cannot be null");
        }
        if (buyerContext == null) {
            throw new IllegalStateException("Buyer context is required");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalStateException("Title is required");
        }
        if (eventType == null) {
            throw new IllegalStateException("Event type is required");
        }
        if (status == null) {
            throw new IllegalStateException("Event status is required");
        }
        if (productSpecification == null) {
            throw new IllegalStateException("Product specification is required");
        }
        if (timeline == null) {
            throw new IllegalStateException("Timeline is required");
        }
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean acceptsResponses() {
        return status.acceptsResponses();
    }

    public boolean hasAwardedSupplier() {
        return awardedSupplierId != null;
    }

    public BuyerContext getBuyerContext() {
        return buyerContext;
    }

    public void updateDetails(String newTitle, String newDescription, ProductSpecification newSpecification) {
        if (!status.allowsModification()) {
            throw new IllegalStateException("Event does not allow modifications in current status");
        }
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            this.title = newTitle.trim();
        }
        if (newDescription != null) {
            this.description = newDescription.trim();
        }
        if (newSpecification != null) {
            this.productSpecification = newSpecification;
        }
        addDomainEvent(new SourcingEventUpdatedEvent(
            id.asString(),
            status,
            Instant.now(),
            SourcingEventUpdatedEvent.UpdateType.DETAILS
        ));
        markAsUpdated();
    }

    public void extendSubmissionWindow(Duration duration) {
        this.timeline = timeline.extendSubmission(duration);
        addDomainEvent(SourcingEventUpdatedEvent.timelineExtended(id.asString(), status, timeline.getSubmissionDeadline()));
        markAsUpdated();
    }

    public void validateBidder(String supplierOrganizationId) {
        if (buyerContext != null && buyerContext.getOrganizationId().equals(supplierOrganizationId)) {
            throw new IllegalArgumentException("Buyer organization cannot allow self-bidding");
        }
    }

    public void validateProposalAttributes(java.util.List<com.marketplace.sourcing.domain.valueobject.SpecAttribute> attributes) {
        Integer mccCode = productSpecification != null ? productSpecification.getMccCategoryCode() : null;
        if (mccCode == null) {
            if (attributes != null && !attributes.isEmpty()) {
                throw new IllegalArgumentException("Event has no mccCategoryCode; cannot accept proposal attributes");
            }
            return;
        }
        var category = com.marketplace.sourcing.domain.valueobject.MccCategory.requireFromCode(mccCode);
        com.marketplace.sourcing.domain.valueobject.CategoryAttributeSchema.validate(category, attributes);
    }
}
