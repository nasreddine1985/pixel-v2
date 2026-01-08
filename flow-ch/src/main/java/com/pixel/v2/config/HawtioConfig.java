package com.pixel.v2.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Hawtio Configuration for Spring Boot 3.x Enables Hawtio monitoring capabilities with proper CORS
 * support
 */
@Configuration
@ConditionalOnClass(name = "io.hawt.springboot.HawtioConfiguration")
public class HawtioConfig {

    /**
     * Additional CORS filter specifically for Hawtio endpoints This ensures that the Hawtio web
     * interface can connect to Jolokia
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> hawtioActuatorCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*",
                "http://pixel-v2-hawtio:*"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/actuator/jolokia/**", config);
        source.registerCorsConfiguration("/actuator/hawtio/**", config);

        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.addUrlPatterns("/actuator/*");

        return bean;
    }
}
