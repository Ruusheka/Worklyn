package com.worklyn.modules.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "attendance")
@CompoundIndex(name = "employee_date_unique", def = "{'employeeId': 1, 'attendanceDate': 1}", unique = true)
public class Attendance {
    @Id private String id;
    @Indexed private String employeeId;
    private LocalDate attendanceDate;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long durationMinutes;
    private AttendanceStatus status;
    private String ipAddress;
    @Indexed private String idempotencyKey;
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getEmployeeId(){return employeeId;} public void setEmployeeId(String v){employeeId=v;}
    public LocalDate getAttendanceDate(){return attendanceDate;} public void setAttendanceDate(LocalDate v){attendanceDate=v;}
    public Instant getCheckInTime(){return checkInTime;} public void setCheckInTime(Instant v){checkInTime=v;}
    public Instant getCheckOutTime(){return checkOutTime;} public void setCheckOutTime(Instant v){checkOutTime=v;}
    public Long getDurationMinutes(){return durationMinutes;} public void setDurationMinutes(Long v){durationMinutes=v;}
    public AttendanceStatus getStatus(){return status;} public void setStatus(AttendanceStatus v){status=v;}
    public String getIpAddress(){return ipAddress;} public void setIpAddress(String v){ipAddress=v;}
    public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;}
}
