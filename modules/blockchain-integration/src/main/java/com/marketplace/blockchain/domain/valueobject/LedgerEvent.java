package com.marketplace.blockchain.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Ledger event emitted by blockchain smart contracts.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LedgerEvent implements Serializable {

    @Column(name = "event_name", nullable = false, length = 120)
    private String eventName;

    @Column(name = "event_payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "event_block", nullable = false)
    private Long blockNumber;

    @Column(name = "event_emitted_at", nullable = false)
    private Instant emittedAt;

    public static LedgerEvent of(String eventName, String payload, Long blockNumber, Instant emittedAt) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be blank");
        }
        if (blockNumber == null || blockNumber < 0) {
            throw new IllegalArgumentException("Block number must be positive");
        }
        return new LedgerEvent(eventName.trim(), payload, blockNumber, emittedAt != null ? emittedAt : Instant.now());
    }
}
