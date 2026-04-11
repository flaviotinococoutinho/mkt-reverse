package com.marketplace.sourcing.adapter.output.persistence;

import com.marketplace.sourcing.domain.valueobject.SourcingEventStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for SourcingEvent with concurrency control.
 * Implements optimistic locking via @Version.
 */
@Entity
@Table(name = "src_sourcing_events", schema = "marketplace_main")
public class SourcingEventEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 5000)
    private String description;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description", length = 5000)
    private String productDescription;

    @Column(name = "mcc_category_code")
    private Integer mccCategoryCode;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure = "un";

    @Column(name = "quantity_required")
    private Integer quantityRequired = 1;

    @Column(name = "estimated_budget", precision = 19, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "currency")
    private String currency = "BRL";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SourcingEventStatus status = SourcingEventStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private String visibility = "PUBLIC";

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "submission_deadline")
    private Instant submissionDeadline;

    @Column(name = "valid_for_hours")
    private Integer validForHours;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "attributes_json", columnDefinition = "TEXT")
    private String attributesJson;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "status_changed_at")
    private Instant statusChangedAt;

    @Column(name = "status_changed_by")
    private String statusChangedBy;

    @OneToMany(mappedBy = "sourcingEvent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupplierResponseEntity> responses = new ArrayList<>();

    // Constructors
    public SourcingEventEntity() {}

    // Getters and Setters (following Object Calisthenics - no magic numbers)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SourcingEventStatus getStatus() { return status; }
    public void setStatus(SourcingEventStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public boolean acceptsResponses() {
        return status == SourcingEventStatus.PUBLISHED 
            || status == SourcingEventStatus.IN_PROGRESS;
    }

    public boolean isCancelled() {
        return status == SourcingEventStatus.CANCELLED;
    }
}