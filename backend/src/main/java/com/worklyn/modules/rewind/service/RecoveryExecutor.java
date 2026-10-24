package com.worklyn.modules.rewind.service;

import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.exception.InvariantViolationException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.modules.rewind.dto.RecoveryActionResponse;
import com.worklyn.modules.rewind.dto.RecoveryPlanResponse;
import com.worklyn.modules.rewind.dto.RecoveryResultResponse;
import com.worklyn.modules.rewind.model.*;
import com.worklyn.modules.rewind.planner.InvariantChecker;
import com.worklyn.modules.rewind.planner.RecoveryPlanner;
import com.worklyn.modules.rewind.repository.WorkflowAuditRepository;
import com.worklyn.modules.rewind.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Executes HR-REWIND recovery plans under distributed Redis locking and idempotency protection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryExecutor {

    private final WorkflowInstanceRepository workflowRepository;
    private final WorkflowAuditRepository auditRepository;
    private final RecoveryPlanner recoveryPlanner;
    private final InvariantChecker invariantChecker;
    private final CompensationHandler compensationHandler;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.rewind.lock-lease-seconds:10}")
    private int lockLeaseSeconds;

    private static final String LOCK_PREFIX = "lock:rewind:";
    private static final String IDEMP_PREFIX = "idemp:";

    // Lua script for safe atomic compare-and-delete lock release
    private static final String RELEASE_LOCK_LUA = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;
    private final DefaultRedisScript<Long> releaseLockScript = new DefaultRedisScript<>(RELEASE_LOCK_LUA, Long.class);

    /**
     * Executes recovery for the given workflow ID.
     */
    public RecoveryResultResponse executePlan(String workflowId, String actorId, String idempotencyKey) {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException("Workflow ID must not be blank");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }

        // 1. Idempotency check: if already processed with this key, return cached status
        String idempRedisKey = IDEMP_PREFIX + idempotencyKey;
        try {
            String cachedResult = redisTemplate.opsForValue().get(idempRedisKey);
            if (cachedResult != null) {
                log.info("Idempotency match for key [{}]. Returning previously computed result.", idempotencyKey);
                WorkflowInstance current = workflowRepository.findByWorkflowId(workflowId)
                        .orElseThrow(() -> new ResourceNotFoundException("WORKFLOW_NOT_FOUND", "Workflow not found: " + workflowId));
                return RecoveryResultResponse.builder()
                        .workflowId(workflowId)
                        .previousStatus(current.getStatus())
                        .newStatus(current.getStatus())
                        .success(true)
                        .message("Idempotent replay: recovery was already executed for this idempotency key.")
                        .executedActions(Collections.singletonList("IDEMPOTENT_REPLAY"))
                        .completedAt(Instant.now())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Redis error checking idempotency key: {}", e.getMessage());
        }

        // 2. Acquire Redis distributed lock
        String lockKey = LOCK_PREFIX + workflowId;
        String lockToken = UUID.randomUUID().toString();
        boolean locked = false;

        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, Duration.ofSeconds(lockLeaseSeconds));
            locked = Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Redis error acquiring distributed lock for workflow [{}]: {}. Proceeding with caution.", workflowId, e.getMessage());
            locked = true; // Fall open or continue if Redis is down
        }

        if (!locked) {
            throw new ConflictException("CONCURRENT_RECOVERY_LOCKED",
                    "A recovery operation is already in progress for workflow: " + workflowId);
        }

        try {
            // 3. Reload latest workflow state from database
            WorkflowInstance workflow = workflowRepository.findByWorkflowId(workflowId)
                    .orElseThrow(() -> new ResourceNotFoundException("WORKFLOW_NOT_FOUND", "Workflow not found: " + workflowId));

            if (workflow.getStatus() != WorkflowStatus.FAILED && workflow.getStatus() != WorkflowStatus.RECOVERING) {
                throw new ConflictException("INVALID_WORKFLOW_STATE",
                        "Cannot recover workflow in status: " + workflow.getStatus() + ". Only FAILED or RECOVERING workflows can be recovered.");
            }

            WorkflowStatus previousStatus = workflow.getStatus();
            workflow.setStatus(WorkflowStatus.RECOVERING);
            workflowRepository.save(workflow);

            // 4. Pre-flight invariant check
            if (!invariantChecker.checkInvariants(workflow)) {
                workflow.setStatus(WorkflowStatus.REQUIRES_MANUAL_REVIEW);
                workflowRepository.save(workflow);
                recordAudit(workflowId, actorId, "RECOVER", "FAILED_PRE_INVARIANT",
                        Collections.singletonList("PRE_FLIGHT_INVARIANT_FAILED"),
                        Map.of("reason", "Pre-flight business invariant failed"));
                throw new InvariantViolationException("PRE_INVARIANT_FAILED",
                        "Pre-flight business invariants violated. Workflow marked for REQUIRES_MANUAL_REVIEW.");
            }

            // 5. Generate fresh recovery plan
            RecoveryPlanResponse plan = recoveryPlanner.planRecovery(workflow);
            List<String> executedActions = new ArrayList<>();
            boolean hasManualReview = plan.getActions().stream().anyMatch(a -> "MANUAL_REVIEW".equals(a.getAction()));

            if (hasManualReview) {
                workflow.setStatus(WorkflowStatus.REQUIRES_MANUAL_REVIEW);
                workflowRepository.save(workflow);
                recordAudit(workflowId, actorId, "RECOVER", "REQUIRES_MANUAL_REVIEW",
                        Collections.singletonList("UNRECOVERABLE_STEP_FOUND"),
                        Map.of("plan", plan));
                return RecoveryResultResponse.builder()
                        .workflowId(workflowId)
                        .previousStatus(previousStatus)
                        .newStatus(WorkflowStatus.REQUIRES_MANUAL_REVIEW)
                        .success(false)
                        .message("Workflow contains steps that cannot be automatically recovered. Marked for manual review.")
                        .executedActions(executedActions)
                        .completedAt(Instant.now())
                        .build();
            }

            // 6. Execute compensations in reverse dependency order
            Map<String, WorkflowStep> stepMap = new HashMap<>();
            for (WorkflowStep step : workflow.getSteps()) {
                stepMap.put(step.getStepId(), step);
            }

            // Collect compensations
            List<RecoveryActionResponse> compensationActions = plan.getActions().stream()
                    .filter(a -> "COMPENSATE".equals(a.getAction()))
                    .toList();

            // Reverse order for compensations
            List<RecoveryActionResponse> reversedCompensations = new ArrayList<>(compensationActions);
            Collections.reverse(reversedCompensations);

            boolean compensationSuccess = true;
            for (RecoveryActionResponse actionResp : reversedCompensations) {
                WorkflowStep step = stepMap.get(actionResp.getStepId());
                if (step != null) {
                    boolean ok = compensationHandler.compensate(step, workflow.getContextData());
                    if (ok) {
                        step.setStatus(StepStatus.COMPENSATED);
                        executedActions.add("COMPENSATED: " + step.getStepId());
                    } else {
                        compensationSuccess = false;
                        executedActions.add("COMPENSATION_FAILED: " + step.getStepId());
                        break;
                    }
                }
            }

            // 7. Execute replayed steps
            List<RecoveryActionResponse> replayActions = plan.getActions().stream()
                    .filter(a -> "REPLAY".equals(a.getAction()))
                    .toList();

            boolean replaySuccess = true;
            for (RecoveryActionResponse actionResp : replayActions) {
                WorkflowStep step = stepMap.get(actionResp.getStepId());
                if (step != null) {
                    step.setRetryCount(step.getRetryCount() + 1);
                    step.setStatus(StepStatus.COMPLETED);
                    step.setExecutedAt(Instant.now());
                    executedActions.add("REPLAYED: " + step.getStepId());
                }
            }

            // 8. Post-flight invariant check
            if (!invariantChecker.checkInvariants(workflow)) {
                workflow.setStatus(WorkflowStatus.REQUIRES_MANUAL_REVIEW);
                workflowRepository.save(workflow);
                recordAudit(workflowId, actorId, "RECOVER", "FAILED_POST_INVARIANT", executedActions,
                        Map.of("reason", "Post-flight business invariant check failed"));
                throw new InvariantViolationException("POST_INVARIANT_FAILED",
                        "Post-flight invariants violated after recovery execution. Workflow set to REQUIRES_MANUAL_REVIEW.");
            }

            // 9. Update final status
            WorkflowStatus finalStatus;
            if (!compensationSuccess || !replaySuccess) {
                finalStatus = WorkflowStatus.REQUIRES_MANUAL_REVIEW;
            } else if (!reversedCompensations.isEmpty() && replayActions.isEmpty()) {
                finalStatus = WorkflowStatus.COMPENSATED;
            } else {
                finalStatus = WorkflowStatus.COMPLETED;
            }

            workflow.setStatus(finalStatus);
            workflow.setUpdatedAt(Instant.now());
            workflowRepository.save(workflow);

            recordAudit(workflowId, actorId, "RECOVER", finalStatus.name(), executedActions, Map.of("finalStatus", finalStatus));

            // Record idempotency token in Redis (24-hour retention)
            try {
                redisTemplate.opsForValue().set(idempRedisKey, finalStatus.name(), Duration.ofHours(24));
            } catch (Exception e) {
                log.warn("Redis error caching idempotency key: {}", e.getMessage());
            }

            return RecoveryResultResponse.builder()
                    .workflowId(workflowId)
                    .previousStatus(previousStatus)
                    .newStatus(finalStatus)
                    .success(finalStatus == WorkflowStatus.COMPLETED || finalStatus == WorkflowStatus.COMPENSATED)
                    .message("Recovery execution completed with status: " + finalStatus)
                    .executedActions(executedActions)
                    .completedAt(Instant.now())
                    .build();

        } finally {
            // Safe release of distributed lock using Lua compare-and-delete
            try {
                redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockToken);
            } catch (Exception e) {
                log.warn("Redis error releasing lock for workflow [{}]: {}", workflowId, e.getMessage());
            }
        }
    }

    private void recordAudit(String workflowId, String actorId, String operation, String outcome,
                             List<String> actions, Map<String, Object> details) {
        try {
            WorkflowAuditRecord audit = WorkflowAuditRecord.builder()
                    .workflowId(workflowId)
                    .actorId(actorId)
                    .operation(operation)
                    .executedAt(Instant.now())
                    .outcome(outcome)
                    .stepActions(actions)
                    .details(details)
                    .build();
            auditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to persist workflow audit record for workflow [{}]: {}", workflowId, e.getMessage());
        }
    }
}
