package com.worklyn.modules.employee.dto;
import com.worklyn.modules.employee.model.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
public record UpdateEmployeeStatusRequest(@NotNull EmployeeStatus status) { }
