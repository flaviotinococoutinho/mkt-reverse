package com.marketplace.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(
    basePackages = {
        "com.marketplace.gateway",
        "com.marketplace.sourcing",
        "com.marketplace.user",
        "com.marketplace.catalog",
        "com.marketplace.shared"
    }
)
@EntityScan(basePackages = {
    "com.marketplace.gateway",
    "com.marketplace.sourcing",
    "com.marketplace.user",
    "com.marketplace.catalog",
    "com.marketplace.shared"
})
@EnableJpaRepositories(basePackages = {
    "com.marketplace.gateway",
    "com.marketplace.sourcing",
    "com.marketplace.user",
    "com.marketplace.catalog",
    "com.marketplace.shared"
})
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
