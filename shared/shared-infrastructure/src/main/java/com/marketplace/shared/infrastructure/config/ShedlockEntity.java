package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Placeholder entity mapping for ShedLock to allow Hibernate ddl-auto
 * to create the 'shedlock' table in environments without Flyway.
 */
@Entity
@Table(name = "shedlock")
public class ShedlockEntity {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private LocalDateTime lockUntil;

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", length = 255, nullable = false)
    private String lockedBy;

    // Getters and setters omitted for brevity; this entity is only meant
    // for schema generation via Hibernate ddl-auto.
}
