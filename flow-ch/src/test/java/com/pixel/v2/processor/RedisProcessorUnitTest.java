package com.pixel.v2.processor;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Simple unit tests for RedisProcessor Tests basic Redis operations with correct header names
 */
@ExtendWith(MockitoExtension.class)
public class RedisProcessorUnitTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisProcessor redisProcessor;
    private CamelContext camelContext;

    @BeforeEach
    public void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisProcessor = new RedisProcessor();

        // Use reflection to inject the mock RedisTemplate
        try {
            java.lang.reflect.Field field = RedisProcessor.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(redisProcessor, redisTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock RedisTemplate", e);
        }

        camelContext = new DefaultCamelContext();
    }

    @Test
    public void testRedisGetWithCacheHit() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "GET");
        exchange.getIn().setHeader("CacheKey", "test-key");

        when(valueOperations.get("test-key")).thenReturn("cached-value");

        redisProcessor.process(exchange);

        assertEquals("cached-value", exchange.getIn().getBody());
        verify(valueOperations).get("test-key");
    }

    @Test
    public void testRedisGetWithCacheMiss() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "GET");
        exchange.getIn().setHeader("CacheKey", "missing-key");

        when(valueOperations.get("missing-key")).thenReturn(null);

        redisProcessor.process(exchange);

        assertNull(exchange.getIn().getBody());
        verify(valueOperations).get("missing-key");
    }

    @Test
    public void testRedisSetExSuccess() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "SETEX");
        exchange.getIn().setHeader("CacheKey", "test-key");
        exchange.getIn().setHeader("CacheTTL", 3600);
        exchange.getIn().setHeader("FlowConfiguration", "test-value");

        redisProcessor.process(exchange);

        verify(valueOperations).set("test-key", "test-value", 3600, TimeUnit.SECONDS);
    }

    @Test
    public void testRedisSetExWithNullValue() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "SETEX");
        exchange.getIn().setHeader("CacheKey", "test-key");
        exchange.getIn().setHeader("CacheTTL", 3600);
        // FlowConfiguration header is null

        redisProcessor.process(exchange);

        // Should not call set when value is null
        verify(valueOperations, never()).set(anyString(), anyString(), anyInt(),
                any(TimeUnit.class));
    }

    @Test
    public void testRedisSetExWithNullTtl() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "SETEX");
        exchange.getIn().setHeader("CacheKey", "test-key");
        exchange.getIn().setHeader("FlowConfiguration", "test-value");
        // CacheTTL header is null

        redisProcessor.process(exchange);

        // Should not call set when TTL is null
        verify(valueOperations, never()).set(anyString(), anyString(), anyInt(),
                any(TimeUnit.class));
    }

    @Test
    public void testUnsupportedRedisCommand() {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "UNSUPPORTED");
        exchange.getIn().setHeader("CacheKey", "test-key");

        assertThrows(IllegalArgumentException.class, () -> {
            redisProcessor.process(exchange);
        });

        verify(valueOperations, never()).get(anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), anyInt(),
                any(TimeUnit.class));
    }

    @Test
    public void testRedisGetWithError() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setHeader("RedisCommand", "GET");
        exchange.getIn().setHeader("CacheKey", "error-key");

        when(valueOperations.get("error-key"))
                .thenThrow(new RuntimeException("Redis connection error"));

        redisProcessor.process(exchange);

        // Should handle error gracefully and return null
        assertNull(exchange.getIn().getBody());
        verify(valueOperations).get("error-key");
    }
}
