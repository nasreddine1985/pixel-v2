package com.pixel.v2.ingestion.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Receiver Kamelets
 * 
 * Tests the integration between receiver kamelets and the ingestion orchestrator.
 */
@CamelSpringBootTest
@SpringBootTest
@ActiveProfiles("test")
@UseAdviceWith
class ReceiverKameletsIntegrationTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    private static final String TEST_XML_CONTENT = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
            <FIToFICstmrCdtTrf>
                <GrpHdr>
                    <MsgId>TEST123456789</MsgId>
                    <CreDtTm>2025-10-21T14:30:00</CreDtTm>
                    <NbOfTxs>1</NbOfTxs>
                </GrpHdr>
                <CdtTrfTxInf>
                    <PmtId>
                        <InstrId>TESTINSTR123</InstrId>
                        <EndToEndId>TESTE2E123</EndToEndId>
                        <TxId>TESTTXN123</TxId>
                    </PmtId>
                    <IntrBkSttlmAmt Ccy="EUR">500.00</IntrBkSttlmAmt>
                </CdtTrfTxInf>
            </FIToFICstmrCdtTrf>
        </Document>
        """;

    private static final String TEST_MULTI_LINE_XML = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">\n" +
        "    <FIToFICstmrCdtTrf>\n" +
        "        <GrpHdr><MsgId>MSG001</MsgId></GrpHdr>\n" +
        "    </FIToFICstmrCdtTrf>\n" +
        "</Document>\n" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">\n" +
        "    <FIToFICstmrCdtTrf>\n" +
        "        <GrpHdr><MsgId>MSG002</MsgId></GrpHdr>\n" +
        "    </FIToFICstmrCdtTrf>\n" +
        "</Document>";

    @BeforeEach
    void setup() throws Exception {
        // Mock the ingestion orchestrator to capture messages from receiver kamelets
        AdviceWith.adviceWith(camelContext, "payment-ingestion-orchestrator", route -> {
            route.replaceFromWith("direct:payment-ingestion");
            route.mockEndpoints("direct:database-persistence");
        });

        // Mock the receiver kamelet routes to avoid external dependencies
        AdviceWith.adviceWith(camelContext, "http-receipt-route", route -> {
            route.replaceFromWith("direct:test-http-receiver");
        });

        AdviceWith.adviceWith(camelContext, "file-receipt-route", route -> {
            route.replaceFromWith("direct:test-file-receiver");
        });

        // Create test directories
        createTestDirectories();

        camelContext.start();
    }

    private void createTestDirectories() throws IOException {
        String[] directories = {
            "/tmp/test-payments-in",
            "/tmp/test-payments-processed",
            "/tmp/test-payments-error"
        };

        for (String dir : directories) {
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }

    @Test
    @DisplayName("HTTP Receiver Kamelet Integration")
    void testHttpReceiverIntegration() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(1);

        // Simulate HTTP receiver kamelet sending message to orchestrator
        Exchange result = producerTemplate.send("direct:test-http-receiver", exchange -> {
            exchange.getIn().setBody(TEST_XML_CONTENT);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
            exchange.getIn().setHeader("receiptChannel", "REST_API");
            exchange.getIn().setHeader("apiPath", "/api/v1/payments");
            exchange.getIn().setHeader("httpMethod", "POST");
        });

        orchestratorMock.assertIsSatisfied();

        // Verify headers are properly set by HTTP receiver
        assertEquals("HTTP_API", result.getIn().getHeader("messageSource"));
        assertEquals("REST_API", result.getIn().getHeader("receiptChannel"));
        assertEquals("/api/v1/payments", result.getIn().getHeader("apiPath"));
        assertEquals("POST", result.getIn().getHeader("httpMethod"));
        assertNotNull(result.getIn().getHeader("IngestionStartTime"));
    }

    @Test
    @DisplayName("File Receiver Kamelet Integration with Single Message")
    void testFileReceiverSingleMessage() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(1);

        // Simulate file receiver kamelet processing single line
        Exchange result = producerTemplate.send("direct:test-file-receiver", exchange -> {
            exchange.getIn().setBody(TEST_XML_CONTENT.trim());
            exchange.getIn().setHeader("messageSource", "CFT_FILE");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.456");
            exchange.getIn().setHeader("receiptChannel", "FILE_CFT");
            exchange.getIn().setHeader("fileName", "test-payment.xml");
            exchange.getIn().setHeader("lineNumber", 1);
            exchange.getIn().setHeader("directoryPath", "/tmp/test-payments-in");
        });

        orchestratorMock.assertIsSatisfied();

        // Verify file-specific headers
        assertEquals("CFT_FILE", result.getIn().getHeader("messageSource"));
        assertEquals("FILE_CFT", result.getIn().getHeader("receiptChannel"));
        assertEquals("test-payment.xml", result.getIn().getHeader("fileName"));
        assertEquals(1, result.getIn().getHeader("lineNumber"));
        assertEquals("/tmp/test-payments-in", result.getIn().getHeader("directoryPath"));
    }

    @Test
    @DisplayName("File Receiver Kamelet Integration with Multiple Messages")
    void testFileReceiverMultipleMessages() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(2);

        // Simulate processing each line of the multi-line file
        String[] lines = TEST_MULTI_LINE_XML.split("\n");
        int lineNumber = 1;

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue; // Skip empty lines as the kamelet would
            }

            producerTemplate.send("direct:test-file-receiver", exchange -> {
                exchange.getIn().setBody(line.trim());
                exchange.getIn().setHeader("messageSource", "CFT_FILE");
                exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.789");
                exchange.getIn().setHeader("receiptChannel", "FILE_CFT");
                exchange.getIn().setHeader("fileName", "multi-payment.xml");
                exchange.getIn().setHeader("lineNumber", lineNumber);
                exchange.getIn().setHeader("directoryPath", "/tmp/test-payments-in");
            });
        }

        orchestratorMock.assertIsSatisfied();
    }

    @Test
    @DisplayName("MQ Receiver Kamelet Simulation")
    void testMQReceiverSimulation() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(1);

        // Simulate MQ receiver kamelet (since actual MQ is not available in test)
        Exchange result = producerTemplate.send("direct:payment-ingestion", exchange -> {
            exchange.getIn().setBody(TEST_XML_CONTENT);
            exchange.getIn().setHeader("messageSource", "MQ_SERIES");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.999");
            exchange.getIn().setHeader("receiptChannel", "MQ_QUEUE");
            exchange.getIn().setHeader("mqQueue", "PAYMENT_INPUT");
        });

        orchestratorMock.assertIsSatisfied();

        // Verify MQ-specific headers
        assertEquals("MQ_SERIES", result.getIn().getHeader("messageSource"));
        assertEquals("MQ_QUEUE", result.getIn().getHeader("receiptChannel"));
        assertEquals("PAYMENT_INPUT", result.getIn().getHeader("mqQueue"));
    }

    @Test
    @DisplayName("Receiver Kamelet Error Handling")
    void testReceiverErrorHandling() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(1);

        // Simulate receiver kamelet with error condition
        try {
            Exchange result = producerTemplate.send("direct:test-http-receiver", exchange -> {
                exchange.getIn().setBody(""); // Empty body should still route
                exchange.getIn().setHeader("messageSource", "HTTP_API");
                exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
                exchange.getIn().setHeader("receiptChannel", "REST_API");
                exchange.getIn().setHeader("errorCondition", "Empty message body");
            });

            orchestratorMock.assertIsSatisfied();

            // Verify error condition is preserved
            assertEquals("Empty message body", result.getIn().getHeader("errorCondition"));
            assertEquals("", result.getIn().getBody(String.class));

        } catch (Exception e) {
            // Expected for certain error conditions
            assertTrue(e.getMessage().contains("error") || e.getMessage().contains("empty"));
        }
    }

    @Test
    @DisplayName("Concurrent Message Processing")
    void testConcurrentMessageProcessing() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(3);

        // Simulate concurrent messages from different receivers
        Thread httpThread = new Thread(() -> {
            producerTemplate.send("direct:test-http-receiver", exchange -> {
                exchange.getIn().setBody(TEST_XML_CONTENT);
                exchange.getIn().setHeader("messageSource", "HTTP_API");
                exchange.getIn().setHeader("receiptChannel", "REST_API");
                exchange.getIn().setHeader("threadId", "HTTP-THREAD");
            });
        });

        Thread fileThread = new Thread(() -> {
            producerTemplate.send("direct:test-file-receiver", exchange -> {
                exchange.getIn().setBody(TEST_XML_CONTENT);
                exchange.getIn().setHeader("messageSource", "CFT_FILE");
                exchange.getIn().setHeader("receiptChannel", "FILE_CFT");
                exchange.getIn().setHeader("threadId", "FILE-THREAD");
            });
        });

        Thread mqThread = new Thread(() -> {
            producerTemplate.send("direct:payment-ingestion", exchange -> {
                exchange.getIn().setBody(TEST_XML_CONTENT);
                exchange.getIn().setHeader("messageSource", "MQ_SERIES");
                exchange.getIn().setHeader("receiptChannel", "MQ_QUEUE");
                exchange.getIn().setHeader("threadId", "MQ-THREAD");
            });
        });

        httpThread.start();
        fileThread.start();
        mqThread.start();

        httpThread.join(5000);
        fileThread.join(5000);
        mqThread.join(5000);

        orchestratorMock.assertIsSatisfied();
    }

    @Test
    @DisplayName("Receiver Kamelet Metadata Validation")
    void testReceiverMetadataValidation() throws Exception {
        MockEndpoint orchestratorMock = camelContext.getEndpoint("mock:direct:database-persistence", MockEndpoint.class);
        orchestratorMock.expectedMessageCount(1);

        // Test that all required metadata is set by receivers
        Exchange result = producerTemplate.send("direct:test-http-receiver", exchange -> {
            exchange.getIn().setBody(TEST_XML_CONTENT);
            exchange.getIn().setHeader("messageSource", "HTTP_API");
            exchange.getIn().setHeader("receiptTimestamp", "2025-10-21 14:30:00.123");
            exchange.getIn().setHeader("receiptChannel", "REST_API");
            exchange.getIn().setHeader("apiPath", "/api/v1/payments");
            exchange.getIn().setHeader("httpMethod", "POST");
        });

        orchestratorMock.assertIsSatisfied();

        // Validate all required metadata is present
        assertNotNull(result.getIn().getHeader("messageSource"), "messageSource should be set");
        assertNotNull(result.getIn().getHeader("receiptTimestamp"), "receiptTimestamp should be set");
        assertNotNull(result.getIn().getHeader("receiptChannel"), "receiptChannel should be set");
        assertNotNull(result.getIn().getHeader("IngestionStartTime"), "IngestionStartTime should be set by orchestrator");

        // Validate HTTP-specific metadata
        assertEquals("HTTP_API", result.getIn().getHeader("messageSource"));
        assertEquals("REST_API", result.getIn().getHeader("receiptChannel"));
        assertEquals("/api/v1/payments", result.getIn().getHeader("apiPath"));
        assertEquals("POST", result.getIn().getHeader("httpMethod"));
    }
}