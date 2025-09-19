package com.marketplace.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * User Management Service Application.
 * 
 * Responsible for:
 * - User registration and authentication
 * - Profile management
 * - Role and permission management
 * - KYC (Know Your Customer) verification
 * - User preferences and settings
 */
@SpringBootApplication(scanBasePackages = {
    "com.marketplace.user",
    "com.marketplace.shared"
})
@EntityScan(basePackages = {
    "com.marketplace.user.domain.model",
    "com.marketplace.shared"
})
@EnableJpaRepositories(basePackages = {
    "com.marketplace.user.domain.repository",
    "com.marketplace.user.infrastructure.persistence"
})
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableKafka
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}

