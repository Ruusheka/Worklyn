package com.worklyn.modules.distributed.autocomplete;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Default stub implementation of {@link EmployeeSearchDataProvider}.
 *
 * <p><strong>Integration Note:</strong> This stub returns an empty list because
 * Person 1's EmployeeRepository is not yet implemented in this codebase.
 * Once Person 1's {@code EmployeeRepository} and {@code Employee} model are available,
 * replace this class with an implementation that queries the MongoDB {@code employees} collection.</p>
 *
 * <p>Wiring: inject Person 1's EmployeeService/Repository here and map Employee records
 * to SuggestionResponse (employeeId, fullName, employeeCode, department).</p>
 */
@Slf4j
@Component
public class DefaultEmployeeSearchDataProvider implements EmployeeSearchDataProvider {

    @Override
    public List<SuggestionResponse> getAllEmployeeSuggestions() {
        // Integration point: replace with real employee repository query when Person 1's module is ready.
        log.debug("EmployeeSearchDataProvider: returning empty list (Person 1 integration pending)");
        return Collections.emptyList();
    }
}
