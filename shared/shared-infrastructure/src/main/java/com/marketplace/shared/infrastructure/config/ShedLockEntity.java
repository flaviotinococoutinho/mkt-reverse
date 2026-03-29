package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Placeholder entity mapping for ShedLock table generation.
 * This ensures Hibernate's ddl-auto creates the `shedlock` table automatically
 * for environments like integration tests where Flyway might be disabled.
 */
@Entity
@Table(name = "shedlock")
@Getter
@Setter
public class ShedLockEntity {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private Instant lockUntil;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    @Column(name = "locked_by", length = 255, nullable = false)
    private String lockedBy;

}
