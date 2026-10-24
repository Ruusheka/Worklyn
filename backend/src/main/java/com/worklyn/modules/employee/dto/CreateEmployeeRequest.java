package com.worklyn.modules.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.worklyn.modules.employee.model.Department;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateEmployeeRequest(
    String employeeCode,
    String userId,
    @NotBlank @Size(min=2, max=50) String firstName,
    @NotBlank @Size(min=2, max=50) String lastName,
    @NotBlank @Email String email,
    @NotNull Department department,
    @NotBlank @Size(max=100) String designation,
    LocalDate dateOfJoining,
    @NotNull @DecimalMin(value="0.01") @Digits(integer=10, fraction=2) BigDecimal baseSalary,
    String managerId,
    String phone
) {
    public CreateEmployeeRequest(
            String employeeCode,
            String userId,
            String firstName,
            String lastName,
            String email,
            Department department,
            String designation,
            LocalDate dateOfJoining,
            BigDecimal baseSalary,
            String managerId
    ) {
        this(employeeCode, userId, firstName, lastName, email, department, designation, dateOfJoining, baseSalary, managerId, null);
    }
}
