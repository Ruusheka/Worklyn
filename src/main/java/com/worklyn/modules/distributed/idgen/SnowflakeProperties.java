package com.worklyn.modules.distributed.idgen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.idgen")
public class SnowflakeProperties {
    /** Datacenter ID, range 0-31 */
    private int datacenterId = 1;
    /** Worker/pod ID, range 0-31 */
    private int workerId = 1;
}
