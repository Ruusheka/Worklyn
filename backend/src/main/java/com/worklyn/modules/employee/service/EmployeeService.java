package com.worklyn.modules.employee.service;

import com.worklyn.common.dto.PagedResponse;
import com.worklyn.common.exception.BadRequestException;
import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.common.security.UserPrincipal;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class EmployeeService {
    private final EmployeeRepository employees; private final UserRepository users; private final MongoTemplate mongo; private final CacheManager cacheManager;
    public EmployeeService(EmployeeRepository employees, UserRepository users, MongoTemplate mongo, CacheManager cacheManager){this.employees=employees;this.users=users;this.mongo=mongo;this.cacheManager=cacheManager;}
    public EmployeeResponse createEmployee(CreateEmployeeRequest req, UserPrincipal actor) {
        requireAny(actor,"ROLE_ADMIN","ROLE_HR");
        String code=req.employeeCode().trim().toUpperCase(); String email=req.email().trim().toLowerCase();
        if(employees.existsByEmployeeCode(code)) throw new ConflictException("EMPLOYEE_CODE_EXISTS","Employee code '"+code+"' is already assigned to an existing record.");
        if(employees.existsByEmailIgnoreCase(email)) throw new ConflictException("EMPLOYEE_EMAIL_EXISTS","Employee email is already assigned to an existing record.");
        if(employees.existsByUserId(req.userId())) throw new ConflictException("EMPLOYEE_USER_EXISTS","This user is already linked to an employee profile.");
        var linkedUser=users.findById(req.userId()).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND","The linked user account does not exist."));
        if(linkedUser.getEmployeeId()!=null && !linkedUser.getEmployeeId().isBlank()) throw new ConflictException("EMPLOYEE_USER_EXISTS","This user is already linked to an employee profile.");
        if(req.managerId()!=null && !req.managerId().isBlank() && !employees.existsById(req.managerId())) throw new ResourceNotFoundException("MANAGER_NOT_FOUND","The specified manager does not exist.");
        Employee e=new Employee(); e.setEmployeeCode(code); e.setUserId(req.userId()); e.setFirstName(req.firstName().trim()); e.setLastName(req.lastName().trim());
        e.setEmail(email); e.setDepartment(req.department()); e.setDesignation(req.designation().trim()); e.setDateOfJoining(req.dateOfJoining());
        e.setStatus(EmployeeStatus.ACTIVE); e.setBaseSalary(req.baseSalary()); e.setManagerId(blankToNull(req.managerId()));
        Employee saved=employees.save(e);
        linkedUser.setEmployeeId(saved.getId()); users.save(linkedUser);
        evict(saved.getId()); return EmployeeResponse.from(saved);
    }
    public EmployeeResponse getEmployeeById(String id, UserPrincipal actor) {
        Cache cache=cacheManager.getCache("employeesById");
        Employee known=cache==null?null:cache.get(id,Employee.class);
        if(known==null) known=employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND","Employee '"+id+"' was not found."));
        // Authorization is evaluated on every request, even when the profile came from cache.
        if(hasRole(actor,"ROLE_EMPLOYEE") && !hasAny(actor,"ROLE_ADMIN","ROLE_HR","ROLE_MANAGER") && !known.getUserId().equals(actor.getId())) throw new AccessDeniedException("Employees may only view their own profile.");
        if(cache!=null) cache.put(id,known);
        return EmployeeResponse.from(known);
    }
    public EmployeeResponse updateEmployee(String id, UpdateEmployeeRequest req, UserPrincipal actor) {
        requireAny(actor,"ROLE_ADMIN","ROLE_HR"); Employee e=employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND","Employee '"+id+"' was not found."));
        if(req.managerId()!=null && !req.managerId().isBlank()) {
            if(id.equals(req.managerId())) throw new BadRequestException("INVALID_MANAGER","An employee cannot be their own manager.");
            if(!employees.existsById(req.managerId())) throw new ResourceNotFoundException("MANAGER_NOT_FOUND","The specified manager does not exist.");
        }
        e.setDepartment(req.department()); e.setDesignation(req.designation().trim()); e.setBaseSalary(req.baseSalary()); e.setManagerId(blankToNull(req.managerId()));
        Employee saved=employees.save(e); evict(id); return EmployeeResponse.from(saved);
    }
    public EmployeeResponse updateStatus(String id, EmployeeStatus newStatus, UserPrincipal actor) {
        requireAny(actor,"ROLE_ADMIN","ROLE_HR"); Employee e=employees.findById(id).orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND","Employee '"+id+"' was not found."));
        EmployeeStatus old=e.getStatus(); boolean allowed=(old==EmployeeStatus.ACTIVE && (newStatus==EmployeeStatus.SUSPENDED || newStatus==EmployeeStatus.TERMINATED))
          || (old==EmployeeStatus.PROBATION && (newStatus==EmployeeStatus.ACTIVE || newStatus==EmployeeStatus.TERMINATED))
          || (old==EmployeeStatus.SUSPENDED && newStatus==EmployeeStatus.TERMINATED)
          || (old==EmployeeStatus.TERMINATED && newStatus!=EmployeeStatus.TERMINATED && hasRole(actor,"ROLE_ADMIN"));
        if(old==newStatus) return EmployeeResponse.from(e);
        if(!allowed) throw new ConflictException("INVALID_STATUS_TRANSITION","Employee status cannot transition from "+old+" to "+newStatus+".");
        e.setStatus(newStatus); Employee saved=employees.save(e); evict(id); return EmployeeResponse.from(saved);
    }
    public PagedResponse<EmployeeResponse> searchEmployees(String queryText, Department dept, EmployeeStatus status, Pageable pageable, UserPrincipal actor) {
        requireAny(actor,"ROLE_ADMIN","ROLE_HR","ROLE_MANAGER");
        if(pageable.getPageSize()>100) throw new BadRequestException("PAGE_SIZE_TOO_LARGE","Page size cannot exceed 100.");
        List<Criteria> criteria=new ArrayList<>();
        if(queryText!=null && !queryText.isBlank()) { String safe=Pattern.quote(queryText.trim()); criteria.add(new Criteria().orOperator(Criteria.where("firstName").regex(safe,"i"),Criteria.where("lastName").regex(safe,"i"),Criteria.where("employeeCode").regex(safe,"i"))); }
        if(dept!=null) criteria.add(Criteria.where("department").is(dept)); if(status!=null) criteria.add(Criteria.where("status").is(status));
        Query q=new Query(); if(!criteria.isEmpty()) q.addCriteria(new Criteria().andOperator(criteria.toArray(Criteria[]::new)));
        long total=mongo.count(q,Employee.class); q.with(pageable); List<Employee> content=mongo.find(q,Employee.class);
        Page<EmployeeResponse> page=new PageImpl<>(content.stream().map(EmployeeResponse::from).toList(),pageable,total);
        return PagedResponse.from(page);
    }
    private void requireAny(UserPrincipal actor,String... roles){if(!hasAny(actor,roles)) throw new AccessDeniedException("Insufficient role for this operation.");}
    private boolean hasAny(UserPrincipal actor,String... roles){return actor!=null && actor.getAuthorities().stream().anyMatch(a -> java.util.Arrays.asList(roles).contains(a.getAuthority()));}
    private boolean hasRole(UserPrincipal actor,String role){return hasAny(actor,role);}
    private String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
    private void evict(String id){Cache c=cacheManager.getCache("employeesById");if(c!=null)c.evict(id);}
}
