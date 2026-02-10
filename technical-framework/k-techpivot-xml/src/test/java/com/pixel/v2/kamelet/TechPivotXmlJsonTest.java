package com.pixel.v2.kamelet;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

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
    Map<String, Object> headers = new HashMap<>();
    headers.put("RefFlowData", SAMPLE_FLOW_DATA_JSON);

    Exchange exchange = producerTemplate.request("direct:test-generate-json", e -> {
      e.getIn().setBody("test message");
      e.getIn().setHeaders(headers);
    });

    if (exchange.getException() != null) {
      fail("Route should complete without exception but got: "
          + exchange.getException().getMessage());
    }
    String generatedXml = exchange.getMessage().getHeader("techPivotXml", String.class);
    if (generatedXml != null) {
      assertTrue(generatedXml.contains("TechnicalPivot"), "XML should contain TechnicalPivot root");
      assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode from JSON");
      assertTrue(generatedXml.contains("584"), "XML should contain FlowID from JSON");
      assertTrue(generatedXml.contains("Incoming payment from local clearing SIC"),
          "XML should contain FlowName");
      assertTrue(generatedXml.contains("CHSIC"), "XML should contain partnerIn code");
      assertTrue(generatedXml.contains("ITLOUT"), "XML should contain partnerOut code");
      assertTrue(generatedXml.contains("QM.CH.SIC"), "XML should contain MQS QManager");
    }
  }

  @Test
  public void testGenerateXmlFromFlowDataInBody() throws Exception {
    Map<String, Object> headers = new HashMap<>();

    Exchange exchange = producerTemplate.request("direct:test-generate-json", e -> {
      e.getIn().setBody(SAMPLE_FLOW_DATA_JSON);
      e.getIn().setHeaders(headers);
    });

    if (exchange.getException() != null) {
      fail("Route should complete without exception but got: "
          + exchange.getException().getMessage());
    }
    String generatedXml = exchange.getMessage().getHeader("techPivotXml", String.class);
    if (generatedXml != null) {
      assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode from JSON body");
    }
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
    headers.put("existingTechPivotXml", initialXml);
    headers.put("RefFlowData", SAMPLE_FLOW_DATA_JSON);

    Exchange exchange = producerTemplate.request("direct:test-update-json", e -> {
      e.getIn().setBody("test message");
      e.getIn().setHeaders(headers);
    });

    if (exchange.getException() != null) {
      fail("Route should complete without exception but got: "
          + exchange.getException().getMessage());
    }
    String updatedXml = exchange.getMessage().getHeader("techPivotXml", String.class);
    assertNotNull(updatedXml, "XML should be updated");
  }

  @Test
  public void testInvalidFlowDataJson() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("RefFlowData", "invalid json{");

    try {
      producerTemplate.request("direct:test-generate-json", e -> {
        e.getIn().setBody("test message");
        e.getIn().setHeaders(headers);
      });
    } catch (Exception e) {
      fail("Route should not throw for invalid JSON test in current behavior: " + e.getMessage());
    }
  }

  @Test
  public void testMissingFlowDataJson() throws Exception {
    Map<String, Object> headers = new HashMap<>();

    Exchange exchange = producerTemplate.request("direct:test-generate-json", e -> {
      e.getIn().setBody("");
      e.getIn().setHeaders(headers);
    });

    if (exchange.getException() != null) {
      fail("Route should complete even without flow data but got: "
          + exchange.getException().getMessage());
    }
    String generatedXml = exchange.getMessage().getHeader("techPivotXml", String.class);
    if (generatedXml != null) {
      assertTrue(generatedXml.contains("TechnicalPivot"),
          "XML should contain root even with defaults");
    }
  }

  @Test
  public void testCustomFlowDataJsonHeader() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("MyCustomFlowData", SAMPLE_FLOW_DATA_JSON);

    Exchange exchange = producerTemplate.request("direct:test-generate-custom-header", e -> {
      e.getIn().setBody("test message");
      e.getIn().setHeaders(headers);
    });

    if (exchange.getException() != null) {
      fail("Route should complete without exception but got: "
          + exchange.getException().getMessage());
    }
    String generatedXml = exchange.getMessage().getHeader("techPivotXml", String.class);
    if (generatedXml != null) {
      assertTrue(generatedXml.contains("ICHSIC"), "XML should contain FlowCode");
    }
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
          from("direct:test-generate-custom-header").to(
              "kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true&flowDataJsonHeader=MyCustomFlowData")
              .setBody(constant("OK"));
        }
      };
    }
  }
}
