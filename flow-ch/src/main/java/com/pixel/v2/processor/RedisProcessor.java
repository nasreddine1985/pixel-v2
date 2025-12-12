package com.pixel.v2.processor;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis processor for handling cache operations directly with RedisTemplate
 */
@Component("redisProcessor")
public class RedisProcessor implements Processor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        String operation = exchange.getIn().getHeader("RedisOperation", String.class);
        String key = exchange.getIn().getHeader("CacheKey", String.class);

        if (operation == null || key == null) {
            throw new IllegalArgumentException("RedisOperation and CacheKey headers are required");
        }

        switch (operation.toUpperCase()) {
            case "GET":
                handleGet(exchange, key);
                break;
            case "SET":
                handleSet(exchange, key);
                break;
            case "DELETE":
                handleDelete(exchange, key);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Redis operation: " + operation);
        }
    }

    private void handleGet(Exchange exchange, String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            exchange.getIn().setBody(value);
        } catch (Exception e) {
            exchange.getIn().setBody(null);
            exchange.getIn().setHeader("RedisError", e.getMessage());
        }
    }

    private void handleSet(Exchange exchange, String key) {
        try {
            String value = exchange.getIn().getHeader("FlowConfiguration", String.class);
            Long ttl = exchange.getIn().getHeader("CacheTTL", Long.class);

            if (value != null) {
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, value);
                }
            }
        } catch (Exception e) {
            exchange.getIn().setHeader("RedisError", e.getMessage());
        }
    }

    private void handleDelete(Exchange exchange, String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            exchange.getIn().setHeader("RedisError", e.getMessage());
        }
    }
}
