package com.worklyn.modules.employee.dto;
import com.worklyn.modules.employee.model.Department;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record UpdateEmployeeRequest(@NotNull Department department,
 @NotBlank @Size(max=100) String designation, @NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal baseSalary,
 String managerId) { }
