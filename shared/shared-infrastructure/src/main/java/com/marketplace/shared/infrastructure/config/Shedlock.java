package com.marketplace.shared.infrastructure.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shedlock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shedlock {

    @Id
    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by")
    private String lockedBy;
}
