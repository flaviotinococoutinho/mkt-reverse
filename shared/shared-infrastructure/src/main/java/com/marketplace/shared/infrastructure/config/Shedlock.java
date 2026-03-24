package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "shedlock")
@Getter
@Setter
public class Shedlock {

    @Id
    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "lock_until")
    private Instant lockUntil;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "locked_by", length = 255)
    private String lockedBy;
}
