package com.worklyn.modules.employee;

import com.worklyn.common.exception.BadRequestException;
import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import com.worklyn.modules.auth.repository.UserRepository;
import com.worklyn.modules.employee.dto.*;
import com.worklyn.modules.employee.model.*;
import com.worklyn.modules.employee.repository.EmployeeRepository;
import com.worklyn.modules.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    EmployeeRepository employees;
    UserRepository users;
    MongoTemplate mongo;
    CacheManager cacheManager;
    Cache cache;
    EmployeeService service;
    UserPrincipal admin;
    UserPrincipal employee;

    @BeforeEach
    void setUp() {
        employees = mock(EmployeeRepository.class);
        users = mock(UserRepository.class);
        mongo = mock(MongoTemplate.class);
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache("employeesById")).thenReturn(cache);
        service = new EmployeeService(employees, users, mongo, cacheManager);
        admin = principal("admin-1", Role.ROLE_ADMIN);
        employee = principal("user-1", Role.ROLE_EMPLOYEE);
    }

    private UserPrincipal principal(String id, Role role) {
        return new UserPrincipal(id, id + "@example.com", "hash", null, Set.of(role), true);
    }

    private CreateEmployeeRequest createRequest() {
        return new CreateEmployeeRequest(" emp-1001 ", "0123456789abcdef01234567",
                " First ", " Person ", " P@example.com ", Department.ENGINEERING,
                " Engineer ", LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"), null);
    }

    private Employee employeeRecord(String id, String userId, EmployeeStatus status) {
        Employee e = new Employee();
        e.setId(id);
        e.setUserId(userId);
        e.setEmployeeCode("EMP-1001");
        e.setFirstName("First");
        e.setLastName("Person");
        e.setEmail("p@example.com");
        e.setDepartment(Department.ENGINEERING);
        e.setDesignation("Engineer");
        e.setDateOfJoining(LocalDate.of(2025, 1, 1));
        e.setBaseSalary(new BigDecimal("1000.00"));
        e.setStatus(status);
        return e;
    }

    @Test
    void employeeCannotCreateEmployee() {
        assertThrows(AccessDeniedException.class,
                () -> service.createEmployee(createRequest(), employee));
        verifyNoInteractions(employees, users);
    }

    @Test
    void createEmployeeNormalizesFieldsAndLinksUser() {
        User linked = new User();
        linked.setId("0123456789abcdef01234567");
        when(employees.existsByEmployeeCode("EMP-1001")).thenReturn(false);
        when(employees.existsByEmailIgnoreCase("p@example.com")).thenReturn(false);
        when(employees.existsByUserId("0123456789abcdef01234567")).thenReturn(false);
        when(users.findById("0123456789abcdef01234567")).thenReturn(Optional.of(linked));
        when(employees.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId("employee-1");
            return e;
        });
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse result = service.createEmployee(createRequest(), admin);

        assertEquals("EMP-1001", result.employeeCode());
        assertEquals("p@example.com", result.email());
        assertEquals("First", result.firstName());
        assertEquals("Person", result.lastName());
        assertEquals(EmployeeStatus.ACTIVE, result.status());
        assertEquals("employee-1", linked.getEmployeeId());
        verify(users).save(linked);
        verify(cache).evict("employee-1");
    }

    @Test
    void createEmployeeRejectsDuplicateCode() {
        when(employees.existsByEmployeeCode("EMP-1001")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.createEmployee(createRequest(), admin));
        verifyNoInteractions(users);
    }

    @Test
    void createEmployeeRejectsDuplicateEmail() {
        when(employees.existsByEmployeeCode("EMP-1001")).thenReturn(false);
        when(employees.existsByEmailIgnoreCase("p@example.com")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.createEmployee(createRequest(), admin));
        verifyNoInteractions(users);
    }

    @Test
    void createEmployeeRejectsMissingLinkedUser() {
        when(employees.existsByEmployeeCode("EMP-1001")).thenReturn(false);
        when(employees.existsByEmailIgnoreCase("p@example.com")).thenReturn(false);
        when(employees.existsByUserId("0123456789abcdef01234567")).thenReturn(false);
        when(users.findById("0123456789abcdef01234567")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createEmployee(createRequest(), admin));
    }

    @Test
    void employeeCanReadOwnProfile() {
        Employee e = employeeRecord("employee-1", "user-1", EmployeeStatus.ACTIVE);
        when(cache.get("employee-1", Employee.class)).thenReturn(null);
        when(employees.findById("employee-1")).thenReturn(Optional.of(e));

        EmployeeResponse result = service.getEmployeeById("employee-1", employee);

        assertEquals("employee-1", result.id());
        verify(cache).put("employee-1", e);
    }

    @Test
    void employeeCannotReadAnotherUsersProfile() {
        Employee e = employeeRecord("employee-2", "other-user", EmployeeStatus.ACTIVE);
        when(cache.get("employee-2", Employee.class)).thenReturn(e);

        assertThrows(AccessDeniedException.class,
                () -> service.getEmployeeById("employee-2", employee));
    }

    @Test
    void getEmployeeReturnsNotFound() {
        when(cache.get("missing", Employee.class)).thenReturn(null);
        when(employees.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getEmployeeById("missing", admin));
    }

    @Test
    void updateRejectsSelfAsManager() {
        Employee e = employeeRecord("employee-1", "user-1", EmployeeStatus.ACTIVE);
        when(employees.findById("employee-1")).thenReturn(Optional.of(e));
        UpdateEmployeeRequest req = new UpdateEmployeeRequest(
                Department.FINANCE, "Analyst", new BigDecimal("2000.00"), "employee-1");

        assertThrows(BadRequestException.class,
                () -> service.updateEmployee("employee-1", req, admin));
        verify(employees, never()).save(any(Employee.class));
    }

    @Test
    void activeEmployeeCanBeSuspended() {
        Employee e = employeeRecord("employee-1", "user-1", EmployeeStatus.ACTIVE);
        when(employees.findById("employee-1")).thenReturn(Optional.of(e));
        when(employees.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse result = service.updateStatus(
                "employee-1", EmployeeStatus.SUSPENDED, admin);

        assertEquals(EmployeeStatus.SUSPENDED, result.status());
        verify(cache).evict("employee-1");
    }

    @Test
    void terminatedEmployeeCannotBeReactivatedByHr() {
        UserPrincipal hr = principal("hr-1", Role.ROLE_HR);
        Employee e = employeeRecord("employee-1", "user-1", EmployeeStatus.TERMINATED);
        when(employees.findById("employee-1")).thenReturn(Optional.of(e));

        assertThrows(ConflictException.class,
                () -> service.updateStatus("employee-1", EmployeeStatus.ACTIVE, hr));
        verify(employees, never()).save(any(Employee.class));
    }

    @Test
    void employeeCannotSearchDirectory() {
        assertThrows(AccessDeniedException.class, () ->
                service.searchEmployees(null, null, null,
                        PageRequest.of(0, 20, Sort.by("createdAt")), employee));
        verifyNoInteractions(mongo);
    }

    @Test
    void searchRejectsPageSizeAbove100() {
        assertThrows(BadRequestException.class, () ->
                service.searchEmployees(null, null, null,
                        PageRequest.of(0, 101), admin));
        verifyNoInteractions(mongo);
    }
}