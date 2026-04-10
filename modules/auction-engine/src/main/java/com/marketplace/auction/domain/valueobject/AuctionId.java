package com.marketplace.auction.domain.valueobject;

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
 * Identifier for auction aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionId implements Serializable {

    @Column(name = "id", nullable = false, updatable = false)
    private UUID value;

    public static AuctionId generate() {
        return new AuctionId(UUID.randomUUID());
    }

    public static AuctionId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AuctionId value cannot be null");
        }
        return new AuctionId(value);
    }

    public static AuctionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("AuctionId value cannot be blank");
        }
        return new AuctionId(UUID.fromString(value.trim()));
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionId auctionId = (AuctionId) o;
        return Objects.equals(value, auctionId.value);
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
