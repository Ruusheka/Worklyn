package com.worklyn.modules.distributed.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of a single crawl operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {
    private String sourceUrl;
    private int totalFound;
    private int deduplicated;
    private List<JobListing> listings;
    private String errorMessage;
    private boolean success;
}
