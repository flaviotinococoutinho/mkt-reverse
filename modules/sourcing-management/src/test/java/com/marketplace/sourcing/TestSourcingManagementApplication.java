package com.marketplace.sourcing;

import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.id.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.marketplace")
@EntityScan(basePackages = "com.marketplace")
@EnableJpaRepositories(basePackages = "com.marketplace")
@EnableJpaAuditing
public class TestSourcingManagementApplication {

    @Bean
    public IdGenerator idGenerator() {
        return new SnowflakeIdGenerator(1, 1);
    }
}
