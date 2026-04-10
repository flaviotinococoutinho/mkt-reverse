package com.marketplace.erp.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Schedule configuration for ERP synchronization.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegrationSchedule implements Serializable {

    @Column(name = "schedule_frequency", nullable = false)
    private int frequencyMinutes;

    @Column(name = "schedule_enabled", nullable = false)
    private boolean enabled;

    @Column(name = "schedule_last_execution")
    private Instant lastExecution;

    public static IntegrationSchedule of(int frequencyMinutes, boolean enabled) {
        if (frequencyMinutes <= 0 || frequencyMinutes > 1440) {
            throw new IllegalArgumentException("frequencyMinutes must be between 1 and 1440");
        }
        return new IntegrationSchedule(frequencyMinutes, enabled, null);
    }

    public IntegrationSchedule executedNow() {
        return new IntegrationSchedule(frequencyMinutes, enabled, Instant.now());
    }

    public boolean isDue(Instant reference) {
        if (!enabled) {
            return false;
        }
        if (lastExecution == null) {
            return true;
        }
        Instant now = reference != null ? reference : Instant.now();
        return lastExecution.plusSeconds(frequencyMinutes * 60L).isBefore(now);
    }
}
