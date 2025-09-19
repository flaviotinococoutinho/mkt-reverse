package com.marketplace.sourcing.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * Sourcing Event Status Enumeration
 * 
 * Represents the different states of a sourcing event throughout its lifecycle.
 * Controls workflow transitions and defines allowed operations for each state.
 * 
 * Design principles:
 * - Clear state machine with defined transitions
 * - Workflow-aware status management
 * - Business rule enforcement
 * - Audit trail support
 */
@Getter
public enum SourcingEventStatus {
    
    /**
     * Draft - Event is being created/configured
     * Event details can be modified, not visible to suppliers
     */
    DRAFT("Draft", "Event is being created and configured", false, true, false, false),
    
    /**
     * Published - Event is live and accepting responses
     * Visible to suppliers, responses can be submitted
     */
    PUBLISHED("Published", "Event is live and accepting supplier responses", true, false, true, false),
    
    /**
     * In Progress - Event is active with ongoing activities
     * For real-time events like auctions, negotiations in progress
     */
    IN_PROGRESS("In Progress", "Event is active with ongoing activities", true, false, true, false),
    
    /**
     * Submission Closed - No more responses accepted
     * Response period ended, evaluation can begin
     */
    SUBMISSION_CLOSED("Submission Closed", "Response period ended, evaluation in progress", true, false, false, false),
    
    /**
     * Under Evaluation - Responses being evaluated
     * Technical and commercial evaluation in progress
     */
    UNDER_EVALUATION("Under Evaluation", "Supplier responses under evaluation", true, false, false, false),
    
    /**
     * Negotiation - Selected suppliers in negotiation phase
     * Final terms and conditions being negotiated
     */
    NEGOTIATION("Negotiation", "Final negotiations with selected suppliers", true, false, true, false),
    
    /**
     * Awarded - Contract awarded to winning supplier(s)
     * Event completed successfully with award decision
     */
    AWARDED("Awarded", "Contract awarded to winning supplier(s)", false, false, false, true),
    
    /**
     * Cancelled - Event cancelled before completion
     * Event terminated without award, suppliers notified
     */
    CANCELLED("Cancelled", "Event cancelled before completion", false, false, false, true),
    
    /**
     * Expired - Event expired without completion
     * Event ended due to time limits without successful conclusion
     */
    EXPIRED("Expired", "Event expired without completion", false, false, false, true),
    
    /**
     * Suspended - Event temporarily suspended
     * Event paused, can be resumed later
     */
    SUSPENDED("Suspended", "Event temporarily suspended", false, true, false, false),
    
    /**
     * Failed - Event failed due to technical or business issues
     * Event could not be completed due to system or process failures
     */
    FAILED("Failed", "Event failed due to technical or business issues", false, false, false, true);

    private final String displayName;
    private final String description;
    private final boolean isActive;
    private final boolean allowsModification;
    private final boolean acceptsResponses;
    private final boolean isFinal;

    SourcingEventStatus(String displayName, String description, boolean isActive, 
                       boolean allowsModification, boolean acceptsResponses, boolean isFinal) {
        this.displayName = displayName;
        this.description = description;
        this.isActive = isActive;
        this.allowsModification = allowsModification;
        this.acceptsResponses = acceptsResponses;
        this.isFinal = isFinal;
    }

    /**
     * Checks if the event is in an active state
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Checks if the event allows modifications
     */
    public boolean allowsModification() {
        return allowsModification;
    }

    /**
     * Checks if the event accepts supplier responses
     */
    public boolean acceptsResponses() {
        return acceptsResponses;
    }

    /**
     * Checks if this is a final state (no further transitions)
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Checks if the event is completed (successfully or not)
     */
    public boolean isCompleted() {
        return this == AWARDED || this == CANCELLED || this == EXPIRED || this == FAILED;
    }

    /**
     * Checks if the event completed successfully
     */
    public boolean isSuccessful() {
        return this == AWARDED;
    }

    /**
     * Checks if the event is in evaluation phase
     */
    public boolean isInEvaluation() {
        return this == UNDER_EVALUATION || this == NEGOTIATION;
    }

