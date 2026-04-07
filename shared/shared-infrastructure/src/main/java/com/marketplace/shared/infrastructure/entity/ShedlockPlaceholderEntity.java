package com.marketplace.shared.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Placeholder entity to force Hibernate ddl-auto to generate the 'shedlock' table
 * during integration tests across various downstream modules.
 */
@Entity
@Table(name = "shedlock")
public class ShedlockPlaceholderEntity {

    @Id
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "lock_until", nullable = false)
    private Instant lockUntil;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    @Column(name = "locked_by", length = 255, nullable = false)
    private String lockedBy;

    // Default constructor for JPA
    protected ShedlockPlaceholderEntity() {}
}
