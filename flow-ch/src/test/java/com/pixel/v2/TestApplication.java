package com.pixel.v2;

import static org.mockito.Mockito.mock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Test application configuration for unit tests Provides minimal Spring Boot setup without complex
 * dependencies
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        @Primary
        public RedisTemplate<String, Object> mockRedisTemplate() {
            return mock(RedisTemplate.class);
        }
    }
}
