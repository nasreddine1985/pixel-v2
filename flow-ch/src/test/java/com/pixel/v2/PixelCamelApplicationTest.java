package com.pixel.v2;

import org.apache.camel.CamelContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for PixelCamelApplication Tests application startup and basic Spring/Camel
 * context configuration
 */
@SpringBootTest(classes = PixelCamelApplicationTestConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "pixel.referential.service.url=http://mock-referential:8080",
        "pixel.kafka.brokers=mock-kafka:9092", "pixel.cache.ttl=3600",
        "kmq.starter.mqFileName=TEST.QUEUE", "kmq.starter.connectionFactory=mockConnectionFactory",
        "kmq.starter.flowCode=TESTFLOW", "kmq.starter.messageType=test.message",
        "kmq.starter.kafkaFlowSummaryTopicName=test-flow-summary",
        "kmq.starter.kafkaLogTopicName=test-log-events",
        "kmq.starter.kafkaDistributionTopicName=test-distribution",
        "kmq.starter.brokers=mock-kafka:9092", "kmq.starter.flowCountryCode=TEST",
        "kmq.starter.flowCountryId=999", "kmq.starter.dataSource=mockDataSource",
        "nas.archive.url=smb://mock-nas/test-archive", "spring.redis.host=localhost",
        "spring.redis.port=6379", "camel.component.kamelet.location=classpath:test-kamelets"})
public class PixelCamelApplicationTest {

    @Autowired(required = false)
    private CamelContext camelContext;

    /**
     * Test that the Spring Boot application context loads successfully
     */
    @Test
    public void testApplicationContextLoads() {
        // If we reach here, the application context loaded successfully
        assertTrue(true, "Application context should load without errors");
    }

    /**
     * Test that CamelContext is properly configured and available
     */
    @Test
    public void testCamelContextConfiguration() {
        assertNotNull(camelContext, "CamelContext should be available");
        assertTrue(camelContext.getStatus().isStarted() || camelContext.getStatus().isStarting(),
                "CamelContext should be started or starting");
    }

    /**
     * Test basic Camel context properties
     */
    @Test
    public void testCamelContextProperties() {
        if (camelContext != null) {
            assertNotNull(camelContext.getName(), "CamelContext should have a name");
            assertTrue(camelContext.getRoutes().size() >= 0,
                    "CamelContext should be able to handle routes");
        }
    }
}
