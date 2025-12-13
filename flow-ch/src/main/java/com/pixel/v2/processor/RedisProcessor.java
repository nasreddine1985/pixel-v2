package com.pixel.v2.processor;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Processor for k-identification kamelet
 * 
 * Handles Redis GET and SETEX operations using Spring RedisTemplate
 */
@Component("redisProcessor")
public class RedisProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(RedisProcessor.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        String command = exchange.getIn().getHeader("RedisCommand", String.class);
        String key = exchange.getIn().getHeader("CacheKey", String.class);

        if ("GET".equals(command)) {
            handleGet(exchange, key);
        } else if ("SETEX".equals(command)) {
            handleSetEx(exchange, key);
        } else {
            throw new IllegalArgumentException("Unsupported Redis command: " + command);
        }
    }

    private void handleGet(Exchange exchange, String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            exchange.getIn().setBody(value);
            logger.debug("Redis GET for key '{}': {}", key, value != null ? "HIT" : "MISS");
        } catch (Exception e) {
            logger.error("Redis GET failed for key '{}': {}", key, e.getMessage());
            exchange.getIn().setBody(null);
        }
    }

    private void handleSetEx(Exchange exchange, String key) {
        try {
            String value = exchange.getIn().getHeader("FlowConfiguration", String.class);
            Integer ttl = exchange.getIn().getHeader("CacheTTL", Integer.class);

            if (value != null && ttl != null) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                logger.debug("Redis SETEX for key '{}' with TTL {} seconds: SUCCESS", key, ttl);
            } else {
                logger.warn("Redis SETEX skipped - missing value or TTL for key '{}'", key);
            }
        } catch (Exception e) {
            logger.error("Redis SETEX failed for key '{}': {}", key, e.getMessage());
        }
    }
}
