package com.worklyn.modules.rewind.dto;

import com.worklyn.modules.rewind.model.WorkflowStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRegistrationRequest {
    @NotBlank(message = "Workflow ID must not be blank")
    private String workflowId;

    @NotBlank(message = "Workflow type must not be blank")
    private String workflowType;

    private String initiatorId;

    @NotEmpty(message = "Workflow must contain at least one step")
    private List<WorkflowStep> steps;

    private Map<String, Object> contextData;
}
