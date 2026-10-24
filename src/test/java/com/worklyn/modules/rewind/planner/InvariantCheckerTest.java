package com.worklyn.modules.rewind.planner;

import com.worklyn.modules.rewind.adapter.DefaultBusinessRecoveryAdapters;
import com.worklyn.modules.rewind.model.WorkflowInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvariantCheckerTest {

    private InvariantChecker invariantChecker;
    private DefaultBusinessRecoveryAdapters adapters;

    @BeforeEach
    void setUp() {
        adapters = new DefaultBusinessRecoveryAdapters();
        invariantChecker = new InvariantChecker(adapters, adapters, adapters);
    }

    @Test
    @DisplayName("Valid business context satisfies invariants")
    void testValidInvariants() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("leaveBalance", 10);
        ctx.put("netPay", 4500.0);
        ctx.put("duplicateAttendanceDetected", false);

        WorkflowInstance wf = WorkflowInstance.builder()
                .workflowId("WF-VAL")
                .contextData(ctx)
                .build();

        assertTrue(invariantChecker.checkInvariants(wf));
    }

    @Test
    @DisplayName("Negative leave balance violates invariant")
    void testNegativeLeaveBalanceViolatesInvariant() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("leaveBalance", -2);

        WorkflowInstance wf = WorkflowInstance.builder()
                .workflowId("WF-INV-LEAVE")
                .contextData(ctx)
                .build();

        assertFalse(invariantChecker.checkInvariants(wf));
    }

    @Test
    @DisplayName("Negative net pay violates invariant")
    void testNegativeNetPayViolatesInvariant() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("netPay", -100.0);

        WorkflowInstance wf = WorkflowInstance.builder()
                .workflowId("WF-INV-PAY")
                .contextData(ctx)
                .build();

        assertFalse(invariantChecker.checkInvariants(wf));
    }

    @Test
    @DisplayName("Duplicate attendance record violates invariant")
    void testDuplicateAttendanceViolatesInvariant() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("duplicateAttendanceDetected", true);

        WorkflowInstance wf = WorkflowInstance.builder()
                .workflowId("WF-INV-ATT")
                .contextData(ctx)
                .build();

        assertFalse(invariantChecker.checkInvariants(wf));
    }
}
