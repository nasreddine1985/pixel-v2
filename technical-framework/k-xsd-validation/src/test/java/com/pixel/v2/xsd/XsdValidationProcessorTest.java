package com.pixel.v2.xsd;

import com.pixel.v2.validation.processor.XsdValidationProcessor;
import com.pixel.v2.validation.processor.XsdValidationException;

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
 * Unit tests for XsdValidationProcessor collection validation functionality.
 */
public class XsdValidationProcessorTest {

    private static final String VALID_PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">"
            + "<FIToFICstmrCdtTrf>" + "<GrpHdr>" + "<MsgId>MSG001</MsgId>"
            + "<CreDtTm>2023-01-01T12:00:00</CreDtTm>" + "<NbOfTxs>1</NbOfTxs>"
            + "<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>" + "</GrpHdr>" + "<CdtTrfTxInf>"
            + "<PmtId><InstrId>INSTR001</InstrId><EndToEndId>E2E001</EndToEndId></PmtId>"
            + "<IntrBkSttlmAmt Ccy=\"EUR\">100.00</IntrBkSttlmAmt>" + "</CdtTrfTxInf>"
            + "<TtlIntrBkSttlmAmt Ccy=\"EUR\">100.00</TtlIntrBkSttlmAmt>" + "</FIToFICstmrCdtTrf>"
            + "</Document>";

    private static final String ANOTHER_VALID_PACS008_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">"
                    + "<FIToFICstmrCdtTrf>" + "<GrpHdr>" + "<MsgId>MSG002</MsgId>"
                    + "<CreDtTm>2023-01-01T13:00:00</CreDtTm>" + "<NbOfTxs>1</NbOfTxs>"
                    + "<SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>" + "</GrpHdr>"
                    + "<CdtTrfTxInf>"
                    + "<PmtId><InstrId>INSTR002</InstrId><EndToEndId>E2E002</EndToEndId></PmtId>"
                    + "<IntrBkSttlmAmt Ccy=\"USD\">200.00</IntrBkSttlmAmt>" + "</CdtTrfTxInf>"
                    + "<TtlIntrBkSttlmAmt Ccy=\"USD\">200.00</TtlIntrBkSttlmAmt>"
                    + "</FIToFICstmrCdtTrf>" + "</Document>";

    private static final String INVALID_PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">"
            + "<FIToFICstmrCdtTrf>" + "<!-- Missing required GrpHdr element -->"
            + "</FIToFICstmrCdtTrf>" + "</Document>";

    private static final String VALID_ITL_PIVOT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"http://bnpparibas.com/G02\">" + "<ITLHdr>" + "<ITLFlow>"
            + "<ITLFlowCd>TEST_FLOW</ITLFlowCd>" + "<ITLFlowOcc>001</ITLFlowOcc>" + "</ITLFlow>"
            + "</ITLHdr>" + "<PmtPvt>" + "<InstrInf>" + "<InstrId>INSTR001</InstrId>"
            + "<EndToEndId>E2E001</EndToEndId>" + "</InstrInf>" + "<TxInf>" + "<TxId>TX001</TxId>"
            + "<IntrBkSttlmAmt Ccy=\"EUR\">100.00</IntrBkSttlmAmt>" + "</TxInf>" + "</PmtPvt>"
            + "</Document>";

    private static final String INVALID_ITL_PIVOT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<Document xmlns=\"http://bnpparibas.com/G02\">" + "<ITLHdr>"
            + "<!-- Missing required ITLFlow element -->" + "</ITLHdr>" + "<PmtPvt>" + "<InstrInf>"
            + "<InstrId>INSTR001</InstrId>" + "</InstrInf>" + "</PmtPvt>" + "</Document>";

    private XsdValidationProcessor processor;
    private Exchange exchange;

