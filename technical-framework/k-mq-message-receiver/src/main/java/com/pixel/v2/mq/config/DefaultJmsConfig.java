package com.pixel.v2.mq.config;

/**
 * Default JMS Configuration for k-mq-message-receiver kamelet Provides JMS connection parameters
 * for Jakarta JMS
 */
public class DefaultJmsConfig {

    private final String brokerUrl;
    private final String username;
    private final String password;

    public DefaultJmsConfig(String brokerUrl, String username, String password) {
        this.brokerUrl = brokerUrl;
        this.username = username;
        this.password = password;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
