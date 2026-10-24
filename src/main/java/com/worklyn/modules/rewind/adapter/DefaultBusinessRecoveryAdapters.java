package com.worklyn.modules.rewind.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Default decoupled recovery adapters providing business invariant validation and compensation.
 * Person 2's services can provide primary beans to seamlessly override these defaults.
 */
@Slf4j
@Component
public class DefaultBusinessRecoveryAdapters
        implements LeaveRecoveryAdapter, AttendanceRecoveryAdapter, PayrollRecoveryAdapter {

    @Override
    public boolean validateLeaveBalanceInvariant(Map<String, Object> contextData) {
        if (contextData == null) return true;
        Object balanceObj = contextData.get("leaveBalance");
        if (balanceObj instanceof Number num) {
            return num.doubleValue() >= 0;
        }
        return true;
    }

    @Override
    public boolean compensateLeaveDeduction(Map<String, Object> contextData, String stepId) {
        log.info("Compensating leave deduction for step [{}] with context: {}", stepId, contextData);
        return true;
    }

    @Override
    public boolean validateAttendanceInvariant(Map<String, Object> contextData) {
        if (contextData == null) return true;
        Object duplicateFlag = contextData.get("duplicateAttendanceDetected");
        if (Boolean.TRUE.equals(duplicateFlag)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean compensateAttendanceMark(Map<String, Object> contextData, String stepId) {
        log.info("Compensating attendance mark for step [{}] with context: {}", stepId, contextData);
        return true;
    }

    @Override
    public boolean validatePayrollInvariant(Map<String, Object> contextData) {
        if (contextData == null) return true;
        Object netPayObj = contextData.get("netPay");
        if (netPayObj instanceof Number num) {
            return num.doubleValue() >= 0;
        }
        return true;
    }

    @Override
    public boolean compensatePayrollDraft(Map<String, Object> contextData, String stepId) {
        log.info("Compensating payroll draft for step [{}] with context: {}", stepId, contextData);
        return true;
    }
}
