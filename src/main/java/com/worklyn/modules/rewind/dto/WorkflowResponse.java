package com.worklyn.modules.rewind.dto;

import com.worklyn.modules.rewind.model.WorkflowStatus;
import com.worklyn.modules.rewind.model.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {
    private String id;
    private String workflowId;
    private String workflowType;
    private String initiatorId;
    private WorkflowStatus status;
    private List<WorkflowStep> steps;
    private Map<String, Object> contextData;
    private Instant createdAt;
    private Instant updatedAt;
}
