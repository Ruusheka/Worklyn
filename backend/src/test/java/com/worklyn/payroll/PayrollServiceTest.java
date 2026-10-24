package com.worklyn.payroll;

import com.worklyn.modules.attendance.model.Attendance;
import com.worklyn.modules.attendance.model.AttendanceStatus;
import com.worklyn.modules.attendance.service.AttendanceService;
import com.worklyn.modules.integration.ActorContext;
import com.worklyn.modules.integration.EmployeeDirectory;
import com.worklyn.modules.integration.EmployeeSnapshot;
import com.worklyn.modules.integration.WorkflowRecoveryPort;
import com.worklyn.modules.leave.model.LeaveRequest;
import com.worklyn.modules.leave.model.LeaveStatus;
import com.worklyn.modules.leave.model.LeaveType;
import com.worklyn.modules.leave.service.LeaveService;
import com.worklyn.modules.payroll.dto.PayrollProcessResult;
import com.worklyn.modules.payroll.dto.PayrollResponse;
import com.worklyn.modules.payroll.model.PayrollRecord;
import com.worklyn.modules.payroll.model.PayrollStatus;
import com.worklyn.modules.payroll.repository.PayrollRepository;
import com.worklyn.modules.payroll.service.PayrollService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PayrollService}.
 * Owner: Person 2
 * Covers: PAY-001 through PAY-006 test cases from the README.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock private PayrollRepository payroll;
    @Mock private EmployeeDirectory employees;
    @Mock private AttendanceService attendance;
    @Mock private LeaveService leaves;
    @Mock private WorkflowRecoveryPort rewind;
    @Mock private ObjectProvider<StringRedisTemplate> redisProvider;

    @InjectMocks
    private PayrollService service;

    private static final String PERIOD    = "2026-10";
    private static final String ACTOR_ID  = "hr-user-01";
    private static final String IDEM_KEY  = "idem-payroll-oct";
    private static final String EMP_ID    = "emp-001";

    private ActorContext hrActor() {
        return new ActorContext(ACTOR_ID, "emp-hr-01", Set.of("HR"));
    }

    private ActorContext employeeActor() {
        return new ActorContext("user-emp-01", EMP_ID, Set.of("EMPLOYEE"));
    }

    private EmployeeSnapshot activeEmployee(String id, BigDecimal salary) {
        return new EmployeeSnapshot(id, "ACTIVE", salary, null);
    }

    private Attendance presentRecord(String empId, LocalDate date) {
        Attendance a = new Attendance();
        a.setEmployeeId(empId);
        a.setAttendanceDate(date);
        a.setStatus(AttendanceStatus.PRESENT);
        return a;
    }

    @BeforeEach
    void setUp() {
        // By default, Redis provider returns null (no Redis in unit tests)
        lenient().when(redisProvider.getIfAvailable()).thenReturn(null);
    }

    // ─── PAY-001: Process Period Success ─────────────────────────────────────

    @Test
    @DisplayName("PAY-001: Payroll processing creates records for all ACTIVE employees")
    void process_validPeriod_createsPayrollRecords() {
        YearMonth ym = YearMonth.of(2026, 10);
        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        EmployeeSnapshot emp = activeEmployee(EMP_ID, new BigDecimal("90000"));
        when(employees.findActiveEmployees()).thenReturn(List.of(emp));
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(false);
        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.empty());

        // Simulate 22 present days (business days in October 2026)
        List<Attendance> attendRecords = first.datesUntil(last.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() < 6)
                .map(d -> presentRecord(EMP_ID, d))
                .toList();
        when(attendance.forPeriod(EMP_ID, first, last)).thenReturn(attendRecords);
        when(leaves.approvedForPeriod(EMP_ID, first, last)).thenReturn(List.of());

        when(payroll.save(any())).thenAnswer(inv -> {
            PayrollRecord r = inv.getArgument(0);
            r.setId("pr-001");
            return r;
        });

        PayrollProcessResult result = service.process(PERIOD, ACTOR_ID, IDEM_KEY);

        assertThat(result.period()).isEqualTo(PERIOD);
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.records()).hasSize(1);
        assertThat(result.replayed()).isFalse();

        PayrollResponse pr = result.records().get(0);
        assertThat(pr.status()).isEqualTo(PayrollStatus.PROCESSED);
        assertThat(pr.baseSalary()).isEqualByComparingTo(new BigDecimal("90000"));
        assertThat(pr.netPay()).isGreaterThan(BigDecimal.ZERO);
    }

    // ─── PAY-003: Invalid Period Format ──────────────────────────────────────

    @Test
    @DisplayName("PAY-003: Invalid period format '2026-13' returns 400")
    void process_invalidPeriodFormat_throws400() {
        assertThatThrownBy(() -> service.process("2026-13", ACTOR_ID, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("YYYY-MM");
    }

    @Test
    @DisplayName("PAY-003b: Null period returns 400")
    void process_nullPeriod_throws400() {
        assertThatThrownBy(() -> service.process(null, ACTOR_ID, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ─── PAY-004: Finalized Period Locked ────────────────────────────────────

    @Test
    @DisplayName("PAY-004: Processing a FINALIZED period returns 409 CONFLICT")
    void process_finalizedPeriod_throws409() {
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(true);

        assertThatThrownBy(() -> service.process(PERIOD, ACTOR_ID, IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("finalized");
    }

    // ─── PAY-002: Idempotency (no Redis available, Mongo check) ──────────────

    @Test
    @DisplayName("PAY-002: Already PROCESSED employee record is not re-computed (Mongo idempotency)")
    void process_existingProcessedRecord_isReturned() {
        YearMonth ym = YearMonth.of(2026, 10);
        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        EmployeeSnapshot emp = activeEmployee(EMP_ID, new BigDecimal("90000"));
        when(employees.findActiveEmployees()).thenReturn(List.of(emp));
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(false);

        PayrollRecord existing = new PayrollRecord();
        existing.setId("pr-existing");
        existing.setEmployeeId(EMP_ID);
        existing.setPayrollPeriod(PERIOD);
        existing.setBaseSalary(new BigDecimal("90000"));
        existing.setWorkingDays(22);
        existing.setPresentDays(22);
        existing.setPaidLeaveDays(0);
        existing.setUnpaidLeaveDays(0);
        existing.setGrossPay(new BigDecimal("90000.00"));
        existing.setTaxDeduction(new BigDecimal("9500.00"));
        existing.setNetPay(new BigDecimal("80500.00"));
        existing.setStatus(PayrollStatus.PROCESSED);
        existing.setProcessedAt(Instant.now());

        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD))
                .thenReturn(Optional.of(existing));

        PayrollProcessResult result = service.process(PERIOD, ACTOR_ID, IDEM_KEY);

        assertThat(result.processedCount()).isEqualTo(1);
        // Should NOT recompute — no save should be called
        verify(payroll, never()).save(any());
    }

    // ─── Gross and net pay computation ───────────────────────────────────────

    @Test
    @DisplayName("Gross pay is pro-rated by actual present days")
    void process_proRatesGrossPay() {
        YearMonth ym = YearMonth.of(2026, 10);
        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        EmployeeSnapshot emp = activeEmployee(EMP_ID, new BigDecimal("66000")); // ₹66,000 / 22 days = ₹3000/day
        when(employees.findActiveEmployees()).thenReturn(List.of(emp));
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(false);
        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.empty());

        // Only 11 present days (half month)
        List<Attendance> halfAttend = first.datesUntil(last.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() < 6)
                .limit(11)
                .map(d -> presentRecord(EMP_ID, d))
                .toList();
        when(attendance.forPeriod(EMP_ID, first, last)).thenReturn(halfAttend);
        when(leaves.approvedForPeriod(EMP_ID, first, last)).thenReturn(List.of());

        when(payroll.save(any())).thenAnswer(inv -> {
            PayrollRecord r = inv.getArgument(0);
            r.setId("pr-half");
            return r;
        });

        PayrollProcessResult result = service.process(PERIOD, ACTOR_ID, IDEM_KEY);

        PayrollResponse pr = result.records().get(0);
        // ₹3000/day * 11 = ₹33,000 gross (approx — tax bracket below 100k → no tax)
        assertThat(pr.grossPay()).isEqualByComparingTo(new BigDecimal("33000.00"));
        assertThat(pr.taxDeduction()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(pr.netPay()).isEqualByComparingTo(new BigDecimal("33000.00"));
    }

    // ─── PAY-005: Employee Views Own Payslip ─────────────────────────────────

    @Test
    @DisplayName("PAY-005: Employee views own payslip successfully")
    void payslip_ownRecord_returnsPayslip() {
        PayrollRecord record = new PayrollRecord();
        record.setId("pr-payslip");
        record.setEmployeeId(EMP_ID);
        record.setPayrollPeriod(PERIOD);
        record.setBaseSalary(new BigDecimal("90000"));
        record.setWorkingDays(22);
        record.setPresentDays(22);
        record.setPaidLeaveDays(0);
        record.setUnpaidLeaveDays(0);
        record.setGrossPay(new BigDecimal("90000.00"));
        record.setTaxDeduction(new BigDecimal("9500.00"));
        record.setNetPay(new BigDecimal("80500.00"));
        record.setStatus(PayrollStatus.PROCESSED);
        record.setProcessedAt(Instant.now());

        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.of(record));

        PayrollResponse resp = service.payslip(EMP_ID, PERIOD, employeeActor());

        assertThat(resp.employeeId()).isEqualTo(EMP_ID);
        assertThat(resp.status()).isEqualTo(PayrollStatus.PROCESSED);
        assertThat(resp.netPay()).isEqualByComparingTo(new BigDecimal("80500.00"));
    }

    // ─── PAY-006: Unauthorized Payslip Access ────────────────────────────────

    @Test
    @DisplayName("PAY-006: Employee cannot view another employee's payslip — gets 403")
    void payslip_otherEmployee_throwsAccessDenied() {
        // employeeActor has employeeId = EMP_ID, trying to view "emp-other"
        assertThatThrownBy(() -> service.payslip("emp-other", PERIOD, employeeActor()))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── Payslip not found ────────────────────────────────────────────────────

    @Test
    @DisplayName("Payslip not found returns 404")
    void payslip_notFound_throws404() {
        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.payslip(EMP_ID, PERIOD, employeeActor()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    // ─── Finalize period ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Finalizing a PROCESSED period marks all records FINALIZED")
    void finalizePeriod_processed_marksFinalized() {
        PayrollRecord rec = new PayrollRecord();
        rec.setId("pr-fin");
        rec.setEmployeeId(EMP_ID);
        rec.setPayrollPeriod(PERIOD);
        rec.setStatus(PayrollStatus.PROCESSED);

        when(payroll.findByPayrollPeriod(PERIOD)).thenReturn(List.of(rec));
        when(payroll.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.finalizePeriod(PERIOD, hrActor());

        assertThat(rec.getStatus()).isEqualTo(PayrollStatus.FINALIZED);
        verify(payroll).saveAll(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<PayrollRecord> records = (List<PayrollRecord>) list;
            return records.stream().allMatch(r -> r.getStatus() == PayrollStatus.FINALIZED);
        }));
    }

    @Test
    @DisplayName("Finalizing an empty period returns 409 CONFLICT")
    void finalizePeriod_noRecords_throws409() {
        when(payroll.findByPayrollPeriod(PERIOD)).thenReturn(List.of());

        assertThatThrownBy(() -> service.finalizePeriod(PERIOD, hrActor()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No payroll records");
    }

    @Test
    @DisplayName("Finalizing when some records are not PROCESSED throws 409")
    void finalizePeriod_draftRecords_throws409() {
        PayrollRecord draft = new PayrollRecord();
        draft.setId("pr-draft");
        draft.setStatus(PayrollStatus.DRAFT); // Not yet processed

        when(payroll.findByPayrollPeriod(PERIOD)).thenReturn(List.of(draft));

        assertThatThrownBy(() -> service.finalizePeriod(PERIOD, hrActor()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("PROCESSED");
    }

    // ─── Tax bracket calculation ──────────────────────────────────────────────

    @Test
    @DisplayName("No tax deducted when gross pay is at or below 100,000")
    void process_belowTaxThreshold_zeroTax() {
        YearMonth ym = YearMonth.of(2026, 10);
        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        // ₹60,000 salary, 22 working days = ₹60,000 gross (below 100k threshold)
        EmployeeSnapshot emp = activeEmployee(EMP_ID, new BigDecimal("60000"));
        when(employees.findActiveEmployees()).thenReturn(List.of(emp));
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(false);
        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.empty());

        List<Attendance> allPresent = first.datesUntil(last.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() < 6)
                .map(d -> presentRecord(EMP_ID, d))
                .toList();
        when(attendance.forPeriod(EMP_ID, first, last)).thenReturn(allPresent);
        when(leaves.approvedForPeriod(EMP_ID, first, last)).thenReturn(List.of());
        when(payroll.save(any())).thenAnswer(inv -> {
            PayrollRecord r = inv.getArgument(0);
            r.setId("pr-notax");
            return r;
        });

        PayrollProcessResult result = service.process(PERIOD, ACTOR_ID, IDEM_KEY);
        PayrollResponse pr = result.records().get(0);

        assertThat(pr.taxDeduction()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        assertThat(pr.netPay()).isEqualByComparingTo(pr.grossPay());
    }

    // ─── Leave-adjusted payroll ───────────────────────────────────────────────

    @Test
    @DisplayName("Approved paid leave days are included in payable days calculation")
    void process_withApprovedPaidLeave_includesLeaveInPay() {
        YearMonth ym = YearMonth.of(2026, 10);
        LocalDate first = ym.atDay(1);
        LocalDate last  = ym.atEndOfMonth();

        EmployeeSnapshot emp = activeEmployee(EMP_ID, new BigDecimal("66000")); // ₹3000/day for 22 days

        when(employees.findActiveEmployees()).thenReturn(List.of(emp));
        when(payroll.existsByPayrollPeriodAndStatus(PERIOD, PayrollStatus.FINALIZED)).thenReturn(false);
        when(payroll.findByEmployeeIdAndPayrollPeriod(EMP_ID, PERIOD)).thenReturn(Optional.empty());

        // 20 present days
        List<Attendance> attend = first.datesUntil(last.plusDays(1))
                .filter(d -> d.getDayOfWeek().getValue() < 6)
                .limit(20)
                .map(d -> presentRecord(EMP_ID, d))
                .toList();
        when(attendance.forPeriod(EMP_ID, first, last)).thenReturn(attend);

        // 2 days approved CASUAL leave (paid)
        LeaveRequest paidLeave = new LeaveRequest();
        paidLeave.setEmployeeId(EMP_ID);
        paidLeave.setLeaveType(LeaveType.CASUAL);
        paidLeave.setStartDate(LocalDate.of(2026, 10, 29));
        paidLeave.setEndDate(LocalDate.of(2026, 10, 30));
        paidLeave.setStatus(LeaveStatus.APPROVED);
        when(leaves.approvedForPeriod(EMP_ID, first, last)).thenReturn(List.of(paidLeave));

        when(payroll.save(any())).thenAnswer(inv -> {
            PayrollRecord r = inv.getArgument(0);
            r.setId("pr-leave");
            return r;
        });

        PayrollProcessResult result = service.process(PERIOD, ACTOR_ID, IDEM_KEY);
        PayrollResponse pr = result.records().get(0);

        // 20 present + 2 paid leave = 22 payable days = full salary
        assertThat(pr.paidLeaveDays()).isEqualTo(2);
        assertThat(pr.grossPay()).isEqualByComparingTo(new BigDecimal("66000.00"));
    }

    // ─── Missing actor or key guard ───────────────────────────────────────────

    @Test
    @DisplayName("Processing with blank actorId returns 400")
    void process_blankActorId_throws400() {
        assertThatThrownBy(() -> service.process(PERIOD, "", IDEM_KEY))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Processor identity");
    }

    @Test
    @DisplayName("Processing with blank idempotency key returns 400")
    void process_blankIdempotencyKey_throws400() {
        assertThatThrownBy(() -> service.process(PERIOD, ACTOR_ID, ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Idempotency-Key");
    }
}
