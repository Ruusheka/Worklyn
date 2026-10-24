package com.worklyn.modules.rewind.dto;

import com.worklyn.modules.rewind.model.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryPlanResponse {
    private String workflowId;
    private String workflowType;
    private WorkflowStatus status;
    private String failedStepId;
    private String failureReason;
    private List<RecoveryActionResponse> actions;
    private boolean invariantsSatisfied;
}
