package com.pixel.v2.kamelet;

import java.nio.file.Files;
import java.nio.file.Paths;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.pixel.v2.common.headers.HeaderConstants;
import com.pixel.v2.common.headers.HeaderUtils;

import static com.pixel.v2.common.headers.HeaderConstants.*;

/**
 * Test du kamelet k-techpivot-xml avec variables dans les headers
 * Tests des opérations generate et update avec FlowDataJson et autres variables headers
 * Utilise HeaderConstants pour une approche type-safe des headers
 */
@CamelSpringBootTest
@SpringBootTest(classes = KameletTestApplication.class)
@TestPropertySource(properties = {
    "camel.component.kamelet.location=classpath:kamelets",
    "spring.main.allow-bean-definition-overriding=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TechPivotXmlHeadersTest {

    @Autowired
    private CamelContext camelContext;

    @Produce("direct:test-headers")
    private ProducerTemplate producer;

    @EndpointInject("mock:xml-output")
    private MockEndpoint mockXmlOutput;

    /**
     * Charge le fichier referentiel-example.json
     */
    private String loadReferentielExample() throws Exception {
        String filePath = "/Users/n.abassi/sources/pixel-v2/data/referentiel-example.json";
        return Files.readString(Paths.get(filePath));
    }

    @Test
    public void testGenerateWithHeaders() throws Exception {
        System.out.println("\n=== TEST GENERATE WITH HEADERS (using HeaderConstants) ===");
        
        // Charger le JSON
        String referentielJson = loadReferentielExample();
        System.out.println("Loaded referentiel-example.json ✓");
        
        // Créer la route de test avec kamelet en mode generate
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-headers").routeId("test-headers-generate")
                        .log("Testing generate mode with headers")
                        .to("kamelet:k-techpivot-xml?operation=generate")
                        .to("mock:xml-output");
            }
        });

        // Setup expectations
        mockXmlOutput.expectedMessageCount(1);

        // === Préparer les headers avec HeaderConstants (type-safe) ===
        String flowOccurId = HeaderUtils.generateFlowOccurId("FLOW");
        String messageId = HeaderUtils.generateMessageId("MSG");
        String correlationId = "CORR-" + System.currentTimeMillis();
        String timestamp = HeaderUtils.generateTimestamp();

        Map<String, Object> headers = new HashMap<>();
        headers.put(FLOW_DATA_JSON, referentielJson);
        headers.put(FLOW_OCCUR_ID, flowOccurId);
        headers.put(RECEIVED_TIMESTAMP, timestamp);
        headers.put(MESSAGE_ID, messageId);
        headers.put(CORRELATION_ID, correlationId);
        headers.put(PROCESSING_MODE, PROCESSING_MODE_BATCH);
        headers.put(BUSINESS_STATUS, BUSINESS_STATUS_VALIDATED);
        headers.put(TECHNICAL_STATUS, TECHNICAL_STATUS_SUCCESS);

        System.out.println("Created headers using HeaderConstants:");
        System.out.println("  " + FLOW_OCCUR_ID + ": " + headers.get(FLOW_OCCUR_ID));
        System.out.println("  " + PROCESSING_MODE + ": " + headers.get(PROCESSING_MODE));
        System.out.println("  " + BUSINESS_STATUS + ": " + headers.get(BUSINESS_STATUS));

        // Envoyer le message avec headers (body peut être vide maintenant)
        producer.sendBodyAndHeaders("trigger", headers);

        // Vérifier les résultats
        mockXmlOutput.assertIsSatisfied(5000);

        Exchange exchange = mockXmlOutput.getReceivedExchanges().get(0);
        String generatedXml = exchange.getIn().getHeader("techPivotXml", String.class);
        assertNotNull(generatedXml, "XML should be generated");

        // Afficher le résultat
        System.out.println("\n🎯 GENERATED XML FROM HEADERS:");
        System.out.println("=" + "=".repeat(80));
        System.out.println(generatedXml);
        System.out.println("=" + "=".repeat(80));

        // Vérifications spécifiques avec les valeurs générées
        assertTrue(generatedXml.contains("584"), "Should contain FlowID from FlowDataJson");
        assertTrue(generatedXml.contains("ICHSIC"), "Should contain FlowCode from FlowDataJson");
        assertTrue(generatedXml.contains(flowOccurId), "Should contain flowOccurId from headers: " + flowOccurId);
        assertTrue(generatedXml.contains(messageId), "Should contain MessageId from headers: " + messageId);
        assertTrue(generatedXml.contains(correlationId), "Should contain CorrelationId from headers: " + correlationId);
        assertTrue(generatedXml.contains(PROCESSING_MODE_BATCH), "Should contain ProcessingMode from headers");
        assertTrue(generatedXml.contains(BUSINESS_STATUS_VALIDATED), "Should contain BusinessStatus from headers");
        assertTrue(generatedXml.contains(TECHNICAL_STATUS_SUCCESS), "Should contain TechnicalStatus from headers");
        assertTrue(generatedXml.contains(timestamp.substring(0, 10)), "Should contain date part of ReceivedTimestamp from headers");

        System.out.println("\n✅ All header-based content verifications PASSED!");
    }

    @Test
    public void testUpdateWithHeaders() throws Exception {
        System.out.println("\n=== TEST UPDATE WITH HEADERS ===");
        
        // XML existant simulé
        String existingXml = """
            <TechnicalPivot>
                <FlowID>123</FlowID>
                <FlowCode>OLDCODE</FlowCode>
                <FlowName>Old Flow Name</FlowName>
                <BusinessStatus>PENDING</BusinessStatus>
                <TechnicalStatus>PROCESSING</TechnicalStatus>
            </TechnicalPivot>""";
        
        // Créer la route de test avec kamelet en mode update
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-headers").routeId("test-headers-update")
                        .log("Testing update mode with headers")
                        .to("kamelet:k-techpivot-xml?operation=update")
                        .to("mock:xml-output");
            }
        });

        // Setup expectations
        mockXmlOutput.expectedMessageCount(1);

        // Préparer les headers pour mise à jour
        Map<String, Object> headers = new HashMap<>();
        headers.put("existingTechPivotXml", existingXml);
        headers.put("flowOccurId", "UPDATED-FLOW-" + System.currentTimeMillis());
        headers.put("ReceivedTimestamp", "2026-01-09T11:30:00.000");
        headers.put("MessageId", "UPDATED-MSG-99999");
        headers.put("BusinessStatus", "COMPLETED");
        headers.put("TechnicalStatus", "FINALIZED");

        // Envoyer le message avec headers pour update
        producer.sendBodyAndHeaders("trigger", headers);

        // Vérifier les résultats
        mockXmlOutput.assertIsSatisfied(5000);

        Exchange exchange = mockXmlOutput.getReceivedExchanges().get(0);
        String updatedXml = exchange.getIn().getHeader("techPivotXml", String.class);
        assertNotNull(updatedXml, "Updated XML should be generated");

        // Afficher le résultat
        System.out.println("\n🎯 UPDATED XML:");
        System.out.println("=" + "=".repeat(80));
        System.out.println(updatedXml);
        System.out.println("=" + "=".repeat(80));

        // Vérifications de mise à jour
        assertTrue(updatedXml.contains("123"), "Should keep original FlowID");
        assertTrue(updatedXml.contains("OLDCODE"), "Should keep original FlowCode");
        assertTrue(updatedXml.contains("UPDATED-FLOW-"), "Should have updated flowOccurId");
        assertTrue(updatedXml.contains("UPDATED-MSG-99999"), "Should have updated MessageId");
        assertTrue(updatedXml.contains("COMPLETED"), "Should have updated BusinessStatus");
        assertTrue(updatedXml.contains("FINALIZED"), "Should have updated TechnicalStatus");
        assertTrue(updatedXml.contains("2026-01-09T11:30:00.000"), "Should have updated timestamp");

        System.out.println("\n✅ All update verifications PASSED!");
    }

    @Test
    public void testGenerateWithRefFlowDataJson() throws Exception {
        System.out.println("\n=== TEST GENERATE WITH REFFLOWDATAJSON ===");
        
        // Charger le JSON
        String referentielJson = loadReferentielExample();
        System.out.println("Loaded referentiel-example.json ✓");
        
        // Créer la route de test avec kamelet en mode generate
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-headers").routeId("test-ref-headers-generate")
                        .log("Testing generate mode with RefFlowDataJson header")
                        .to("kamelet:k-techpivot-xml?operation=generate")
                        .to("mock:xml-output");
            }
        });

        // Setup expectations
        mockXmlOutput.expectedMessageCount(1);

        // Préparer les headers avec RefFlowDataJson au lieu de FlowDataJson
        Map<String, Object> headers = new HashMap<>();
        headers.put("RefFlowDataJson", referentielJson);
        headers.put("flowOccurId", "REF-FLOW-TEST-" + System.currentTimeMillis());
        headers.put("ReceivedTimestamp", "2026-01-09T12:00:00.000");
        headers.put("MessageId", "REF-MSG-54321");
        headers.put("CorrelationId", "REF-CORR-09876");
        headers.put("ProcessingMode", "CREATE");
        headers.put("BusinessStatus", "INITIALIZED");
        headers.put("TechnicalStatus", "ACTIVE");

        // Envoyer le message avec headers RefFlowDataJson
        producer.sendBodyAndHeaders("trigger", headers);

        // Vérifier les résultats
        mockXmlOutput.assertIsSatisfied(5000);

        Exchange exchange = mockXmlOutput.getReceivedExchanges().get(0);
        String generatedXml = exchange.getIn().getHeader("techPivotXml", String.class);
        assertNotNull(generatedXml, "XML should be generated from RefFlowDataJson");

        // Afficher le résultat
        System.out.println("\n🎯 GENERATED XML FROM REFFLOWDATAJSON:");
        System.out.println("=" + "=".repeat(80));
        System.out.println(generatedXml);
        System.out.println("=" + "=".repeat(80));

        // Vérifications spécifiques avec RefFlowDataJson
        assertTrue(generatedXml.contains("584"), "Should contain FlowID from RefFlowDataJson");
        assertTrue(generatedXml.contains("ICHSIC"), "Should contain FlowCode from RefFlowDataJson");
        assertTrue(generatedXml.contains("REF-FLOW-TEST-"), "Should contain flowOccurId from headers");
        assertTrue(generatedXml.contains("REF-MSG-54321"), "Should contain MessageId from headers");
        assertTrue(generatedXml.contains("REF-CORR-09876"), "Should contain CorrelationId from headers");
        assertTrue(generatedXml.contains("CREATE"), "Should contain ProcessingMode from headers");
        assertTrue(generatedXml.contains("INITIALIZED"), "Should contain BusinessStatus from headers");
        assertTrue(generatedXml.contains("ACTIVE"), "Should contain TechnicalStatus from headers");
        assertTrue(generatedXml.contains("2026-01-09T12:00:00.000"), "Should contain ReceivedTimestamp from headers");
        assertTrue(generatedXml.contains("Incoming payment from local clearing SIC"), "Should contain FlowName from RefFlowDataJson");
        assertTrue(generatedXml.contains("ITL"), "Should contain Application Code from RefFlowDataJson");
        assertTrue(generatedXml.contains("CHSIC"), "Should contain PartnerIn Code from RefFlowDataJson");

        System.out.println("\n✅ All RefFlowDataJson verifications PASSED!");
    }
}