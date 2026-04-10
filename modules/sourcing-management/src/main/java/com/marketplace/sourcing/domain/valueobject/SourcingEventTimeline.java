package com.marketplace.sourcing.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Timeline definition for sourcing events.
 * Controls lifecycle milestones and allows controlled extensions.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SourcingEventTimeline implements Serializable {

    @Column(name = "published_at", nullable = false)
    private Instant publicationAt;

    @Column(name = "submission_open_at", nullable = false)
    private Instant submissionOpenAt;

    @Column(name = "submission_deadline", nullable = false)
    private Instant submissionDeadline;

    @Column(name = "evaluation_start_at")
    private Instant evaluationStartAt;

    @Column(name = "evaluation_deadline")
    private Instant evaluationDeadline;

    @Column(name = "negotiation_deadline")
    private Instant negotiationDeadline;

    @Column(name = "award_target_at")
    private Instant awardTargetAt;

    @Column(name = "max_extensions", nullable = false)
    private int maxExtensions;

    @Column(name = "extensions_used", nullable = false)
    private int extensionsUsed;

    @Column(name = "last_extension_at")
    private Instant lastExtensionAt;

    public static SourcingEventTimeline create(
        Instant publicationAt,
        Instant submissionOpenAt,
        Instant submissionDeadline,
        Instant evaluationStartAt,
        Instant evaluationDeadline,
        Instant negotiationDeadline,
        Instant awardTargetAt,
        int maxExtensions
    ) {
        validateTimeline(publicationAt, submissionOpenAt, submissionDeadline,
            evaluationStartAt, evaluationDeadline, negotiationDeadline, awardTargetAt, maxExtensions);

        return new SourcingEventTimeline(
            publicationAt,
            submissionOpenAt,
            submissionDeadline,
            evaluationStartAt,
            evaluationDeadline,
            negotiationDeadline,
            awardTargetAt,
            Math.max(0, maxExtensions),
            0,
            null
        );
    }

    private static void validateTimeline(
        Instant publicationAt,
        Instant submissionOpenAt,
        Instant submissionDeadline,
        Instant evaluationStartAt,
        Instant evaluationDeadline,
        Instant negotiationDeadline,
        Instant awardTargetAt,
        int maxExtensions
    ) {
        Objects.requireNonNull(publicationAt, "publicationAt is required");
        Objects.requireNonNull(submissionOpenAt, "submissionOpenAt is required");
        Objects.requireNonNull(submissionDeadline, "submissionDeadline is required");

        if (submissionOpenAt.isBefore(publicationAt)) {
            throw new IllegalArgumentException("submissionOpenAt cannot be before publicationAt");
        }
        if (!submissionDeadline.isAfter(submissionOpenAt)) {
            throw new IllegalArgumentException("submissionDeadline must be after submissionOpenAt");
        }
        if (evaluationStartAt != null && !evaluationStartAt.isAfter(submissionDeadline)) {
            throw new IllegalArgumentException("evaluationStartAt must be after submissionDeadline");
        }
        if (evaluationDeadline != null && evaluationStartAt != null && !evaluationDeadline.isAfter(evaluationStartAt)) {
            throw new IllegalArgumentException("evaluationDeadline must be after evaluationStartAt");
        }
        if (negotiationDeadline != null && evaluationDeadline != null && !negotiationDeadline.isAfter(evaluationDeadline)) {
            throw new IllegalArgumentException("negotiationDeadline must be after evaluationDeadline");
        }
        if (awardTargetAt != null && negotiationDeadline != null && !awardTargetAt.isAfter(negotiationDeadline)) {
            throw new IllegalArgumentException("awardTargetAt must be after negotiationDeadline");
        }
        if (maxExtensions < 0 || maxExtensions > 5) {
            throw new IllegalArgumentException("maxExtensions must be between 0 and 5");
        }
    }

    public boolean isSubmissionOpen(Instant reference) {
        Instant now = reference != null ? reference : Instant.now();
        return !now.isBefore(submissionOpenAt) && now.isBefore(submissionDeadline);
    }

    public boolean submissionsClosed(Instant reference) {
        Instant now = reference != null ? reference : Instant.now();
        return !now.isBefore(submissionDeadline);
    }

    public boolean evaluationWindowStarted(Instant reference) {
        if (evaluationStartAt == null) {
            return false;
        }
        Instant now = reference != null ? reference : Instant.now();
        return !now.isBefore(evaluationStartAt);
    }

    public boolean isExpired(Instant reference) {
        Instant now = reference != null ? reference : Instant.now();
        return awardTargetAt != null && !now.isBefore(awardTargetAt);
    }

    public boolean canExtendSubmission(Duration extensionDuration) {
        if (extensionsUsed >= maxExtensions) {
            return false;
        }
        if (extensionDuration == null || extensionDuration.isZero() || extensionDuration.isNegative()) {
            return false;
        }
        return extensionDuration.compareTo(Duration.of(48, ChronoUnit.HOURS)) <= 0;
    }

    public SourcingEventTimeline extendSubmission(Duration extensionDuration) {
        if (!canExtendSubmission(extensionDuration)) {
            throw new IllegalStateException("Submission deadline cannot be extended further");
        }

        return new SourcingEventTimeline(
            publicationAt,
            submissionOpenAt,
            submissionDeadline.plus(extensionDuration),
            evaluationStartAt != null ? evaluationStartAt.plus(extensionDuration) : null,
            evaluationDeadline != null ? evaluationDeadline.plus(extensionDuration) : null,
            negotiationDeadline != null ? negotiationDeadline.plus(extensionDuration) : null,
            awardTargetAt != null ? awardTargetAt.plus(extensionDuration) : null,
            maxExtensions,
            extensionsUsed + 1,
            Instant.now()
        );
    }

    public long remainingSubmissionHours(Instant reference) {
        Instant now = reference != null ? reference : Instant.now();
        if (now.isAfter(submissionDeadline)) {
            return 0L;
        }
        return Duration.between(now, submissionDeadline).toHours();
    }

    public boolean hasNegotiationPhase() {
        return negotiationDeadline != null;
    }

    public boolean hasEvaluationPhase() {
        return evaluationStartAt != null;
    }
}
