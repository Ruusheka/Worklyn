package com.worklyn.modules.integration;

import java.util.List;
import java.util.Optional;

/** Person 1 should provide an adapter backed by EmployeeService/EmployeeRepository. */
public interface EmployeeDirectory {
    Optional<EmployeeSnapshot> findById(String employeeId);
    List<EmployeeSnapshot> findActiveEmployees();
    default boolean canApprove(String approverUserId, EmployeeSnapshot employee) {
        return employee != null && approverUserId != null && approverUserId.equals(employee.managerId());
    }
}
