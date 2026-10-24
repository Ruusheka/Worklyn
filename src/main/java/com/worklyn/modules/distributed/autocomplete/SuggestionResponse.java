package com.worklyn.modules.distributed.autocomplete;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Typed employee suggestion DTO returned by the autocomplete endpoint.
 * Only safe, non-sensitive fields are included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {
    private String employeeId;
    private String fullName;
    private String employeeCode;
    private String department;
}
