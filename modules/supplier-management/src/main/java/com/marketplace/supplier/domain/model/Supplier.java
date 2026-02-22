package com.marketplace.supplier.domain.model;

import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.supplier.domain.event.SupplierComplianceStatusChangedEvent;
import com.marketplace.supplier.domain.event.SupplierRegisteredEvent;
import com.marketplace.supplier.domain.event.SupplierStatusChangedEvent;
import com.marketplace.supplier.domain.valueobject.Certification;
import com.marketplace.supplier.domain.valueobject.CertificationStatus;
import com.marketplace.supplier.domain.valueobject.ComplianceStatus;
import com.marketplace.supplier.domain.valueobject.SupplierId;
import com.marketplace.supplier.domain.valueobject.SupplierProfile;
import com.marketplace.supplier.domain.valueobject.SupplierRating;
import com.marketplace.supplier.domain.valueobject.SupplierStatus;
import com.marketplace.supplier.domain.valueobject.TaxIdentifier;
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

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Supplier aggregate covering onboarding, compliance and performance tracking.
 *
 * <p><strong>QueroJá note:</strong> although the legacy name is "Supplier", in the
 * reverse marketplace (buyer-first) this aggregate represents a <em>Seller/Merchant Profile</em>
 * scoped to a single tenant (= market/nicho).
 */
