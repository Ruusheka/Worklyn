package com.worklyn.modules.rewind.service;

import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.modules.rewind.dto.WorkflowRegistrationRequest;
import com.worklyn.modules.rewind.dto.WorkflowResponse;
import com.worklyn.modules.rewind.model.WorkflowInstance;
import com.worklyn.modules.rewind.model.WorkflowStatus;
import com.worklyn.modules.rewind.planner.DependencyValidator;
import com.worklyn.modules.rewind.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowInstanceRepository repository;
    private final DependencyValidator dependencyValidator;

    /**
     * Registers a new workflow after validating its DAG structure.
     */
    public WorkflowResponse registerWorkflow(WorkflowRegistrationRequest request) {
        if (repository.existsByWorkflowId(request.getWorkflowId())) {
            throw new ConflictException("WORKFLOW_EXISTS", "Workflow already exists with ID: " + request.getWorkflowId());
        }

        // Validate DAG structure before persisting
        dependencyValidator.validateDAG(request.getSteps());

        WorkflowInstance instance = WorkflowInstance.builder()
                .workflowId(request.getWorkflowId())
                .workflowType(request.getWorkflowType())
                .initiatorId(request.getInitiatorId())
                .status(WorkflowStatus.PENDING)
                .steps(request.getSteps())
                .contextData(request.getContextData())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        WorkflowInstance saved = repository.save(instance);
        log.info("Registered workflow [{}] with [{}] steps", saved.getWorkflowId(), saved.getSteps().size());
        return mapToResponse(saved);
    }

    public WorkflowResponse getWorkflow(String workflowId) {
        WorkflowInstance instance = repository.findByWorkflowId(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("WORKFLOW_NOT_FOUND", "Workflow not found: " + workflowId));
        return mapToResponse(instance);
    }

    public List<WorkflowResponse> listWorkflows() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public WorkflowResponse mapToResponse(WorkflowInstance instance) {
        return WorkflowResponse.builder()
                .id(instance.getId())
                .workflowId(instance.getWorkflowId())
                .workflowType(instance.getWorkflowType())
                .initiatorId(instance.getInitiatorId())
                .status(instance.getStatus())
                .steps(instance.getSteps())
                .contextData(instance.getContextData())
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();
    }
}
