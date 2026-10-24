package com.worklyn.modules.rewind.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rewind_workflows")
public class WorkflowInstance {

    @Id
    private String id;

    @Indexed(unique = true)
    private String workflowId;

    private String workflowType;
    private String initiatorId;

    @Indexed
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> contextData = new HashMap<>();

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
