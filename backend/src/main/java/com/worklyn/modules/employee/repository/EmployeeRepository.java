package com.worklyn.modules.employee.repository;
import com.worklyn.modules.employee.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;
public interface EmployeeRepository extends MongoRepository<Employee,String> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmailIgnoreCase(String email);
    Optional<Employee> findByUserId(String userId);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUserId(String userId);
    Page<Employee> findByDepartment(Department department, Pageable pageable);
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
    @Query("{'$or':[{'firstName':{$regex:?0,$options:'i'}},{'lastName':{$regex:?0,$options:'i'}},{'employeeCode':{$regex:?0,$options:'i'}}]}")
    Page<Employee> searchEmployees(String regex, Pageable pageable);
}
