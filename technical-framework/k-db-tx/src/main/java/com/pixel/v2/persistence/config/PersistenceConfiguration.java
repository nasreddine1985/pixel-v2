package com.pixel.v2.persistence.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for the persistence layer Enables JPA repositories, transaction management,
 * and component scanning
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.pixel.v2.persistence.repository")
@ComponentScan(basePackages = "com.pixel.v2.persistence")
@EnableTransactionManagement
public class PersistenceConfiguration {

    // Configuration class - no additional beans needed
    // JPA entities will be discovered automatically by Spring Boot
    // All JPA and transaction configuration is handled via annotations and application.properties

}
