package com.marketplace.contract.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Contract term and renewal settings.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractTerm implements Serializable {

    @Column(name = "term_start", nullable = false)
    private LocalDate startDate;

    @Column(name = "term_end", nullable = false)
    private LocalDate endDate;

    @Column(name = "term_auto_renew", nullable = false)
    private boolean autoRenew;

    @Column(name = "term_renewal_months")
    private Integer renewalMonths;

    @Column(name = "term_notice_period_days", nullable = false)
    private int noticePeriodDays;

    public static ContractTerm of(LocalDate startDate, LocalDate endDate, boolean autoRenew, Integer renewalMonths, int noticePeriodDays) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Contract term dates cannot be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Contract end date must be after start date");
        }
        if (autoRenew && (renewalMonths == null || renewalMonths <= 0)) {
            throw new IllegalArgumentException("Auto renew contracts must have renewal months defined");
        }
        if (noticePeriodDays < 0 || noticePeriodDays > 180) {
            throw new IllegalArgumentException("Notice period must be between 0 and 180 days");
        }

        return new ContractTerm(startDate, endDate, autoRenew, autoRenew ? renewalMonths : null, noticePeriodDays);
    }

    public boolean isExpired(LocalDate reference) {
        LocalDate checkDate = reference != null ? reference : LocalDate.now();
        return checkDate.isAfter(endDate);
    }
}
