package com.worklyn.modules.employee.dto;
import com.worklyn.modules.employee.model.Department;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record CreateEmployeeRequest(@NotBlank @Size(min=3,max=32) String employeeCode,
 @NotBlank @jakarta.validation.constraints.Pattern(regexp="^[0-9a-fA-F]{24}$", message="Must be a 24-character MongoDB ObjectId") String userId, @NotBlank @Size(min=2,max=50) String firstName,
 @NotBlank @Size(min=2,max=50) String lastName, @NotBlank @Email String email,
 @NotNull Department department, @NotBlank @Size(max=100) String designation,
 @NotNull LocalDate dateOfJoining, @NotNull @DecimalMin(value="0.01") @Digits(integer=10,fraction=2) BigDecimal baseSalary,
 String managerId) { }
