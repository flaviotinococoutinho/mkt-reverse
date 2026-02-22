package com.marketplace.contract.domain.valueobject;

import com.marketplace.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Milestone definition for contract deliverables.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Milestone implements Serializable {

    @Column(name = "milestone_name", nullable = false, length = 150)
    private String name;

    @Column(name = "milestone_description", length = 500)
    private String description;

    @Column(name = "milestone_due_date", nullable = false)
    private LocalDate dueDate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "milestone_amount", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "milestone_currency", length = 3))
    })
    private Money amount;

    @Column(name = "milestone_completed", nullable = false)
    private boolean completed;

    @Column(name = "milestone_completed_at")
    private Instant completedAt;

    public static Milestone of(String name, LocalDate dueDate, Money amount, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Milestone name cannot be blank");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Milestone dueDate cannot be null");
        }
        if (amount == null || amount.isNegative()) {
            throw new IllegalArgumentException("Milestone amount must be positive");
        }
        return new Milestone(name.trim(), description != null ? description.trim() : null, dueDate, amount, false, null);
    }

    public Milestone markCompleted() {
        return new Milestone(name, description, dueDate, amount, true, Instant.now());
    }
}
