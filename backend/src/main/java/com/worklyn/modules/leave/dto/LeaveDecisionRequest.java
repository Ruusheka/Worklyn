package com.worklyn.modules.leave.dto;
import jakarta.validation.constraints.Size;
public record LeaveDecisionRequest(@Size(max=500) String remarks,String idempotencyKey) {}
