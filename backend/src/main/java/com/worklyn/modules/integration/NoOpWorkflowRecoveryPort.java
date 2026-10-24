package com.worklyn.modules.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import java.util.Map;

/** Allows Person 2 to develop before Person 3. Person 3's bean automatically replaces this fallback. */
public class NoOpWorkflowRecoveryPort implements WorkflowRecoveryPort {
    @Override public String registerLeaveApproval(String leaveId, String employeeId, String key, Map<String, Object> context) { return null; }
    @Override public void recordPayrollFailure(String period, String key, String reason, Map<String, Object> context) { }
}
