package com.worklyn.modules.leave.repository;
import com.worklyn.modules.leave.model.LeaveBalance;import org.springframework.data.mongodb.repository.MongoRepository;import java.util.Optional;
public interface LeaveBalanceRepository extends MongoRepository<LeaveBalance,String>{Optional<LeaveBalance> findByEmployeeIdAndYear(String employeeId,Integer year);}
