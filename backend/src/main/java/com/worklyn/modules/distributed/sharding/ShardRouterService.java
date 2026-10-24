package com.worklyn.modules.distributed.sharding;

import com.worklyn.modules.distributed.hashing.ConsistentHashRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Application-level partition-routing service that wraps the {@link ConsistentHashRouter}.
 *
 * <p><strong>Logical vs Physical Sharding:</strong> This service assigns logical shard labels
 * to entity keys (e.g., employee IDs, tenant IDs) using consistent hashing.
 * It does NOT automatically move MongoDB documents between physical database nodes.
 * Actual data migration after a ring change requires a separate ETL process outside this service.</p>
 *
 * <p>Routing is deterministic for an unchanged shard configuration: the same key always maps
 * to the same shard as long as the ring topology is stable.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShardRouterService {

    private final ConsistentHashRouter router;

    /**
     * Register a shard in the ring.
     *
     * @param shardId unique shard identifier (must not be blank or duplicate)
     * @throws IllegalArgumentException if shardId is blank or already registered
     */
    public void registerShard(String shardId) {
        if (shardId == null || shardId.isBlank()) {
            throw new IllegalArgumentException("Shard ID must not be blank");
        }
        router.addShard(shardId);
        log.info("Registered shard [{}] in routing ring", shardId);
    }

    /**
     * Remove a shard from the ring.
     *
     * @param shardId the shard to remove
     * @throws IllegalArgumentException if shardId is blank or not registered
     * @throws IllegalStateException    if removing this shard would empty the ring
     */
    public void deregisterShard(String shardId) {
        if (shardId == null || shardId.isBlank()) {
            throw new IllegalArgumentException("Shard ID must not be blank");
        }
        router.removeShard(shardId);
        log.info("Deregistered shard [{}] from routing ring", shardId);
    }

    /**
     * Resolve a logical key (e.g., an employee ID or tenant key) to its responsible shard.
     *
     * @param key the entity key to route
     * @return the shard ID responsible for storing or processing this entity
     * @throws IllegalArgumentException if key is blank
     * @throws IllegalStateException    if no shards are registered
     */
    public String resolveShardForKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Routing key must not be blank");
        }
        String shardId = router.getShardForKey(key);
        log.trace("Key [{}] routed to shard [{}]", key, shardId);
        return shardId;
    }

    /**
     * @return an immutable diagnostic snapshot of the hash ring positions to shard IDs.
     */
    public Map<Long, String> getRingDiagnosticSnapshot() {
        return router.getRingSnapshot();
    }

    /**
     * @return the number of currently registered physical shards
     */
    public int getShardCount() {
        return router.getShardCount();
    }

    /**
     * @return the set of currently registered shard IDs
     */
    public Set<String> getRegisteredShards() {
        return router.getRegisteredShards();
    }
}
