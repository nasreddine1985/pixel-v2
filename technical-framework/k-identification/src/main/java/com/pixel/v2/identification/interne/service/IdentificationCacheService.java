package com.pixel.v2.identification.interne.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Cache Management Service for k-identification-interne module
 * Provides high-level cache operations and statistics
 */
@Service
public class IdentificationCacheService {

    private static final Logger logger = LoggerFactory.getLogger(IdentificationCacheService.class);
    
    private final CacheManager cacheManager;
    private static final String DEFAULT_CACHE_NAME = "flowConfigCache";
    
    public IdentificationCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get flow configuration from cache
     */
    public Optional<String> getFlowConfiguration(String flowCode) {
        return getCachedValue(DEFAULT_CACHE_NAME, flowCode);
    }

    /**
     * Put flow configuration into cache
     */
    public void putFlowConfiguration(String flowCode, String configuration) {
        putCachedValue(DEFAULT_CACHE_NAME, flowCode, configuration);
    }

    /**
     * Remove flow configuration from cache
     */
    public void evictFlowConfiguration(String flowCode) {
        evictCachedValue(DEFAULT_CACHE_NAME, flowCode);
    }

    /**
     * Clear all flow configurations from cache
     */
    public void clearFlowConfigurations() {
        clearCache(DEFAULT_CACHE_NAME);
    }

    /**
     * Get cached value from specified cache
     */
    public Optional<String> getCachedValue(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found: {}", cacheName);
                return Optional.empty();
            }
            
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                Object value = valueWrapper.get();
                if (value instanceof String) {
                    logger.debug("Cache HIT for key: {} in cache: {}", key, cacheName);
                    return Optional.of((String) value);
                } else {
                    logger.warn("Cached value is not a String for key: {} in cache: {}", key, cacheName);
                }
            }
            
            logger.debug("Cache MISS for key: {} in cache: {}", key, cacheName);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error retrieving from cache: {} with key: {}", cacheName, key, e);
            return Optional.empty();
        }
    }

    /**
     * Put value into specified cache
     */
    public void putCachedValue(String cacheName, String key, String value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found for PUT operation: {}", cacheName);
                return;
            }
            
            cache.put(key, value);
            logger.debug("Cache PUT successful for key: {} in cache: {}", key, cacheName);
        } catch (Exception e) {
            logger.error("Error putting value into cache: {} with key: {}", cacheName, key, e);
        }
    }

    /**
     * Evict specific key from cache
     */
    public void evictCachedValue(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found for EVICT operation: {}", cacheName);
                return;
            }
            
            cache.evict(key);
            logger.debug("Cache EVICT successful for key: {} in cache: {}", key, cacheName);
        } catch (Exception e) {
            logger.error("Error evicting from cache: {} with key: {}", cacheName, key, e);
        }
    }

    /**
     * Clear entire cache
     */
    public void clearCache(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found for CLEAR operation: {}", cacheName);
                return;
            }
            
            cache.clear();
            logger.info("Cache CLEAR successful for cache: {}", cacheName);
        } catch (Exception e) {
            logger.error("Error clearing cache: {}", cacheName, e);
        }
    }

    /**
     * Get cache statistics (if available)
     */
    public Map<String, Object> getCacheStatistics(String cacheName) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                stats.put("error", "Cache not found: " + cacheName);
                return stats;
            }
            
            stats.put("cacheName", cacheName);
            stats.put("cacheType", cache.getClass().getSimpleName());
            
            // Try to get Caffeine statistics if available
            Object nativeCache = cache.getNativeCache();
            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
                
                com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = caffeineCache.stats();
                stats.put("hitCount", caffeineStats.hitCount());
                stats.put("missCount", caffeineStats.missCount());
                stats.put("hitRate", caffeineStats.hitRate());
                stats.put("evictionCount", caffeineStats.evictionCount());
                stats.put("estimatedSize", caffeineCache.estimatedSize());
            } else {
                stats.put("note", "Detailed statistics not available for this cache type");
            }
            
        } catch (Exception e) {
            logger.error("Error getting cache statistics for: {}", cacheName, e);
            stats.put("error", "Error getting statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get all available cache names
     */
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    /**
     * Check if cache exists
     */
    public boolean cacheExists(String cacheName) {
        return cacheManager.getCache(cacheName) != null;
    }
}