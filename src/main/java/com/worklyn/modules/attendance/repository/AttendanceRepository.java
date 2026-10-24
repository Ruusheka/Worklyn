package com.worklyn.modules.attendance.repository;
import com.worklyn.modules.attendance.model.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.*;
public interface AttendanceRepository extends MongoRepository<Attendance,String> {
 Optional<Attendance> findByEmployeeIdAndAttendanceDate(String employeeId, LocalDate date);
 boolean existsByIdempotencyKey(String key);
 Page<Attendance> findByEmployeeIdAndAttendanceDateBetween(String employeeId, LocalDate start, LocalDate end, Pageable pageable);
 List<Attendance> findByEmployeeIdAndAttendanceDateBetween(String employeeId, LocalDate start, LocalDate end);
 List<Attendance> findByAttendanceDateBetween(LocalDate start, LocalDate end);
}
