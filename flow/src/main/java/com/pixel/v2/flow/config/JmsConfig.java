package com.pixel.v2.flow.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JmsConfig {

    @Value("${spring.artemis.broker-url}")
    private String brokerUrl;

    @Value("${spring.artemis.user}")
    private String username;

    @Value("${spring.artemis.password}")
    private String password;

    @Bean
    @Primary
    public ActiveMQConnectionFactory artemisConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);

        // Optimize connection factory for high-throughput message consumption
        // Configure connection pooling and performance settings
        connectionFactory.setConnectionTTL(60000); // 60 seconds connection TTL
        connectionFactory.setClientFailureCheckPeriod(30000); // 30 seconds check period
        connectionFactory.setCallTimeout(30000); // 30 seconds call timeout
        connectionFactory.setCallFailoverTimeout(30000); // 30 seconds failover timeout

        // Configure consumer settings for better throughput
        connectionFactory.setConsumerWindowSize(1024 * 1024); // 1MB consumer window
        connectionFactory.setConsumerMaxRate(-1); // Unlimited consumer rate
        connectionFactory.setConfirmationWindowSize(1024 * 1024); // 1MB confirmation window

        // Configure producer settings
        connectionFactory.setProducerWindowSize(1024 * 1024); // 1MB producer window
        connectionFactory.setProducerMaxRate(-1); // Unlimited producer rate

        // Configure threading and connection settings
        connectionFactory.setUseGlobalPools(false); // Use dedicated thread pools
        connectionFactory.setThreadPoolMaxSize(30); // Maximum thread pool size
        connectionFactory.setScheduledThreadPoolMaxSize(5); // Scheduled thread pool size

        return connectionFactory;
    }
}
