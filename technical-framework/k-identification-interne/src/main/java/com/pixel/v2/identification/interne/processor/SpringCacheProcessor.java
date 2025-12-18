package com.pixel.v2.identification.interne.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Spring Cache Processor for k-identification-interne kamelet
 * Handles cache operations using Spring's caching framework with memory cache
 */
@Component("springCacheProcessorInterne")
public class SpringCacheProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(SpringCacheProcessor.class);
    
    private final CacheManager cacheManager;
    
    public SpringCacheProcessor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // Default process method - not used for this processor
    }

    /**
     * Get cached value from Spring cache
     */
    public void getCachedValue(Exchange exchange) throws Exception {
        String cacheName = exchange.getIn().getHeader("SpringCacheName", String.class);
        String cacheKey = exchange.getIn().getHeader("SpringCacheKey", String.class);
        
        if (cacheName == null || cacheKey == null) {
            logger.warn("Missing cache name or key - cacheName: {}, cacheKey: {}", cacheName, cacheKey);
            exchange.getIn().setBody(null);
            return;
        }
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found: {}", cacheName);
                exchange.getIn().setBody(null);
                return;
            }
            
            Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper != null) {
                Object cachedValue = valueWrapper.get();
                logger.debug("Cache HIT for key: {} in cache: {}", cacheKey, cacheName);
                exchange.getIn().setBody(cachedValue);
            } else {
                logger.debug("Cache MISS for key: {} in cache: {}", cacheKey, cacheName);
                exchange.getIn().setBody(null);
            }
        } catch (Exception e) {
            logger.error("Error retrieving from cache: {} with key: {}", cacheName, cacheKey, e);
            exchange.getIn().setBody(null);
        }
    }

    /**
     * Put value into Spring cache
     */
    public void putCachedValue(Exchange exchange) throws Exception {
        String cacheName = exchange.getIn().getHeader("SpringCacheName", String.class);
        String cacheKey = exchange.getIn().getHeader("SpringCacheKey", String.class);
        String cacheValue = exchange.getIn().getHeader("SpringCacheValue", String.class);
        
        if (cacheName == null || cacheKey == null || cacheValue == null) {
            logger.warn("Missing cache parameters - cacheName: {}, cacheKey: {}, cacheValue present: {}", 
                       cacheName, cacheKey, cacheValue != null);
            return;
        }
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found for PUT operation: {}", cacheName);
                return;
            }
            
            cache.put(cacheKey, cacheValue);
            logger.debug("Cache PUT successful for key: {} in cache: {}", cacheKey, cacheName);
        } catch (Exception e) {
            logger.error("Error putting value into cache: {} with key: {}", cacheName, cacheKey, e);
        }
    }

    /**
     * Evict specific key from Spring cache
     */
    public void evictCachedValue(Exchange exchange) throws Exception {
        String cacheName = exchange.getIn().getHeader("SpringCacheName", String.class);
        String cacheKey = exchange.getIn().getHeader("SpringCacheKey", String.class);
        
        if (cacheName == null || cacheKey == null) {
            logger.warn("Missing cache name or key for eviction - cacheName: {}, cacheKey: {}", cacheName, cacheKey);
            return;
        }
        
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("Cache not found for EVICT operation: {}", cacheName);
                return;
            }
            
            cache.evict(cacheKey);
            logger.debug("Cache EVICT successful for key: {} in cache: {}", cacheKey, cacheName);
        } catch (Exception e) {
            logger.error("Error evicting from cache: {} with key: {}", cacheName, cacheKey, e);
        }
    }

    /**
     * Clear entire Spring cache
     */
    public void clearCache(Exchange exchange) throws Exception {
        String cacheName = exchange.getIn().getHeader("SpringCacheName", String.class);
        
        if (cacheName == null) {
            logger.warn("Missing cache name for clear operation");
            return;
        }
        
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
}