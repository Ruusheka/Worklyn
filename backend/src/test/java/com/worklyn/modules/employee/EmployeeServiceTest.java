package com.worklyn.modules.employee;

import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.repository.UserRepository;
import com.worklyn.modules.employee.dto.CreateEmployeeRequest;
import com.worklyn.modules.employee.model.Department;
import com.worklyn.modules.employee.repository.EmployeeRepository;
import com.worklyn.modules.employee.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
 @Test void employeeCannotCreateEmployee() {
  EmployeeService service=new EmployeeService(mock(EmployeeRepository.class),mock(UserRepository.class),mock(MongoTemplate.class),mock(CacheManager.class));
  UserPrincipal employee=new UserPrincipal("u1","e@example.com","hash",null,Set.of(Role.ROLE_EMPLOYEE),true);
  CreateEmployeeRequest request=new CreateEmployeeRequest("EMP-1001","u2","First","Person","p@example.com",Department.ENGINEERING,"Engineer",LocalDate.now(),new BigDecimal("1000.00"),null);
  assertThrows(AccessDeniedException.class,()->service.createEmployee(request,employee));
 }
}
