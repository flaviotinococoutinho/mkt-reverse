package com.marketplace.proposal.domain.valueobject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object representing delivery time estimation.
 * 
 * Encapsulates delivery time logic:
 * - Days and hours estimation
 * - Estimated delivery date calculation
 * - Validation of reasonable delivery times
 * 
 * Immutable and defined by its value.
 */
public record DeliveryTime(
    Integer days,
    Integer hours,
    Instant estimatedDate
) {
    
    private static final int MAX_DELIVERY_DAYS = 365;
    private static final int HOURS_PER_DAY = 24;
    
    public DeliveryTime {
        Objects.requireNonNull(days, "Delivery days cannot be null");
        Objects.requireNonNull(hours, "Delivery hours cannot be null");
        Objects.requireNonNull(estimatedDate, "Estimated date cannot be null");
        
        validateDeliveryDays(days);
        validateDeliveryHours(hours);
        validateEstimatedDate(estimatedDate);
    }
    
    /**
     * Creates DeliveryTime from days only.
     * 
     * @param days number of days
     * @return DeliveryTime instance
     */
    public static DeliveryTime fromDays(Integer days) {
        Instant estimatedDate = calculateEstimatedDate(days, 0);
        return new DeliveryTime(days, 0, estimatedDate);
    }
    
    /**
     * Creates DeliveryTime from days and hours.
     * 
     * @param days number of days
     * @param hours number of hours
     * @return DeliveryTime instance
     */
    public static DeliveryTime fromDaysAndHours(Integer days, Integer hours) {
        Instant estimatedDate = calculateEstimatedDate(days, hours);
        return new DeliveryTime(days, hours, estimatedDate);
    }
    
    /**
     * Creates DeliveryTime from specific date.
     * 
     * @param estimatedDate estimated delivery date
     * @return DeliveryTime instance
     */
    public static DeliveryTime fromDate(Instant estimatedDate) {
        long totalHours = ChronoUnit.HOURS.between(Instant.now(), estimatedDate);
        int days = (int) (totalHours / HOURS_PER_DAY);
        int hours = (int) (totalHours % HOURS_PER_DAY);
        
        return new DeliveryTime(days, hours, estimatedDate);
    }
    
    /**
     * Returns total delivery time in hours.
     * 
     * @return total hours
     */
    public long totalHours() {
        return (days * HOURS_PER_DAY) + hours;
    }
    
    /**
     * Checks if delivery is express (less than 24 hours).
     * 
     * @return true if express delivery
     */
    public boolean isExpress() {
        return totalHours() < HOURS_PER_DAY;
    }
    
    /**
     * Checks if delivery is standard (1-7 days).
     * 
     * @return true if standard delivery
     */
    public boolean isStandard() {
        return days >= 1 && days <= 7;
    }
    
    /**
     * Checks if delivery is extended (more than 7 days).
     * 
     * @return true if extended delivery
     */
    public boolean isExtended() {
        return days > 7;
    }
    
    /**
     * Compares with another delivery time.
     * 
     * @param other other delivery time
     * @return true if this is faster
     */
    public boolean isFasterThan(DeliveryTime other) {
        return totalHours() < other.totalHours();
    }
    
    private static Instant calculateEstimatedDate(Integer days, Integer hours) {
        return Instant.now()
            .plus(days, ChronoUnit.DAYS)
            .plus(hours, ChronoUnit.HOURS);
    }
    
    private void validateDeliveryDays(Integer days) {
        if (days < 0) {
            throw new IllegalArgumentException("Delivery days cannot be negative");
        }
        
        if (days > MAX_DELIVERY_DAYS) {
            throw new IllegalArgumentException(
                String.format("Delivery days cannot exceed %d", MAX_DELIVERY_DAYS)
            );
        }
    }
    
    private void validateDeliveryHours(Integer hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Delivery hours cannot be negative");
        }
        
        if (hours >= HOURS_PER_DAY) {
            throw new IllegalArgumentException(
                String.format("Delivery hours must be less than %d", HOURS_PER_DAY)
            );
        }
    }
    
    private void validateEstimatedDate(Instant estimatedDate) {
        if (estimatedDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Estimated date cannot be in the past");
        }
    }
    
    @Override
    public String toString() {
        if (days == 0) {
            return String.format("%d hours", hours);
        }
        
        if (hours == 0) {
            return String.format("%d days", days);
        }
        
        return String.format("%d days and %d hours", days, hours);
    }
}
