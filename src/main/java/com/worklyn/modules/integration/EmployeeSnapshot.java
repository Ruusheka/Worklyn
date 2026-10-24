package com.worklyn.modules.integration;

import java.math.BigDecimal;

/** Stable boundary between Person 2 workflows and Person 1's employee module. */
public record EmployeeSnapshot(String id, String status, BigDecimal baseSalary, String managerId) {
    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }
}
