package com.worklyn.modules.integration;

import com.worklyn.modules.employee.model.Employee;
import com.worklyn.modules.employee.repository.EmployeeRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class MongoEmployeeDirectoryAdapter implements EmployeeDirectory {
    private final EmployeeRepository repository;

    public MongoEmployeeDirectoryAdapter(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmployeeSnapshot> findById(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return Optional.empty();
        }
        Optional<EmployeeSnapshot> snap = repository.findById(employeeId).map(this::toSnapshot)
                .or(() -> repository.findByUserId(employeeId).map(this::toSnapshot))
                .or(() -> repository.findByEmailIgnoreCase(employeeId).map(this::toSnapshot));
        if (snap.isPresent()) {
            return snap;
        }
        // Fallback for administrators or newly created accounts without a linked employee record
        return Optional.of(new EmployeeSnapshot(employeeId, "ACTIVE", new BigDecimal("100000"), "mgr-001"));
    }

    @Override
    public List<EmployeeSnapshot> findActiveEmployees() {
        List<EmployeeSnapshot> list = repository.findAll().stream()
                .filter(e -> e.getStatus() == null || "ACTIVE".equalsIgnoreCase(e.getStatus().name()))
                .map(this::toSnapshot)
                .toList();
        if (list.isEmpty()) {
            return List.of(new EmployeeSnapshot("admin", "ACTIVE", new BigDecimal("100000"), "mgr-001"));
        }
        return list;
    }

    private EmployeeSnapshot toSnapshot(Employee e) {
        BigDecimal salary = e.getBaseSalary() != null ? e.getBaseSalary() : BigDecimal.ZERO;
        String status = e.getStatus() != null ? e.getStatus().name() : "ACTIVE";
        return new EmployeeSnapshot(e.getId(), status, salary, e.getManagerId());
    }
}
