package com.worklyn.modules.rewind.repository;

import com.worklyn.modules.rewind.model.WorkflowInstance;
import com.worklyn.modules.rewind.model.WorkflowStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends MongoRepository<WorkflowInstance, String> {
    Optional<WorkflowInstance> findByWorkflowId(String workflowId);
    List<WorkflowInstance> findByStatus(WorkflowStatus status);
    boolean existsByWorkflowId(String workflowId);
}
