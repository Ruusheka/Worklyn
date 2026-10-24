package com.worklyn.modules.rewind.repository;

import com.worklyn.modules.rewind.model.WorkflowAuditRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowAuditRepository extends MongoRepository<WorkflowAuditRecord, String> {
    List<WorkflowAuditRecord> findByWorkflowIdOrderByExecutedAtDesc(String workflowId);
}
