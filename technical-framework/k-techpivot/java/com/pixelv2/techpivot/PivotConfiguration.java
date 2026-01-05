package com.pixelv2.techpivot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PivotConfiguration {
    @Bean
    public PivotService pivotService() {
        return new PivotService();
    }
}
