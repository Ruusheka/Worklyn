package com.worklyn.modules.distributed.autocomplete;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Employee prefix-based autocomplete service using an in-memory Radix Trie
 * plus a Redis Sorted Set as a secondary index.
 *
 * <p><strong>Hybrid approach:</strong>
 * <ul>
 *   <li>The in-memory {@link RadixTrie} is used as the primary lookup (sub-millisecond).</li>
 *   <li>The Redis Sorted Set {@code autocomplete:employees} provides a distributed secondary
 *       index using lexicographical ordering via {@code ZRANGEBYLEX}.</li>
 * </ul>
 *
 * <p><strong>Ranking:</strong> Results are sorted alphabetically by full name (deterministic).</p>
 *
 * <p><strong>Minimum query length:</strong> queries shorter than 2 characters return an empty list.</p>
 *
 * <p><strong>Maximum limit:</strong> enforced at 50 results.</p>
 *
 * <p><strong>Integration note:</strong> Employee data is loaded via {@link EmployeeSearchDataProvider}.
 * Person 1's EmployeeRepository must be wired through that interface for real data.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutocompleteService {

    private static final String REDIS_KEY = "autocomplete:employees";
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LIMIT = 10;

    private final EmployeeSearchDataProvider dataProvider;
    private final StringRedisTemplate redisTemplate;

    private final RadixTrie trie = new RadixTrie();

    /** Reload the in-memory trie from the data provider on startup. */
    @PostConstruct
    public void initializeIndex() {
        try {
            List<SuggestionResponse> all = dataProvider.getAllEmployeeSuggestions();
            refreshIndex(all);
            log.info("Autocomplete index initialized with {} employees", all.size());
        } catch (Exception e) {
            log.warn("Failed to initialize autocomplete index: {}. Index will be empty until refreshed.", e.getMessage());
        }
    }

    /**
     * Search employees by name prefix.
     *
     * @param query  the prefix string (min 2 chars, trimmed and lowercased)
     * @param limit  max results (capped at {@value MAX_LIMIT})
     * @return a list of matching employee suggestions, sorted alphabetically
     */
    public List<SuggestionResponse> suggestEmployees(String query, int limit) {
        if (query == null || query.trim().length() < MIN_QUERY_LENGTH) {
            return Collections.emptyList();
        }

        int effectiveLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);

        // Primary: in-memory trie (fast)
        List<SuggestionResponse> results = trie.search(normalizedQuery, effectiveLimit);

        // Secondary: Redis sorted set (distributed/persistent index), used if trie is empty
        if (results.isEmpty()) {
            results = queryRedisIndex(normalizedQuery, effectiveLimit);
        }

        // Sort alphabetically for deterministic ranking
        results.sort(Comparator.comparing(SuggestionResponse::getFullName, String.CASE_INSENSITIVE_ORDER));

        // Deduplicate by employeeId
        return deduplicateById(results, effectiveLimit);
    }

    /**
     * Refresh the in-memory trie and Redis sorted set with updated employee data.
     * Called when employee data changes.
     */
    public void refreshIndex(List<SuggestionResponse> employees) {
        trie.clear();
        try {
            redisTemplate.delete(REDIS_KEY);
        } catch (Exception e) {
            log.warn("Failed to clear Redis autocomplete index: {}", e.getMessage());
        }

        for (SuggestionResponse emp : employees) {
            if (emp.getFullName() != null && !emp.getFullName().isBlank()) {
                trie.insert(emp.getFullName(), emp);

                // Redis sorted set: lexicographical member = "fullname:empcode:deptid:empid"
                String member = buildRedisMember(emp);
                try {
                    redisTemplate.opsForZSet().add(REDIS_KEY, member, 0.0);
                } catch (Exception e) {
                    log.warn("Failed to add employee [{}] to Redis autocomplete index: {}", emp.getEmployeeCode(), e.getMessage());
                }
            }
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<SuggestionResponse> queryRedisIndex(String prefix, int limit) {
        try {
            String min = "[" + prefix;
            String max = "[" + prefix + "\uffff";
            org.springframework.data.domain.Range<String> range = org.springframework.data.domain.Range.closed(prefix, prefix + "\uffff");
            org.springframework.data.redis.connection.Limit redisLimit = org.springframework.data.redis.connection.Limit.limit().count(limit);
            Set<String> members = redisTemplate.opsForZSet().rangeByLex(REDIS_KEY, range, redisLimit);

            if (members == null) return Collections.emptyList();
            List<SuggestionResponse> results = new ArrayList<>();
            for (String member : members) {
                SuggestionResponse parsed = parseRedisMember(member);
                if (parsed != null) results.add(parsed);
            }
            return results;
        } catch (Exception e) {
            log.warn("Redis autocomplete query failed: {}. Returning empty result.", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildRedisMember(SuggestionResponse emp) {
        return emp.getFullName().toLowerCase(Locale.ROOT).trim()
                + ":" + nullSafe(emp.getEmployeeCode())
                + ":" + nullSafe(emp.getDepartment())
                + ":" + nullSafe(emp.getEmployeeId());
    }

    private SuggestionResponse parseRedisMember(String member) {
        try {
            String[] parts = member.split(":", 4);
            if (parts.length < 4) return null;
            return SuggestionResponse.builder()
                    .fullName(parts[0])
                    .employeeCode(parts[1])
                    .department(parts[2])
                    .employeeId(parts[3])
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }

    private List<SuggestionResponse> deduplicateById(List<SuggestionResponse> input, int limit) {
        Set<String> seen = new LinkedHashSet<>();
        List<SuggestionResponse> result = new ArrayList<>();
        for (SuggestionResponse s : input) {
            String key = s.getEmployeeId() != null ? s.getEmployeeId() : s.getFullName();
            if (seen.add(key)) {
                result.add(s);
                if (result.size() >= limit) break;
            }
        }
        return result;
    }
}
