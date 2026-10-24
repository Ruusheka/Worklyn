package com.worklyn.modules.employee.dto;
import com.worklyn.modules.employee.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
public record EmployeeResponse(String id,String employeeCode,String userId,String firstName,String lastName,String email,
 Department department,String designation,LocalDate dateOfJoining,EmployeeStatus status,BigDecimal baseSalary,
 String managerId,Instant createdAt,Instant updatedAt) {
 public static EmployeeResponse from(Employee e){return new EmployeeResponse(e.getId(),e.getEmployeeCode(),e.getUserId(),e.getFirstName(),e.getLastName(),e.getEmail(),e.getDepartment(),e.getDesignation(),e.getDateOfJoining(),e.getStatus(),e.getBaseSalary(),e.getManagerId(),e.getCreatedAt(),e.getUpdatedAt());}
}
