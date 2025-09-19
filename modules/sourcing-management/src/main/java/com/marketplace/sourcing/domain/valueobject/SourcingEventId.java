package com.marketplace.sourcing.domain.valueobject;

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
    private UUID value;

    /**
     * Generates a new unique SourcingEventId
     */
    public static SourcingEventId generate() {
        return new SourcingEventId(UUID.randomUUID());
    }

    /**
     * Creates a SourcingEventId from an existing UUID
     */
    public static SourcingEventId of(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return new SourcingEventId(uuid);
    }

    /**
     * Creates a SourcingEventId from a string representation
     */
    public static SourcingEventId of(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("UUID string cannot be null or empty");
        }
        
        try {
            return new SourcingEventId(UUID.fromString(uuidString.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }

    /**
     * Returns the string representation of the UUID
     */
    public String asString() {
        return value.toString();
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
        return value.toString();
    }
}

