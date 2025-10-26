package com.pixel.v2.validation.processor;

import com.pixel.v2.validation.model.ValidationResult;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentMessageValidator
 */
class PaymentMessageValidatorTest {
    
    private PaymentMessageValidator validator;
    private CamelContext camelContext;
    
    @BeforeEach
    void setUp() {
        validator = new PaymentMessageValidator();
        camelContext = new DefaultCamelContext();
    }
    
    @Test
    void testValidPacs008Message() throws Exception {
        String validPacs008 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123456789</MsgId>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                        <InstgAgt>
                            <FinInstnId>
                                <BIC>ABCDEFG1234</BIC>
                            </FinInstnId>
                        </InstgAgt>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <InstrId>INSTR123</InstrId>
                            <EndToEndId>E2E123</EndToEndId>
                        </PmtId>
                        <Amt>
                            <InstdAmt Ccy="EUR">1000.00</InstdAmt>
                        </Amt>
                        <Dbtr>
                            <Nm>John Doe</Nm>
                        </Dbtr>
                        <DbtrAcct>
                            <Id>
                                <IBAN>DE89370400440532013000</IBAN>
                            </Id>
                        </DbtrAcct>
                        <Cdtr>
                            <Nm>Jane Smith</Nm>
                        </Cdtr>
                        <CdtrAcct>
                            <Id>
                                <IBAN>FR1420041010050500013M02606</IBAN>
                            </Id>
                        </CdtrAcct>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(validPacs008);
        exchange.getIn().setHeader("MessageType", "pacs.008");
        
        validator.process(exchange);
        
        ValidationResult result = exchange.getIn().getHeader("ValidationResult", ValidationResult.class);
        assertNotNull(result);
        assertEquals("MSG123456789", result.getMessageId());
        assertEquals("pacs.008", result.getMessageType());
        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
        
        Boolean isValid = exchange.getIn().getHeader("IsValid", Boolean.class);
        assertTrue(isValid);
    }
    
    @Test
    void testInvalidPacs008Message_MissingMsgId() throws Exception {
        String invalidPacs008 = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <InstrId>INSTR123</InstrId>
                            <EndToEndId>E2E123</EndToEndId>
                        </PmtId>
                        <Amt>
                            <InstdAmt Ccy="EUR">1000.00</InstdAmt>
                        </Amt>
                        <Dbtr>
                            <Nm>John Doe</Nm>
                        </Dbtr>
                        <Cdtr>
                            <Nm>Jane Smith</Nm>
                        </Cdtr>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(invalidPacs008);
        exchange.getIn().setHeader("MessageType", "pacs.008");
        
        validator.process(exchange);
        
        ValidationResult result = exchange.getIn().getHeader("ValidationResult", ValidationResult.class);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCount() > 0);
        
        Boolean isValid = exchange.getIn().getHeader("IsValid", Boolean.class);
        assertFalse(isValid);
        
        Boolean validationFailed = exchange.getIn().getHeader("ValidationFailed", Boolean.class);
        assertTrue(validationFailed);
    }
    
    @Test
    void testInvalidXmlMessage() throws Exception {
        String invalidXml = "This is not XML";
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(invalidXml);
        
        validator.process(exchange);
        
        ValidationResult result = exchange.getIn().getHeader("ValidationResult", ValidationResult.class);
        assertNotNull(result);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCount() > 0);
        
        // Should have a parse error
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "XML_INVALID".equals(error.getErrorCode())));
    }
    
    @Test
    void testAutoDetectMessageType() throws Exception {
        String pan001Message = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
                <PmtInitn>
                    <GrpHdr>
                        <MsgId>PAN123456789</MsgId>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>PMT123</PmtInfId>
                        <PmtMtd>TRF</PmtMtd>
                    </PmtInf>
                </PmtInitn>
            </Document>
            """;
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(pan001Message);
        // No MessageType header - should auto-detect
        
        validator.process(exchange);
        
        ValidationResult result = exchange.getIn().getHeader("ValidationResult", ValidationResult.class);
        assertNotNull(result);
        assertEquals("pan.001", result.getMessageType());
        assertEquals("PAN123456789", result.getMessageId());
    }
    
    @Test 
    void testAmountValidation() throws Exception {
        String invalidAmount = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>MSG123</MsgId>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <InstrId>INSTR123</InstrId>
                            <EndToEndId>E2E123</EndToEndId>
                        </PmtId>
                        <Amt>
                            <InstdAmt Ccy="EUR">-100.00</InstdAmt>
                        </Amt>
                        <Dbtr>
                            <Nm>John Doe</Nm>
                        </Dbtr>
                        <Cdtr>
                            <Nm>Jane Smith</Nm>
                        </Cdtr>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """;
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(invalidAmount);
        exchange.getIn().setHeader("MessageType", "pacs.008");
        
        validator.process(exchange);
        
        ValidationResult result = exchange.getIn().getHeader("ValidationResult", ValidationResult.class);
        assertNotNull(result);
        assertFalse(result.isValid());
        
        // Should have amount validation error
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.getField().contains("Amt") && 
                                 "INVALID_VALUE".equals(error.getErrorCode())));
    }
}