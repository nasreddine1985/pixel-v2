package com.pixel.v2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple integration test for PixelCamelApplication startup Verifies application context loads
 * successfully
 */
@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(
        properties = {"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa",
                "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.redis.host=localhost", "spring.redis.port=6379",
                "camel.springboot.main-run-controller=false"})
public class PixelCamelApplicationSimpleTest {

    @Test
    public void testApplicationContextLoads() {
        // Test passes if application context loads successfully
        assertTrue(true, "Application context should load without errors");
    }
}
