package com.marketplace.shared.infrastructure.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Snowflake ID Generator for distributed unique ID generation.
 * 
 * Structure (64 bits):
 * - 1 bit: unused (always 0)
 * - 41 bits: timestamp in milliseconds since custom epoch
 * - 10 bits: worker/datacenter ID (0-1023)
 * - 12 bits: sequence number (0-4095)
 * 
 * This provides:
 * - Unique IDs across distributed systems
 * - Temporal ordering
 * - No coordination required
 * - ~4096 IDs per millisecond per worker
 */
@Component
public class SnowflakeIdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);
    
    private static final long CUSTOM_EPOCH = 1704067200000L; // 2024-01-01 00:00:00 UTC
    
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    public SnowflakeIdGenerator(@Value("${snowflake.worker-id:1}") long workerId) {
        validateWorkerId(workerId);
        this.workerId = workerId;
        logger.info("Snowflake ID Generator initialized with worker ID: {}", workerId);
    }
    
    /**
     * Generates next unique ID.
     * Thread-safe method that generates sequential IDs.
     * 
     * @return unique 64-bit long ID
     * @throws ClockMovedBackwardsException if system clock moves backwards
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();
        
        validateTimestamp(currentTimestamp);
        
        if (isTimestampUnchanged(currentTimestamp)) {
            incrementSequence();
            if (hasSequenceOverflowed()) {
                currentTimestamp = waitForNextMillisecond(lastTimestamp);
            }
        } else {
            resetSequence();
        }
        
        lastTimestamp = currentTimestamp;
        
        return buildId(currentTimestamp);
    }
    
    /**
     * Extracts timestamp from Snowflake ID.
     * 
     * @param id Snowflake ID
     * @return timestamp in milliseconds since custom epoch
     */
    public long extractTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + CUSTOM_EPOCH;
    }
    
    /**
     * Extracts worker ID from Snowflake ID.
     * 
     * @param id Snowflake ID
     * @return worker ID
     */
    public long extractWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }
    
    /**
     * Extracts sequence number from Snowflake ID.
     * 
     * @param id Snowflake ID
     * @return sequence number
     */
    public long extractSequence(long id) {
        return id & MAX_SEQUENCE;
    }
    
    private void validateWorkerId(long workerId) {
        if (isWorkerIdInvalid(workerId)) {
            String errorMessage = String.format(
                "Worker ID must be between 0 and %d, but got: %d", 
                MAX_WORKER_ID, 
                workerId
            );
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    private boolean isWorkerIdInvalid(long workerId) {
        return workerId < 0 || workerId > MAX_WORKER_ID;
    }
    
    private long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }
    
    private void validateTimestamp(long currentTimestamp) {
        if (hasClockMovedBackwards(currentTimestamp)) {
            long timeDifference = lastTimestamp - currentTimestamp;
            String errorMessage = String.format(
                "Clock moved backwards. Refusing to generate ID for %d milliseconds", 
                timeDifference
            );
            logger.error(errorMessage);
            throw new ClockMovedBackwardsException(errorMessage);
        }
    }
    
    private boolean hasClockMovedBackwards(long currentTimestamp) {
        return currentTimestamp < lastTimestamp;
    }
    
    private boolean isTimestampUnchanged(long currentTimestamp) {
        return currentTimestamp == lastTimestamp;
    }
    
    private void incrementSequence() {
        sequence = (sequence + 1) & MAX_SEQUENCE;
    }
    
    private boolean hasSequenceOverflowed() {
        return sequence == 0;
    }
    
    private void resetSequence() {
        sequence = 0L;
    }
    
    private long waitForNextMillisecond(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
    
    private long buildId(long timestamp) {
        long timestampPart = (timestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT;
        long workerIdPart = workerId << WORKER_ID_SHIFT;
        long sequencePart = sequence;
        
        return timestampPart | workerIdPart | sequencePart;
    }
    
    /**
     * Exception thrown when system clock moves backwards.
     */
    public static class ClockMovedBackwardsException extends RuntimeException {
        public ClockMovedBackwardsException(String message) {
            super(message);
        }
    }
}
