package com.worklyn.attendance;

import com.worklyn.modules.attendance.dto.AttendanceResponse;
import com.worklyn.modules.attendance.model.Attendance;
import com.worklyn.modules.attendance.model.AttendanceStatus;
import com.worklyn.modules.attendance.repository.AttendanceRepository;
import com.worklyn.modules.attendance.service.AttendanceService;
import com.worklyn.modules.integration.EmployeeDirectory;
import com.worklyn.modules.integration.EmployeeSnapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AttendanceService}.
 * Owner: Person 2
 * Covers: ATT-001 through ATT-007 test cases from the README.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository repo;

    @Mock
    private EmployeeDirectory employees;

    @InjectMocks
    private AttendanceService service;

    private static final String EMP_ID  = "emp-001";
    private static final String IDEM_KEY = "idem-key-abc";
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private EmployeeSnapshot activeEmployee;

    @BeforeEach
    void setUp() {
        // Inject the timezone property value that @Value would normally supply
        ReflectionTestUtils.setField(service, "zone", ZONE);
        activeEmployee = new EmployeeSnapshot(EMP_ID, "ACTIVE", new BigDecimal("90000"), null);
    }

    // ─── ATT-001: Valid Check-in ───────────────────────────────────────────────

    @Test
    @DisplayName("ATT-001: Check-in before 09:30 marks attendance PRESENT")
    void checkIn_beforeCutoff_marksPresent() {
        Instant ts = LocalDate.now(ZONE).atTime(8, 55).atZone(ZONE).toInstant();
        LocalDate today = ts.atZone(ZONE).toLocalDate();

        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee));
        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> {
            Attendance a = inv.getArgument(0);
            a.setId("rec-1");
            return a;
        });

        AttendanceResponse resp = service.checkIn(EMP_ID, ts, "127.0.0.1", IDEM_KEY);

        assertThat(resp.status()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(resp.employeeId()).isEqualTo(EMP_ID);
        assertThat(resp.attendanceDate()).isEqualTo(today);
        assertThat(resp.checkInTime()).isEqualTo(ts);
    }

    // ─── ATT-006: Late Marking ────────────────────────────────────────────────

    @Test
    @DisplayName("ATT-006: Check-in after 09:30 marks attendance LATE")
    void checkIn_afterCutoff_marksLate() {
        Instant ts = LocalDate.now(ZONE).atTime(10, 15).atZone(ZONE).toInstant();
        LocalDate today = ts.atZone(ZONE).toLocalDate();

        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee));
        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> {
            Attendance a = inv.getArgument(0);
            a.setId("rec-2");
            return a;
        });

        AttendanceResponse resp = service.checkIn(EMP_ID, ts, "10.0.0.1", IDEM_KEY);

        assertThat(resp.status()).isEqualTo(AttendanceStatus.LATE);
    }

    // ─── ATT-002: Duplicate Check-in ─────────────────────────────────────────

    @Test
    @DisplayName("ATT-002: Duplicate check-in on same day returns 409 CONFLICT")
    void checkIn_duplicate_throws409() {
        Instant ts = LocalDate.now(ZONE).atTime(9, 0).atZone(ZONE).toInstant();
        LocalDate today = ts.atZone(ZONE).toLocalDate();

        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee));
        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today))
                .thenReturn(Optional.of(new Attendance()));

        assertThatThrownBy(() -> service.checkIn(EMP_ID, ts, "127.0.0.1", IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Already checked in");
    }

    // ─── ATT-007: Idempotency Key Replay ─────────────────────────────────────

    @Test
    @DisplayName("ATT-007: Duplicate idempotency key on check-in returns 409 CONFLICT")
    void checkIn_duplicateIdempotencyKey_throws409() {
        Instant ts = LocalDate.now(ZONE).atTime(9, 0).atZone(ZONE).toInstant();

        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee));
        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(true);

        assertThatThrownBy(() -> service.checkIn(EMP_ID, ts, "127.0.0.1", IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Duplicate idempotency key");
    }

    // ─── Inactive employee ────────────────────────────────────────────────────

    @Test
    @DisplayName("Check-in rejected when employee is not ACTIVE")
    void checkIn_inactiveEmployee_throws400() {
        Instant ts = LocalDate.now(ZONE).atTime(9, 0).atZone(ZONE).toInstant();
        EmployeeSnapshot inactive = new EmployeeSnapshot(EMP_ID, "TERMINATED", null, null);

        when(employees.findById(EMP_ID)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.checkIn(EMP_ID, ts, "127.0.0.1", IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ACTIVE");
    }

    // ─── ATT-003: Valid Check-out ─────────────────────────────────────────────

    @Test
    @DisplayName("ATT-003: Valid check-out computes duration and persists checkOutTime")
    void checkOut_valid_computesDuration() {
        LocalDate today = LocalDate.now(ZONE);
        Instant checkIn  = today.atTime(9, 0).atZone(ZONE).toInstant();
        Instant checkOut = today.atTime(17, 0).atZone(ZONE).toInstant();

        Attendance existing = new Attendance();
        existing.setId("rec-3");
        existing.setEmployeeId(EMP_ID);
        existing.setAttendanceDate(today);
        existing.setCheckInTime(checkIn);
        existing.setStatus(AttendanceStatus.PRESENT);

        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse resp = service.checkOut(EMP_ID, checkOut, IDEM_KEY);

        assertThat(resp.checkOutTime()).isEqualTo(checkOut);
        assertThat(resp.durationMinutes()).isEqualTo(480L);
        assertThat(resp.status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    // ─── Half-day on short duration ──────────────────────────────────────────

    @Test
    @DisplayName("Short check-out duration (< 4h) marks attendance HALF_DAY")
    void checkOut_shortDuration_marksHalfDay() {
        LocalDate today = LocalDate.now(ZONE);
        Instant checkIn  = today.atTime(9, 0).atZone(ZONE).toInstant();
        Instant checkOut = today.atTime(11, 0).atZone(ZONE).toInstant();

        Attendance existing = new Attendance();
        existing.setId("rec-4");
        existing.setEmployeeId(EMP_ID);
        existing.setAttendanceDate(today);
        existing.setCheckInTime(checkIn);
        existing.setStatus(AttendanceStatus.PRESENT);

        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse resp = service.checkOut(EMP_ID, checkOut, IDEM_KEY);

        assertThat(resp.status()).isEqualTo(AttendanceStatus.HALF_DAY);
        assertThat(resp.durationMinutes()).isEqualTo(120L);
    }

    // ─── ATT-004: Check-out Without Check-in ─────────────────────────────────

    @Test
    @DisplayName("ATT-004: Check-out without prior check-in returns 404 NOT FOUND")
    void checkOut_noCheckin_throws404() {
        LocalDate today = LocalDate.now(ZONE);
        Instant checkOut = today.atTime(17, 0).atZone(ZONE).toInstant();

        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkOut(EMP_ID, checkOut, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No check-in record found");
    }

    // ─── ATT-005: Repeated Check-out ─────────────────────────────────────────

    @Test
    @DisplayName("ATT-005: Repeated check-out on an already checked-out record returns 409")
    void checkOut_alreadyCheckedOut_throws409() {
        LocalDate today = LocalDate.now(ZONE);
        Instant checkIn   = today.atTime(9, 0).atZone(ZONE).toInstant();
        Instant firstOut  = today.atTime(17, 0).atZone(ZONE).toInstant();
        Instant secondOut = today.atTime(18, 0).atZone(ZONE).toInstant();

        Attendance existing = new Attendance();
        existing.setEmployeeId(EMP_ID);
        existing.setAttendanceDate(today);
        existing.setCheckInTime(checkIn);
        existing.setCheckOutTime(firstOut);
        existing.setStatus(AttendanceStatus.PRESENT);

        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.checkOut(EMP_ID, secondOut, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already checked out");
    }

    // ─── Check-out before check-in ───────────────────────────────────────────

    @Test
    @DisplayName("Check-out timestamp before check-in timestamp returns 400")
    void checkOut_beforeCheckIn_throws400() {
        LocalDate today = LocalDate.now(ZONE);
        Instant checkIn = today.atTime(9, 0).atZone(ZONE).toInstant();
        Instant badOut  = today.atTime(8, 30).atZone(ZONE).toInstant();

        Attendance existing = new Attendance();
        existing.setEmployeeId(EMP_ID);
        existing.setAttendanceDate(today);
        existing.setCheckInTime(checkIn);
        existing.setStatus(AttendanceStatus.PRESENT);

        when(repo.existsByIdempotencyKey(IDEM_KEY)).thenReturn(false);
        when(repo.findByEmployeeIdAndAttendanceDate(EMP_ID, today)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.checkOut(EMP_ID, badOut, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("earlier than check-in");
    }

    // ─── History date range validation ───────────────────────────────────────

    @Test
    @DisplayName("History with start after end returns 400")
    void history_invalidDateRange_throws400() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate start = LocalDate.now(ZONE);
        LocalDate end   = start.minusDays(5);

        assertThatThrownBy(() -> service.history(EMP_ID, start, end, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("startDate must be on or before endDate");
    }

    @Test
    @DisplayName("History range > 366 days returns 400")
    void history_rangeTooLarge_throws400() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate start = LocalDate.now(ZONE).minusDays(400);
        LocalDate end   = LocalDate.now(ZONE);

        assertThatThrownBy(() -> service.history(EMP_ID, start, end, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("366 days");
    }

    @Test
    @DisplayName("History with valid date range delegates to repository")
    void history_validRange_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate start = LocalDate.now(ZONE).minusDays(7);
        LocalDate end   = LocalDate.now(ZONE);

        Attendance a = new Attendance();
        a.setId("rec-5");
        a.setEmployeeId(EMP_ID);
        a.setAttendanceDate(end);
        a.setStatus(AttendanceStatus.PRESENT);
        Page<Attendance> page = new PageImpl<>(List.of(a), pageable, 1);

        when(repo.findByEmployeeIdAndAttendanceDateBetween(EMP_ID, start, end, pageable)).thenReturn(page);

        Page<AttendanceResponse> result = service.history(EMP_ID, start, end, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    // ─── markOnLeave helper ───────────────────────────────────────────────────

    @Test
    @DisplayName("markOnLeave creates ON_LEAVE records for each weekday in range")
    void markOnLeave_createsWeekdayRecords() {
        // Monday → Friday (2026-10-19 to 2026-10-23)
        LocalDate start = LocalDate.of(2026, 10, 19);
        LocalDate end   = LocalDate.of(2026, 10, 23);

        when(repo.findByEmployeeIdAndAttendanceDate(eq(EMP_ID), any())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markOnLeave(EMP_ID, start, end);

        verify(repo, times(5)).save(argThat(a ->
                a.getStatus() == AttendanceStatus.ON_LEAVE && EMP_ID.equals(a.getEmployeeId())
        ));
    }

    @Test
    @DisplayName("markOnLeave skips Saturday and Sunday")
    void markOnLeave_skipsWeekends() {
        LocalDate start = LocalDate.of(2026, 10, 24); // Saturday
        LocalDate end   = LocalDate.of(2026, 10, 25); // Sunday

        service.markOnLeave(EMP_ID, start, end);

        verify(repo, never()).save(any());
    }
}
