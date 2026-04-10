package com.marketplace.shared.id;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Twitter Snowflake-inspired 64-bit id generator.
 *
 * Layout (most significant -> least):
 * - 41 bits timestamp (ms since custom epoch)
 * -  5 bits datacenter id
 * -  5 bits worker id
 * - 12 bits sequence
 *
 * Properties:
 * - Monotonic per worker within the same millisecond.
 * - Sortable by time (roughly).
 * - No external dependencies.
 */
public final class SnowflakeIdGenerator implements IdGenerator {

    private static final int SEQUENCE_BITS = 12;
    private static final int WORKER_BITS = 5;
    private static final int DATACENTER_BITS = 5;

    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long MAX_WORKER = (1L << WORKER_BITS) - 1;
    private static final long MAX_DATACENTER = (1L << DATACENTER_BITS) - 1;

    private static final int WORKER_SHIFT = SEQUENCE_BITS;
    private static final int DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    /**
     * Default epoch: 2024-01-01T00:00:00Z.
     * (Not Twitter's original epoch; configurable per product, avoids negative ids and extends useful lifetime.)
     */
    public static final long DEFAULT_EPOCH_MILLIS = 1704067200000L;

    private final long epochMillis;
    private final long datacenterId;
    private final long workerId;

    // state
    private final AtomicLong lastTimestamp = new AtomicLong(-1L);
    private final AtomicLong sequence = new AtomicLong(0L);

    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        this(DEFAULT_EPOCH_MILLIS, datacenterId, workerId);
    }

    public SnowflakeIdGenerator(long epochMillis, long datacenterId, long workerId) {
        if (epochMillis <= 0) {
            throw new IllegalArgumentException("epochMillis must be positive");
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER) {
            throw new IllegalArgumentException("datacenterId must be between 0 and " + MAX_DATACENTER);
        }
        if (workerId < 0 || workerId > MAX_WORKER) {
            throw new IllegalArgumentException("workerId must be between 0 and " + MAX_WORKER);
        }

        this.epochMillis = epochMillis;
        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    @Override
    public long nextId() {
        long currentTs = timestamp();

        while (true) {
            long lastTs = lastTimestamp.get();

            if (currentTs < lastTs) {
                // clock moved backwards -> fail fast
                throw new IllegalStateException("Clock moved backwards. Refusing to generate id for " + (lastTs - currentTs) + "ms");
            }

            if (currentTs == lastTs) {
                long nextSeq = sequence.incrementAndGet() & MAX_SEQUENCE;
                if (nextSeq == 0) {
                    // sequence overflow in same ms -> spin until next ms
                    currentTs = waitNextMillis(currentTs);
                    continue;
                }
                return assemble(currentTs, nextSeq);
            }

            // new millisecond -> reset sequence and try to claim timestamp
            if (lastTimestamp.compareAndSet(lastTs, currentTs)) {
                sequence.set(0L);
                return assemble(currentTs, 0L);
            }

            // CAS failed -> retry
            currentTs = timestamp();
        }
    }

    private long assemble(long timestampMillis, long seq) {
        long delta = timestampMillis - epochMillis;
        if (delta < 0) {
            throw new IllegalStateException("Current time is before epoch");
        }

        return (delta << TIMESTAMP_SHIFT)
            | (datacenterId << DATACENTER_SHIFT)
            | (workerId << WORKER_SHIFT)
            | seq;
    }

    private long waitNextMillis(long currentTs) {
        long ts = timestamp();
        while (ts <= currentTs) {
            ts = timestamp();
        }
        return ts;
    }

    private static long timestamp() {
        return Instant.now().toEpochMilli();
    }

    @Override
    public String toString() {
        return "SnowflakeIdGenerator{" +
            "epochMillis=" + epochMillis +
            ", datacenterId=" + datacenterId +
            ", workerId=" + workerId +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnowflakeIdGenerator that)) return false;
        return epochMillis == that.epochMillis && datacenterId == that.datacenterId && workerId == that.workerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(epochMillis, datacenterId, workerId);
    }
}

