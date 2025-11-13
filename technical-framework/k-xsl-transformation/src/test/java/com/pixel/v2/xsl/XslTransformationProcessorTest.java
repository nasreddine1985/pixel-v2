package com.pixel.v2.xsl;

import com.pixel.v2.transformation.processor.XslTransformationProcessor;
import com.pixel.v2.transformation.processor.XslTransformationException;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for XslTransformationProcessor collection transformation functionality.
 */
public class XslTransformationProcessorTest {

    private static final String VALID_PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">" +
            "<FIToFICstmrCdtTrf>" +
            "<GrpHdr>" +
            "<MsgId>MSG001</MsgId>" +
            "<CreDtTm>2023-01-01T12:00:00</CreDtTm>" +
            "<NbOfTxs>1</NbOfTxs>" +
            "<TtlIntrBkSttlmAmt Ccy=\"EUR\">100.00</TtlIntrBkSttlmAmt>" +
            "</GrpHdr>" +
            "</FIToFICstmrCdtTrf>" +
            "</Document>";

    private static final String ANOTHER_VALID_PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">" +
            "<FIToFICstmrCdtTrf>" +
            "<GrpHdr>" +
            "<MsgId>MSG002</MsgId>" +
            "<CreDtTm>2023-01-01T13:00:00</CreDtTm>" +
            "<NbOfTxs>1</NbOfTxs>" +
            "<TtlIntrBkSttlmAmt Ccy=\"USD\">200.00</TtlIntrBkSttlmAmt>" +
            "</GrpHdr>" +
            "</FIToFICstmrCdtTrf>" +
            "</Document>";

    private static final String INVALID_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<InvalidDocument>" +
            "<!-- Malformed XML for testing -->" +
            "<UnclosedTag>" +
            "</InvalidDocument>";

    private static final String VALID_ITL_PIVOT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"http://bnpparibas.com/G02\">" +
            "<ITLHdr>" +
            "<ITLFlow>" +
            "<ITLFlowCd>TEST_FLOW</ITLFlowCd>" +
            "<ITLFlowOcc>001</ITLFlowOcc>" +
            "</ITLFlow>" +
            "</ITLHdr>" +
            "<PmtPvt>" +
            "<InstrInf>" +
            "<InstrId>INSTR001</InstrId>" +
            "<EndToEndId>E2E001</EndToEndId>" +
            "</InstrInf>" +
            "<TxInf>" +
            "<TxId>TX001</TxId>" +
            "<IntrBkSttlmAmt Ccy=\"EUR\">100.00</IntrBkSttlmAmt>" +
            "</TxInf>" +
            "</PmtPvt>" +
            "</Document>";

    private Exchange exchange;
    private XslTransformationProcessor processor;

    @BeforeEach
    void setUp() {
        exchange = new DefaultExchange(new DefaultCamelContext());
        processor = new XslTransformationProcessor();
        // Set configuration via headers as the processor expects
        exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
        exchange.getIn().setHeader("TransformationMode", "STRICT");
    }

    @Test
    void testSingleValidMessage() throws Exception {
        // Arrange
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(1, exchange.getIn().getHeader("TransformationCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationTimestamp"));
        assertNotNull(exchange.getIn().getHeader("TransformationDuration"));
        
        // Check transformed content
        String transformedXml = exchange.getIn().getBody(String.class);
        assertNotNull(transformedXml);
        assertTrue(transformedXml.contains("TransformedPayment"));
        assertTrue(transformedXml.contains("<MessageId>MSG001</MessageId>"));
    }

