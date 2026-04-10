package com.marketplace.sourcing.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Operational settings that control sourcing event behaviour.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SourcingEventSettings implements Serializable {

    @Column(name = "min_participants", nullable = false)
    private int minimumParticipants;

    @Column(name = "max_suppliers", nullable = false)
    private int maximumSuppliers;

    @Column(name = "allow_multiple_winners", nullable = false)
    private boolean allowMultipleWinners;

    @Column(name = "max_winners", nullable = false)
    private int maximumWinners;

    @Column(name = "auto_extend_on_low_participation", nullable = false)
    private boolean autoExtendOnLowParticipation;

    @Column(name = "auto_extend_minutes", nullable = false)
    private int autoExtendMinutes;

    @Column(name = "requires_nda", nullable = false)
    private boolean requiresNda;

    @Column(name = "allow_anonymous_bids", nullable = false)
    private boolean allowAnonymousBids;

    @Column(name = "allow_supplier_questions", nullable = false)
    private boolean allowSupplierQuestions;

    @Column(name = "allow_attachments", nullable = false)
    private boolean allowAttachments;

    @Column(name = "scoring_strategy", nullable = false, length = 40)
    private String scoringStrategy;

    public static SourcingEventSettings defaults() {
        return new SourcingEventSettings(
            2,
            50,
            false,
            1,
            true,
            60,
            true,
            false,
            true,
            true,
            "WEIGHTED"
        );
    }

    public static SourcingEventSettings create(
        int minimumParticipants,
        int maximumSuppliers,
        boolean allowMultipleWinners,
        int maximumWinners,
        boolean autoExtendOnLowParticipation,
        int autoExtendMinutes,
        boolean requiresNda,
        boolean allowAnonymousBids,
        boolean allowSupplierQuestions,
        boolean allowAttachments,
        String scoringStrategy
    ) {
        validate(minimumParticipants, maximumSuppliers, allowMultipleWinners, maximumWinners,
            autoExtendMinutes, scoringStrategy);

        return new SourcingEventSettings(
            minimumParticipants,
            maximumSuppliers,
            allowMultipleWinners,
            allowMultipleWinners ? Math.max(1, maximumWinners) : 1,
            autoExtendOnLowParticipation,
            autoExtendMinutes,
            requiresNda,
            allowAnonymousBids,
            allowSupplierQuestions,
            allowAttachments,
            scoringStrategy.trim().toUpperCase()
        );
    }

    private static void validate(
        int minimumParticipants,
        int maximumSuppliers,
        boolean allowMultipleWinners,
        int maximumWinners,
        int autoExtendMinutes,
        String scoringStrategy
    ) {
        if (minimumParticipants < 1) {
            throw new IllegalArgumentException("minimumParticipants must be at least 1");
        }
        if (maximumSuppliers < minimumParticipants) {
            throw new IllegalArgumentException("maximumSuppliers must be >= minimumParticipants");
        }
        if (allowMultipleWinners && maximumWinners < 2) {
            throw new IllegalArgumentException("maximumWinners must be >= 2 when allowMultipleWinners is true");
        }
        if (autoExtendMinutes < 0 || autoExtendMinutes > 240) {
            throw new IllegalArgumentException("autoExtendMinutes must be between 0 and 240");
        }
        if (scoringStrategy == null || scoringStrategy.trim().isEmpty()) {
            throw new IllegalArgumentException("scoringStrategy cannot be blank");
        }
    }

    public boolean shouldAutoExtend(int currentParticipants) {
        return autoExtendOnLowParticipation && currentParticipants < minimumParticipants;
    }

    public boolean supportsMultipleWinners() {
        return allowMultipleWinners;
    }

    public boolean permitsAnonymousBids(SourcingEventType type) {
        if (!allowAnonymousBids || type == null) {
            return false;
        }
        return switch (type) {
            case SEALED_BID, REVERSE_AUCTION, DUTCH_AUCTION -> true;
            default -> false;
        };
    }
}
