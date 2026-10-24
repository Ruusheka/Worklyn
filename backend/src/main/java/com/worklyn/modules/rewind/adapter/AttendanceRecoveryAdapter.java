package com.worklyn.modules.rewind.adapter;

import java.util.Map;

/**
 * Adapter interface for attendance validations and compensations.
 */
public interface AttendanceRecoveryAdapter {
    boolean validateAttendanceInvariant(Map<String, Object> contextData);
    boolean compensateAttendanceMark(Map<String, Object> contextData, String stepId);
}
