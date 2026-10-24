package com.worklyn.modules.rewind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryActionResponse {
    private String stepId;
    private String action; // REPLAY, COMPENSATE, SKIP, PRESERVE, MANUAL_REVIEW
    private String reason;
}
