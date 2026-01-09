package com.pixel.v2.kamelet;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(classes = TechPivotXmlKameletTest.TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TechPivotXmlKameletTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testGenerateXmlFromHeaders() throws Exception {
        // Setup headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("FlowCode", "TEST_FLOW");
        headers.put("FlowID", 12345);
        headers.put("FlowName", "Test Flow");
        headers.put("FileName", "test-file.xml");
        headers.put("PartnerCode", "PARTNER001");
        headers.put("QueueName", "TEST.QUEUE");

        // Send message through kamelet
        String result = producerTemplate.requestBodyAndHeaders(
            "direct:test-generate", 
            "test message", 
            headers, 
            String.class
        );

        // Verify XML was generated in header
        String generatedXml = (String) headers.get("techPivotXml");
        assertNotNull(generatedXml, "XML should be generated in techPivotXml header");
        assertTrue(generatedXml.contains("TEST_FLOW"), "XML should contain FlowCode");
        assertTrue(generatedXml.contains("PARTNER001"), "XML should contain PartnerCode");
        assertTrue(generatedXml.contains("TEST.QUEUE"), "XML should contain QueueName");
        assertTrue(generatedXml.contains("TechPivotRoot"), "XML should contain root element");
    }

    @Test
    public void testUpdateExistingXml() throws Exception {
        // Setup initial XML
        String initialXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <TechPivotRoot xmlns="http://bnpp.com/TechPivot">
                <Flow>
                    <FlowCode>OLD_FLOW</FlowCode>
                </Flow>
            </TechPivotRoot>
            """;

        Map<String, Object> headers = new HashMap<>();
        headers.put("techPivotXml", initialXml);
        headers.put("FlowCode", "NEW_FLOW");

        // Update XML
        producerTemplate.requestBodyAndHeaders(
            "direct:test-update", 
            "test message", 
            headers, 
            String.class
        );

        // Verify XML was updated
        String updatedXml = (String) headers.get("techPivotXml");
        assertNotNull(updatedXml, "XML should be updated in techPivotXml header");
        assertTrue(updatedXml.contains("NEW_FLOW"), "XML should contain updated FlowCode");
        assertFalse(updatedXml.contains("OLD_FLOW"), "XML should not contain old FlowCode");
    }

    @Test
    public void testValidateXml() throws Exception {
        // Setup valid XML
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <TechPivotRoot xmlns="http://bnpp.com/TechPivot">
                <Flow>
                    <FlowCode>TEST_FLOW</FlowCode>
                </Flow>
            </TechPivotRoot>
            """;

        Map<String, Object> headers = new HashMap<>();
        headers.put("techPivotXml", validXml);

        // Validate XML
        producerTemplate.requestBodyAndHeaders(
            "direct:test-validate", 
            "test message", 
            headers, 
            String.class
        );

        // Verify validation result
        String validationResult = (String) headers.get("xmlValidationResult");
        assertNotNull(validationResult, "Validation result should be set");
        assertEquals("VALID", validationResult, "XML should be valid");
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration {

        @org.springframework.context.annotation.Bean
        public RouteBuilder testRoutes() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:test-generate")
                        .to("kamelet:k-techpivot-xml?operation=generate")
                        .setBody(constant("OK"));

                    from("direct:test-update")
                        .to("kamelet:k-techpivot-xml?operation=update")
                        .setBody(constant("OK"));

                    from("direct:test-validate")
                        .to("kamelet:k-techpivot-xml?operation=validate")
                        .setBody(constant("OK"));
                }
            };
        }
    }
}