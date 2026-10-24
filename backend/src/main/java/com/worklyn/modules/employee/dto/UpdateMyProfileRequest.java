package com.worklyn.modules.employee.dto;

import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,
        @Size(max = 20) String phone,
        @Size(max = 100) String designation
) {}
