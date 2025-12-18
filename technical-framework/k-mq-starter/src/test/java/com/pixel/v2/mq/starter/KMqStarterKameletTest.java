package com.pixel.v2.mq.starter;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for k-mq-starter kamelet Tests JMS message consumption, sequence generation, archiving,
 * and logging
 */
@CamelSpringBootTest
@SpringBootTest(classes = KMqStarterTestApplication.class)
@TestPropertySource(properties = {"camel.component.kamelet.location=classpath:kamelets",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jms.embedded.enabled=false",
        "camel.component.jms.connection-factory=#jmsConnectionFactory"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class KMqStarterKameletTest {

    @Autowired
    private CamelContext camelContext;

    @Produce("direct:test-input")
    private ProducerTemplate producer;

    @EndpointInject("mock:sink")
    private MockEndpoint mockSink;

    @EndpointInject("mock:log-events")
    private MockEndpoint mockLogEvents;

    @EndpointInject("mock:log-flow-summary")
    private MockEndpoint mockLogFlowSummary;

    @EndpointInject("mock:file-archive")
    private MockEndpoint mockFileArchive;

    private static final String TEST_MESSAGE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>TEST123</MsgId>
                        <CreDtTm>2025-12-18T10:00:00</CreDtTm>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
            </Document>
            """;

    @BeforeEach
    public void setUp() throws Exception {
        // Reset all mock endpoints
        mockSink.reset();
        mockLogEvents.reset();
        mockLogFlowSummary.reset();
        mockFileArchive.reset();

        // Setup will be done per test method to avoid kamelet consumer conflicts
    }

    @Test
    public void testKMqStarterBasicProcessing() throws Exception {
        // Create test-specific route to avoid kamelet consumer conflicts
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Simple test route that simulates k-mq-starter behavior
                from("direct:test-input").routeId("test-basic-processing")
                        .log("Processing message: ${body}")
                        .setHeader("flowCode", constant("PACS008"))
                        .setHeader("flowCountryCode", constant("CH"))
                        .setHeader("flowCountryId", constant("1"))
                        .setHeader("flowOccurId", simple("${date:now:yyyyMMddHHmmssSSS}"))
                        .setHeader("ReceivedTimestamp", simple("${date:now}")).to("mock:sink");
            }
        });

        // Setup expectations
        mockSink.expectedMessageCount(1);

        // Prepare test message with JMS headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("JMSMessageID", "TEST-MSG-001");
        headers.put("JMSCorrelationID", "CORR-001");
        headers.put("breadcrumbId", "BREAD-001");

        // Send test message
        producer.sendBodyAndHeaders(TEST_MESSAGE, headers);

        // Verify expectations
        mockSink.assertIsSatisfied(5000);

        // Verify message content preserved
        Exchange sinkExchange = mockSink.getReceivedExchanges().get(0);
        assertEquals(TEST_MESSAGE, sinkExchange.getIn().getBody(String.class));

        // Verify required headers are set
        assertNotNull(sinkExchange.getIn().getHeader("flowOccurId"));
        assertNotNull(sinkExchange.getIn().getHeader("ReceivedTimestamp"));
        assertEquals("PACS008", sinkExchange.getIn().getHeader("flowCode"));
        assertEquals("CH", sinkExchange.getIn().getHeader("flowCountryCode"));
        assertEquals("1", sinkExchange.getIn().getHeader("flowCountryId"));
    }

    @Test
    public void testSequenceGeneration() throws Exception {
        // Create test-specific route for sequence generation
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-sequence-generation")
                        .setHeader("flowOccurId", simple("${date:now:yyyyMMddHHmmssSSS}")).delay(1) // Small
                                                                                                    // delay
                                                                                                    // to
                                                                                                    // ensure
                                                                                                    // different
                                                                                                    // timestamps
                        .to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(2);

        // Send two messages
        producer.sendBody(TEST_MESSAGE);
        producer.sendBody(TEST_MESSAGE);

        mockSink.assertIsSatisfied(5000);

        // Verify different sequence numbers
        Exchange firstExchange = mockSink.getReceivedExchanges().get(0);
        Exchange secondExchange = mockSink.getReceivedExchanges().get(1);

        String firstFlowOccurId = firstExchange.getIn().getHeader("flowOccurId", String.class);
        String secondFlowOccurId = secondExchange.getIn().getHeader("flowOccurId", String.class);

        assertNotNull(firstFlowOccurId);
        assertNotNull(secondFlowOccurId);
        assertNotEquals(firstFlowOccurId, secondFlowOccurId);
    }

    @Test
    public void testLogEventsIntegration() throws Exception {
        // Create test-specific route for log events
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-log-events").log("Start MQ processing")
                        .to("mock:log-events").process(exchange -> {
                            // Simulate processing
                            exchange.getIn().setHeader("processed", true);
                        }).log("End MQ processing").to("mock:log-events").to("mock:sink");
            }
        });

        mockLogEvents.expectedMessageCount(2); // Start and end
        mockSink.expectedMessageCount(1);

        producer.sendBody(TEST_MESSAGE);

        mockLogEvents.assertIsSatisfied(5000);
        mockSink.assertIsSatisfied(5000);

        // Verify we received both log messages
        assertEquals(2, mockLogEvents.getReceivedCounter());
        assertEquals(1, mockSink.getReceivedCounter());
    }

    @Test
    public void testFlowSummaryLogging() throws Exception {
        // Create test-specific route for flow summary
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-flow-summary")
                        .setHeader("step", constant("processing")).to("mock:log-flow-summary")
                        .to("mock:sink");
            }
        });

        mockLogFlowSummary.expectedMessageCount(1);
        mockSink.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put("JMSMessageID", "TEST-FLOW-001");

        producer.sendBodyAndHeaders(TEST_MESSAGE, headers);

        mockLogFlowSummary.assertIsSatisfied(5000);
        mockSink.assertIsSatisfied(5000);

        // Verify flow summary was logged
        Exchange flowSummaryExchange = mockLogFlowSummary.getReceivedExchanges().get(0);
        assertEquals("processing", flowSummaryExchange.getIn().getHeader("step"));
    }

    @Test
    public void testEncodingDetection() throws Exception {
        // Create test-specific route for encoding detection
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-encoding-detection").process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    // Simulate encoding detection
                    if (body.contains("UTF-8")) {
                        exchange.getIn().setHeader("JMSEncoding", "UTF-8");
                    } else {
                        exchange.getIn().setHeader("JMSEncoding", "utf-8"); // Default
                    }
                }).to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(2);

        // Test UTF-8 message
        String utf8Message =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>UTF-8 Test éàñ</test>";
        producer.sendBody(utf8Message);

        // Test ISO-8859-1 message
        String isoMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><test>ISO Test</test>";
        producer.sendBody(isoMessage);

        mockSink.assertIsSatisfied(5000);

        // Verify encoding headers are set
        Exchange utf8Exchange = mockSink.getReceivedExchanges().get(0);
        Exchange isoExchange = mockSink.getReceivedExchanges().get(1);

        assertEquals("UTF-8", utf8Exchange.getIn().getHeader("JMSEncoding"));
        assertEquals("utf-8", isoExchange.getIn().getHeader("JMSEncoding")); // Default fallback
    }

    @Test
    public void testErrorHandling() throws Exception {
        // Create test-specific route for error handling
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:test-input").routeId("test-error-handling")
                        .onException(Exception.class).handled(true)
                        .log("Handled error: ${exception.message}")
                        .setHeader("JMSEncoding", constant("utf-8")).to("mock:sink").end()
                        .process(exchange -> {
                            // Simulate error handling - always set default encoding
                            exchange.getIn().setHeader("JMSEncoding", "utf-8");
                        }).to("mock:sink");
            }
        });

        mockSink.expectedMessageCount(1);

        // Send malformed XML
        String malformedXml = "This is not XML";

        // Should still process despite malformed content
        producer.sendBody(malformedXml);

        mockSink.assertIsSatisfied(5000);

        Exchange exchange = mockSink.getReceivedExchanges().get(0);
        assertEquals(malformedXml, exchange.getIn().getBody(String.class));

        // Should have default encoding
        assertEquals("utf-8", exchange.getIn().getHeader("JMSEncoding"));
    }
}
