package com.worklyn.modules.distributed.crawler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.crawler")
public class CrawlerProperties {
    /** Whitelisted hostnames — only these may be crawled */
    private List<String> allowedDomains = new ArrayList<>();
    /** Connection timeout in milliseconds */
    private int connectTimeoutMs = 3000;
    /** Read/request timeout in milliseconds */
    private int readTimeoutMs = 5000;
    /** Maximum HTML response body size in bytes (default 2 MB) */
    private int maxResponseSizeBytes = 2 * 1024 * 1024;
}
