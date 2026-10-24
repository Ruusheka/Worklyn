
package com.worklyn.modules.attendance.service;

import com.worklyn.modules.attendance.dto.AttendanceResponse;
import com.worklyn.modules.attendance.model.*;
import com.worklyn.modules.attendance.repository.AttendanceRepository;
import com.worklyn.modules.integration.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceRepository repo;
    private final EmployeeDirectory employees;
    private final ZoneId zone;

    public AttendanceService(
            AttendanceRepository repo,
            EmployeeDirectory employees,
            @Value("${worklyn.corporate-timezone:Asia/Kolkata}") String timezone) {

        this.repo = repo;
        this.employees = employees;
        this.zone = (timezone != null && !timezone.isBlank()) ? ZoneId.of(timezone) : ZoneId.of("Asia/Kolkata");
    }

    public AttendanceResponse checkIn(
            String employeeId,
            Instant timestamp,
            String ip,
            String key) {

        if (employeeId == null || employeeId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Authenticated user has no linked employee profile");
        }

        Instant at = timestamp == null ? Instant.now() : timestamp;
        LocalDate date = at.atZone(zone).toLocalDate();

        EmployeeSnapshot emp = employees.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee profile not found; confirm Person 1 EmployeeDirectory adapter is integrated"));

        if (!emp.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ACTIVE employees can check in");
        }

        if (repo.existsByIdempotencyKey(key)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate idempotency key");
        }

        if (repo.findByEmployeeIdAndAttendanceDate(employeeId, date).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already checked in for date: " + date);
        }

        LocalTime local = at.atZone(zone).toLocalTime();

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setAttendanceDate(date);
        attendance.setCheckInTime(at);
        attendance.setIpAddress(ip);
        attendance.setIdempotencyKey(key);

        attendance.setStatus(
                local.isAfter(LocalTime.of(9, 30))
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.PRESENT);

        try {
            return AttendanceResponse.from(repo.save(attendance));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance already exists for this employee and date",
                    ex);
        }
    }

    public AttendanceResponse checkOut(
            String employeeId,
            Instant timestamp,
            String key) {

        if (employeeId == null || employeeId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Authenticated user has no linked employee profile");
        }

        Instant at = timestamp == null ? Instant.now() : timestamp;
        LocalDate date = at.atZone(zone).toLocalDate();

        if (repo.existsByIdempotencyKey(key)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Duplicate idempotency key");
        }

        Attendance attendance = repo
                .findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee already checked out");
        }

        if (at.isBefore(attendance.getCheckInTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Check-out cannot be earlier than check-in");
        }

        long minutes = Duration.between(
                attendance.getCheckInTime(),
                at).toMinutes();

        attendance.setCheckOutTime(at);
        attendance.setDurationMinutes(minutes);
        attendance.setIdempotencyKey(key);

        if (minutes < 240) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        return AttendanceResponse.from(repo.save(attendance));
    }

    public Page<AttendanceResponse> history(
            String employeeId,
            LocalDate start,
            LocalDate end,
            Pageable pageable) {

        if (start == null || end == null || start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate must be on or before endDate");
        }

        if (ChronoUnit.DAYS.between(start, end) > 366) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Date range cannot exceed 366 days");
        }

        return repo
                .findByEmployeeIdAndAttendanceDateBetween(
                        employeeId, start, end, pageable)
                .map(AttendanceResponse::from);
    }

    public void markOnLeave(
            String employeeId,
            LocalDate start,
            LocalDate end) {

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            // Skip Saturday and Sunday.
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }

            if (repo.findByEmployeeIdAndAttendanceDate(
                    employeeId, date).isEmpty()) {

                Attendance attendance = new Attendance();
                attendance.setEmployeeId(employeeId);
                attendance.setAttendanceDate(date);
                attendance.setStatus(AttendanceStatus.ON_LEAVE);

                repo.save(attendance);
            }
        }
    }

    public void removeOnLeave(
            String employeeId,
            LocalDate start,
            LocalDate end) {

        List<Attendance> records = repo.findByEmployeeIdAndAttendanceDateBetween(
                employeeId, start, end);

        for (Attendance attendance : records) {
            if (attendance.getStatus() == AttendanceStatus.ON_LEAVE) {
                repo.delete(attendance);
            }
        }
    }

    public List<Attendance> forPeriod(
            String employeeId,
            LocalDate start,
            LocalDate end) {

        return repo.findByEmployeeIdAndAttendanceDateBetween(
                employeeId, start, end);
    }
}
