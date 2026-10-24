package com.worklyn.modules.rewind.adapter;

import java.util.Map;

/**
 * Adapter interface for payroll validations and compensations.
 */
public interface PayrollRecoveryAdapter {
    boolean validatePayrollInvariant(Map<String, Object> contextData);
    boolean compensatePayrollDraft(Map<String, Object> contextData, String stepId);
}
