package com.pixel.v2.persistence.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Database configuration for the persistence module
 */
@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class DatabaseConfiguration {

    /**
     * Transaction Manager Bean for k-db-tx operations
     * 
     * This transaction manager is used for all database persistence operations performed by the
     * k-db-tx kamelet.
     * 
     * @param entityManagerFactory the JPA entity manager factory (auto-configured by Spring Boot)
     * @return the configured transaction manager
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
