package com.pixel.v2.identification.interne;

import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Test application for k-identification-interne kamelet tests Provides embedded database, cache
 * configuration, and Camel context setup
 */
@SpringBootApplication
@EnableCaching
@Import(CamelAutoConfiguration.class)
public class KIdentificationInterneTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KIdentificationInterneTestApplication.class, args);
    }

    /**
     * Configure embedded H2 database for tests
     */
    @Bean
    public DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    /**
     * Configure in-memory cache manager for Spring Cache
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of("flowConfigCache"));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
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
}