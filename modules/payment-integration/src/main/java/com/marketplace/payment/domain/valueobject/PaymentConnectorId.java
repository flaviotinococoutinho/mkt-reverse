package com.marketplace.payment.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identifier for payment connector aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentConnectorId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static PaymentConnectorId generate() {
        return new PaymentConnectorId(UUID.randomUUID());
    }

    public static PaymentConnectorId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PaymentConnectorId value cannot be null");
        }
        return new PaymentConnectorId(value);
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentConnectorId that = (PaymentConnectorId) o;
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
