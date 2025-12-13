package com.pixel.v2.config;

import java.lang.reflect.Method;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jms.connection.CachingConnectionFactory;

import jakarta.jms.ConnectionFactory;

/**
 * Camel Configuration for PIXEL-V2
 * 
 * Configures Camel components, connection factories, and integration patterns.
 */
@Configuration
public class CamelConfiguration {

    @Value("${pixel.activemq.broker-url:tcp://pixel-v2-activemq:61616}")
    private String brokerUrl;

    @Value("${pixel.activemq.username:admin}")
    private String username;

    @Value("${pixel.activemq.password:admin}")
    private String password;

    @Value("${pixel.redis.host:redis}")
    private String redisHost;

    @Value("${pixel.redis.port:6379}")
    private int redisPort;

    /**
     * ActiveMQ Connection Factory for JMS integration Uses reflection to create ActiveMQ connection
     * factory dynamically
     */
    @Bean
    public ConnectionFactory activeMQConnectionFactory() {
        try {
            // Try to load ActiveMQ connection factory dynamically
            Class<?> factoryClass = Class.forName("org.apache.activemq.ActiveMQConnectionFactory");
            Object factory = factoryClass.getDeclaredConstructor().newInstance();

            // Set broker URL
            Method setBrokerURL = factoryClass.getMethod("setBrokerURL", String.class);
            setBrokerURL.invoke(factory, brokerUrl);

            // Set username
            Method setUserName = factoryClass.getMethod("setUserName", String.class);
            setUserName.invoke(factory, username);

            // Set password
            Method setPassword = factoryClass.getMethod("setPassword", String.class);
            setPassword.invoke(factory, password);

            // Set trust all packages
            try {
                Method setTrustAllPackages =
                        factoryClass.getMethod("setTrustAllPackages", boolean.class);
                setTrustAllPackages.invoke(factory, true);
            } catch (NoSuchMethodException e) {
                // Method might not exist in all versions, ignore
            }

            return (ConnectionFactory) factory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ActiveMQ connection factory", e);
        }
    }

    /**
     * Cached Connection Factory for better performance
     */
    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setTargetConnectionFactory(activeMQConnectionFactory());
        factory.setSessionCacheSize(10);
        factory.setCacheProducers(true);
        factory.setCacheConsumers(true);
        return factory;
    }

    /**
     * JMS Connection Factory bean for route references
     */
    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        return cachingConnectionFactory();
    }

    /**
     * JMS Component for Camel
     */
    @Bean
    public JmsComponent jmsComponent(CamelContext camelContext) {
        JmsComponent jms = JmsComponent.jmsComponentAutoAcknowledge(cachingConnectionFactory());
        jms.setDeliveryPersistent(true);
        jms.setExplicitQosEnabled(true);
        jms.setConcurrentConsumers(5);
        jms.setMaxConcurrentConsumers(10);
        return jms;
    }

    /**
     * Redis Connection Factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis Template for k-identification kamelet
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }

}
