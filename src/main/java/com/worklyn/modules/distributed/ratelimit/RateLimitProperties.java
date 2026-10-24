package com.worklyn.modules.distributed.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {

    private PolicyConfig authenticated = new PolicyConfig(100L, 10.0);
    private PolicyConfig publicTier = new PolicyConfig(20L, 2.0);
    private PolicyConfig expensive = new PolicyConfig(5L, 0.2);

    @Data
    public static class PolicyConfig {
        private long capacity;
        private double refillRate;

        public PolicyConfig() {
        }

        public PolicyConfig(long capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
        }
    }
}
