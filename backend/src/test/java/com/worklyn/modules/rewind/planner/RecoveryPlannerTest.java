package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.dto.RecoveryActionResponse;
import com.worklyn.modules.rewind.dto.RecoveryPlanResponse;
import com.worklyn.modules.rewind.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecoveryPlannerTest {

    private RecoveryPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new RecoveryPlanner(new DependencyValidator());
    }

    @Test
    @DisplayName("Non-mutating preview: generates plan for leave cascade failure preserving successful steps")
    void testNonMutatingPreviewLeaveCascade() {
        WorkflowStep step1 = WorkflowStep.builder()
                .stepId("STEP_DEDUCT_LEAVE_BALANCE")
                .actionType(StepActionType.STEP_DEDUCT_LEAVE_BALANCE)
                .status(StepStatus.COMPLETED)
                .isCompensable(true)
                .isRetryable(false)
                .build();

        WorkflowStep step2 = WorkflowStep.builder()
                .stepId("STEP_MARK_ATTENDANCE")
                .actionType(StepActionType.STEP_MARK_ATTENDANCE)
                .dependsOnStepIds(List.of("STEP_DEDUCT_LEAVE_BALANCE"))
                .status(StepStatus.COMPLETED)
                .isCompensable(true)
                .isRetryable(false)
                .build();

        WorkflowStep step3 = WorkflowStep.builder()
                .stepId("STEP_ADJUST_PAYROLL")
                .actionType(StepActionType.STEP_CREATE_PAYROLL_ENTRY)
                .dependsOnStepIds(List.of("STEP_MARK_ATTENDANCE"))
                .status(StepStatus.FAILED)
                .failureReason("Timeout connecting to payroll service")
                .isCompensable(true)
                .isRetryable(true)
                .retryCount(1)
                .maxRetries(3)
                .build();

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowId("WF-1001")
                .workflowType("LEAVE_APPROVAL_CASCADE")
                .status(WorkflowStatus.FAILED)
                .steps(Arrays.asList(step1, step2, step3))
                .build();

        RecoveryPlanResponse plan = planner.planRecovery(instance);

        assertNotNull(plan);
        assertEquals("WF-1001", plan.getWorkflowId());
        assertEquals("STEP_ADJUST_PAYROLL", plan.getFailedStepId());

        // Step 3 is retryable (1/3) -> proposed REPLAY
        RecoveryActionResponse step3Action = plan.getActions().stream()
                .filter(a -> a.getStepId().equals("STEP_ADJUST_PAYROLL"))
                .findFirst().orElseThrow();
        assertEquals("REPLAY", step3Action.getAction());

        // Steps 1 & 2 remain PRESERVE
        RecoveryActionResponse step1Action = plan.getActions().stream()
                .filter(a -> a.getStepId().equals("STEP_DEDUCT_LEAVE_BALANCE"))
                .findFirst().orElseThrow();
        assertEquals("PRESERVE", step1Action.getAction());

        // Verify instance was not mutated!
        assertEquals(StepStatus.FAILED, step3.getStatus());
        assertEquals(1, step3.getRetryCount());
    }

    @Test
    @DisplayName("Non-retryable failure triggers compensation in reverse order")
    void testNonRetryableFailureTriggersCompensation() {
        WorkflowStep step1 = WorkflowStep.builder()
                .stepId("STEP_1")
                .actionType(StepActionType.STEP_DEDUCT_LEAVE_BALANCE)
                .status(StepStatus.COMPLETED)
                .isCompensable(true)
                .build();

        WorkflowStep step2 = WorkflowStep.builder()
                .stepId("STEP_2")
                .actionType(StepActionType.STEP_CREATE_PAYROLL_ENTRY)
                .dependsOnStepIds(List.of("STEP_1"))
                .status(StepStatus.FAILED)
                .failureReason("Fatal account lock")
                .isCompensable(true)
                .isRetryable(false)
                .build();

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowId("WF-ROLLBACK")
                .status(WorkflowStatus.FAILED)
                .steps(Arrays.asList(step1, step2))
                .build();

        RecoveryPlanResponse plan = planner.planRecovery(instance);

        RecoveryActionResponse step2Action = plan.getActions().stream()
                .filter(a -> a.getStepId().equals("STEP_2"))
                .findFirst().orElseThrow();
        assertEquals("COMPENSATE", step2Action.getAction());

        RecoveryActionResponse step1Action = plan.getActions().stream()
                .filter(a -> a.getStepId().equals("STEP_1"))
                .findFirst().orElseThrow();
        assertEquals("COMPENSATE", step1Action.getAction());
    }

    @Test
    @DisplayName("Exhausted retries trigger MANUAL_REVIEW if not compensable")
    void testExhaustedRetriesTriggerManualReview() {
        WorkflowStep step = WorkflowStep.builder()
                .stepId("STEP_FAIL")
                .status(StepStatus.FAILED)
                .isRetryable(true)
                .retryCount(3)
                .maxRetries(3)
                .isCompensable(false)
                .build();

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowId("WF-EXHAUSTED")
                .status(WorkflowStatus.FAILED)
                .steps(List.of(step))
                .build();

        RecoveryPlanResponse plan = planner.planRecovery(instance);

        RecoveryActionResponse action = plan.getActions().get(0);
        assertEquals("MANUAL_REVIEW", action.getAction());
    }
}
