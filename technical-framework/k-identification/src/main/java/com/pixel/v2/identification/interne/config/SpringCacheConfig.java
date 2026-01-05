package com.pixel.v2.identification.interne.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Spring Cache Configuration for k-identification-interne module
 * Configures memory-based caching using Caffeine with fallback to ConcurrentMap
 */
@Configuration
@EnableCaching
public class SpringCacheConfig {

    /**
     * Primary cache manager using Caffeine for better performance and features
     * Includes TTL, maximum size limits, and statistics
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure Caffeine cache with TTL and size limits
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)     // 1 hour TTL
            .maximumSize(1000)                        // Max 1000 entries per cache
            .recordStats());                         // Enable statistics
        
        // Pre-configure common cache names
        cacheManager.setCacheNames(java.util.List.of("flowConfigCache", "referentielCache", "identificationCache"));
        
        return cacheManager;
    }

    /**
     * Fallback cache manager using ConcurrentMap (simple memory cache)
     * Used when Caffeine is not available or as secondary cache
     */
    @Bean("concurrentMapCacheManager")
    @ConditionalOnMissingBean(name = "caffeineCacheManager")
    public CacheManager concurrentMapCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Pre-configure common cache names
        cacheManager.setCacheNames(java.util.List.of("flowConfigCache", "referentielCache", "identificationCache"));
        
        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }

    /**
     * Cache configuration properties bean for external configuration
     */
    @Bean
    public CacheConfigProperties cacheConfigProperties() {
        return new CacheConfigProperties();
    }

    /**
     * Configuration properties class for cache settings
     */
    public static class CacheConfigProperties {
        private int ttlHours = 1;
        private int maximumSize = 1000;
        private boolean recordStats = true;
        private String[] cacheNames = {"flowConfigCache", "referentielCache", "identificationCache"};

        // Getters and setters
        public int getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }

        public int getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(int maximumSize) {
            this.maximumSize = maximumSize;
        }

        public boolean isRecordStats() {
            return recordStats;
        }

        public void setRecordStats(boolean recordStats) {
            this.recordStats = recordStats;
        }

        public String[] getCacheNames() {
            return cacheNames;
        }

        public void setCacheNames(String[] cacheNames) {
            this.cacheNames = cacheNames;
        }
    }
}