package com.worklyn.modules.distributed.hashing;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Deterministic 64-bit Murmur3 hash function implementation.
 * Uses the MurmurHash3 x64 128-bit variant, returning only the first 64 bits.
 * This provides uniform distribution across the 64-bit signed long space.
 *
 * <p>This is NOT Java's Object.hashCode(). It is a standalone algorithm adapted
 * from Austin Appleby's original C implementation.</p>
 */
@Component
public class Murmur3HashFunction implements HashFunction {

    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int SEED = 0;

    @Override
    public long hash(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Hash key must not be null");
        }
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        return murmurhash3_x64_128_first64(data, SEED);
    }

    /**
     * Computes the first 64 bits of the Murmur3 x64 128-bit hash.
     */
    private static long murmurhash3_x64_128_first64(byte[] data, long seed) {
        final int length = data.length;
        final int nblocks = length >> 4; // 16-byte blocks

        long h1 = seed;
        long h2 = seed;

        // Body - process 16 bytes at a time
        for (int i = 0; i < nblocks; i++) {
            int base = i * 16;
            long k1 = getLong(data, base);
            long k2 = getLong(data, base + 8);

            k1 *= C1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729L;

            k2 *= C2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5L;
        }

        // Tail - process remaining bytes
        int tailBase = nblocks * 16;
        long k1 = 0;
        long k2 = 0;

        switch (length & 15) {
            case 15: k2 ^= ((long) data[tailBase + 14] & 0xff) << 48;
            case 14: k2 ^= ((long) data[tailBase + 13] & 0xff) << 40;
            case 13: k2 ^= ((long) data[tailBase + 12] & 0xff) << 32;
            case 12: k2 ^= ((long) data[tailBase + 11] & 0xff) << 24;
            case 11: k2 ^= ((long) data[tailBase + 10] & 0xff) << 16;
            case 10: k2 ^= ((long) data[tailBase + 9] & 0xff) << 8;
            case 9:
                k2 ^= ((long) data[tailBase + 8] & 0xff);
                k2 *= C2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= C1;
                h2 ^= k2;
            case 8:  k1 ^= ((long) data[tailBase + 7] & 0xff) << 56;
            case 7:  k1 ^= ((long) data[tailBase + 6] & 0xff) << 48;
            case 6:  k1 ^= ((long) data[tailBase + 5] & 0xff) << 40;
            case 5:  k1 ^= ((long) data[tailBase + 4] & 0xff) << 32;
            case 4:  k1 ^= ((long) data[tailBase + 3] & 0xff) << 24;
            case 3:  k1 ^= ((long) data[tailBase + 2] & 0xff) << 16;
            case 2:  k1 ^= ((long) data[tailBase + 1] & 0xff) << 8;
            case 1:
                k1 ^= ((long) data[tailBase] & 0xff);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= C2;
                h1 ^= k1;
            default:
                break;
        }

        // Finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        // h2 += h1; // second 64 bits, not needed

        return h1;
    }

    private static long getLong(byte[] data, int offset) {
        return ((long) (data[offset] & 0xff))
                | ((long) (data[offset + 1] & 0xff) << 8)
                | ((long) (data[offset + 2] & 0xff) << 16)
                | ((long) (data[offset + 3] & 0xff) << 24)
                | ((long) (data[offset + 4] & 0xff) << 32)
                | ((long) (data[offset + 5] & 0xff) << 40)
                | ((long) (data[offset + 6] & 0xff) << 48)
                | ((long) (data[offset + 7] & 0xff) << 56);
    }

    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
}
