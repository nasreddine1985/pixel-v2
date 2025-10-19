package com.pixel.v2.validation.config;

import com.pixel.v2.validation.processor.PaymentMessageValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for k-validation kamelet components
 */
@Configuration
public class ValidationConfiguration {
    
    /**
     * Register PaymentMessageValidator as a Spring bean
     * so it can be referenced in Camel routes
     */
    @Bean("PaymentMessageValidator")
    public PaymentMessageValidator paymentMessageValidator() {
        return new PaymentMessageValidator();
    }
}