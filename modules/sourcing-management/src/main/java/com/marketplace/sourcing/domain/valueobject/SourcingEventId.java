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

/**
 * Sourcing Event ID Value Object
 * 
 * Represents a unique identifier for a SourcingEvent aggregate.
 * Implements value object pattern with immutability and equality based on value.
 * 
 * Design principles:
 * - Immutable
 * - Value-based equality
 * - Type safety (prevents mixing with other IDs)
 * - Self-validating
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SourcingEventId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private Long value;

    /**
     * Generates a new unique SourcingEventId
     */
    public static SourcingEventId generate() {
        // Deprecated path: prefer injecting IdGenerator in application layer.
        return new SourcingEventId(System.nanoTime());
    }

    /**
     * Creates a SourcingEventId from an existing UUID
     */
    public static SourcingEventId of(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        return new SourcingEventId(id);
    }

    /**
     * Creates a SourcingEventId from a string representation
     */
    public static SourcingEventId of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("id string cannot be null or empty");
        }
        try {
            return of(Long.parseLong(raw.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id format: " + raw, e);
        }
    }

    /**
     * Returns the string representation of the UUID
     */
    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourcingEventId that = (SourcingEventId) o;
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
