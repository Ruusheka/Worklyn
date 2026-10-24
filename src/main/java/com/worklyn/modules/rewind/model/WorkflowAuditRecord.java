package com.worklyn.modules.rewind.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rewind_audits")
public class WorkflowAuditRecord {

    @Id
    private String id;

    @Indexed
    private String workflowId;

    private String actorId;
    private String operation;

    @Indexed
    @CreatedDate
    @Builder.Default
    private Instant executedAt = Instant.now();

    private String outcome;
    private List<String> stepActions;
    private Map<String, Object> details;
}
