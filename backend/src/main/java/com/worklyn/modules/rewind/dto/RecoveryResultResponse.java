package com.worklyn.modules.rewind.dto;

import com.worklyn.modules.rewind.model.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryResultResponse {
    private String workflowId;
    private WorkflowStatus previousStatus;
    private WorkflowStatus newStatus;
    private boolean success;
    private String message;
    private List<String> executedActions;
    private Instant completedAt;
}
