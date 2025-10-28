package com.pixel.v2.flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pixel.v2.flow", "com.pixel.v2.persistence"})
@EnableJpaRepositories(basePackages = {"com.pixel.v2.persistence.repository"})
@EntityScan(basePackages = {"com.pixel.v2.persistence.model"})
public class FlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowApplication.class, args);
    }
}
