package com.worklyn.modules.distributed.sharding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Describes a logical shard (partition) in the application-level shard routing layer.
 * This is NOT a MongoDB physical shard descriptor; it is a logical routing label.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShardDescriptor {
    /** Unique logical shard identifier, e.g. "shard-alpha" */
    private String shardId;
    /** Optional human-readable description of this shard's purpose or tenant range */
    private String description;
}
