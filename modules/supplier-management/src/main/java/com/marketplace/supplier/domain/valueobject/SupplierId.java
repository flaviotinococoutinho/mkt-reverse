package com.marketplace.supplier.domain.valueobject;

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
 * Value object representing the identifier of a supplier aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplierId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static SupplierId generate() {
        return new SupplierId(UUID.randomUUID());
    }

    public static SupplierId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("SupplierId value cannot be null");
        }
        return new SupplierId(value);
    }

    public static SupplierId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SupplierId value cannot be blank");
        }
        return new SupplierId(UUID.fromString(value.trim()));
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierId that = (SupplierId) o;
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
