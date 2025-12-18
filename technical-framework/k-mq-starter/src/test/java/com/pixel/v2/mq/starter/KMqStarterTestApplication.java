package com.pixel.v2.mq.starter;

import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jms.connection.CachingConnectionFactory;

/**
 * Test application configuration for k-mq-starter kamelet tests
 */
@SpringBootApplication
@Import(CamelAutoConfiguration.class)
public class KMqStarterTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KMqStarterTestApplication.class, args);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean("jmsConnectionFactory")
        public CachingConnectionFactory jmsConnectionFactory() {
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            return new CachingConnectionFactory(connectionFactory);
        }

        @Bean("dataSource")
        public DataSource dataSource() {
            org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
            ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            ds.setUser("sa");
            ds.setPassword("");
            return ds;
        }
    }
}
