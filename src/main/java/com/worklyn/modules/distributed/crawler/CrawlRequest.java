package com.worklyn.modules.distributed.crawler;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {
    @NotBlank(message = "Source URI must not be blank")
    private String sourceUri;
}
