package com.worklyn.modules.rewind.service;

import com.worklyn.modules.rewind.adapter.AttendanceRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.LeaveRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.PayrollRecoveryAdapter;
import com.worklyn.modules.rewind.dto.RecoveryResultResponse;
import com.worklyn.modules.rewind.model.*;
import com.worklyn.modules.rewind.planner.DependencyValidator;
import com.worklyn.modules.rewind.planner.InvariantChecker;
import com.worklyn.modules.rewind.planner.RecoveryPlanner;
import com.worklyn.modules.rewind.repository.WorkflowAuditRepository;
import com.worklyn.modules.rewind.repository.WorkflowInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowRecoveryServiceTest {

    @Mock
    private WorkflowInstanceRepository workflowRepository;
    @Mock
    private WorkflowAuditRepository auditRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private LeaveRecoveryAdapter leaveAdapter;
    @Mock
    private AttendanceRecoveryAdapter attendanceAdapter;
    @Mock
    private PayrollRecoveryAdapter payrollAdapter;

    private RecoveryPlanner recoveryPlanner;
    private InvariantChecker invariantChecker;
    private CompensationHandler compensationHandler;
    private RecoveryExecutor recoveryExecutor;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        recoveryPlanner = new RecoveryPlanner(new DependencyValidator());
        invariantChecker = new InvariantChecker(leaveAdapter, attendanceAdapter, payrollAdapter);
        compensationHandler = new CompensationHandler(leaveAdapter, attendanceAdapter, payrollAdapter);

        recoveryExecutor = new RecoveryExecutor(
                workflowRepository,
                auditRepository,
                recoveryPlanner,
                invariantChecker,
                compensationHandler,
                redisTemplate
        );
        ReflectionTestUtils.setField(recoveryExecutor, "lockLeaseSeconds", 10);
    }

    @Test
    @DisplayName("Successful recovery executes reverse compensations and marks workflow COMPENSATED")
    void testSuccessfulRecoveryRollback() {
        String wfId = "WF-100";
        WorkflowStep step1 = WorkflowStep.builder()
                .stepId("STEP_LEAVE")
                .actionType(StepActionType.STEP_DEDUCT_LEAVE_BALANCE)
                .status(StepStatus.COMPLETED)
                .isCompensable(true)
                .build();
        WorkflowStep step2 = WorkflowStep.builder()
                .stepId("STEP_ATTENDANCE")
                .actionType(StepActionType.STEP_MARK_ATTENDANCE)
                .dependsOnStepIds(List.of("STEP_LEAVE"))
                .status(StepStatus.FAILED)
                .failureReason("DB Timeout")
                .isCompensable(true)
                .isRetryable(false)
                .build();

        WorkflowInstance wf = WorkflowInstance.builder()
                .workflowId(wfId)
                .status(WorkflowStatus.FAILED)
                .steps(Arrays.asList(step1, step2))
                .contextData(new HashMap<>())
                .build();

        when(workflowRepository.findByWorkflowId(wfId)).thenReturn(Optional.of(wf));
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(java.time.Duration.class))).thenReturn(true);
        when(leaveAdapter.validateLeaveBalanceInvariant(any())).thenReturn(true);
        when(attendanceAdapter.validateAttendanceInvariant(any())).thenReturn(true);
        when(payrollAdapter.validatePayrollInvariant(any())).thenReturn(true);
        when(leaveAdapter.compensateLeaveDeduction(any(), anyString())).thenReturn(true);
        when(attendanceAdapter.compensateAttendanceMark(any(), anyString())).thenReturn(true);

        RecoveryResultResponse result = recoveryExecutor.executePlan(wfId, "ADMIN_USER", "IDEMP-001");

        assertTrue(result.isSuccess());
        assertEquals(WorkflowStatus.COMPENSATED, result.getNewStatus());
        verify(attendanceAdapter, times(1)).compensateAttendanceMark(any(), eq("STEP_ATTENDANCE"));
        verify(leaveAdapter, times(1)).compensateLeaveDeduction(any(), eq("STEP_LEAVE"));
        verify(auditRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Concurrent recovery request returns ConflictException on lock acquisition failure")
    void testConcurrentRecoveryLockFailure() {
        String wfId = "WF-CONCURRENT";
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(java.time.Duration.class))).thenReturn(false);

        assertThrows(com.worklyn.common.exception.ConflictException.class,
                () -> recoveryExecutor.executePlan(wfId, "ADMIN", "KEY-CONCURRENT"));
    }
}
