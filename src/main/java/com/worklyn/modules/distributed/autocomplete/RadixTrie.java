package com.worklyn.modules.distributed.autocomplete;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Thread-safe in-memory Radix Trie for prefix-based employee name lookup.
 *
 * <p>Each trie node stores a map of child characters and a list of {@link SuggestionResponse}
 * entries that match the full path from root to that node.</p>
 *
 * <p>The trie is rebuilt or updated when employee data changes. Reads are safe during
 * iteration via snapshot-on-read pattern.</p>
 */
@Slf4j
public class RadixTrie {

    private final TrieNode root = new TrieNode();

    /**
     * Insert an employee suggestion indexed by their full name (lowercase).
     */
    public synchronized void insert(String key, SuggestionResponse suggestion) {
        if (key == null || key.isBlank()) return;
        String normalizedKey = key.toLowerCase(Locale.ROOT).trim();
        TrieNode current = root;
        for (char c : normalizedKey.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.suggestions.add(suggestion);
    }

    /**
     * Find all suggestions whose indexed key starts with the given prefix.
     * Returns results in alphabetical order, limited to {@code maxResults}.
     */
    public synchronized List<SuggestionResponse> search(String prefix, int maxResults) {
        if (prefix == null || prefix.isBlank()) return Collections.emptyList();
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT).trim();

        TrieNode current = root;
        for (char c : normalizedPrefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) return Collections.emptyList();
        }

        List<SuggestionResponse> results = new ArrayList<>();
        collectAll(current, results, maxResults);
        return results;
    }

    /** Clear all entries. */
    public synchronized void clear() {
        root.children.clear();
        root.suggestions.clear();
    }

    private void collectAll(TrieNode node, List<SuggestionResponse> results, int maxResults) {
        if (results.size() >= maxResults) return;
        results.addAll(node.suggestions);
        // DFS through children in alphabetical order for deterministic ranking
        for (char c : new TreeMap<>(node.children).keySet()) {
            if (results.size() >= maxResults) break;
            collectAll(node.children.get(c), results, maxResults);
        }
    }

    private static class TrieNode {
        final Map<Character, TrieNode> children = new HashMap<>();
        final List<SuggestionResponse> suggestions = new ArrayList<>();
    }
}
