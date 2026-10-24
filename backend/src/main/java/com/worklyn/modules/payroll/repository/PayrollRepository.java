package com.worklyn.modules.payroll.repository;
import com.worklyn.modules.payroll.model.*;import org.springframework.data.mongodb.repository.MongoRepository;import java.util.*;
public interface PayrollRepository extends MongoRepository<PayrollRecord,String>{Optional<PayrollRecord> findByEmployeeIdAndPayrollPeriod(String employeeId,String period);List<PayrollRecord> findByPayrollPeriod(String period);boolean existsByPayrollPeriodAndStatus(String period,PayrollStatus status);Optional<PayrollRecord> findFirstByPayrollPeriodAndIdempotencyKey(String period,String key);}
