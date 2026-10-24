package com.worklyn.modules.leave.dto;
import com.worklyn.modules.leave.model.LeaveType;import jakarta.validation.constraints.*;import java.time.LocalDate;
public record ApplyLeaveRequest(@NotNull LeaveType leaveType,@NotNull LocalDate startDate,@NotNull LocalDate endDate,@NotBlank @Size(min=5,max=255) String reason) {}
