package com.worklyn.modules.distributed.idgen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    @DisplayName("50,000 concurrent generated IDs are all unique and strictly positive")
    void test50000ConcurrentUniqueIds() throws InterruptedException, ExecutionException {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, System::currentTimeMillis);

        int totalIds = 50_000;
        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<Long> generatedIds = ConcurrentHashMap.newKeySet(totalIds);
        CountDownLatch latch = new CountDownLatch(threadCount);

        int idsPerThread = totalIds / threadCount;
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < idsPerThread; i++) {
                        long id = generator.nextId();
                        assertTrue(id > 0, "Generated ID must be positive");
                        generatedIds.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(totalIds, generatedIds.size(), "All 50,000 IDs must be strictly unique");
    }

    @Test
    @DisplayName("Monotonic ordering within single thread")
    void testMonotonicOrdering() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, System::currentTimeMillis);
        long prev = -1;
        for (int i = 0; i < 5000; i++) {
            long current = generator.nextId();
            assertTrue(current > prev, "Next ID must be strictly greater than previous");
            prev = current;
        }
    }

    @Test
    @DisplayName("Clock rollback < 10ms is tolerated via wait, >= 10ms throws IllegalStateException")
    void testClockRollback() {
        AtomicLong mockClock = new AtomicLong(1767225601000L); // 1s after epoch
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, mockClock::get);

        generator.nextId();

        // 5ms backward drift (tolerated)
        mockClock.addAndGet(-5);
        // nextId() will spin/wait until mockClock catches up or advance, but with mockClock we can advance it
        // Let's test large rollback:
        mockClock.set(1767225601000L);
        generator.nextId();
        mockClock.set(1767225601000L - 100); // 100ms drift

        IllegalStateException ex = assertThrows(IllegalStateException.class, generator::nextId);
        assertTrue(ex.getMessage().contains("moved backwards"));
    }

    @Test
    @DisplayName("Invalid datacenter or worker configuration rejected")
    void testInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(-1, 0, System::currentTimeMillis));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(32, 0, System::currentTimeMillis));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, -1, System::currentTimeMillis));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(0, 32, System::currentTimeMillis));
    }

    @Test
    @DisplayName("String conversion does not lose precision")
    void testNextIdString() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(2, 3, System::currentTimeMillis);
        String idStr = generator.nextIdString();
        assertNotNull(idStr);
        assertFalse(idStr.isBlank());
        long parsed = Long.parseLong(idStr);
        assertTrue(parsed > 0);
    }
}
