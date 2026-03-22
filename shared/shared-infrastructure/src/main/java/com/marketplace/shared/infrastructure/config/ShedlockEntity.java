package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Placeholder entity to support ShedLock table creation via Hibernate's ddl-auto: update/create.
 * ShedLock primarily interacts with this table via JdbcTemplate, but this entity ensures
 * the table exists in environments where Flyway is disabled and Hibernate manages the schema
 * (like integration tests).
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

    protected ShedlockEntity() {
        // JPA requires default constructor
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }
}
