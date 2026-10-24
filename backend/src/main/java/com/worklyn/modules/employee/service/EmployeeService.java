package com.worklyn.modules.employee.service;

import com.worklyn.common.dto.PagedResponse;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final UserRepository users;
    private final MongoTemplate mongo;
    private final CacheManager cacheManager;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employees, UserRepository users, MongoTemplate mongo, CacheManager cacheManager) {
        this(employees, users, mongo, cacheManager, new BCryptPasswordEncoder());
    }

    @Autowired
    public EmployeeService(EmployeeRepository employees, UserRepository users, MongoTemplate mongo, CacheManager cacheManager, PasswordEncoder passwordEncoder) {
        this.employees = employees;
        this.users = users;
        this.mongo = mongo;
        this.cacheManager = cacheManager;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeeResponse createEmployee(CreateEmployeeRequest req, UserPrincipal actor) {
        requireAny(actor, "ROLE_ADMIN", "ROLE_HR");
        String code = (req.employeeCode() != null && !req.employeeCode().isBlank()) ? req.employeeCode().trim().toUpperCase() : generateUniqueEmployeeCode();
        String email = req.email().trim().toLowerCase();

        if (employees.existsByEmployeeCode(code)) {
            throw new ConflictException("EMPLOYEE_CODE_EXISTS", "Employee code '" + code + "' is already assigned to an existing record.");
        }
        if (employees.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMPLOYEE_EMAIL_EXISTS", "Employee email is already assigned to an existing record.");
        }

        User linkedUser;
        if (req.userId() != null && !req.userId().isBlank()) {
            if (employees.existsByUserId(req.userId())) {
                throw new ConflictException("EMPLOYEE_USER_EXISTS", "This user is already linked to an employee profile.");
            }
            linkedUser = users.findById(req.userId()).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "The linked user account does not exist."));
            if (linkedUser.getEmployeeId() != null && !linkedUser.getEmployeeId().isBlank()) {
                throw new ConflictException("EMPLOYEE_USER_EXISTS", "This user is already linked to an employee profile.");
            }
        } else {
            linkedUser = users.findByEmailIgnoreCase(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setPasswordHash(passwordEncoder.encode("Password@123"));
                u.setRoles(Set.of(Role.ROLE_EMPLOYEE));
                u.setEnabled(true);
                return users.save(u);
            });
            if (employees.existsByUserId(linkedUser.getId())) {
                throw new ConflictException("EMPLOYEE_USER_EXISTS", "This user is already linked to an employee profile.");
            }
        }

        if (req.managerId() != null && !req.managerId().isBlank() && !employees.existsById(req.managerId())) {
            throw new ResourceNotFoundException("MANAGER_NOT_FOUND", "The specified manager does not exist.");
        }

        Employee e = new Employee();
        e.setEmployeeCode(code);
        e.setUserId(linkedUser.getId());
        e.setFirstName(req.firstName().trim());
        e.setLastName(req.lastName().trim());
        e.setEmail(email);
        e.setDepartment(req.department());
        e.setDesignation(req.designation().trim());
        e.setDateOfJoining(req.dateOfJoining() != null ? req.dateOfJoining() : java.time.LocalDate.now());
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setBaseSalary(req.baseSalary());
        e.setManagerId(blankToNull(req.managerId()));

        Employee saved = employees.save(e);
        linkedUser.setEmployeeId(saved.getId());
        users.save(linkedUser);

        evict(saved.getId());
        return EmployeeResponse.from(saved);
    }

    public EmployeeResponse getEmployeeById(String id, UserPrincipal actor) {
        Cache cache = cacheManager.getCache("employeesById");
        Employee known = cache == null ? null : cache.get(id, Employee.class);
        if (known == null) {
            known = employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Employee '" + id + "' was not found."));
        }
        if (hasRole(actor, "ROLE_EMPLOYEE") && !hasAny(actor, "ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER") && !known.getUserId().equals(actor.getId())) {
            throw new AccessDeniedException("Employees may only view their own profile.");
        }
        if (cache != null) {
            cache.put(id, known);
        }
        return EmployeeResponse.from(known);
    }

    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest req, UserPrincipal actor) {
        requireAny(actor, "ROLE_ADMIN", "ROLE_HR");
        Employee e = employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Employee '" + id + "' was not found."));
        if (req.managerId() != null && !req.managerId().isBlank()) {
            if (id.equals(req.managerId())) {
                throw new BadRequestException("INVALID_MANAGER", "An employee cannot be their own manager.");
            }
            if (!employees.existsById(req.managerId())) {
                throw new ResourceNotFoundException("MANAGER_NOT_FOUND", "The specified manager does not exist.");
            }
        }
        e.setDepartment(req.department());
        e.setDesignation(req.designation().trim());
        e.setBaseSalary(req.baseSalary());
        e.setManagerId(blankToNull(req.managerId()));
        Employee saved = employees.save(e);
        evict(id);
        return EmployeeResponse.from(saved);
    }

    public EmployeeResponse updateStatus(String id, EmployeeStatus status, UserPrincipal actor) {
        requireAny(actor, "ROLE_ADMIN", "ROLE_HR");
        Employee e = employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Employee '" + id + "' was not found."));
        e.setStatus(status);
        Employee saved = employees.save(e);
        evict(id);
        return EmployeeResponse.from(saved);
    }

    public PagedResponse<EmployeeResponse> searchEmployees(String query, Department department, EmployeeStatus status, Pageable pageable, UserPrincipal actor) {
        requireAny(actor, "ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER");
        List<Criteria> criteria = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            String sanitized = Pattern.quote(query.trim());
            criteria.add(new Criteria().orOperator(
                Criteria.where("firstName").regex(sanitized, "i"),
                Criteria.where("lastName").regex(sanitized, "i"),
                Criteria.where("employeeCode").regex(sanitized, "i"),
                Criteria.where("email").regex(sanitized, "i")
            ));
        }
        if (department != null) {
            criteria.add(Criteria.where("department").is(department.name()));
        }
        if (status != null) {
            criteria.add(Criteria.where("status").is(status.name()));
        }
        Query q = new Query();
        if (!criteria.isEmpty()) {
            q.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        long total = mongo.count(q, Employee.class);
        q.with(pageable);
        List<EmployeeResponse> items = mongo.find(q, Employee.class).stream().map(EmployeeResponse::from).toList();
        return PagedResponse.from(new PageImpl<>(items, pageable, total));
    }

    public EmployeeResponse getMyProfile(UserPrincipal actor) {
        if (actor == null) throw new AccessDeniedException("Authentication required.");
        Employee emp = null;
        if (actor.getEmployeeId() != null && !actor.getEmployeeId().isBlank())
            emp = employees.findById(actor.getEmployeeId()).orElse(null);
        if (emp == null && actor.getId() != null)
            emp = employees.findByUserId(actor.getId()).orElse(null);
        if (emp == null && actor.getEmail() != null)
            emp = employees.findByEmailIgnoreCase(actor.getEmail().trim()).orElse(null);
        if (emp == null) {
            Employee e = new Employee();
            e.setEmployeeCode("EMP-ADMIN-" + actor.getId().substring(0, Math.min(6, actor.getId().length())));
            e.setUserId(actor.getId());
            e.setFirstName("Admin"); e.setLastName("User"); e.setEmail(actor.getEmail());
            e.setDepartment(Department.HUMAN_RESOURCES); e.setDesignation("System Administrator");
            e.setDateOfJoining(java.time.LocalDate.now()); e.setStatus(EmployeeStatus.ACTIVE);
            e.setBaseSalary(new java.math.BigDecimal("120000.00"));
            emp = employees.save(e);
        }
        return EmployeeResponse.from(emp);
    }

    public EmployeeResponse updateMyProfile(UpdateMyProfileRequest req, UserPrincipal actor) {
        if (actor == null) throw new AccessDeniedException("Authentication required.");
        Employee emp = null;
        if (actor.getEmployeeId() != null && !actor.getEmployeeId().isBlank())
            emp = employees.findById(actor.getEmployeeId()).orElse(null);
        if (emp == null && actor.getId() != null)
            emp = employees.findByUserId(actor.getId()).orElse(null);
        if (emp == null && actor.getEmail() != null)
            emp = employees.findByEmailIgnoreCase(actor.getEmail().trim()).orElse(null);
        if (emp == null) throw new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Employee profile not found.");
        if (req.firstName() != null && !req.firstName().isBlank()) emp.setFirstName(req.firstName().trim());
        if (req.lastName() != null && !req.lastName().isBlank()) emp.setLastName(req.lastName().trim());
        if (req.designation() != null && !req.designation().isBlank() && hasAny(actor, "ROLE_ADMIN", "ROLE_HR"))
            emp.setDesignation(req.designation().trim());
        Employee saved = employees.save(emp);
        evict(saved.getId());
        return EmployeeResponse.from(saved);
    }

    private String generateUniqueEmployeeCode() {
        for (int i = 0; i < 50; i++) {
            String candidate = "EMP-" + (int)(1000 + Math.random() * 9000);
            if (!employees.existsByEmployeeCode(candidate)) {
                return candidate;
            }
        }
        return "EMP-" + (System.currentTimeMillis() % 100000);
    }

    private void evict(String id) {
        Cache cache = cacheManager.getCache("employeesById");
        if (cache != null) {
            cache.evict(id);
        }
    }

    private static boolean hasRole(UserPrincipal u, String r) {
        return u != null && u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(r));
    }

    private static boolean hasAny(UserPrincipal u, String... roles) {
        for (String r : roles) {
            if (hasRole(u, r)) return true;
        }
        return false;
    }

    private static void requireAny(UserPrincipal u, String... roles) {
        if (!hasAny(u, roles)) {
            throw new AccessDeniedException("Access denied: required role missing.");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
