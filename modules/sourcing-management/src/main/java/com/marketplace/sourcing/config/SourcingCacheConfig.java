package com.marketplace.sourcing.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for sourcing operations.
 * 
 * Implements caching for:
 * - Category facets (rarely changes)
 * - MCC category lookups
 * - User preferences
 */
@Configuration
@EnableCaching
public class SourcingCacheConfig {

    public static final String CACHE_MCC_CATEGORIES = "mccCategories";
    public static final String CACHE_CATEGORY_FACETS = "categoryFacets";
    public static final String CACHE_USER_PREFERENCES = "userPreferences";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CACHE_MCC_CATEGORIES,
                CACHE_CATEGORY_FACETS,
                CACHE_USER_PREFERENCES
        );
    }
}