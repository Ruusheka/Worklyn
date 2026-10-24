package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.dto.RecoveryActionResponse;
import com.worklyn.modules.rewind.dto.RecoveryPlanResponse;
import com.worklyn.modules.rewind.model.StepStatus;
import com.worklyn.modules.rewind.model.WorkflowInstance;
import com.worklyn.modules.rewind.model.WorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Deterministic, non-mutating recovery plan generator.
 * Evaluates the failed workflow DAG and proposes safe recovery actions.
 */
@Component
@RequiredArgsConstructor
public class RecoveryPlanner {

    private final DependencyValidator dependencyValidator;

    /**
     * Generate a read-only recovery plan for the workflow instance.
     * Guaranteed to never mutate the workflow instance or database records.
     */
    public RecoveryPlanResponse planRecovery(WorkflowInstance workflow) {
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow instance must not be null");
        }

        List<WorkflowStep> steps = workflow.getSteps() != null ? workflow.getSteps() : Collections.emptyList();
        List<WorkflowStep> sortedSteps = steps.isEmpty() ? Collections.emptyList() : dependencyValidator.validateDAG(steps);

        Map<String, WorkflowStep> stepMap = new HashMap<>();
        for (WorkflowStep step : sortedSteps) {
            stepMap.put(step.getStepId(), step);
        }

        // Identify failed steps
        List<WorkflowStep> failedSteps = sortedSteps.stream()
                .filter(s -> s.getStatus() == StepStatus.FAILED)
                .toList();

        String primaryFailedStepId = failedSteps.isEmpty() ? null : failedSteps.get(0).getStepId();
        String primaryFailureReason = failedSteps.isEmpty() ? null : failedSteps.get(0).getFailureReason();

        // Find all downstream dependents of failed steps
        Set<String> downstreamOfFailed = new HashSet<>();
        for (WorkflowStep failed : failedSteps) {
            collectDownstream(failed.getStepId(), sortedSteps, downstreamOfFailed);
        }

        // Determine if any failed step requires overall workflow rollback
        boolean requiresRollback = sortedSteps.stream().anyMatch(step ->
                step.getStatus() == StepStatus.FAILED && (!step.isRetryable() || step.getRetryCount() >= step.getMaxRetries()) && step.isCompensable()
        );

        List<RecoveryActionResponse> actions = new ArrayList<>();

        // Determine actions for all steps
        for (WorkflowStep step : sortedSteps) {
            if (step.getStatus() == StepStatus.FAILED) {
                if (step.isRetryable() && step.getRetryCount() < step.getMaxRetries()) {
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("REPLAY")
                            .reason("Step is marked idempotent and has retry attempts remaining ("
                                    + (step.getRetryCount() + 1) + "/" + step.getMaxRetries() + ").")
                            .build());
                } else if (!step.isRetryable() && step.isCompensable()) {
                    requiresRollback = true;
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("COMPENSATE")
                            .reason("Step is non-retryable or has exhausted retries; reversible side-effect marked for compensation.")
                            .build());
                } else {
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("MANUAL_REVIEW")
                            .reason("Step is non-retryable and non-compensable or retry limit exhausted; flagged for manual review.")
                            .build());
                }
            } else if (downstreamOfFailed.contains(step.getStepId())) {
                if (step.getStatus() == StepStatus.PENDING) {
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("SKIP")
                            .reason("Prerequisite step failed; downstream pending step skipped.")
                            .build());
                }
            } else if (step.getStatus() == StepStatus.COMPLETED) {
                // Upstream completed step
                if (requiresRollback && step.isCompensable()) {
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("COMPENSATE")
                            .reason("Upstream side-effect requires reversal during overall workflow compensation.")
                            .build());
                } else {
                    actions.add(RecoveryActionResponse.builder()
                            .stepId(step.getStepId())
                            .action("PRESERVE")
                            .reason("Step completed successfully and is independent of failure.")
                            .build());
                }
            } else if (step.getStatus() == StepStatus.COMPENSATED) {
                actions.add(RecoveryActionResponse.builder()
                        .stepId(step.getStepId())
                        .action("PRESERVE")
                        .reason("Step has already been compensated.")
                        .build());
            }
        }

        return RecoveryPlanResponse.builder()
                .workflowId(workflow.getWorkflowId())
                .workflowType(workflow.getWorkflowType())
                .status(workflow.getStatus())
                .failedStepId(primaryFailedStepId)
                .failureReason(primaryFailureReason)
                .actions(actions)
                .invariantsSatisfied(true)
                .build();
    }

    private void collectDownstream(String parentId, List<WorkflowStep> sortedSteps, Set<String> downstream) {
        for (WorkflowStep step : sortedSteps) {
            if (step.getDependsOnStepIds() != null && step.getDependsOnStepIds().contains(parentId)) {
                if (downstream.add(step.getStepId())) {
                    collectDownstream(step.getStepId(), sortedSteps, downstream);
                }
            }
        }
    }
}
