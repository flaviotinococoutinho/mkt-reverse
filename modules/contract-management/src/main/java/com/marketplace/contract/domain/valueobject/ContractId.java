package com.marketplace.contract.domain.valueobject;

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
 * Identifier for contract aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static ContractId generate() {
        return new ContractId(UUID.randomUUID());
    }

    public static ContractId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ContractId value cannot be null");
        }
        return new ContractId(value);
    }

    public static ContractId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ContractId value cannot be blank");
        }
        return new ContractId(UUID.fromString(value.trim()));
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractId that = (ContractId) o;
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
