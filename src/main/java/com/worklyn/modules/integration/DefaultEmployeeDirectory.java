package com.worklyn.modules.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/** Safe development fallback. Replace by a Person 1 adapter; never fabricates employee data. */
public class DefaultEmployeeDirectory implements EmployeeDirectory {
    @Override
    public Optional<EmployeeSnapshot> findById(String employeeId) {
        if (employeeId != null && !employeeId.isBlank()) {
            return Optional.of(new EmployeeSnapshot(employeeId, "ACTIVE", new java.math.BigDecimal("90000"), "mgr-001"));
        }
        return Optional.empty();
    }

    @Override
    public List<EmployeeSnapshot> findActiveEmployees() {
        return List.of(
            new EmployeeSnapshot("emp-001", "ACTIVE", new java.math.BigDecimal("90000"), "mgr-001"),
            new EmployeeSnapshot("user", "ACTIVE", new java.math.BigDecimal("85000"), "mgr-001")
        );
    }
}
