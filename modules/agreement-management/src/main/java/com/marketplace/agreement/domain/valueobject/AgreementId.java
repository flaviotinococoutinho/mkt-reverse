package com.marketplace.agreement.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Identifier of an Agreement aggregate (Snowflake id generated at the
 * application layer via IdGenerator).
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgreementId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private Long value;

    public static AgreementId of(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        return new AgreementId(id);
    }

    public static AgreementId of(String raw) {
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
        AgreementId that = (AgreementId) o;
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
