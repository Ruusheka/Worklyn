package com.worklyn.modules.rewind.adapter;

import java.util.Map;

/**
 * Adapter interface for leave balance validations and compensations.
 */
public interface LeaveRecoveryAdapter {
    boolean validateLeaveBalanceInvariant(Map<String, Object> contextData);
    boolean compensateLeaveDeduction(Map<String, Object> contextData, String stepId);
}
