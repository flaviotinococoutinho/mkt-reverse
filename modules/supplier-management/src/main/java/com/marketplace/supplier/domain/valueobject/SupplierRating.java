package com.marketplace.supplier.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Performance rating snapshot for suppliers.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplierRating implements Serializable {

    @Column(name = "rating_quality", nullable = false)
    private int quality;

    @Column(name = "rating_delivery", nullable = false)
    private int delivery;

    @Column(name = "rating_cost", nullable = false)
    private int cost;

    @Column(name = "rating_compliance", nullable = false)
    private int compliance;

    @Column(name = "rating_innovation", nullable = false)
    private int innovation;

    public static SupplierRating initial() {
        return new SupplierRating(50, 50, 50, 50, 50);
    }

    public static SupplierRating of(int quality, int delivery, int cost, int compliance, int innovation) {
        validateScore(quality, "quality");
        validateScore(delivery, "delivery");
        validateScore(cost, "cost");
        validateScore(compliance, "compliance");
        validateScore(innovation, "innovation");
        return new SupplierRating(quality, delivery, cost, compliance, innovation);
    }

    public SupplierRating update(int quality, int delivery, int cost, int compliance, int innovation) {
        return of(quality, delivery, cost, compliance, innovation);
    }

    public int overallScore() {
        return Math.round((quality * 0.25f) + (delivery * 0.25f) + (cost * 0.2f) + (compliance * 0.2f) + (innovation * 0.1f));
    }

    public String riskLevel() {
        int score = overallScore();
        if (score >= 80) {
            return "LOW";
        } else if (score >= 60) {
            return "MEDIUM";
        }
        return "HIGH";
    }

    private static void validateScore(int value, String field) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(field + " score must be between 0 and 100");
        }
    }
}