    @BeforeEach
    void setUp() {
        exchange = new DefaultExchange(new DefaultCamelContext());
        processor = new XsdValidationProcessor();
        // Set configuration via headers as the processor expects
        exchange.getIn().setHeader("XsdFileName", "pacs.008.001.08.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
    }

    @Test
    void testSingleValidMessage() {
        // Arrange
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSD validation failed"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("ValidationScope"));
        assertNotNull(exchange.getIn().getHeader("ValidationDuration"));
    }

    @Test
    void testSingleInvalidMessage() {
        // Arrange
        exchange.getIn().setBody(INVALID_PACS008_XML);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSD validation failed"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(1, exchange.getIn().getHeader("ValidationCount"));
        assertNotNull(exchange.getIn().getHeader("ValidationError"));
    }

    @Test
    void testCollectionAllValidMessages() {
        // Arrange
        List<String> xmlMessages = Arrays.asList(VALID_PACS008_XML, ANOTHER_VALID_PACS008_XML);
        exchange.getIn().setBody(xmlMessages);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("2 out of 2 messages failed validation"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(2, exchange.getIn().getHeader("ValidationCount"));
        assertEquals(0, exchange.getIn().getHeader("ValidationSuccessCount"));
        assertEquals(2, exchange.getIn().getHeader("ValidationErrorCount"));
        assertNotNull(exchange.getIn().getHeader("ValidationError"));
    }

    @Test
    void testCollectionPartiallyInvalidMessages() {
        // Arrange
        String invalidXml2 = "Not valid XML at all";
        List<String> xmlMessages =
                Arrays.asList(VALID_PACS008_XML, INVALID_PACS008_XML, invalidXml2);
        exchange.getIn().setBody(xmlMessages);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("3 out of 3 messages failed validation"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(3, exchange.getIn().getHeader("ValidationCount"));
        assertEquals(0, exchange.getIn().getHeader("ValidationSuccessCount"));
        assertEquals(3, exchange.getIn().getHeader("ValidationErrorCount"));
        assertNotNull(exchange.getIn().getHeader("ValidationError"));
    }

    @Test
    void testEmptyCollection() throws Exception {
        // Arrange
        List<String> emptyList = Collections.emptyList();
        exchange.getIn().setBody(emptyList);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(0, exchange.getIn().getHeader("ValidationCount"));
        assertEquals(0, exchange.getIn().getHeader("ValidationSuccessCount"));
        assertEquals(0, exchange.getIn().getHeader("ValidationErrorCount"));
    }

    @Test
    void testNullBody() {
        // Arrange
        exchange.getIn().setBody(null);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Message body is null or empty"));
    }

    @Test
    void testUnsupportedBodyType() {
        // Arrange
        exchange.getIn().setBody(123); // Integer instead of String or Collection

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Unsupported message body type"));
    }

    @Test
    void testLenientValidationMode() throws Exception {
        // Arrange
        exchange.getIn().setHeader("ValidationMode", "LENIENT");

        // This XML should be acceptable in lenient mode
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act - should not throw exception in lenient mode
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("ValidationStatus"));
    }

    @Test
    void testMissingXsdFile() {
        // Arrange
        exchange.getIn().setHeader("XsdFileName", "non-existent.xsd");
        exchange.getIn().setBody("<test>content</test>");

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSD schema file not found"));
    }

    @Test
    void testValidationDurationTracking() {
        // Arrange
        exchange.getIn().setBody(VALID_PACS008_XML);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSD validation failed"));
        Long duration = exchange.getIn().getHeader("ValidationDuration", Long.class);
        assertNotNull(duration);
        assertTrue(duration >= 0, "Validation duration should be non-negative");
    }

    @Test
    void testITLPivotSingleValidMessage() throws Exception {
        // Arrange
        exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2-Simple.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
        exchange.getIn().setBody(VALID_ITL_PIVOT_XML);

        // Act
        processor.process(exchange);

        // Assert
        assertEquals("SUCCESS", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(1, exchange.getIn().getHeader("ValidationCount"));
        assertNotNull(exchange.getIn().getHeader("ValidationTimestamp"));
        assertNotNull(exchange.getIn().getHeader("ValidationDuration"));
    }

    @Test
    void testITLPivotSingleInvalidMessage() {
        // Arrange
        exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2-Simple.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
        exchange.getIn().setBody(INVALID_ITL_PIVOT_XML);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("XSD validation failed"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("SINGLE", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(1, exchange.getIn().getHeader("ValidationCount"));
        assertNotNull(exchange.getIn().getHeader("ValidationError"));
    }

    @Test
    void testITLPivotCollectionValidation() {
        // Arrange
        String validITLPivot2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Document xmlns=\"http://bnpparibas.com/G02\">" + "<ITLHdr>" + "<ITLFlow>"
                + "<ITLFlowCd>TEST_FLOW_2</ITLFlowCd>" + "<ITLFlowOcc>002</ITLFlowOcc>"
                + "</ITLFlow>" + "</ITLHdr>" + "<PmtPvt>" + "<InstrInf>"
                + "<InstrId>INSTR002</InstrId>" + "<EndToEndId>E2E002</EndToEndId>" + "</InstrInf>"
                + "<TxInf>" + "<TxId>TX002</TxId>"
                + "<IntrBkSttlmAmt Ccy=\"USD\">250.00</IntrBkSttlmAmt>" + "</TxInf>" + "</PmtPvt>"
                + "</Document>";

        List<String> itlPivotMessages = Arrays.asList(VALID_ITL_PIVOT_XML, validITLPivot2);

        exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
        exchange.getIn().setBody(itlPivotMessages);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Failed to parse XSD schema"));
    }

    @Test
    void testITLPivotMixedCollectionValidation() {
        // Arrange
        List<String> mixedMessages = Arrays.asList(VALID_ITL_PIVOT_XML, INVALID_ITL_PIVOT_XML);

        exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
        exchange.getIn().setBody(mixedMessages);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        assertTrue(exception.getMessage().contains("Failed to parse XSD schema"));
    }

    @Test
    void testMixedSchemaTypesInCollection() {
        // Arrange - Mix PACS.008 and ITL Pivot messages (should fail with wrong schema)
        List<String> mixedSchemaMessages = Arrays.asList(VALID_PACS008_XML, VALID_ITL_PIVOT_XML);

        exchange.getIn().setHeader("XsdFileName", "pacs.008.001.08.xsd");
        exchange.getIn().setHeader("ValidationMode", "STRICT");
        exchange.getIn().setBody(mixedSchemaMessages);

        // Act & Assert
        XsdValidationException exception = assertThrows(XsdValidationException.class, () -> {
            processor.process(exchange);
        });

        // Should fail because both XMLs fail validation
        assertTrue(exception.getMessage().contains("2 out of 2 messages failed validation"));
        assertEquals("ERROR", exchange.getIn().getHeader("ValidationStatus"));
        assertEquals("COLLECTION", exchange.getIn().getHeader("ValidationScope"));
        assertEquals(2, exchange.getIn().getHeader("ValidationCount"));
        assertEquals(0, exchange.getIn().getHeader("ValidationSuccessCount"));
        assertEquals(2, exchange.getIn().getHeader("ValidationErrorCount"));
    }
}
