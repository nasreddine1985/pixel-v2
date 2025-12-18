package com.pixel.v2.routes;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * Test application for ChRoute tests Provides embedded database, Redis template configuration, and
 * Camel context setup
 */
@SpringBootApplication
@Import(CamelAutoConfiguration.class)
public class ChRouteTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChRouteTestApplication.class, args);
    }

    /**
     * Configure embedded H2 database for tests
     */
    @Bean
    public DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:chtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
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
     * Configure Redis connection factory for tests using embedded Redis
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Use lettuce connection factory with localhost for tests
        // In a real test environment, you might want to use an embedded Redis
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
