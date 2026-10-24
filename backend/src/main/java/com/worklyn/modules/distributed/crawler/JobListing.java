package com.worklyn.modules.distributed.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single extracted job listing from a crawled source.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobListing {
    private String title;
    private String location;
    private String sourceUrl;
    private String company;
    private String description;
}
