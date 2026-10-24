package com.worklyn.modules.leave.repository;

import com.worklyn.modules.leave.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.*;

public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status);
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
    List<LeaveRequest> findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String employeeId, Collection<LeaveStatus> statuses, LocalDate end, LocalDate start);
    List<LeaveRequest> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LeaveStatus status, LocalDate end, LocalDate start);
    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateBetween(String employeeId, LeaveStatus status, LocalDate start, LocalDate end);
    List<LeaveRequest> findByEmployeeIdAndStatusAndEndDateBetween(String employeeId, LeaveStatus status, LocalDate start, LocalDate end);
    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String employeeId, LeaveStatus status, LocalDate end, LocalDate start);
}