    /**
     * Checks if the event can be extended
     */
    public boolean canBeExtended() {
        return this == PUBLISHED || this == IN_PROGRESS;
    }

    /**
     * Checks if the event can be suspended
     */
    public boolean canBeSuspended() {
        return isActive && !isFinal;
    }

    /**
     * Checks if the event can be cancelled
     */
    public boolean canBeCancelled() {
        return !isFinal && this != CANCELLED;
    }

    /**
     * Gets the valid transitions from this status
     */
    public Set<SourcingEventStatus> getValidTransitions() {
        return switch (this) {
            case DRAFT -> Set.of(PUBLISHED, CANCELLED, SUSPENDED);
            case PUBLISHED -> Set.of(IN_PROGRESS, SUBMISSION_CLOSED, CANCELLED, SUSPENDED, EXPIRED);
            case IN_PROGRESS -> Set.of(SUBMISSION_CLOSED, AWARDED, CANCELLED, SUSPENDED, EXPIRED, FAILED);
            case SUBMISSION_CLOSED -> Set.of(UNDER_EVALUATION, CANCELLED, EXPIRED);
            case UNDER_EVALUATION -> Set.of(NEGOTIATION, AWARDED, CANCELLED, FAILED);
            case NEGOTIATION -> Set.of(AWARDED, CANCELLED, FAILED);
            case SUSPENDED -> Set.of(PUBLISHED, IN_PROGRESS, CANCELLED);
            case AWARDED, CANCELLED, EXPIRED, FAILED -> Set.of(); // Final states
        };
    }

    /**
     * Checks if transition to another status is valid
     */
    public boolean canTransitionTo(SourcingEventStatus newStatus) {
        return getValidTransitions().contains(newStatus);
    }

    /**
     * Gets the next logical status in the workflow
     */
    public SourcingEventStatus getNextStatus() {
        return switch (this) {
            case DRAFT -> PUBLISHED;
            case PUBLISHED -> IN_PROGRESS;
            case IN_PROGRESS -> SUBMISSION_CLOSED;
            case SUBMISSION_CLOSED -> UNDER_EVALUATION;
            case UNDER_EVALUATION -> NEGOTIATION;
            case NEGOTIATION -> AWARDED;
            case SUSPENDED -> PUBLISHED; // Resume
            default -> this; // Final states remain unchanged
        };
    }

    /**
     * Gets the priority level for this status (for sorting/filtering)
     */
    public int getPriority() {
        return switch (this) {
            case FAILED -> 1; // Highest priority - needs immediate attention
            case EXPIRED -> 2;
            case IN_PROGRESS -> 3; // Active events need attention
            case NEGOTIATION -> 4;
            case UNDER_EVALUATION -> 5;
            case PUBLISHED -> 6;
            case SUBMISSION_CLOSED -> 7;
            case SUSPENDED -> 8;
            case DRAFT -> 9;
            case AWARDED -> 10;
            case CANCELLED -> 11; // Lowest priority - completed
        };
    }

    /**
     * Gets the CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case DRAFT -> "status-draft";
            case PUBLISHED, IN_PROGRESS -> "status-active";
            case SUBMISSION_CLOSED, UNDER_EVALUATION -> "status-info";
            case NEGOTIATION -> "status-warning";
            case AWARDED -> "status-success";
            case CANCELLED -> "status-secondary";
            case EXPIRED, FAILED -> "status-danger";
            case SUSPENDED -> "status-warning";
        };
    }

    /**
     * Gets the color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case DRAFT -> "#6c757d"; // Gray
            case PUBLISHED, IN_PROGRESS -> "#007bff"; // Blue
            case SUBMISSION_CLOSED, UNDER_EVALUATION -> "#17a2b8"; // Cyan
            case NEGOTIATION -> "#ffc107"; // Yellow
            case AWARDED -> "#28a745"; // Green
            case CANCELLED -> "#6c757d"; // Gray
            case EXPIRED, FAILED -> "#dc3545"; // Red
            case SUSPENDED -> "#fd7e14"; // Orange
        };
    }

    /**
     * Gets the icon name for UI display
     */
    public String getIconName() {
        return switch (this) {
            case DRAFT -> "edit";
            case PUBLISHED -> "eye";
            case IN_PROGRESS -> "play-circle";
            case SUBMISSION_CLOSED -> "stop-circle";
            case UNDER_EVALUATION -> "search";
            case NEGOTIATION -> "message-circle";
            case AWARDED -> "award";
            case CANCELLED -> "x-circle";
            case EXPIRED -> "clock";
            case SUSPENDED -> "pause-circle";
            case FAILED -> "alert-triangle";
        };
    }

