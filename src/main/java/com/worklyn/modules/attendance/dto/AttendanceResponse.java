package com.worklyn.modules.attendance.dto;
import com.worklyn.modules.attendance.model.*;
import java.time.*;
public record AttendanceResponse(String id, String employeeId, LocalDate attendanceDate, Instant checkInTime, Instant checkOutTime, Long durationMinutes, AttendanceStatus status) {
 public static AttendanceResponse from(Attendance a){return new AttendanceResponse(a.getId(),a.getEmployeeId(),a.getAttendanceDate(),a.getCheckInTime(),a.getCheckOutTime(),a.getDurationMinutes(),a.getStatus());}
}
