package com.worklyn.modules.distributed.hashing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashRouterTest {

    private ConsistentHashRouter router;
    private Murmur3HashFunction hashFunction;

    @BeforeEach
    void setUp() {
        hashFunction = new Murmur3HashFunction();
        router = new ConsistentHashRouter(hashFunction, 150);
    }

    @Test
    @DisplayName("Deterministic routing: routing the same key 100 times returns the identical shard")
    void testDeterministicRouting() {
        router.addShard("shard-alpha");
        router.addShard("shard-beta");
        router.addShard("shard-gamma");

        String initialShard = router.getShardForKey("EMP-101");
        assertNotNull(initialShard);

        for (int i = 0; i < 100; i++) {
            assertEquals(initialShard, router.getShardForKey("EMP-101"), "Route drifted at iteration " + i);
        }
    }

    @Test
    @DisplayName("Reject duplicate and blank shard registration")
    void testInvalidShardRegistration() {
        router.addShard("shard-alpha");

        assertThrows(IllegalArgumentException.class, () -> router.addShard("shard-alpha"));
        assertThrows(IllegalArgumentException.class, () -> router.addShard(""));
        assertThrows(IllegalArgumentException.class, () -> router.addShard(null));
    }

    @Test
    @DisplayName("Reject empty ring queries and removing the last shard")
    void testEmptyRingAndLastShardRemoval() {
        assertThrows(IllegalStateException.class, () -> router.getShardForKey("some-key"));

        router.addShard("shard-only");
        assertThrows(IllegalStateException.class, () -> router.removeShard("shard-only"));
    }

    @Test
    @DisplayName("Snapshot immutability test")
    void testSnapshotImmutability() {
        router.addShard("shard-1");
        Map<Long, String> snapshot = router.getRingSnapshot();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.put(999L, "rogue-shard"));
    }

    @Test
    @DisplayName("Shard addition: adding 4th shard remaps approximately 25% of keys")
    void testMinimalRemapping() {
        router.addShard("shard-1");
        router.addShard("shard-2");
        router.addShard("shard-3");

        int sampleSize = 10_000;
        Map<String, String> initialRoutes = new HashMap<>();
        for (int i = 0; i < sampleSize; i++) {
            String key = "test-key-" + i;
            initialRoutes.put(key, router.getShardForKey(key));
        }

        router.addShard("shard-4");

        int remappedCount = 0;
        for (int i = 0; i < sampleSize; i++) {
            String key = "test-key-" + i;
            String newRoute = router.getShardForKey(key);
            if (!newRoute.equals(initialRoutes.get(key))) {
                remappedCount++;
                assertEquals("shard-4", newRoute, "Remapped key should map to the new shard");
            }
        }

        double remappedRatio = (double) remappedCount / sampleSize;
        // Statistically ~25% (allowing +/- 6% variance due to hash distribution)
        assertTrue(remappedRatio >= 0.18 && remappedRatio <= 0.32,
                "Expected remapping ~25%, but got: " + (remappedRatio * 100) + "%");
    }

    @Test
    @DisplayName("Concurrent reads and shard routing are thread-safe")
    void testConcurrency() throws InterruptedException {
        router.addShard("shard-A");
        router.addShard("shard-B");

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 1000; i++) {
                        String shard = router.getShardForKey("key-" + threadId + "-" + i);
                        assertNotNull(shard);
                    }
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        assertEquals(threadCount, successCount.get());
    }
}
