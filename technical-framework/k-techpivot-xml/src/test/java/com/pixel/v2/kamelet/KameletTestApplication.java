package com.pixel.v2.kamelet;

import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CamelAutoConfiguration.class)
@ComponentScan(basePackages = "com.pixel.v2")
public class KameletTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(KameletTestApplication.class, args);
    }
}