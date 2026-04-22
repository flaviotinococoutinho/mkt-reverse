package com.marketplace.user.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * KYC Status Enumeration
 * 
 * Represents the different states of KYC verification process.
 * Controls the workflow and allowed transitions in the verification process.
 * 
 * Design principles:
 * - Clear state machine with defined transitions
 * - Workflow-aware status management
 * - Audit trail support
 */
@Getter
public enum KycStatus {
    
    /**
     * Pending - Initial state, no documents submitted
     * User needs to submit required documents
     */
    PENDING("Pending", "Awaiting document submission", false, true),
    
    /**
     * In Review - Documents submitted and under review
     * Manual or automated review in progress
     */
    IN_REVIEW("In Review", "Documents submitted and under review", false, false),
    
    /**
     * Verified - KYC successfully completed
     * All requirements met, user fully verified
     */
    VERIFIED("Verified", "KYC verification completed successfully", true, false),
    
    /**
     * Rejected - KYC verification failed
     * Documents rejected, user needs to resubmit
     */
    REJECTED("Rejected", "KYC verification rejected, resubmission required", false, true),
    
    /**
     * Expired - Previously verified but now expired
     * Needs re-verification due to time limits
     */
    EXPIRED("Expired", "KYC verification expired, re-verification required", false, true);

    private final String displayName;
    private final String description;
    private final boolean isCompleted;
    private final boolean allowsResubmission;

    KycStatus(String displayName, String description, boolean isCompleted, boolean allowsResubmission) {
        this.displayName = displayName;
        this.description = description;
        this.isCompleted = isCompleted;
        this.allowsResubmission = allowsResubmission;
    }

    /**
     * Checks if this status represents a completed verification
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Checks if this status allows document resubmission
     */
    public boolean allowsResubmission() {
        return allowsResubmission;
    }

    /**
     * Checks if this status is in progress
     */
    public boolean isInProgress() {
        return this == IN_REVIEW;
    }

    /**
     * Checks if this status indicates failure
     */
    public boolean isFailure() {
        return this == REJECTED || this == EXPIRED;
    }

    /**
     * Checks if this status requires action from user
     */
    public boolean requiresUserAction() {
        return this == PENDING || this == REJECTED || this == EXPIRED;
    }

    /**
     * Checks if this status requires action from admin/system
     */
    public boolean requiresAdminAction() {
        return this == IN_REVIEW;
    }

    /**
     * Gets the valid transitions from this status
     */
    public Set<KycStatus> getValidTransitions() {
        return switch (this) {
            case PENDING -> Set.of(IN_REVIEW, REJECTED);
            case IN_REVIEW -> Set.of(VERIFIED, REJECTED);
            case VERIFIED -> Set.of(EXPIRED);
            case REJECTED -> Set.of(IN_REVIEW, PENDING);
            case EXPIRED -> Set.of(IN_REVIEW, PENDING);
        };
    }

    /**
     * Checks if transition to another status is valid
     */
    public boolean canTransitionTo(KycStatus newStatus) {
        return getValidTransitions().contains(newStatus);
    }

    /**
     * Gets the next logical status in the workflow
     */
    public KycStatus getNextStatus() {
        return switch (this) {
            case PENDING -> IN_REVIEW;
            case IN_REVIEW -> VERIFIED; // Optimistic path
            case VERIFIED -> VERIFIED; // Already completed
            case REJECTED -> PENDING; // Restart process
            case EXPIRED -> PENDING; // Restart process
        };
    }

    /**
     * Gets the priority level for this status (for sorting/filtering)
     */
    public int getPriority() {
        return switch (this) {
            case REJECTED -> 1; // Highest priority - needs immediate attention
            case EXPIRED -> 2;
            case PENDING -> 3;
            case IN_REVIEW -> 4;
            case VERIFIED -> 5; // Lowest priority - completed
        };
    }

    /**
     * Gets the CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "status-pending";
            case IN_REVIEW -> "status-info";
            case VERIFIED -> "status-success";
            case REJECTED -> "status-danger";
            case EXPIRED -> "status-warning";
        };
    }

    /**
     * Gets the color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case PENDING -> "#ffc107"; // Yellow
            case IN_REVIEW -> "#17a2b8"; // Blue
            case VERIFIED -> "#28a745"; // Green
            case REJECTED -> "#dc3545"; // Red
            case EXPIRED -> "#fd7e14"; // Orange
        };
    }

    /**
     * Gets the icon name for UI display
     */
    public String getIconName() {
        return switch (this) {
            case PENDING -> "clock";
            case IN_REVIEW -> "eye";
            case VERIFIED -> "check-circle";
            case REJECTED -> "x-circle";
            case EXPIRED -> "alert-triangle";
        };
    }

    /**
     * Gets the progress percentage (0-100)
     */
    public int getProgressPercentage() {
        return switch (this) {
            case PENDING -> 10;
            case IN_REVIEW -> 70;
            case VERIFIED -> 100;
            case REJECTED -> 0;
            case EXPIRED -> 0;
        };
    }

    /**
     * Gets the estimated completion time in business days
     */
    public int getEstimatedCompletionDays() {
        return switch (this) {
            case PENDING -> 0; // Immediate action required
            case IN_REVIEW -> 3; // 3 business days for review
            case VERIFIED -> 0; // Already completed
            case REJECTED -> 1; // 1 day to resubmit
            case EXPIRED -> 1; // 1 day to resubmit
        };
    }

    /**
     * Gets the user-friendly message for this status
     */
    public String getUserMessage() {
        return switch (this) {
            case PENDING -> "Please submit your verification documents to continue.";
            case IN_REVIEW -> "Your documents are being reviewed. We'll notify you once complete.";
            case VERIFIED -> "Your identity has been successfully verified.";
            case REJECTED -> "Your verification was rejected. Please check the feedback and resubmit.";
            case EXPIRED -> "Your verification has expired. Please submit updated documents.";
        };
    }

    /**
     * Gets the admin message for this status
     */
    public String getAdminMessage() {
        return switch (this) {
            case PENDING -> "User has not submitted documents yet.";
            case IN_REVIEW -> "Documents submitted and awaiting review.";
            case VERIFIED -> "KYC verification completed successfully.";
            case REJECTED -> "Documents rejected, user notified for resubmission.";
            case EXPIRED -> "Verification expired, user needs to re-verify.";
        };
    }

    /**
     * Creates a KycStatus from string (case-insensitive)
     */
    public static KycStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("KYC status cannot be null or empty");
        }

        try {
            return KycStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid KYC status: " + status);
        }
    }

    /**
     * Checks if the given string is a valid KYC status
     */
    public static boolean isValid(String status) {
        try {
            fromString(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets all statuses that require user action
     */
    public static Set<KycStatus> getUserActionRequired() {
        return Set.of(PENDING, REJECTED, EXPIRED);
    }

    /**
     * Gets all statuses that require admin action
     */
    public static Set<KycStatus> getAdminActionRequired() {
        return Set.of(IN_REVIEW);
    }

    /**
     * Gets all completed statuses
     */
    public static Set<KycStatus> getCompletedStatuses() {
        return Set.of(VERIFIED);
    }

    /**
     * Gets all failed statuses
     */
    public static Set<KycStatus> getFailedStatuses() {
        return Set.of(REJECTED, EXPIRED);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

