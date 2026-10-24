package com.worklyn.modules.distributed.autocomplete;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/autocomplete")
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    /**
     * GET /api/v1/autocomplete/employees?q={prefix}&limit={limit}
     * Returns employee name suggestions for a given prefix.
     * Requires authentication (enforced in SecurityConfig).
     */
    @GetMapping("/employees")
    public ResponseEntity<List<SuggestionResponse>> suggestEmployees(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {

        List<SuggestionResponse> suggestions = autocompleteService.suggestEmployees(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}
