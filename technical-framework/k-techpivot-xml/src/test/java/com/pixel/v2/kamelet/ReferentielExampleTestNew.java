package com.pixel.v2.kamelet;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for k-techpivot-xml kamelet
 * Tests XML generation from referentiel-example.json data
 */
@CamelSpringBootTest
@SpringBootTest(classes = KameletTestApplication.class)
@TestPropertySource(properties = {
    "camel.component.kamelet.location=classpath:kamelets",
    "spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

public class ReferentielExampleTestNew {

    @Autowired
    private CamelContext camelContext;

    @Produce("direct:test-input")
    private ProducerTemplate producer;

    @EndpointInject("mock:xml-output")
    private MockEndpoint mockXmlOutput;

    @BeforeEach
    public void setUp() throws Exception {
        // Reset mock endpoints
        mockXmlOutput.reset();
    }

    private String loadReferentielExample() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/referentiel-example.json")) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testGenerationModeWithReferentielExample() throws Exception {
        System.out.println("\n=== TEST GENERATION MODE ===");
        
        // Charger le fichier referentiel-example.json
        String referentielJson = loadReferentielExample();
        System.out.println("Loaded referentiel-example.json ✓");
        
        // Create test-specific route following KMqStarterKameletTest pattern
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-generation-mode")
                        .log("Testing generation mode with referentiel-example.json")
                        .to("kamelet:k-techpivot-xml?operation=generate")
                        .to("mock:xml-output");
            }
        });

        // Setup expectations
        mockXmlOutput.expectedMessageCount(1);

        // Send the JSON content as body
        producer.sendBody(referentielJson);

        // Verify expectations
        mockXmlOutput.assertIsSatisfied(5000);

        // Récupérer le XML généré
        Exchange exchange = mockXmlOutput.getReceivedExchanges().get(0);
        String generatedXml = exchange.getIn().getHeader("techPivotXml", String.class);
        assertNotNull(generatedXml, "XML should be generated");

        // Afficher le résultat
        System.out.println("\n🎯 GENERATED XML FROM REFERENTIEL-EXAMPLE.JSON:");
        System.out.println("=" + "=".repeat(80));
        System.out.println(generatedXml);
        System.out.println("=" + "=".repeat(80));

        // Vérifications spécifiques du contenu referentiel-example.json
        assertTrue(generatedXml.contains("584"), "Should contain FlowID: 584");
        assertTrue(generatedXml.contains("ICHSIC"), "Should contain FlowCode: ICHSIC");
        assertTrue(generatedXml.contains("Incoming payment from local clearing SIC"), "Should contain FlowName");
        assertTrue(generatedXml.contains("CHSIC"), "Should contain partnerIn code: CHSIC");
        assertTrue(generatedXml.contains("CH - Switzerland ACH Clearing"), "Should contain partnerIn name");
        assertTrue(generatedXml.contains("FRITL01Z"), "Should contain MQS QManager: FRITL01Z");
        assertTrue(generatedXml.contains("OITARTMI01"), "Should contain MQS QName: OITARTMI01");
        assertTrue(generatedXml.contains("DOME"), "Should contain partnerOut code: DOME");
        assertTrue(generatedXml.contains("Domestic payment engine"), "Should contain partnerOut name");
        assertTrue(generatedXml.contains("BPPBCHGGXXX"), "Should contain BIC from FlowFuncPrty");
        assertTrue(generatedXml.contains("SWITZERLAND"), "Should contain country");
        assertTrue(generatedXml.contains("10000"), "Should contain flowMaximum: 10000");
        
        System.out.println("\n✅ All content verifications PASSED!");
        System.out.println("📊 XML contains all expected elements from referentiel-example.json");
    }

    /*
    // Additional tests commented out for now - will be adapted later
    @Test
    public void testUpdateModeWithReferentielExample() throws Exception {
        // TODO: Adapt this test to follow Spring Boot pattern
    }

    @Test
    public void testValidationModeWithGeneratedXml() throws Exception {
        // TODO: Adapt this test to follow Spring Boot pattern
    }

    @Test 
    public void testCompleteWorkflowWithReferentielExample() throws Exception {
        // TODO: Adapt this test to follow Spring Boot pattern
    }
    */
}