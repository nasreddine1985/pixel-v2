package com.pixel.v2;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Test configuration for PixelCamelApplication tests Provides minimal configuration required for
 * testing without external dependencies
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@Import(CamelAutoConfiguration.class)
public class PixelCamelApplicationTestConfig {

    public static void main(String[] args) {
        SpringApplication.run(PixelCamelApplicationTestConfig.class, args);
    }

    /**
     * Configure embedded H2 database for tests
     */
    @Bean
    public DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pixeltest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    /**
     * Configure ActiveMQ connection factory for JMS tests
     */
    @Bean
    public ActiveMQConnectionFactory activeMqConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("vm://test-broker?broker.persistent=false");
        connectionFactory.setTrustAllPackages(true);
        return connectionFactory;
    }

    /**
     * Configure Redis connection factory for tests
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Mock Redis connection for tests
        return new LettuceConnectionFactory("localhost", 6379);
    }

    /**
     * Configure RedisTemplate for String operations
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
