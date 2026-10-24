package com.worklyn.modules.distributed.crawler;

import com.worklyn.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Controlled, domain-allowlisted job listing crawler.
 *
 * <p><strong>Safety controls:</strong>
 * <ul>
 *   <li>Only fetches URLs from domains listed in {@code app.crawler.allowed-domains}.</li>
 *   <li>DNS resolution checked — rejects any URL that resolves to a private/loopback/internal address.</li>
 *   <li>Redirect targets are revalidated before following.</li>
 *   <li>Per-host rate limiting: at most 1 request per 2 seconds.</li>
 *   <li>Maximum response body: 2 MB (enforced while reading, not just after download).</li>
 *   <li>Connect timeout: 3 s; read timeout: 5 s.</li>
 * </ul>
 *
 * <p><strong>Extraction strategy:</strong> Generic selectors for common job-site structures.
 * Returns partial or empty results when no recognizable listings are found.</p>
 *
 * <p><strong>Testability:</strong> External HTTP connections are performed only through the
 * {@link #fetchHtml} method, which can be overridden or mocked in tests.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerService {

    private static final String USER_AGENT = "WorklynBot/1.0";
    private static final int RATE_LIMIT_INTERVAL_MS = 2000;

    private final CrawlerProperties crawlerProperties;

    /** Per-host last-request timestamps for rate limiting. Thread-safe. */
    private final ConcurrentHashMap<String, Long> lastRequestTimeByHost = new ConcurrentHashMap<>();

    /**
     * Crawl the given source URI and extract job listings.
     *
     * @param sourceUri the URI to crawl (must be in the allowed-domains list)
     * @return a {@link CrawlResult} containing extracted, deduplicated listings
     * @throws BadRequestException if the URI is disallowed or resolves to an internal address
     */
    public CrawlResult crawlSource(String sourceUri) {
        URI uri = parseAndValidateUri(sourceUri);
        String host = uri.getHost().toLowerCase(Locale.ROOT);

        enforceHostRateLimit(host);

        String html;
        try {
            html = fetchHtml(uri);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Crawl failed for [{}]: {}", sourceUri, e.getMessage());
            return CrawlResult.builder()
                    .sourceUrl(sourceUri)
                    .success(false)
                    .errorMessage("Fetch failed: " + e.getMessage())
                    .listings(Collections.emptyList())
                    .build();
        }

        List<JobListing> raw = parseJobListings(html, sourceUri);
        List<JobListing> deduped = deduplicate(raw);

        return CrawlResult.builder()
                .sourceUrl(sourceUri)
                .success(true)
                .totalFound(raw.size())
                .deduplicated(deduped.size())
                .listings(deduped)
                .build();
    }

    // ─── Visible for override in tests ────────────────────────────────────────

    /**
     * Fetches the HTML content of the given URI with size and timeout limits.
     * Override this method in tests to avoid live HTTP calls.
     */
    protected String fetchHtml(URI uri) throws IOException {
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false); // We handle redirects manually
        conn.setConnectTimeout(crawlerProperties.getConnectTimeoutMs());
        conn.setReadTimeout(crawlerProperties.getReadTimeoutMs());
        conn.setRequestProperty("User-Agent", USER_AGENT);

        int statusCode = conn.getResponseCode();
        // Handle redirects manually to revalidate targets
        if (statusCode >= 300 && statusCode < 400) {
            String location = conn.getHeaderField("Location");
            if (location == null || location.isBlank()) {
                throw new IOException("Redirect response missing Location header");
            }
            URI redirectUri;
            try {
                redirectUri = new URI(location);
                if (!redirectUri.isAbsolute()) {
                    redirectUri = uri.resolve(redirectUri);
                }
            } catch (URISyntaxException e) {
                throw new IOException("Invalid redirect Location: " + location);
            }
            // Revalidate redirect target against allowlist and internal IP check
            parseAndValidateUri(redirectUri.toString());
            conn.disconnect();
            return fetchHtml(redirectUri);
        }

        if (statusCode != 200) {
            conn.disconnect();
            throw new IOException("HTTP " + statusCode + " for " + uri);
        }

        String contentType = conn.getContentType();
        if (contentType != null && !contentType.contains("text/html") && !contentType.contains("text/plain")) {
            conn.disconnect();
            throw new BadRequestException("UNSUPPORTED_CONTENT_TYPE",
                    "Crawler only supports HTML responses, got: " + contentType);
        }

        int maxSize = crawlerProperties.getMaxResponseSizeBytes();
        try (InputStream is = conn.getInputStream()) {
            byte[] buffer = new byte[4096];
            StringBuilder sb = new StringBuilder();
            int totalRead = 0;
            int read;
            while ((read = is.read(buffer)) != -1) {
                totalRead += read;
                if (totalRead > maxSize) {
                    log.warn("Response from [{}] exceeded max size ({} bytes). Truncating.", uri, maxSize);
                    sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    break;
                }
                sb.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    protected void validateHostResolution(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress()
                        || addr.isLinkLocalAddress() || addr.isAnyLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new BadRequestException("SSRF_BLOCKED",
                            "URI host [" + host + "] resolves to a private/internal address");
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("DNS_RESOLUTION_FAILED",
                    "Cannot resolve host: " + host);
        }
    }

    private URI parseAndValidateUri(String rawUri) {
        URI uri;
        try {
            uri = new URI(rawUri).normalize();
        } catch (URISyntaxException e) {
            throw new BadRequestException("INVALID_URI", "Malformed URI: " + rawUri);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new BadRequestException("INVALID_SCHEME", "Only HTTP/HTTPS URIs are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BadRequestException("INVALID_HOST", "URI must have a valid hostname");
        }

        // Allowlist check
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        boolean allowed = crawlerProperties.getAllowedDomains().stream()
                .anyMatch(d -> normalizedHost.equals(d.toLowerCase(Locale.ROOT))
                        || normalizedHost.endsWith("." + d.toLowerCase(Locale.ROOT)));

        if (!allowed) {
            throw new BadRequestException("DOMAIN_NOT_ALLOWED",
                    "Domain [" + host + "] is not in the crawler allowed-domains list");
        }

        // SSRF: resolve and check all IP addresses
        validateHostResolution(host);

        return uri;
    }

    private void enforceHostRateLimit(String host) {
        long now = System.currentTimeMillis();
        Long last = lastRequestTimeByHost.get(host);
        if (last != null) {
            long elapsed = now - last;
            if (elapsed < RATE_LIMIT_INTERVAL_MS) {
                long waitMs = RATE_LIMIT_INTERVAL_MS - elapsed;
                log.debug("Rate limit: waiting {} ms before crawling {}", waitMs, host);
                try {
                    TimeUnit.MILLISECONDS.sleep(waitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        lastRequestTimeByHost.put(host, System.currentTimeMillis());
    }

    private List<JobListing> parseJobListings(String html, String sourceUrl) {
        if (html == null || html.isBlank()) {
            return Collections.emptyList();
        }

        Document doc;
        try {
            doc = Jsoup.parse(html, sourceUrl);
        } catch (Exception e) {
            log.warn("Failed to parse HTML from [{}]: {}", sourceUrl, e.getMessage());
            return Collections.emptyList();
        }

        List<JobListing> listings = new ArrayList<>();

        // Strategy 1: look for common job-listing container patterns
        Elements jobElements = doc.select("[class*=job-listing],[class*=job-card],[class*=position],[data-job-id]");

        for (Element el : jobElements) {
            String title = extractText(el, "[class*=job-title],[class*=title],[data-job-title],h1,h2,h3,h4");
            String location = extractText(el, "[class*=location],[class*=city],[data-location]");
            String link = el.select("a[href]").first() != null ? el.select("a[href]").first().absUrl("href") : sourceUrl;

            if (title != null && !title.isBlank()) {
                listings.add(JobListing.builder()
                        .title(title.trim())
                        .location(location != null ? location.trim() : "")
                        .sourceUrl(link)
                        .build());
            }
        }

        // Strategy 2: generic fallback with structured data patterns
        if (listings.isEmpty()) {
            Elements headings = doc.select("h1,h2,h3");
            for (Element heading : headings) {
                String text = heading.text().trim();
                if (!text.isBlank() && text.length() < 200) {
                    listings.add(JobListing.builder()
                            .title(text)
                            .location("")
                            .sourceUrl(sourceUrl)
                            .build());
                }
            }
        }

        return listings;
    }

    private String extractText(Element parent, String cssSelector) {
        Element el = parent.select(cssSelector).first();
        return el != null ? el.text() : null;
    }

    private List<JobListing> deduplicate(List<JobListing> raw) {
        Set<String> seen = new LinkedHashSet<>();
        List<JobListing> result = new ArrayList<>();
        for (JobListing listing : raw) {
            String key = normalize(listing.getTitle()) + "|" + normalize(listing.getLocation());
            if (seen.add(key)) {
                result.add(listing);
            }
        }
        return result;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
