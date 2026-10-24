package com.worklyn.modules.distributed.crawler;

import com.worklyn.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobCrawlerServiceTest {

    private JobCrawlerService crawlerService;
    private CrawlerProperties properties;

    @BeforeEach
    void setUp() {
        properties = new CrawlerProperties();
        properties.setAllowedDomains(List.of("careers.example.com", "jobs.techcorp.io"));
        properties.setMaxResponseSizeBytes(1024 * 1024);
        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(1000);

        crawlerService = new JobCrawlerService(properties) {
            @Override
            protected void validateHostResolution(String host) {
                // Mock out DNS resolution in unit test environment to prevent external network calls
            }

            @Override
            protected String fetchHtml(URI uri) throws IOException {
                if (uri.getHost().contains("oversized")) {
                    return "<html>" + "A".repeat(2 * 1024 * 1024) + "</html>";
                }
                return """
                        <html>
                          <body>
                            <div class="job-listing">
                              <h2 class="job-title">Senior Java Engineer</h2>
                              <span class="location">Remote</span>
                            </div>
                            <div class="job-listing">
                              <h2 class="job-title">Senior Java Engineer</h2>
                              <span class="location">Remote</span>
                            </div>
                            <div class="job-listing">
                              <h2 class="job-title">Engineering Manager</h2>
                              <span class="location">San Francisco, CA</span>
                            </div>
                          </body>
                        </html>
                        """;
            }
        };
    }

    @Test
    @DisplayName("Whitelisted domain is allowed, parsed, and duplicate listings are deduplicated")
    void testCrawlAllowedDomain() {
        CrawlResult result = crawlerService.crawlSource("https://careers.example.com/openings");

        assertTrue(result.isSuccess());
        assertEquals(3, result.getTotalFound());
        assertEquals(2, result.getDeduplicated(), "Duplicates must be filtered out");
        assertEquals(2, result.getListings().size());
        assertEquals("Senior Java Engineer", result.getListings().get(0).getTitle());
    }

    @Test
    @DisplayName("Disallowed domain is blocked with BadRequestException")
    void testDisallowedDomainBlocked() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> crawlerService.crawlSource("https://malicious-site.com/jobs"));
        assertEquals("DOMAIN_NOT_ALLOWED", ex.getCode());
    }

    @Test
    @DisplayName("Malformed or non-HTTP scheme is rejected")
    void testInvalidSchemeRejected() {
        assertThrows(BadRequestException.class,
                () -> crawlerService.crawlSource("ftp://careers.example.com/jobs"));
    }
}
