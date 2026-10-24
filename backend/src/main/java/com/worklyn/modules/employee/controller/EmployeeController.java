package com.worklyn.modules.employee.controller;

import com.worklyn.common.dto.PagedResponse;
import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.employee.dto.*;
import com.worklyn.modules.employee.model.*;
import com.worklyn.modules.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService service;
    public EmployeeController(EmployeeService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponse create(@Valid @RequestBody CreateEmployeeRequest request,@AuthenticationPrincipal UserPrincipal actor){return service.createEmployee(request,actor);}
    @GetMapping("/{id}") @PreAuthorize("isAuthenticated()")
    public EmployeeResponse getById(@PathVariable String id,@AuthenticationPrincipal UserPrincipal actor){return service.getEmployeeById(id,actor);}
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponse update(@PathVariable String id,@Valid @RequestBody UpdateEmployeeRequest request,@AuthenticationPrincipal UserPrincipal actor){return service.updateEmployee(id,request,actor);}
    @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public EmployeeResponse updateStatus(@PathVariable String id,@Valid @RequestBody UpdateEmployeeStatusRequest request,@AuthenticationPrincipal UserPrincipal actor){return service.updateStatus(id,request.status(),actor);}
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public PagedResponse<EmployeeResponse> search(@RequestParam(required=false) String q,@RequestParam(required=false) Department department,
       @RequestParam(required=false) EmployeeStatus status,@RequestParam(defaultValue="0") int page,
       @RequestParam(defaultValue="20") int size,@RequestParam(defaultValue="createdAt") String sortBy,
       @RequestParam(defaultValue="desc") String direction,@AuthenticationPrincipal UserPrincipal actor){
        if(page<0 || size<1) throw new com.worklyn.common.exception.BadRequestException("INVALID_PAGINATION","Page must be >= 0 and size must be >= 1.");
        String safeSort=java.util.Set.of("createdAt","employeeCode","firstName","lastName","dateOfJoining").contains(sortBy)?sortBy:"createdAt";
        Sort sort="asc".equalsIgnoreCase(direction)?Sort.by(safeSort).ascending():Sort.by(safeSort).descending();
        return service.searchEmployees(q,department,status,PageRequest.of(page,Math.min(size,100),sort),actor);
    }
}
