package com.worklyn.modules.leave.controller;

import com.worklyn.modules.integration.ActorContext;
import com.worklyn.modules.leave.dto.*;
import com.worklyn.modules.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/leave", "/api/v1/leaves"})
public class LeaveController {
    private final LeaveService service;

    public LeaveController(LeaveService s) {
        service = s;
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponse apply(@Valid @RequestBody ApplyLeaveRequest req, Authentication auth) {
        return service.apply(req, ActorContext.from(auth).employeeId());
    }

    @GetMapping({"/balance", "/balances"})
    public LeaveBalanceResponse balance(Authentication auth) {
        return service.balance(ActorContext.from(auth).employeeId());
    }

    @GetMapping("/my-requests")
    public List<LeaveResponse> mine(Authentication auth) {
        return service.myRequests(ActorContext.from(auth).employeeId());
    }

    @GetMapping({"/pending-approvals", "/pending"})
    public List<LeaveResponse> pendingApprovals(Authentication auth) {
        return service.pendingApprovals(ActorContext.from(auth));
    }

    @GetMapping({"/all", "/requests"})
    public List<LeaveResponse> allRequests(Authentication auth) {
        return service.allRequests(ActorContext.from(auth));
    }

    @PostMapping("/{id}/approve")
    public LeaveResponse approve(@PathVariable String id, @RequestBody(required = false) LeaveDecisionRequest req, Authentication auth) {
        ActorContext a = ActorContext.from(auth);
        return service.approve(id, a, req == null ? null : req.remarks(), req == null ? null : req.idempotencyKey());
    }

    @PostMapping("/{id}/reject")
    public LeaveResponse reject(@PathVariable String id, @RequestBody(required = false) LeaveDecisionRequest req, Authentication auth) {
        return service.reject(id, ActorContext.from(auth), req == null ? null : req.remarks());
    }

    @PostMapping("/{id}/cancel")
    public LeaveResponse cancel(@PathVariable String id, Authentication auth) {
        return service.cancel(id, ActorContext.from(auth));
    }
}
