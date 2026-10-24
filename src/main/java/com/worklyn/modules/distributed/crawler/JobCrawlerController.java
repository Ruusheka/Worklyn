package com.worklyn.modules.distributed.crawler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only endpoint to trigger a controlled crawl of an allowlisted job source.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/crawler")
public class JobCrawlerController {

    private final JobCrawlerService crawlerService;

    /**
     * POST /api/v1/crawler/jobs
     * Triggers a crawl of the specified source URI (must be in the allowed-domains list).
     * Restricted to ROLE_ADMIN.
     */
    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrawlResult> crawlJobs(@Valid @RequestBody CrawlRequest request) {
        CrawlResult result = crawlerService.crawlSource(request.getSourceUri());
        return ResponseEntity.ok(result);
    }
}
