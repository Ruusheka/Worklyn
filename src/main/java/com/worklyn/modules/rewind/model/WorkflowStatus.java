package com.worklyn.modules.rewind.model;

public enum WorkflowStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    RECOVERING,
    COMPENSATED,
    REQUIRES_MANUAL_REVIEW
}