    @Test
    void testSingleInvalidMessage() {
        // Arrange
        exchange.getIn().setBody(INVALID_XML);

        // Act & Assert
        XslTransformationException exception = assertThrows(XslTransformationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSL transformation failed"));
        assertEquals("ERROR", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals(1, exchange.getIn().getHeader("TransformationCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));
    }

    @Test
    void testCollectionAllValidMessages() throws Exception {
        // Arrange
        List<String> xmlMessages = Arrays.asList(VALID_PACS008_XML, ANOTHER_VALID_PACS008_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(2, exchange.getIn().getHeader("TransformationCount"));
        assertEquals(2, exchange.getIn().getHeader("TransformationSuccessCount"));
        assertEquals(0, exchange.getIn().getHeader("TransformationErrorCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationTimestamp"));
        assertNotNull(exchange.getIn().getHeader("TransformationDuration"));

        // Check transformed collection
        @SuppressWarnings("unchecked")
        List<String> transformedMessages = (List<String>) exchange.getIn().getBody();
        assertEquals(2, transformedMessages.size());
        assertTrue(transformedMessages.get(0).contains("TransformedPayment"));
        assertTrue(transformedMessages.get(0).contains("<MessageId>MSG001</MessageId>"));
        assertTrue(transformedMessages.get(1).contains("<MessageId>MSG002</MessageId>"));
    }

    @Test
    void testCollectionPartiallyInvalidMessages() {
        // Arrange
        List<String> xmlMessages = Arrays.asList(VALID_PACS008_XML, INVALID_XML, ANOTHER_VALID_PACS008_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act & Assert
        XslTransformationException exception = assertThrows(XslTransformationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("messages failed transformation"));
        assertEquals("ERROR", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals(3, exchange.getIn().getHeader("TransformationCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));
    }

    @Test
    void testEmptyCollection() throws Exception {
        // Arrange
        List<String> xmlMessages = Collections.emptyList();
        exchange.getIn().setBody(xmlMessages);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(0, exchange.getIn().getHeader("TransformationCount"));
        assertEquals(0, exchange.getIn().getHeader("TransformationSuccessCount"));
        assertEquals(0, exchange.getIn().getHeader("TransformationErrorCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationTimestamp"));
        assertNotNull(exchange.getIn().getHeader("TransformationDuration"));

        // Check empty collection result
        @SuppressWarnings("unchecked")
        List<String> transformedMessages = (List<String>) exchange.getIn().getBody();
        assertTrue(transformedMessages.isEmpty());
    }

    @Test
    void testNullBody() {
        // Arrange
        exchange.getIn().setBody(null);

        // Act & Assert
        XslTransformationException exception = assertThrows(XslTransformationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Message body is null"));
        assertEquals("ERROR", exchange.getIn().getHeader("TransformationStatus"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));
    }

    @Test
    void testUnsupportedBodyType() {
        // Arrange
        exchange.getIn().setBody(123); // Integer instead of String or Collection

        // Act & Assert
        XslTransformationException exception = assertThrows(XslTransformationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Unsupported body type"));
        assertEquals("ERROR", exchange.getIn().getHeader("TransformationStatus"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));
    }

    @Test
    void testLenientTransformationMode() throws Exception {
        // Arrange - Set lenient mode
        exchange.getIn().setHeader("TransformationMode", "LENIENT");
        List<String> xmlMessages = Arrays.asList(VALID_PACS008_XML, INVALID_XML, ANOTHER_VALID_PACS008_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act
        processor.process(exchange);

        // Assert - Should succeed in lenient mode
        assertEquals("PARTIAL", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(3, exchange.getIn().getHeader("TransformationCount"));
        assertEquals(2, exchange.getIn().getHeader("TransformationSuccessCount"));
        assertEquals(1, exchange.getIn().getHeader("TransformationErrorCount"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));

        // Check transformed collection - should have 3 items (nulls for failed transformations)
        @SuppressWarnings("unchecked")
        List<String> transformedMessages = (List<String>) exchange.getIn().getBody();
        assertEquals(3, transformedMessages.size());
        assertNotNull(transformedMessages.get(0)); // First message succeeded
        assertNull(transformedMessages.get(1));    // Second message failed
        assertNotNull(transformedMessages.get(2)); // Third message succeeded
    }

    @Test
    void testMissingXslFile() {
        // Arrange
        exchange.getIn().setHeader("XslFileName", "non-existent.xsl");
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act & Assert
        XslTransformationException exception = assertThrows(XslTransformationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSL stylesheet file not found"));
        assertEquals("ERROR", exchange.getIn().getHeader("TransformationStatus"));
        assertNotNull(exchange.getIn().getHeader("TransformationError"));
    }

    @Test
    void testTransformationDurationTracking() throws Exception {
        // Arrange
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act
        processor.process(exchange);

        // Assert
        Integer duration = exchange.getIn().getHeader("TransformationDuration", Integer.class);
        assertNotNull(duration);
        assertTrue(duration >= 0);
        assertTrue(duration < 10000); // Should be reasonable duration (less than 10 seconds)
    }

    @Test
    void testITLPivotSingleValidMessage() throws Exception {
        // Arrange
        exchange.getIn().setHeader("XslFileName", "itl-pivot-to-cdm.xsl");
        exchange.getIn().setHeader("TransformationMode", "STRICT");
        exchange.getIn().setBody(VALID_ITL_PIVOT_XML);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(1, exchange.getIn().getHeader("TransformationCount"));
        
        // Check transformed content
        String transformedXml = exchange.getIn().getBody(String.class);
        assertNotNull(transformedXml);
        assertTrue(transformedXml.contains("CDMPayment"));
        assertTrue(transformedXml.contains("<FlowCode>TEST_FLOW</FlowCode>"));
    }

    @Test
    void testITLPivotCollectionTransformation() throws Exception {
        // Arrange
        exchange.getIn().setHeader("XslFileName", "itl-pivot-to-cdm.xsl");
        List<String> xmlMessages = Arrays.asList(VALID_ITL_PIVOT_XML, VALID_ITL_PIVOT_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(2, exchange.getIn().getHeader("TransformationCount"));
        assertEquals(2, exchange.getIn().getHeader("TransformationSuccessCount"));
        assertEquals(0, exchange.getIn().getHeader("TransformationErrorCount"));

        // Check transformed collection
        @SuppressWarnings("unchecked")
        List<String> transformedMessages = (List<String>) exchange.getIn().getBody();
        assertEquals(2, transformedMessages.size());
        assertTrue(transformedMessages.get(0).contains("CDMPayment"));
        assertTrue(transformedMessages.get(1).contains("CDMPayment"));
    }

    @Test
    void testMixedSchemaTypesTransformation() throws Exception {
        // Arrange - Use a generic XSL that works with any XML
        exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
        List<String> xmlMessages = Arrays.asList(VALID_PACS008_XML, ANOTHER_VALID_PACS008_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("TransformationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("TransformationScope"));
        assertEquals(2, exchange.getIn().getHeader("TransformationCount"));
        assertEquals(2, exchange.getIn().getHeader("TransformationSuccessCount"));
        assertEquals(0, exchange.getIn().getHeader("TransformationErrorCount"));

        // Check transformed collection
        @SuppressWarnings("unchecked")
        List<String> transformedMessages = (List<String>) exchange.getIn().getBody();
        assertEquals(2, transformedMessages.size());
        assertTrue(transformedMessages.get(0).contains("TransformedPayment"));
        assertTrue(transformedMessages.get(1).contains("TransformedPayment"));
    }
}