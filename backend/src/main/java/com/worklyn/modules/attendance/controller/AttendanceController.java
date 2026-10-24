package com.worklyn.modules.attendance.controller;
import com.worklyn.modules.attendance.dto.*;
import com.worklyn.modules.attendance.service.AttendanceService;
import com.worklyn.modules.integration.ActorContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
@RestController @RequestMapping("/api/v1/attendance")
public class AttendanceController {
 private final AttendanceService service; public AttendanceController(AttendanceService service){this.service=service;}
 @PostMapping("/check-in") @ResponseStatus(HttpStatus.CREATED) public AttendanceResponse checkIn(@Valid @RequestBody CheckInRequest body, Authentication auth, HttpServletRequest request){ActorContext a=ActorContext.from(auth);return service.checkIn(a.employeeId(),body.timestamp(),request.getRemoteAddr(),body.idempotencyKey());}
 @PostMapping("/check-out") public AttendanceResponse checkOut(@Valid @RequestBody CheckOutRequest body, Authentication auth){ActorContext a=ActorContext.from(auth);return service.checkOut(a.employeeId(),body.timestamp(),body.idempotencyKey());}
 @GetMapping({"/my-history", "/history"})
 public Page<AttendanceResponse> myHistory(
     @RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
     @RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
     @PageableDefault(size=20,sort="attendanceDate",direction=Sort.Direction.DESC) Pageable pageable,
     Authentication auth){
     LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
     LocalDate end = endDate != null ? endDate : LocalDate.now();
     return service.history(ActorContext.from(auth).employeeId(),start,end,pageable);
 }
 @GetMapping("/employee/{empId}") public Page<AttendanceResponse> employeeHistory(@PathVariable String empId,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,@PageableDefault(size=20,sort="attendanceDate",direction=Sort.Direction.DESC) Pageable pageable,Authentication auth){ActorContext a=ActorContext.from(auth);if(!a.hasAnyRole("ADMIN","HR","MANAGER","MGR") && !empId.equals(a.employeeId()))throw new org.springframework.security.access.AccessDeniedException("Not allowed to view this attendance history");return service.history(empId,startDate,endDate,pageable);}
}
