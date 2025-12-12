package com.pixel.v2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database Configuration
 * 
 * Configures DataSource beans for the application
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Primary DataSource bean for PostgreSQL database Explicitly configured with injected
     * properties
     */
    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().url(url).username(username).password(password)
                .driverClassName(driverClassName).build();
    }
}
