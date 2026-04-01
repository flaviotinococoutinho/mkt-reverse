package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Placeholder entity to ensure Hibernate ddl-auto creates the 'shedlock' table
 * for modules/tests using in-memory databases or auto-schema generation.
 * This table is used exclusively by net.javacrumbs.shedlock and is not queried via JPA.
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

    // Protected constructor for JPA
    protected ShedlockEntity() {}

    // Getters and Setters are not strictly needed for ddl-auto,
    // but provided for completeness in case of accidental usage.
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
}
