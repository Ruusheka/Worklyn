package com.worklyn.modules.rewind.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {
    private String stepId;
    private StepActionType actionType;
    @Builder.Default
    private List<String> dependsOnStepIds = new ArrayList<>();
    @Builder.Default
    private StepStatus status = StepStatus.PENDING;
    private boolean isCompensable;
    private boolean isRetryable;
    private String failureReason;
    @Builder.Default
    private int retryCount = 0;
    @Builder.Default
    private int maxRetries = 3;
    private Instant executedAt;
}
