package com.marketplace.user.domain.valueobject;

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
 * User ID Value Object
 * 
 * Represents a unique identifier for a User aggregate.
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
public class UserId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    /**
     * Generates a new unique UserId
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Creates a UserId from an existing UUID
     */
    public static UserId of(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        return new UserId(uuid);
    }

    /**
     * Creates a UserId from a string representation
     */
    public static UserId of(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("UUID string cannot be null or empty");
        }
        
        try {
            return new UserId(UUID.fromString(uuidString.trim()));
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
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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

