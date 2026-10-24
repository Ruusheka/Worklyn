package com.worklyn.modules.integration;

import java.util.Map;

/** Person 3 implements this port to connect HR workflows to HR-REWIND. */
public interface WorkflowRecoveryPort {
    String registerLeaveApproval(String leaveId, String employeeId, String idempotencyKey, Map<String, Object> context);
    void recordPayrollFailure(String period, String idempotencyKey, String reason, Map<String, Object> context);
}
