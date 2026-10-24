package com.worklyn.modules.distributed.autocomplete;

import java.util.List;

/**
 * Decoupled provider interface for employee search data.
 *
 * <p>Person 1's EmployeeRepository is not yet implemented. This interface provides a
 * clean integration point. Inject a stub (DefaultEmployeeSearchDataProvider) or
 * the real implementation once Person 1's module is available.</p>
 */
public interface EmployeeSearchDataProvider {

    /**
     * Return a list of all employees as suggestion candidates.
     * Implementations must return only safe, non-sensitive fields.
     */
    List<SuggestionResponse> getAllEmployeeSuggestions();
}
