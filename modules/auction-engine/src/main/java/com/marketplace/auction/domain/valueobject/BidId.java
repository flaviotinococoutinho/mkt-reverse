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
 * Identifier for bids inside auctions.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BidId implements Serializable {

    @Column(name = "bid_id", nullable = false, updatable = false)
    private UUID value;

    public static BidId generate() {
        return new BidId(UUID.randomUUID());
    }

    public static BidId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("BidId value cannot be null");
        }
        return new BidId(value);
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BidId bidId = (BidId) o;
        return Objects.equals(value, bidId.value);
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
