package com.marketplace.sourcing.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplierResponseId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private Long value;

    public static SupplierResponseId generate() {
        // Deprecated path: prefer injecting IdGenerator in application layer.
        return new SupplierResponseId(System.nanoTime());
    }

    public static SupplierResponseId of(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        return new SupplierResponseId(id);
    }

    public static SupplierResponseId of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("id string cannot be null or empty");
        }
        try {
            return of(Long.parseLong(raw.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id format: " + raw, e);
        }
    }

    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierResponseId that = (SupplierResponseId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
