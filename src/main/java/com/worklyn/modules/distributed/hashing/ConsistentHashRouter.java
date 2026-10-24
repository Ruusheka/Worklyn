package com.worklyn.modules.distributed.hashing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Virtual-node consistent hash ring backed by a ConcurrentSkipListMap.
 *
 * <p>Each physical shard is placed on the ring as {@code virtualNodesPerShard} virtual nodes,
 * using deterministic keys of the form {@code shardId + "#vn#" + index}.
 * Keys are routed to the nearest clockwise virtual node (ceiling entry), with wraparound
 * to the first entry when the hash falls past the last position.</p>
 *
 * <p>Important: this router operates at the <em>application layer</em>. It determines
 * logical partition assignment but does NOT perform physical MongoDB data migration.
 * Routing an entity to a new shard after ring modification requires separate data migration work.</p>
 *
 * <p>Thread safety: all operations on the ring use ConcurrentSkipListMap, and shard-set
 * membership is tracked in a ConcurrentHashSet. Concurrent adds/removes/reads are safe.</p>
 *
 * <p>Hash collision handling: when two virtual nodes hash to the same long value, the
 * later insertion probes adjacent slots (+1, +2, ...) to avoid silently dropping nodes.</p>
 */
@Slf4j
@Component
public class ConsistentHashRouter {

    private final int virtualNodesPerShard;
    private final HashFunction hashFunction;

    // ring: hash position -> shardId
    private final ConcurrentSkipListMap<Long, String> ring = new ConcurrentSkipListMap<>();
    // tracks registered physical shard IDs
    private final Set<String> registeredShards = ConcurrentHashMap.newKeySet();

    public ConsistentHashRouter(
            HashFunction hashFunction,
            @Value("${app.hashing.virtual-nodes:150}") int virtualNodesPerShard) {
        if (virtualNodesPerShard <= 0) {
            throw new IllegalArgumentException("virtualNodesPerShard must be positive, got: " + virtualNodesPerShard);
        }
        this.hashFunction = hashFunction;
        this.virtualNodesPerShard = virtualNodesPerShard;
    }

    /**
     * Add a physical shard to the ring with {@code virtualNodesPerShard} virtual nodes.
     *
     * @throws IllegalArgumentException if shardId is blank or already registered
     */
    public void addShard(String shardId) {
        if (shardId == null || shardId.isBlank()) {
            throw new IllegalArgumentException("Shard ID must not be blank");
        }
        if (registeredShards.contains(shardId)) {
            throw new IllegalArgumentException("Shard already registered: " + shardId);
        }

        for (int i = 0; i < virtualNodesPerShard; i++) {
            String vnKey = shardId + "#vn#" + i;
            long hash = hashFunction.hash(vnKey);
            // Probe to avoid collision
            while (ring.containsKey(hash)) {
                hash++;
            }
            ring.put(hash, shardId);
        }
        registeredShards.add(shardId);
        log.debug("Added shard [{}] with {} virtual nodes. Ring size: {}", shardId, virtualNodesPerShard, ring.size());
    }

    /**
     * Remove a physical shard and all its virtual nodes from the ring.
     *
     * @throws IllegalStateException if removing this shard would leave the ring empty
     */
    public void removeShard(String shardId) {
        if (shardId == null || shardId.isBlank()) {
            throw new IllegalArgumentException("Shard ID must not be blank");
        }
        if (!registeredShards.contains(shardId)) {
            throw new IllegalArgumentException("Shard not registered: " + shardId);
        }
        if (registeredShards.size() == 1) {
            throw new IllegalStateException("Cannot remove the last shard from the ring: " + shardId);
        }

        ring.entrySet().removeIf(entry -> shardId.equals(entry.getValue()));
        registeredShards.remove(shardId);
        log.debug("Removed shard [{}]. Ring size: {}", shardId, ring.size());
    }

    /**
     * Route a key to a shard using clockwise lookup.
     *
     * @return the shard ID responsible for this key
     * @throws IllegalStateException if the ring is empty
     */
    public String getShardForKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Routing key must not be blank");
        }
        if (ring.isEmpty()) {
            throw new IllegalStateException("Hash ring is empty — no shards registered");
        }

        long hash = hashFunction.hash(key);
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            // Wraparound: key hash is past the last position
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * @return an immutable snapshot of the ring for diagnostics. Safe to inspect without locking.
     */
    public Map<Long, String> getRingSnapshot() {
        return Collections.unmodifiableMap(new ConcurrentSkipListMap<>(ring));
    }

    /** @return the number of registered physical shards */
    public int getShardCount() {
        return registeredShards.size();
    }

    /** @return the set of registered shard IDs (unmodifiable view) */
    public Set<String> getRegisteredShards() {
        return Collections.unmodifiableSet(registeredShards);
    }
}
