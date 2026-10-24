package com.worklyn.modules.attendance.dto;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
public record CheckInRequest(Instant timestamp, @NotBlank String idempotencyKey) {}
