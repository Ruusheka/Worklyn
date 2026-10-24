package com.worklyn.modules.payroll.controller;

import com.worklyn.modules.integration.ActorContext;
import com.worklyn.modules.payroll.dto.*;
import com.worklyn.modules.payroll.model.PayrollRecord;
import com.worklyn.modules.payroll.repository.PayrollRepository;
import com.worklyn.modules.payroll.service.PayrollService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {
    private final PayrollService service;
    private final PayrollRepository payroll;

    public PayrollController(PayrollService s, PayrollRepository p) {
        this.service = s;
        this.payroll = p;
    }

    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PayrollProcessResult process(
            @RequestBody ProcessRequest req,
            Authentication auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String header) {
        ActorContext a = ActorContext.from(auth);
        if (!a.isHrOrAdmin()) throw new org.springframework.security.access.AccessDeniedException("Only HR/ADMIN can process payroll");
        String key = (header != null && !header.isBlank()) ? header : req.idempotencyKey();
        return service.process(req.period(), a.userId(), key);
    }

    @GetMapping({"/period/{period}", "/records"})
    public List<PayrollResponse> records(
            @PathVariable(required = false) String period,
            @RequestParam(value = "period", required = false) String paramPeriod,
            Authentication auth) {
        String p = (period != null && !period.isBlank()) ? period : paramPeriod;
        if (p == null || p.isBlank()) {
            p = YearMonth.now().toString();
        }
        ActorContext a = ActorContext.from(auth);
        if (!a.isHrOrAdmin()) throw new org.springframework.security.access.AccessDeniedException("Only HR/ADMIN can view period payroll");
        return service.period(p);
    }

    @GetMapping("/periods")
    public List<String> periods(Authentication auth) {
        List<String> recorded = payroll.findAll().stream()
                .map(PayrollRecord::getPayrollPeriod)
                .filter(x -> x != null && !x.isBlank())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
        if (recorded.isEmpty()) {
            YearMonth now = YearMonth.now();
            return List.of(now.toString(), now.minusMonths(1).toString(), now.minusMonths(2).toString());
        }
        return recorded;
    }

    @GetMapping({"/payslip", "/payslip/{employeeId}"})
    public ResponseEntity<PayrollResponse> payslip(
            @PathVariable(required = false) String employeeId,
            @RequestParam(value = "period", required = false) String period,
            Authentication auth) {
        ActorContext a = ActorContext.from(auth);
        String target = (employeeId != null && !employeeId.isBlank()) ? employeeId : a.employeeId();
        String p = (period != null && !period.isBlank()) ? period : YearMonth.now().toString();
        try {
            return ResponseEntity.ok(service.payslip(target, p, a));
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.ok(null);
            }
            throw ex;
        }
    }

    @PostMapping({"/finalize", "/periods/{period}/finalize"})
    public void finalizePeriod(
            @PathVariable(required = false) String period,
            @RequestBody(required = false) FinalizeRequest req,
            Authentication auth) {
        String p = (period != null && !period.isBlank()) ? period : (req != null ? req.period() : null);
        if (p == null || p.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period is required");
        service.finalizePeriod(p, ActorContext.from(auth));
    }

    public record ProcessRequest(@NotBlank String period, String idempotencyKey) {}
    public record FinalizeRequest(String period) {}
}