@Entity
@Table(name = "SUP_SUPPLIERS", indexes = {
    @Index(name = "idx_sup_supplier_tenant", columnList = "tenant_id"),
    @Index(name = "idx_sup_supplier_actor", columnList = "actor_id"),
    @Index(name = "idx_sup_supplier_status", columnList = "status"),
    @Index(name = "idx_sup_supplier_compliance", columnList = "compliance_status"),
    @Index(name = "idx_sup_supplier_tax_id", columnList = "tenant_id, tax_id_number", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier extends AggregateRoot<SupplierId> {

    @EmbeddedId
    private SupplierId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    /**
     * Link to the Actor/User identity owning this seller profile.
     *
     * <p>Important for multi-market: the same actor can have multiple seller profiles
     * across different tenants/markets.
     */
    @Column(name = "actor_id", nullable = false, length = 36)
    private String actorId;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Embedded
    private TaxIdentifier taxIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_nature", nullable = false, length = 20)
    private com.marketplace.supplier.domain.valueobject.SellerNature sellerNature;

    @Embedded
    private SupplierProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupplierStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 20)
    private ComplianceStatus complianceStatus;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "quality", column = @Column(name = "rating_quality")),
        @AttributeOverride(name = "delivery", column = @Column(name = "rating_delivery")),
        @AttributeOverride(name = "cost", column = @Column(name = "rating_cost")),
        @AttributeOverride(name = "compliance", column = @Column(name = "rating_compliance")),
        @AttributeOverride(name = "innovation", column = @Column(name = "rating_innovation"))
    })
    private SupplierRating rating;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "SUP_SUPPLIER_CATEGORIES", joinColumns = @JoinColumn(name = "supplier_id"))
    @Column(name = "category", nullable = false, length = 100)
    private Set<String> categories = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "SUP_SUPPLIER_CAPABILITIES", joinColumns = @JoinColumn(name = "supplier_id"))
    @Column(name = "capability", nullable = false, length = 120)
    private Set<String> capabilities = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "SUP_SUPPLIER_CERTIFICATIONS", joinColumns = @JoinColumn(name = "supplier_id"))
    private Set<Certification> certifications = new HashSet<>();

    @Column(name = "onboarding_started_at", nullable = false)
    private Instant onboardingStartedAt;

    @Column(name = "onboarding_completed_at")
    private Instant onboardingCompletedAt;

    @Column(name = "compliance_approved_at")
    private Instant complianceApprovedAt;

    @Column(name = "last_review_at")
    private Instant lastReviewAt;

    @Column(name = "blacklist_reason", length = 500)
    private String blacklistReason;

    private Supplier(
        SupplierId id,
        String tenantId,
        String actorId,
        String legalName,
        String tradeName,
        TaxIdentifier taxIdentifier,
        SupplierProfile profile,
        Set<String> categories
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.actorId = actorId;
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.taxIdentifier = taxIdentifier;
        this.sellerNature = com.marketplace.supplier.domain.valueobject.SellerNature.from(taxIdentifier);
        this.profile = profile;
        if (categories != null) {
            this.categories.addAll(categories);
        }
        this.status = SupplierStatus.ONBOARDING;
        this.complianceStatus = ComplianceStatus.NOT_STARTED;
        this.rating = SupplierRating.initial();
        this.onboardingStartedAt = Instant.now();
    }

    public static Supplier register(
        String tenantId,
        String actorId,
        String legalName,
        String tradeName,
        TaxIdentifier taxIdentifier,
        SupplierProfile profile,
        Set<String> categories
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(actorId, "actorId is required");
        Objects.requireNonNull(legalName, "legalName is required");
        Objects.requireNonNull(taxIdentifier, "taxIdentifier is required");

        if (actorId.trim().isEmpty()) {
            throw new IllegalArgumentException("actorId cannot be blank");
        }

        Supplier supplier = new Supplier(
            SupplierId.generate(),
            tenantId.trim(),
            actorId.trim(),
            legalName.trim(),
            tradeName != null ? tradeName.trim() : null,
            taxIdentifier,
            profile,
            categories
        );

        supplier.markAsCreated();
        supplier.addDomainEvent(new SupplierRegisteredEvent(
            supplier.getId().asString(),
            supplier.getLegalName(),
            supplier.getTenantId(),
            supplier.getActorId(),
            supplier.getTaxIdentifier().getNumber()
        ));
        return supplier;
    }

    /**
     * Computes seller limits for this supplier given a MarketPolicy snapshot.
     *
     * <p>This keeps policy enforcement out of low-level code paths while still
     * providing a single canonical calculation.
     */
    public com.marketplace.supplier.domain.valueobject.SellerLimits resolveLimits(
        com.marketplace.shared.tenant.MarketPolicySnapshot policy
    ) {
        Objects.requireNonNull(policy, "policy is required");
        return com.marketplace.supplier.domain.valueobject.SellerLimits.from(policy, sellerNature);
    }

    public void completeOnboarding() {
        ensureStatus(SupplierStatus.ONBOARDING);
        this.onboardingCompletedAt = Instant.now();
        transitionStatus(SupplierStatus.PENDING_REVIEW, null);
    }

    public void approveCompliance() {
        this.complianceStatus = ComplianceStatus.APPROVED;
        this.complianceApprovedAt = Instant.now();
        if (SupplierStatus.PENDING_REVIEW.equals(status) || SupplierStatus.ONBOARDING.equals(status)) {
            transitionStatus(SupplierStatus.ACTIVE, null);
        }
        addDomainEvent(new SupplierComplianceStatusChangedEvent(
            id.asString(),
            ComplianceStatus.APPROVED,
            Instant.now()
        ));
    }

    public void rejectCompliance(String reason) {
        this.complianceStatus = ComplianceStatus.REJECTED;
        addDomainEvent(new SupplierComplianceStatusChangedEvent(
            id.asString(),
            ComplianceStatus.REJECTED,
            Instant.now(),
            reason
        ));
        transitionStatus(SupplierStatus.SUSPENDED, reason);
    }

    public void updateComplianceStatus(ComplianceStatus status) {
        this.complianceStatus = status;
        addDomainEvent(new SupplierComplianceStatusChangedEvent(id.asString(), status, Instant.now()));
    }

    public void activate() {
        transitionStatus(SupplierStatus.ACTIVE, null);
    }

    public void suspend(String reason) {
        transitionStatus(SupplierStatus.SUSPENDED, reason);
    }

    public void blacklist(String reason) {
        transitionStatus(SupplierStatus.BLACKLISTED, reason);
        this.blacklistReason = reason;
    }

    public void archive() {
        transitionStatus(SupplierStatus.ARCHIVED, null);
    }

    public void reactivate() {
        if (status == SupplierStatus.BLACKLISTED) {
            throw new IllegalStateException("Blacklisted suppliers cannot be reactivated without governance approval");
        }
        transitionStatus(SupplierStatus.ACTIVE, null);
    }

    public void updateProfile(SupplierProfile profile) {
        this.profile = profile;
        markAsUpdated();
    }

    public void updateRating(SupplierRating newRating) {
        this.rating = newRating;
        this.lastReviewAt = Instant.now();
        markAsUpdated();
    }

    public void addCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("category cannot be blank");
        }
        categories.add(category.trim());
    }

    public void addCapability(String capability) {
        if (capability == null || capability.trim().isEmpty()) {
            throw new IllegalArgumentException("capability cannot be blank");
        }
        capabilities.add(capability.trim());
    }

    public void addCertification(Certification certification) {
        certifications.add(certification);
    }

    public void refreshCertificationStatus(Instant reference) {
        certifications = certifications.stream()
            .map(cert -> cert.isExpired(reference) ? cert.markRevoked() : cert)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public Set<String> getCapabilities() {
        return Collections.unmodifiableSet(capabilities);
    }

    public Set<Certification> getCertifications() {
        return Collections.unmodifiableSet(certifications);
    }

    private void transitionStatus(SupplierStatus target, String reason) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Cannot transition supplier from " + status + " to " + target);
        }
        SupplierStatus previousStatus = this.status;
        this.status = target;
        addDomainEvent(new SupplierStatusChangedEvent(
            id.asString(),
            previousStatus,
            target,
            reason
        ));
        markAsUpdated();
    }

    private void ensureStatus(SupplierStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Supplier must be in status " + expected + " but was " + status);
        }
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("Supplier id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (actorId == null || actorId.trim().isEmpty()) {
            throw new IllegalStateException("actorId cannot be null");
        }
        if (legalName == null || legalName.trim().isEmpty()) {
            throw new IllegalStateException("legalName cannot be null");
        }
        if (taxIdentifier == null) {
            throw new IllegalStateException("taxIdentifier cannot be null");
        }
        if (sellerNature == null) {
            throw new IllegalStateException("sellerNature cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
        if (complianceStatus == null) {
            throw new IllegalStateException("complianceStatus cannot be null");
        }
    }
}
