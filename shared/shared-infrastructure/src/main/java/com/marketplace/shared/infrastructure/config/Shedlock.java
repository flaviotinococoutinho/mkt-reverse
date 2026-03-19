package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "shedlock")
public class Shedlock {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", columnDefinition = "TIMESTAMP")
    private LocalDateTime lockUntil;

    @Column(name = "locked_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", length = 255)
    private String lockedBy;

    // Getters and Setters can be omitted if not needed directly by the application layer.
    // They are usually required by JPA/Hibernate, so we provide default ones.

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getLockUntil() { return lockUntil; }
    public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
}
