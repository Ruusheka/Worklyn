package com.worklyn.modules.distributed.system;

import com.worklyn.modules.distributed.sharding.ShardRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Diagnostic endpoints for inspecting the consistent hash ring state.
 * Restricted to ROLE_ADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system")
public class SystemDiagnosticController {

    private final ShardRouterService shardRouterService;

    /**
     * GET /api/v1/system/hash-ring
     * Returns an immutable snapshot of the current hash ring for diagnostics.
     */
    @GetMapping("/hash-ring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HashRingSnapshot> getHashRingSnapshot() {
        Map<Long, String> snapshot = shardRouterService.getRingDiagnosticSnapshot();
        Set<String> shards = shardRouterService.getRegisteredShards();
        return ResponseEntity.ok(new HashRingSnapshot(shards, snapshot.size(), snapshot));
    }

    public record HashRingSnapshot(Set<String> registeredShards, int totalVirtualNodes, Map<Long, String> ring) {}
}
