package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Placeholder entity to ensure Hibernate ddl-auto generates the shedlock table
 * for integration tests and local environments relying on auto-DDL.
 * See https://github.com/lukas-krecan/ShedLock#jdbctemplate
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

    // Default constructor required by JPA
    public ShedlockEntity() {
    }
}
