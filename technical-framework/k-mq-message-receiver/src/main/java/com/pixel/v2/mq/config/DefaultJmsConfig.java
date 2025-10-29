package com.pixel.v2.mq.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Default JMS Configuration for k-mq-message-receiver kamelet Provides JMS connectivity using
 * ActiveMQ Artemis with configurable parameters
 */
@Configuration
public class DefaultJmsConfig {

    @Value("${mq.broker-url}")
    private String brokerUrl;

    @Value("${mq.user}")
    private String username;

    @Value("${mq.password}")
    private String password;

    @Bean("artemisConnectionFactory")
    public ActiveMQConnectionFactory artemisConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
}
