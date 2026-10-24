package com.worklyn.modules.distributed.shortlink;

import com.worklyn.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * POST /api/v1/shortlinks — create a short link.
     * Requires authentication.
     */
    @PostMapping("/api/v1/shortlinks")
    public ResponseEntity<ShortLinkResponse> createShortLink(
            @Valid @RequestBody ShortLinkRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String creatorId = (principal != null) ? principal.getId() : null;
        ShortLinkResponse response = shortLinkService.createShortLink(
                request.getDestinationUrl(),
                request.getTtlDays(),
                creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /s/{code} — resolve and redirect to the destination URL.
     * Public (no auth required). Returns HTTP 302.
     */
    @GetMapping("/s/{code}")
    public ResponseEntity<Void> resolveShortLink(@PathVariable String code) {
        String destinationUrl = shortLinkService.resolveShortLink(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destinationUrl))
                .build();
    }
}
