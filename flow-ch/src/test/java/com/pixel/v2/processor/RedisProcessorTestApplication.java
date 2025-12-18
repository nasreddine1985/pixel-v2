package com.pixel.v2.processor;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import static org.mockito.Mockito.mock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Test application for RedisProcessor tests Provides embedded database and Camel context setup
 */
@SpringBootApplication
@Import(CamelAutoConfiguration.class)
public class RedisProcessorTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisProcessorTestApplication.class, args);
    }

    /**
     * Configure embedded H2 database for tests
     */
    @Bean
    public DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:redistest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
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
     * Mock RedisTemplate bean for tests
     */
    @Bean
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, String> redisTemplate() {
        return mock(RedisTemplate.class);
    }

    /**
     * RedisProcessor bean for tests
     */
    @Bean
    public RedisProcessor redisProcessor() {
        return new RedisProcessor();
    }
}
