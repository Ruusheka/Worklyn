package com.worklyn.modules.rewind.controller;

import com.worklyn.common.exception.BadRequestException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.rewind.dto.*;
import com.worklyn.modules.rewind.model.WorkflowInstance;
import com.worklyn.modules.rewind.planner.RecoveryPlanner;
import com.worklyn.modules.rewind.repository.WorkflowInstanceRepository;
import com.worklyn.modules.rewind.service.RecoveryExecutor;
import com.worklyn.modules.rewind.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HR-REWIND Recovery Engine REST Controller.
 * Restricts administrative recovery operations to ROLE_ADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rewind")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowInstanceRepository workflowRepository;
    private final RecoveryPlanner recoveryPlanner;
    private final RecoveryExecutor recoveryExecutor;

    /**
     * POST /api/v1/rewind/workflows
     * Register a new multi-step DAG workflow.
     */
    @PostMapping("/workflows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowResponse> registerWorkflow(
            @Valid @RequestBody WorkflowRegistrationRequest request) {
        WorkflowResponse response = workflowService.registerWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/rewind/workflows
     * List all tracked workflows.
     */
    @GetMapping("/workflows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkflowResponse>> listWorkflows() {
        return ResponseEntity.ok(workflowService.listWorkflows());
    }

    /**
     * GET /api/v1/rewind/workflows/{id}
     * Get detailed workflow step and DAG status.
     */
    @GetMapping("/workflows/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable("id") String id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    /**
     * GET /api/v1/rewind/workflows/{id}/preview
     * Generates a non-mutating recovery plan.
     */
    @GetMapping("/workflows/{id}/preview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecoveryPlanResponse> previewRecovery(@PathVariable("id") String id) {
        WorkflowInstance instance = workflowRepository.findByWorkflowId(id)
                .orElseThrow(() -> new ResourceNotFoundException("WORKFLOW_NOT_FOUND", "Workflow not found: " + id));

        RecoveryPlanResponse plan = recoveryPlanner.planRecovery(instance);
        return ResponseEntity.ok(plan);
    }

    /**
     * POST /api/v1/rewind/workflows/{id}/recover
     * Execute dependency-aware recovery under distributed lock and idempotency.
     */
    @PostMapping("/workflows/{id}/recover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecoveryResultResponse> executeRecovery(
            @PathVariable("id") String id,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("MISSING_IDEMPOTENCY_KEY", "X-Idempotency-Key header is required for recovery operations");
        }

        String actorId = (principal != null) ? principal.getId() : "ADMIN_SYSTEM";
        RecoveryResultResponse result = recoveryExecutor.executePlan(id, actorId, idempotencyKey);
        return ResponseEntity.ok(result);
    }
}
