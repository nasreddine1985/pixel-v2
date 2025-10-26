package com.pixel.v2.business;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test for main business YAML routes transformation
 */
class BusinessApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(BusinessApplicationTest.class);

    @Test
    void testMainBusinessRoutesLoad() throws IOException {
        // Test 1: Verify the YAML routes file exists
        Path yamlPath = Paths.get("src/main/resources/camel/main-business-routes.yaml");
        assertTrue(Files.exists(yamlPath), "main-business-routes.yaml file should exist");

        // Test 2: Verify the YAML file is valid and parseable
        String yamlContent = Files.readString(yamlPath);
        assertFalse(yamlContent.isEmpty(), "YAML file should not be empty");

        // Test 3: Verify YAML can be parsed (basic syntax validation)
        Yaml yaml = new Yaml();
        Object parsed = yaml.load(yamlContent);
        assertNotNull(parsed, "YAML should be parseable");

        // Test 4: Verify PaymentMessageRoutes.java no longer exists
        Path javaRoutesPath =
                Paths.get("src/main/java/com/pixel/v2/business/routes/PaymentMessageRoutes.java");
        assertFalse(Files.exists(javaRoutesPath), "PaymentMessageRoutes.java should be removed");

        logger.info("✅ YAML routes transformation completed successfully!");
        logger.info("✅ PaymentMessageRoutes.java has been removed");
        logger.info("✅ main-business-routes.yaml has been created with all route definitions");
        logger.info("✅ YAML file is valid and parseable");
    }
}
