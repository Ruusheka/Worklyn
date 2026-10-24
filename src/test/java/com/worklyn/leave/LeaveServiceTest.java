package com.worklyn.leave;

import com.worklyn.modules.attendance.service.AttendanceService;
import com.worklyn.modules.integration.ActorContext;
import com.worklyn.modules.integration.EmployeeDirectory;
import com.worklyn.modules.integration.EmployeeSnapshot;
import com.worklyn.modules.integration.WorkflowRecoveryPort;
import com.worklyn.modules.leave.dto.ApplyLeaveRequest;
import com.worklyn.modules.leave.dto.LeaveBalanceResponse;
import com.worklyn.modules.leave.dto.LeaveResponse;
import com.worklyn.modules.leave.model.LeaveBalance;
import com.worklyn.modules.leave.model.LeaveRequest;
import com.worklyn.modules.leave.model.LeaveStatus;
import com.worklyn.modules.leave.model.LeaveType;
import com.worklyn.modules.leave.repository.LeaveBalanceRepository;
import com.worklyn.modules.leave.repository.LeaveRequestRepository;
import com.worklyn.modules.leave.service.LeaveService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveService}.
 * Owner: Person 2
 * Covers: LEAVE-001 through LEAVE-008 test cases from the README.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRequestRepository requests;
    @Mock private LeaveBalanceRepository balances;
    @Mock private EmployeeDirectory employees;
    @Mock private AttendanceService attendance;
    @Mock private WorkflowRecoveryPort rewind;

    @InjectMocks
    private LeaveService service;

    private static final String EMP_ID  = "emp-001";
    private static final String APPROVER_ID = "user-hr-01";

    // Helpers to build common objects
    private LeaveBalance fullBalance() {
        LeaveBalance b = new LeaveBalance();
        b.setId("bal-001");
        b.setEmployeeId(EMP_ID);
        b.setYear(LocalDate.now().getYear());
        b.setCasualLeaveAvailable(12);
        b.setSickLeaveAvailable(10);
        b.setEarnedLeaveAvailable(15);
        return b;
    }

    private ActorContext hrActor() {
        return new ActorContext(APPROVER_ID, "emp-hr-01", Set.of("HR"));
    }

    private ActorContext employeeActor() {
        return new ActorContext("user-emp-01", EMP_ID, Set.of("EMPLOYEE"));
    }

    private EmployeeSnapshot activeEmployee() {
        return new EmployeeSnapshot(EMP_ID, "ACTIVE", new BigDecimal("90000"), APPROVER_ID);
    }

    // ─── LEAVE-001: Apply Valid Leave ─────────────────────────────────────────

    @Test
    @DisplayName("LEAVE-001: Apply casual leave with sufficient balance returns PENDING request")
    void apply_validCasualLeave_returnsPending() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(2);
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL, start, end,
                "Annual vacation leave request");

        when(requests.findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(EMP_ID), any(), any(), any())).thenReturn(List.of());
        when(balances.findByEmployeeIdAndYear(EMP_ID, start.getYear())).thenReturn(Optional.of(fullBalance()));
        when(requests.save(any())).thenAnswer(inv -> {
            LeaveRequest r = inv.getArgument(0);
            r.setId("lr-001");
            return r;
        });

        LeaveResponse resp = service.apply(req, EMP_ID);

        assertThat(resp.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(resp.leaveType()).isEqualTo(LeaveType.CASUAL);
        assertThat(resp.employeeId()).isEqualTo(EMP_ID);
        assertThat(resp.totalDays()).isGreaterThan(0);
    }

    // ─── LEAVE-002: End Date Before Start Date ────────────────────────────────

    @Test
    @DisplayName("LEAVE-002: End date before start date returns 400")
    void apply_endBeforeStart_throws400() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end   = start.minusDays(2);
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL, start, end, "Some reason");

        assertThatThrownBy(() -> service.apply(req, EMP_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("valid");
    }

    @Test
    @DisplayName("LEAVE-002b: Start date in the past returns 400")
    void apply_startInPast_throws400() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end   = start.plusDays(2);
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL, start, end, "Some reason");

        assertThatThrownBy(() -> service.apply(req, EMP_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot start in the past");
    }

    // ─── LEAVE-003: Overlapping Leave ────────────────────────────────────────

    @Test
    @DisplayName("LEAVE-003: Overlapping pending/approved leave returns 409 CONFLICT")
    void apply_overlappingLeave_throws409() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(3);
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL, start, end, "Overlap test");

        LeaveRequest existing = new LeaveRequest();
        existing.setId("lr-existing");
        existing.setStatus(LeaveStatus.PENDING);

        when(requests.findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(EMP_ID), any(), any(), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.apply(req, EMP_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("overlapping");
    }

    // ─── LEAVE-004: Insufficient Balance ─────────────────────────────────────

    @Test
    @DisplayName("LEAVE-004: Requesting more days than available balance returns 409 CONFLICT")
    void apply_insufficientBalance_throws409() {
        // Request 15 days but only 12 casual available
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(20); // spans many business days
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL, start, end,
                "Long vacation request");

        when(requests.findByEmployeeIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(EMP_ID), any(), any(), any())).thenReturn(List.of());

        LeaveBalance lowBalance = fullBalance();
        lowBalance.setCasualLeaveAvailable(2); // only 2 days available
        when(balances.findByEmployeeIdAndYear(EMP_ID, start.getYear()))
                .thenReturn(Optional.of(lowBalance));

        assertThatThrownBy(() -> service.apply(req, EMP_ID))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Insufficient leave balance");
    }

    // ─── LEAVE-005: Manager Approves Leave ───────────────────────────────────

    @Test
    @DisplayName("LEAVE-005: HR approves pending leave — balance deducted and attendance marked")
    void approve_byHr_approvesAndDeductsBalance() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(2);

        LeaveRequest pending = new LeaveRequest();
        pending.setId("lr-002");
        pending.setEmployeeId(EMP_ID);
        pending.setLeaveType(LeaveType.CASUAL);
        pending.setStartDate(start);
        pending.setEndDate(end);
        pending.setTotalDays(3);
        pending.setStatus(LeaveStatus.PENDING);
        pending.setReason("Approved request");
        pending.setCreatedAt(Instant.now());

        when(requests.findById("lr-002")).thenReturn(Optional.of(pending));
        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee()));
        when(balances.findByEmployeeIdAndYear(EMP_ID, start.getYear()))
                .thenReturn(Optional.of(fullBalance()));
        when(balances.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rewind.registerLeaveApproval(any(), any(), any(), any())).thenReturn("wf-001");
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse resp = service.approve("lr-002", hrActor(), "Looks good", "idem-key-approve-1");

        assertThat(resp.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(resp.approverId()).isEqualTo(APPROVER_ID);
        verify(attendance).markOnLeave(EMP_ID, start, end);
    }

    // ─── LEAVE-006: Unauthorized Approval ────────────────────────────────────

    @Test
    @DisplayName("LEAVE-006: Regular employee cannot approve leave — gets 403 FORBIDDEN")
    void approve_byEmployee_throwsAccessDenied() {
        assertThatThrownBy(() ->
                service.approve("lr-003", employeeActor(), "Remarks", "key"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── LEAVE-007: Double Approval Attempt ──────────────────────────────────

    @Test
    @DisplayName("LEAVE-007: Approving an already-approved request returns 409 CONFLICT")
    void approve_alreadyApproved_throws409() {
        LeaveRequest approved = new LeaveRequest();
        approved.setId("lr-004");
        approved.setEmployeeId(EMP_ID);
        approved.setStatus(LeaveStatus.APPROVED); // already approved

        when(requests.findById("lr-004")).thenReturn(Optional.of(approved));

        assertThatThrownBy(() ->
                service.approve("lr-004", hrActor(), null, "idem-key"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only pending leave can be approved");
    }

    // ─── LEAVE-008: Employee Cancels Pending Leave ────────────────────────────

    @Test
    @DisplayName("LEAVE-008: Employee cancels own pending leave — balance unchanged")
    void cancel_pendingLeave_byOwner_cancels() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(2);

        LeaveRequest pending = new LeaveRequest();
        pending.setId("lr-005");
        pending.setEmployeeId(EMP_ID);
        pending.setLeaveType(LeaveType.CASUAL);
        pending.setStartDate(start);
        pending.setEndDate(end);
        pending.setTotalDays(2);
        pending.setStatus(LeaveStatus.PENDING);
        pending.setReason("Cancel this");
        pending.setCreatedAt(Instant.now());

        when(requests.findById("lr-005")).thenReturn(Optional.of(pending));
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse resp = service.cancel("lr-005", employeeActor());

        assertThat(resp.status()).isEqualTo(LeaveStatus.CANCELLED);
        // Balance should NOT be modified for a PENDING leave
        verify(balances, never()).save(any());
        verify(attendance, never()).removeOnLeave(any(), any(), any());
    }

    // ─── Cancel approved leave by HR ─────────────────────────────────────────

    @Test
    @DisplayName("HR cancels an approved leave — balance credited back, ON_LEAVE removed")
    void cancel_approvedLeave_byHr_creditsBalance() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = start.plusDays(2);

        LeaveRequest approved = new LeaveRequest();
        approved.setId("lr-006");
        approved.setEmployeeId(EMP_ID);
        approved.setLeaveType(LeaveType.CASUAL);
        approved.setStartDate(start);
        approved.setEndDate(end);
        approved.setTotalDays(3);
        approved.setStatus(LeaveStatus.APPROVED);
        approved.setReason("Approved leave");
        approved.setCreatedAt(Instant.now());

        LeaveBalance bal = fullBalance();
        bal.setCasualLeaveAvailable(9);
        bal.setCasualLeaveUsed(3);

        when(requests.findById("lr-006")).thenReturn(Optional.of(approved));
        when(balances.findByEmployeeIdAndYear(EMP_ID, start.getYear())).thenReturn(Optional.of(bal));
        when(balances.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse resp = service.cancel("lr-006", hrActor());

        assertThat(resp.status()).isEqualTo(LeaveStatus.CANCELLED);
        verify(balances).save(argThat(b -> b.getCasualLeaveAvailable() == 12));
        verify(attendance).removeOnLeave(EMP_ID, start, end);
    }

    // ─── Employee cannot cancel approved leave ────────────────────────────────

    @Test
    @DisplayName("Employee cannot cancel an already-approved leave — 403 FORBIDDEN")
    void cancel_approvedLeave_byEmployee_throwsAccessDenied() {
        LeaveRequest approved = new LeaveRequest();
        approved.setId("lr-007");
        approved.setEmployeeId(EMP_ID);
        approved.setStatus(LeaveStatus.APPROVED);

        when(requests.findById("lr-007")).thenReturn(Optional.of(approved));

        assertThatThrownBy(() -> service.cancel("lr-007", employeeActor()))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── Cannot cancel already-cancelled leave ────────────────────────────────

    @Test
    @DisplayName("Cancel a CANCELLED leave returns 409 CONFLICT")
    void cancel_alreadyCancelled_throws409() {
        LeaveRequest cancelled = new LeaveRequest();
        cancelled.setId("lr-008");
        cancelled.setEmployeeId(EMP_ID);
        cancelled.setStatus(LeaveStatus.CANCELLED);

        when(requests.findById("lr-008")).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(() -> service.cancel("lr-008", hrActor()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cancelled");
    }

    // ─── Reject leave ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reject pending leave with remarks sets status to REJECTED")
    void reject_pendingLeave_setsRejectedStatus() {
        LeaveRequest pending = new LeaveRequest();
        pending.setId("lr-009");
        pending.setEmployeeId(EMP_ID);
        pending.setStatus(LeaveStatus.PENDING);
        pending.setLeaveType(LeaveType.SICK);
        pending.setStartDate(LocalDate.now().plusDays(1));
        pending.setEndDate(LocalDate.now().plusDays(1));
        pending.setReason("Sick day request");
        pending.setCreatedAt(Instant.now());

        when(requests.findById("lr-009")).thenReturn(Optional.of(pending));
        when(employees.findById(EMP_ID)).thenReturn(Optional.of(activeEmployee()));
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse resp = service.reject("lr-009", hrActor(), "Insufficient medical certificate");

        assertThat(resp.status()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(resp.decisionRemarks()).isEqualTo("Insufficient medical certificate");
    }

    @Test
    @DisplayName("Reject without remarks returns 400")
    void reject_blankRemarks_throws400() {
        assertThatThrownBy(() -> service.reject("lr-010", hrActor(), ""))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Remarks are required");
    }

    // ─── Balance view ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Balance returns correct available and used counts for current year")
    void balance_returnsCurrentYearBalance() {
        int year = LocalDate.now().getYear();
        LeaveBalance bal = fullBalance();
        when(balances.findByEmployeeIdAndYear(EMP_ID, year)).thenReturn(Optional.of(bal));

        LeaveBalanceResponse resp = service.balance(EMP_ID);

        assertThat(resp.casualAvailable()).isEqualTo(12);
        assertThat(resp.sickAvailable()).isEqualTo(10);
        assertThat(resp.earnedAvailable()).isEqualTo(15);
        assertThat(resp.casualUsed()).isEqualTo(0);
    }

    // ─── Null employeeId guard ────────────────────────────────────────────────

    @Test
    @DisplayName("Apply with null employeeId returns 400 BAD_REQUEST")
    void apply_nullEmployeeId_throws400() {
        ApplyLeaveRequest req = new ApplyLeaveRequest(LeaveType.CASUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "Test reason");

        assertThatThrownBy(() -> service.apply(req, null))
                .isInstanceOf(ResponseStatusException.class);
    }
}
