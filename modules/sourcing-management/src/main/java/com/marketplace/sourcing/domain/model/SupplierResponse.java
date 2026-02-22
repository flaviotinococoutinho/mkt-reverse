package com.marketplace.sourcing.domain.model;

import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.valueobject.CurrencyCode;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.sourcing.domain.valueobject.OfferCondition;
import com.marketplace.sourcing.domain.valueobject.ShippingMode;
import com.marketplace.sourcing.domain.valueobject.SpecAttribute;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Minimal supplier response/proposal (MVP) for a sourcing event.
 * In the "reverse marketplace" this is the seller's offer.
 */
@Entity
@Table(name = "SRC_SUPPLIER_RESPONSES", indexes = {
    @Index(name = "idx_src_resp_event", columnList = "event_id"),
    @Index(name = "idx_src_resp_supplier", columnList = "supplier_id"),
    @Index(name = "idx_src_resp_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierResponse extends AggregateRoot<SupplierResponseId> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @EmbeddedId
    private SupplierResponseId id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "event_id", nullable = false))
    private SourcingEventId eventId;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupplierResponseStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "offer_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "offer_currency", length = 3))
    })
    private Money offerAmount;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", length = 20)
    private OfferCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_mode", length = 20)
    private ShippingMode shippingMode;

    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes; // JSON array of SpecAttribute

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    private SupplierResponse(
        SupplierResponseId id,
        SourcingEventId eventId,
        String supplierId,
        Money offerAmount,
        String message,
        Integer leadTimeDays,
        Integer warrantyMonths,
        OfferCondition condition,
        ShippingMode shippingMode,
        String attributes,
        Instant submittedAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.supplierId = supplierId;
        this.offerAmount = offerAmount;
        this.message = message;
        this.leadTimeDays = leadTimeDays;
        this.warrantyMonths = warrantyMonths;
        this.condition = condition != null ? condition : OfferCondition.UNKNOWN;
        this.shippingMode = shippingMode != null ? shippingMode : ShippingMode.UNKNOWN;
        this.attributes = attributes;
        this.submittedAt = submittedAt;
        this.status = SupplierResponseStatus.SUBMITTED;
        markAsCreated();
    }

    public static SupplierResponse submit(
        SupplierResponseId id,
        SourcingEventId eventId,
        String supplierId,
        Money offerAmount,
        String message,
        Integer leadTimeDays,
        Integer warrantyMonths,
        OfferCondition condition,
        ShippingMode shippingMode,
        List<SpecAttribute> attributes
    ) {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(eventId, "eventId is required");
        if (supplierId == null || supplierId.trim().isEmpty()) {
            throw new IllegalArgumentException("supplierId is required");
        }
        if (offerAmount == null) {
            offerAmount = Money.zero(CurrencyCode.BRL);
        }
        String attrsJson = null;
        if (attributes != null && !attributes.isEmpty()) {
            try {
                attrsJson = objectMapper.writeValueAsString(attributes);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid attributes", e);
            }
        }

        return new SupplierResponse(
            id,
            eventId,
            supplierId.trim(),
            offerAmount,
            message != null ? message.trim() : null,
            leadTimeDays,
            warrantyMonths,
            condition,
            shippingMode,
            attrsJson,
            Instant.now()
        );
    }

    public void accept(Instant reference) {
        if (status != SupplierResponseStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted responses can be accepted");
        }
        this.status = SupplierResponseStatus.ACCEPTED;
        this.acceptedAt = reference != null ? reference : Instant.now();
        markAsUpdated();
    }

    public void reject(Instant reference) {
        if (status != SupplierResponseStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted responses can be rejected");
        }
        this.status = SupplierResponseStatus.REJECTED;
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) throw new IllegalStateException("id is required");
        if (eventId == null) throw new IllegalStateException("eventId is required");
        if (supplierId == null || supplierId.isBlank()) throw new IllegalStateException("supplierId is required");
        if (status == null) throw new IllegalStateException("status is required");
    }

    public List<SpecAttribute> getAttributesList() {
        if (attributes == null || attributes.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(attributes, new TypeReference<List<SpecAttribute>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
