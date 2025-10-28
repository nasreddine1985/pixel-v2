package com.pixel.v2.flow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = FlowApplication.class)
@TestPropertySource(properties = {"camel.springboot.main-run-controller=false",
        "spring.main.allow-bean-definition-overriding=true", "spring.profiles.active=test"})
class FlowApplicationTests {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }
}
