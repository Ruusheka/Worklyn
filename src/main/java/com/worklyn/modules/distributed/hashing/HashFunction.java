package com.worklyn.modules.distributed.hashing;

/**
 * 64-bit hash function abstraction.
 * Used by the consistent hash ring to deterministically map string keys to positions.
 */
public interface HashFunction {
    long hash(String key);
}
