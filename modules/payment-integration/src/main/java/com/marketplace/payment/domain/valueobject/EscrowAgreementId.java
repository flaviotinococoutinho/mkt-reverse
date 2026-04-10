package com.marketplace.payment.domain.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Identifier for an EscrowAgreement aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EscrowAgreementId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static EscrowAgreementId generate() {
        return new EscrowAgreementId(UUID.randomUUID());
    }

    public static EscrowAgreementId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("EscrowAgreementId value cannot be null");
        }
        return new EscrowAgreementId(value);
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EscrowAgreementId that = (EscrowAgreementId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
