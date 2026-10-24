package com.worklyn.modules.distributed.idgen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.function.LongSupplier;

/**
 * Snowflake-style 64-bit distributed unique ID generator.
 *
 * <p><strong>Bit layout (64-bit signed long):</strong>
 * <pre>
 *  Bit 63:      Unused sign bit (always 0)
 *  Bits 62–22:  41-bit timestamp relative to Worklyn epoch (2026-01-01T00:00:00Z = 1767225600000L ms)
 *  Bits 21–17:  5-bit datacenter ID (0–31)
 *  Bits 16–12:  5-bit worker ID (0–31)
 *  Bits 11–0:   12-bit sequence counter (0–4095 per millisecond)
 * </pre>
 *
 * <p><strong>Global uniqueness guarantee:</strong> IDs are unique across multiple instances only when
 * each instance is assigned a distinct (datacenterId, workerId) pair through configuration or
 * a coordinated allocation mechanism. Collisions can occur if two instances share the same pair.</p>
 *
 * <p><strong>Clock rollback policy:</strong>
 * <ul>
 *   <li>Drift &lt; 10 ms: safe wait/spin until the clock catches up.</li>
 *   <li>Drift &ge; 10 ms: throws {@code IllegalStateException} to prevent duplicate IDs.</li>
 * </ul>
 *
 * <p><strong>Throughput:</strong> Up to 4,096,000 unique IDs per second per instance.</p>
 */
@Slf4j
@Component
public class SnowflakeIdGenerator {

    /** Worklyn custom epoch: 2026-01-01T00:00:00Z */
    public static final long WORKLYN_EPOCH = 1767225600000L;

    private static final int DATACENTER_BITS = 5;
    private static final int WORKER_BITS = 5;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS); // 31
    private static final long MAX_WORKER_ID     = ~(-1L << WORKER_BITS);     // 31
    private static final long MAX_SEQUENCE      = ~(-1L << SEQUENCE_BITS);   // 4095

    private static final int WORKER_SHIFT      = SEQUENCE_BITS;
    private static final int DATACENTER_SHIFT  = SEQUENCE_BITS + WORKER_BITS;
    private static final int TIMESTAMP_SHIFT   = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    private final long datacenterId;
    private final long workerId;
    private final LongSupplier clock;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    /** Standard constructor used by Spring (reads from SnowflakeProperties). */
    @org.springframework.beans.factory.annotation.Autowired
    public SnowflakeIdGenerator(SnowflakeProperties props) {
        this(props != null ? props.getDatacenterId() : 1, props != null ? props.getWorkerId() : 1, System::currentTimeMillis);
    }

    /**
     * Test-friendly constructor that accepts an injectable clock.
     *
     * @param datacenterId 0–31
     * @param workerId     0–31
     * @param clock        millisecond timestamp supplier (injectable for deterministic tests)
     */
    public SnowflakeIdGenerator(int datacenterId, int workerId, LongSupplier clock) {
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    "datacenterId must be in [0, " + MAX_DATACENTER_ID + "], got: " + datacenterId);
        }
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "workerId must be in [0, " + MAX_WORKER_ID + "], got: " + workerId);
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.datacenterId = datacenterId;
        this.workerId = workerId;
        this.clock = clock;
    }

    /**
     * Generate the next monotonic 64-bit ID. Thread-safe.
     *
     * @return a unique Snowflake ID
     * @throws IllegalStateException if the clock rolls back by &ge; 10 ms
     */
    public synchronized long nextId() {
        long timestamp = currentMs();

        if (timestamp < lastTimestamp) {
            long drift = lastTimestamp - timestamp;
            if (drift <= 10L) {
                // Safe: spin-wait for the clock to catch up
                log.warn("Clock moved backwards by {} ms — waiting for next millisecond", drift);
                timestamp = waitForNextMs(lastTimestamp);
            } else {
                throw new IllegalStateException(
                        "NTP Clock moved backwards by " + drift + " ms; refusing to generate IDs");
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Sequence exhausted within this millisecond; wait for next ms
                timestamp = waitForNextMs(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        long elapsed = timestamp - WORKLYN_EPOCH;
        if (elapsed < 0) {
            throw new IllegalStateException(
                    "System clock is before the Worklyn epoch (2026-01-01). Cannot generate IDs.");
        }
        // Check 41-bit overflow (~69 years from epoch)
        if (elapsed >= (1L << 41)) {
            throw new IllegalStateException("Snowflake timestamp bits exhausted. The Worklyn epoch must be updated.");
        }

        return (elapsed << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    /**
     * Generate the next ID as a string.
     * Use this for JSON serialization to avoid JavaScript number precision loss for large longs.
     */
    public String nextIdString() {
        return Long.toString(nextId());
    }

    private long currentMs() {
        return clock.getAsLong();
    }

    private long waitForNextMs(long lastTs) {
        long ts = currentMs();
        int safetyCounter = 0;
        while (ts <= lastTs) {
            Thread.onSpinWait();
            ts = currentMs();
            if (++safetyCounter > 100_000) {
                throw new IllegalStateException("Clock appears stalled; waited too long for next millisecond");
            }
        }
        return ts;
    }
}