    /**
     * Gets the progress percentage (0-100)
     */
    public int getProgressPercentage() {
        return switch (this) {
            case DRAFT -> 10;
            case PUBLISHED -> 25;
            case IN_PROGRESS -> 40;
            case SUBMISSION_CLOSED -> 60;
            case UNDER_EVALUATION -> 75;
            case NEGOTIATION -> 90;
            case AWARDED -> 100;
            case CANCELLED, EXPIRED, FAILED -> 0;
            case SUSPENDED -> 50; // Paused state
        };
    }

    /**
     * Gets the user-friendly message for this status
     */
    public String getUserMessage() {
        return switch (this) {
            case DRAFT -> "Event is being prepared and will be published soon.";
            case PUBLISHED -> "Event is live and accepting supplier responses.";
            case IN_PROGRESS -> "Event is active with ongoing activities.";
            case SUBMISSION_CLOSED -> "Response period has ended. Evaluation in progress.";
            case UNDER_EVALUATION -> "Supplier responses are being evaluated.";
            case NEGOTIATION -> "Final negotiations are in progress with selected suppliers.";
            case AWARDED -> "Contract has been awarded. Event completed successfully.";
            case CANCELLED -> "Event has been cancelled.";
            case EXPIRED -> "Event has expired without completion.";
            case SUSPENDED -> "Event is temporarily suspended.";
            case FAILED -> "Event failed due to technical or business issues.";
        };
    }

    /**
     * Gets the admin message for this status
     */
    public String getAdminMessage() {
        return switch (this) {
            case DRAFT -> "Event configuration in progress.";
            case PUBLISHED -> "Event published and visible to suppliers.";
            case IN_PROGRESS -> "Event activities ongoing, monitoring required.";
            case SUBMISSION_CLOSED -> "Submission period ended, ready for evaluation.";
            case UNDER_EVALUATION -> "Evaluation in progress, decision pending.";
            case NEGOTIATION -> "Negotiations ongoing with shortlisted suppliers.";
            case AWARDED -> "Award decision made and communicated.";
            case CANCELLED -> "Event cancelled by buyer or admin.";
            case EXPIRED -> "Event expired due to time limits.";
            case SUSPENDED -> "Event suspended, awaiting resolution.";
            case FAILED -> "Event failed, investigation required.";
        };
    }

    /**
     * Creates a SourcingEventStatus from string (case-insensitive)
     */
    public static SourcingEventStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Sourcing event status cannot be null or empty");
        }

        try {
            return SourcingEventStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sourcing event status: " + status);
        }
    }

    /**
     * Checks if the given string is a valid status
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
     * Gets all active statuses
     */
    public static Set<SourcingEventStatus> getActiveStatuses() {
        return Set.of(PUBLISHED, IN_PROGRESS, SUBMISSION_CLOSED, UNDER_EVALUATION, NEGOTIATION);
    }

    /**
     * Gets all final statuses
     */
    public static Set<SourcingEventStatus> getFinalStatuses() {
        return Set.of(AWARDED, CANCELLED, EXPIRED, FAILED);
    }

    /**
     * Gets all statuses that accept responses
     */
    public static Set<SourcingEventStatus> getResponseAcceptingStatuses() {
        return Set.of(PUBLISHED, IN_PROGRESS, NEGOTIATION);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

