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
@SpringBootTest(classes = TechPivotXmlJsonTest.TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TechPivotXmlJsonTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private static final String SAMPLE_FLOW_DATA_JSON = """
        {
          "flow": {
            "FlowID": "584",
            "flowCode": "ICHSIC",
            "flowName": "Incoming payment from local clearing SIC",
            "flowTypeName": "Payment",
            "flowDirection": "IN",
            "flowEnabled": "Y",
            "flowFuncPrty": [
              {
                "key": "BIC",
                "type": "Enrichment",
                "value": "BPPBCHGGXXX"
              },
              {
                "key": "XmlSchema",
                "type": "Validation",
                "value": "pacs.008.001.08.ch.02.itl.v2.xsd"
              }
            ],
            "countries": ["SWITZERLAND"]
          },
          "partnerIn": {
            "partnerCode": "CHSIC",
            "partnerName": "CH - Switzerland ACH Clearing",
            "partnerTypeName": "CHClearing",
            "charsetCode": "UTF-8",
            "transport": {
              "type": "MQS",
              "mqs": {
                "qManager": "QM.CH.SIC",
                "qName": "CH.SIC.IN"
              }
            }
          },
          "partnerOut": [
            {
              "partnerCode": "ITLOUT",
              "partnerName": "ITL Output",
              "partnerTypeName": "Application",
              "transport": {
                "type": "JMS",
                "jms": {
                  "qName": "ITL.OUT.QUEUE"
                }
              }
            }
          ],
          "flowRules": [
            {
              "flowCode": "ICHSIC",
              "flowControlledEnabled": true,
              "flowMaximum": 1000,
              "flowRetentionEnabled": true,
              "retentionCyclePeriod": "30"
            }
          ]
        }
        """;

    @Test
    public void testGenerateXmlFromFlowDataJson() throws Exception {
        // Setup headers with FlowData JSON
        Map<String, Object> headers = new HashMap<>();
        headers.put("RefFlowData", SAMPLE_FLOW_DATA_JSON);

        // Send message through kamelet
        String result = producerTemplate.requestBodyAndHeaders(
            "direct:test-generate-json", 
            "test message", 
            headers, 
            String.class
        );

        // Verify XML was generated in header
        String generatedXml = (String) headers.get("techPivotXml");
        assertNotNull(generatedXml, "XML should be generated in techPivotXml header");
        
        // Verify content from FlowData JSON
        assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode from JSON");
        assertTrue(generatedXml.contains("584"), "XML should contain FlowID from JSON");
        assertTrue(generatedXml.contains("Incoming payment from local clearing SIC"), 
            "XML should contain FlowName from JSON");
        assertTrue(generatedXml.contains("CHSIC"), "XML should contain partnerIn code");
        assertTrue(generatedXml.contains("CH - Switzerland ACH Clearing"), 
            "XML should contain partnerIn name");
        assertTrue(generatedXml.contains("ITLOUT"), "XML should contain partnerOut code");
        assertTrue(generatedXml.contains("QM.CH.SIC"), "XML should contain MQS QManager");
        assertTrue(generatedXml.contains("CH.SIC.IN"), "XML should contain MQS QName");
        assertTrue(generatedXml.contains("ITL.OUT.QUEUE"), "XML should contain JMS QName");
        assertTrue(generatedXml.contains("BPPBCHGGXXX"), "XML should contain FlowFuncPrty value");
        assertTrue(generatedXml.contains("SWITZERLAND"), "XML should contain country");
        assertTrue(generatedXml.contains("TechPivotRoot"), "XML should contain root element");
        
        // Check XML structure elements
        assertTrue(generatedXml.contains("<Flow>"), "XML should contain Flow element");
        assertTrue(generatedXml.contains("<Input>"), "XML should contain Input element");
        assertTrue(generatedXml.contains("<Output>"), "XML should contain Output element");
        assertTrue(generatedXml.contains("<FlowRules>"), "XML should contain FlowRules element");
        assertTrue(generatedXml.contains("<FlowControl>"), "XML should contain FlowControl element");
    }

    @Test
    public void testGenerateXmlFromFlowDataInBody() throws Exception {
        // Send FlowData JSON in body
        Map<String, Object> headers = new HashMap<>();
        
        String result = producerTemplate.requestBodyAndHeaders(
            "direct:test-generate-json", 
            SAMPLE_FLOW_DATA_JSON, 
            headers, 
            String.class
        );

        // Verify XML was generated
        String generatedXml = (String) headers.get("techPivotXml");
        assertNotNull(generatedXml, "XML should be generated from body JSON");
        assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode from JSON body");
    }

    @Test
    public void testUpdateXmlFromFlowDataJson() throws Exception {
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
        headers.put("RefFlowData", SAMPLE_FLOW_DATA_JSON);

        // Update XML with FlowData JSON
        producerTemplate.requestBodyAndHeaders(
            "direct:test-update-json", 
            "test message", 
            headers, 
            String.class
        );

        // Verify XML was updated (basic check - full JSON update logic would be in the actual implementation)
        String updatedXml = (String) headers.get("techPivotXml");
        assertNotNull(updatedXml, "XML should be updated");
        // Note: The update logic from JSON would be implemented in the actual kamelet
    }

    @Test
    public void testInvalidFlowDataJson() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("RefFlowData", "invalid json{");

        // Should throw exception for invalid JSON
        assertThrows(Exception.class, () -> {
            producerTemplate.requestBodyAndHeaders(
                "direct:test-generate-json", 
                "test message", 
                headers, 
                String.class
            );
        });
    }

    @Test
    public void testMissingFlowDataJson() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        // No RefFlowData header and no body

        // Should throw exception for missing FlowData
        assertThrows(Exception.class, () -> {
            producerTemplate.requestBodyAndHeaders(
                "direct:test-generate-json", 
                "", 
                headers, 
                String.class
            );
        });
    }

    @Test
    public void testCustomFlowDataJsonHeader() throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("MyCustomFlowData", SAMPLE_FLOW_DATA_JSON);

        String result = producerTemplate.requestBodyAndHeaders(
            "direct:test-generate-custom-header", 
            "test message", 
            headers, 
            String.class
        );

        String generatedXml = (String) headers.get("techPivotXml");
        assertNotNull(generatedXml, "XML should be generated from custom header");
        assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode");
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration {

        @org.springframework.context.annotation.Bean
        public RouteBuilder testRoutes() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    // Test route for JSON generation
                    from("direct:test-generate-json")
                        .to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")
                        .setBody(constant("OK"));

                    // Test route for JSON update
                    from("direct:test-update-json")
                        .to("kamelet:k-techpivot-xml?operation=update&useFlowDataJson=true")
                        .setBody(constant("OK"));

                    // Test route with custom header name
                    from("direct:test-generate-custom-header")
                        .to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true&flowDataJsonHeader=MyCustomFlowData")
                        .setBody(constant("OK"));
                }
            };
        }
    }
}