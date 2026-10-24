package com.worklyn.modules.distributed.autocomplete;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AutocompleteServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private AutocompleteService autocompleteService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        EmployeeSearchDataProvider dataProvider = () -> Arrays.asList(
                SuggestionResponse.builder().employeeId("1").fullName("David Chen").employeeCode("EMP-101").department("ENGINEERING").build(),
                SuggestionResponse.builder().employeeId("2").fullName("Daniel Smith").employeeCode("EMP-102").department("FINANCE").build(),
                SuggestionResponse.builder().employeeId("3").fullName("Sarah Jenkins").employeeCode("EMP-103").department("HR").build(),
                SuggestionResponse.builder().employeeId("4").fullName("David Miller").employeeCode("EMP-104").department("SALES").build()
        );

        autocompleteService = new AutocompleteService(dataProvider, redisTemplate);
        autocompleteService.initializeIndex();
    }

    @Test
    @DisplayName("Query with length < 2 returns empty list")
    void testQueryTooShort() {
        assertTrue(autocompleteService.suggestEmployees("", 10).isEmpty());
        assertTrue(autocompleteService.suggestEmployees(" ", 10).isEmpty());
        assertTrue(autocompleteService.suggestEmployees("D", 10).isEmpty());
    }

    @Test
    @DisplayName("Prefix match returns expected suggestions ranked deterministically")
    void testPrefixMatch() {
        List<SuggestionResponse> results = autocompleteService.suggestEmployees("da", 10);
        assertEquals(3, results.size());
        assertEquals("Daniel Smith", results.get(0).getFullName());
        assertEquals("David Chen", results.get(1).getFullName());
        assertEquals("David Miller", results.get(2).getFullName());
    }

    @Test
    @DisplayName("Limits are enforced up to max limit")
    void testLimitEnforced() {
        List<SuggestionResponse> results = autocompleteService.suggestEmployees("da", 2);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("No matches returns empty list")
    void testNoMatch() {
        List<SuggestionResponse> results = autocompleteService.suggestEmployees("xyz", 10);
        assertTrue(results.isEmpty());
    }
}
