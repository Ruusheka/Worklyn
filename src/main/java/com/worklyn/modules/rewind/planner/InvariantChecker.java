package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.adapter.AttendanceRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.LeaveRecoveryAdapter;
import com.worklyn.modules.rewind.adapter.PayrollRecoveryAdapter;
import com.worklyn.modules.rewind.model.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Pre-flight and post-flight business invariant checker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvariantChecker {

    private final LeaveRecoveryAdapter leaveAdapter;
    private final AttendanceRecoveryAdapter attendanceAdapter;
    private final PayrollRecoveryAdapter payrollAdapter;

    /**
     * Check all business invariants for a given workflow instance.
     * Returns true if all invariants hold; false otherwise.
     */
    public boolean checkInvariants(WorkflowInstance workflow) {
        if (workflow == null) return false;
        Map<String, Object> ctx = workflow.getContextData();

        if (leaveAdapter != null && !leaveAdapter.validateLeaveBalanceInvariant(ctx)) {
            log.warn("Invariant violation: negative leave balance detected for workflow [{}]", workflow.getWorkflowId());
            return false;
        }

        if (attendanceAdapter != null && !attendanceAdapter.validateAttendanceInvariant(ctx)) {
            log.warn("Invariant violation: duplicate attendance detected for workflow [{}]", workflow.getWorkflowId());
            return false;
        }

        if (payrollAdapter != null && !payrollAdapter.validatePayrollInvariant(ctx)) {
            log.warn("Invariant violation: negative net pay detected for workflow [{}]", workflow.getWorkflowId());
            return false;
        }

        return true;
    }
}
