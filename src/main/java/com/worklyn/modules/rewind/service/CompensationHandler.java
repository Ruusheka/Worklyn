package com.worklyn.modules.rewind.service;

import com.worklyn.modules.rewind.adapter.AttendanceRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.LeaveRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.PayrollRecoveryAdapter;
import com.worklyn.modules.rewind.model.StepActionType;
import com.worklyn.modules.rewind.model.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Dispatches typed compensation operations to the registered domain adapters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationHandler {

    private final LeaveRecoveryAdapter leaveAdapter;
    private final AttendanceRecoveryAdapter attendanceAdapter;
    private final PayrollRecoveryAdapter payrollAdapter;

    /**
     * Executes idempotent compensation for a given workflow step.
     */
    public boolean compensate(WorkflowStep step, Map<String, Object> contextData) {
        if (step == null) {
            return false;
        }

        StepActionType action = step.getActionType();
        log.info("Executing compensation for stepId [{}] action [{}]", step.getStepId(), action);

        if (action == null) {
            log.warn("Step [{}] has null actionType, cannot compensate", step.getStepId());
            return false;
        }

        return switch (action) {
            case STEP_DEDUCT_LEAVE_BALANCE, BALANCE_DEDUCTION ->
                    leaveAdapter != null && leaveAdapter.compensateLeaveDeduction(contextData, step.getStepId());
            case STEP_MARK_ATTENDANCE, STATUS_UPDATE ->
                    attendanceAdapter != null && attendanceAdapter.compensateAttendanceMark(contextData, step.getStepId());
            case STEP_CREATE_PAYROLL_ENTRY, DB_MUTATION ->
                    payrollAdapter != null && payrollAdapter.compensatePayrollDraft(contextData, step.getStepId());
            case EXTERNAL_NOTIFICATION -> {
                log.info("External notification cannot be rolled back, acknowledging compensation as no-op");
                yield true;
            }
        };
    }
}
