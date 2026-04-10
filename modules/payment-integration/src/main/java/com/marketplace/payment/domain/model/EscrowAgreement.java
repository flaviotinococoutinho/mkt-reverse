package com.marketplace.payment.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.marketplace.payment.domain.valueobject.EscrowAgreementId;
import com.marketplace.payment.domain.valueobject.EscrowStatus;
import com.marketplace.shared.domain.model.AggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Escrow Agreement representing the contract between consumer and merchant.
 */
@Entity
@Table(name = "ESCROW_AGREEMENTS", indexes = {
    @Index(name = "idx_escrow_consumer", columnList = "consumer_id"),
    @Index(name = "idx_escrow_merchant", columnList = "merchant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EscrowAgreement extends AggregateRoot<EscrowAgreementId> {

    @EmbeddedId
    private EscrowAgreementId id;

    @Column(name = "consumer_id", nullable = false, length = 36)
    private String consumerId;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "terms", nullable = false, length = 1000)
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EscrowStatus status;

    private EscrowAgreement(EscrowAgreementId id, String consumerId, String merchantId, BigDecimal amount, String terms) {
        this.id = id;
        this.consumerId = consumerId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.terms = terms;
        this.status = EscrowStatus.CREATED;
    }

    public static EscrowAgreement create(String consumerId, String merchantId, BigDecimal amount, String terms) {
        Objects.requireNonNull(consumerId, "consumerId is required");
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(amount, "amount is required");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        Objects.requireNonNull(terms, "terms are required");
        
        EscrowAgreement agreement = new EscrowAgreement(EscrowAgreementId.generate(), consumerId, merchantId, amount, terms);
        agreement.markAsCreated();
        return agreement;
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("EscrowAgreement id cannot be null");
        }
        if (consumerId == null || consumerId.trim().isEmpty()) {
            throw new IllegalStateException("consumerId cannot be null or empty");
        }
        if (merchantId == null || merchantId.trim().isEmpty()) {
            throw new IllegalStateException("merchantId cannot be null or empty");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("amount must be greater than zero");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
    }
    
    public void fund() {
        if (this.status != EscrowStatus.CREATED) {
            throw new IllegalStateException("Can only fund created agreements");
        }
        this.status = EscrowStatus.FUNDED;
        markAsUpdated();
    }
}
