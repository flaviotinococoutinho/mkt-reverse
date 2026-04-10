package com.marketplace.shared.infrastructure.config;

import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.id.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default IdGenerator for local/dev.
 *
 * Configurable via env vars:
 * - MKT_DATACENTER_ID (0-31)
 * - MKT_WORKER_ID (0-31)
 */
@Configuration
public class IdGeneratorConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        long datacenter = readLong("MKT_DATACENTER_ID", 1L);
        long worker = readLong("MKT_WORKER_ID", 1L);
        return new SnowflakeIdGenerator(datacenter, worker);
    }

    private static long readLong(String env, long defaultValue) {
        String raw = System.getenv(env);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

