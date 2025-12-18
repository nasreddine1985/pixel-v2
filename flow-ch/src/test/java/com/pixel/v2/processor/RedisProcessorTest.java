package com.pixel.v2.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Unit test for RedisProcessor Tests Redis GET and SETEX operations with various scenarios
 * including success, failure, and edge cases
 */
@CamelSpringBootTest
@SpringBootTest(classes = RedisProcessorTestApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RedisProcessorTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Produce("direct:test-input")
    private ProducerTemplate producer;

    @EndpointInject("mock:sink")
    private MockEndpoint mockSink;

    @EndpointInject("mock:error")
    private MockEndpoint mockError;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockSink.reset();
        mockError.reset();

        // Setup RedisTemplate mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Test successful Redis GET operation with cache hit
     */
    @Test
    public void testRedisGetCacheHit() throws Exception {
        String cacheKey = "flow_config_PACS008";
        String cachedValue = "{\"flowCode\":\"PACS008\",\"enabled\":true}";

        // Mock Redis GET returning a value (cache hit)
        when(valueOperations.get(cacheKey)).thenReturn(cachedValue);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-get-hit")
                        .setHeader("RedisCommand", constant("GET"))
                        .setHeader("CacheKey", constant(cacheKey)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        Exchange resultExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals(cachedValue, resultExchange.getIn().getBody(String.class));

        verify(valueOperations).get(cacheKey);
    }

    /**
     * Test Redis GET operation with cache miss
     */
    @Test
    public void testRedisGetCacheMiss() throws Exception {
        String cacheKey = "flow_config_NONEXISTENT";

        // Mock Redis GET returning null (cache miss)
        when(valueOperations.get(cacheKey)).thenReturn(null);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-get-miss")
                        .setHeader("RedisCommand", constant("GET"))
                        .setHeader("CacheKey", constant(cacheKey)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        Exchange resultExchange = mockSink.getReceivedExchanges().get(0);
        assertNull(resultExchange.getIn().getBody());

        verify(valueOperations).get(cacheKey);
    }

    /**
     * Test Redis GET operation with error
     */
    @Test
    public void testRedisGetError() throws Exception {
        String cacheKey = "flow_config_ERROR";

        // Mock Redis GET throwing an exception
        when(valueOperations.get(cacheKey))
                .thenThrow(new RuntimeException("Redis connection failed"));

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-get-error")
                        .setHeader("RedisCommand", constant("GET"))
                        .setHeader("CacheKey", constant(cacheKey)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        Exchange resultExchange = mockSink.getReceivedExchanges().get(0);
        assertNull(resultExchange.getIn().getBody());

        verify(valueOperations).get(cacheKey);
    }

    /**
     * Test successful Redis SETEX operation
     */
    @Test
    public void testRedisSetExSuccess() throws Exception {
        String cacheKey = "flow_config_PACS008";
        String flowConfig = "{\"flowCode\":\"PACS008\",\"enabled\":true}";
        Integer ttl = 3600;

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-setex-success")
                        .setHeader("RedisCommand", constant("SETEX"))
                        .setHeader("CacheKey", constant(cacheKey))
                        .setHeader("FlowConfiguration", constant(flowConfig))
                        .setHeader("CacheTTL", constant(ttl)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        verify(valueOperations).set(cacheKey, flowConfig, ttl, TimeUnit.SECONDS);
    }

    /**
     * Test Redis SETEX operation with missing value
     */
    @Test
    public void testRedisSetExMissingValue() throws Exception {
        String cacheKey = "flow_config_PACS008";
        Integer ttl = 3600;

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-setex-missing-value")
                        .setHeader("RedisCommand", constant("SETEX"))
                        .setHeader("CacheKey", constant(cacheKey))
                        // FlowConfiguration header is missing
                        .setHeader("CacheTTL", constant(ttl)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        // Verify that SET was not called due to missing value
        verify(valueOperations, org.mockito.Mockito.never()).set(anyString(), anyString(),
                any(Integer.class), any(TimeUnit.class));
    }

    /**
     * Test Redis SETEX operation with missing TTL
     */
    @Test
    public void testRedisSetExMissingTtl() throws Exception {
        String cacheKey = "flow_config_PACS008";
        String flowConfig = "{\"flowCode\":\"PACS008\",\"enabled\":true}";

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-setex-missing-ttl")
                        .setHeader("RedisCommand", constant("SETEX"))
                        .setHeader("CacheKey", constant(cacheKey))
                        .setHeader("FlowConfiguration", constant(flowConfig))
                        // CacheTTL header is missing
                        .process("redisProcessor").to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        // Verify that SET was not called due to missing TTL
        verify(valueOperations, org.mockito.Mockito.never()).set(anyString(), anyString(),
                any(Integer.class), any(TimeUnit.class));
    }

    /**
     * Test Redis SETEX operation with error
     */
    @Test
    public void testRedisSetExError() throws Exception {
        String cacheKey = "flow_config_PACS008";
        String flowConfig = "{\"flowCode\":\"PACS008\",\"enabled\":true}";
        Integer ttl = 3600;

        // Mock Redis SETEX throwing an exception
        doThrow(new RuntimeException("Redis connection failed")).when(valueOperations)
                .set(eq(cacheKey), eq(flowConfig), eq(ttl), eq(TimeUnit.SECONDS));

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-redis-setex-error")
                        .setHeader("RedisCommand", constant("SETEX"))
                        .setHeader("CacheKey", constant(cacheKey))
                        .setHeader("FlowConfiguration", constant(flowConfig))
                        .setHeader("CacheTTL", constant(ttl)).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        producer.sendBody("test message");

        mockSink.assertIsSatisfied(5000);

        verify(valueOperations).set(cacheKey, flowConfig, ttl, TimeUnit.SECONDS);
    }

    /**
     * Test unsupported Redis command
     */
    @Test
    public void testUnsupportedRedisCommand() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Global exception handler
                onException(IllegalArgumentException.class)
                        .setHeader("ErrorType", simple("${exception.class.simpleName}"))
                        .setHeader("ErrorMessage", simple("${exception.message}")).to("mock:error")
                        .handled(true);

                from("direct:test-input").routeId("test-unsupported-command")
                        .setHeader("RedisCommand", constant("DELETE"))
                        .setHeader("CacheKey", constant("test_key")).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockError.expectedMessageCount(1);

        producer.sendBody("test message");

        mockError.assertIsSatisfied(5000);

        Exchange errorExchange = mockError.getReceivedExchanges().get(0);
        assertEquals("IllegalArgumentException", errorExchange.getIn().getHeader("ErrorType"));
        assertEquals("Unsupported Redis command: DELETE",
                errorExchange.getIn().getHeader("ErrorMessage"));
    }

    /**
     * Test Redis operations with various cache keys
     */
    @Test
    public void testMultipleCacheKeys() throws Exception {
        Map<String, String> testData = new HashMap<>();
        testData.put("flow_config_PACS008", "{\"flowCode\":\"PACS008\"}");
        testData.put("flow_config_PACS004", "{\"flowCode\":\"PACS004\"}");
        testData.put("flow_config_PAIN001", "{\"flowCode\":\"PAIN001\"}");

        // Mock Redis GET for each key
        testData.forEach((key, value) -> when(valueOperations.get(key)).thenReturn(value));

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-multiple-keys")
                        .setHeader("RedisCommand", constant("GET")).process("redisProcessor")
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(3);

        // Test each cache key
        for (String key : testData.keySet()) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("CacheKey", key);
            producer.sendBodyAndHeaders("test message", headers);
        }

        mockSink.assertIsSatisfied(5000);

        // Verify results
        for (int i = 0; i < testData.size(); i++) {
            Exchange exchange = mockSink.getReceivedExchanges().get(i);
            String cacheKey = exchange.getIn().getHeader("CacheKey", String.class);
            String expectedValue = testData.get(cacheKey);
            assertEquals(expectedValue, exchange.getIn().getBody(String.class));
        }

        // Verify all GET operations were called
        testData.keySet().forEach(key -> verify(valueOperations).get(key));
    }
}
