package com.pixel.v2.idempotence.processor;

import com.pixel.v2.idempotence.model.IdempotenceResult;
import com.pixel.v2.idempotence.repository.impl.InMemoryIdempotenceRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotenceProcessor
 */
class IdempotenceProcessorTest {
    
    private IdempotenceProcessor processor;
    private InMemoryIdempotenceRepository repository;
    private CamelContext camelContext;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryIdempotenceRepository();
        processor = new IdempotenceProcessor(repository);
        camelContext = new DefaultCamelContext();
    }
    
    @Test
    void testFirstTimeProcessing() throws Exception {
        // Configure processor to only check InstrId
        processor.setIdentifierTypes(new String[]{"InstrId"});
        
        String pacs008Message = createValidPacs008Message("INSTR123", "E2E123", "MSG123");
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(pacs008Message);
        exchange.getIn().setHeader("MessageId", "MSG123");
        
        processor.process(exchange);
        
        assertFalse(exchange.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
        assertTrue(exchange.getIn().getHeader("IdempotenceChecked", Boolean.class));
        assertEquals("INSTR123", exchange.getIn().getHeader("PrimaryIdentifier"));
        assertEquals("InstrId", exchange.getIn().getHeader("PrimaryIdentifierType"));
        assertEquals("PROCESS", exchange.getIn().getHeader("IdempotenceAction"));
        
        // Repository should contain the identifier
        assertEquals(1, repository.size());
    }
    
    @Test
    void testDuplicateDetection() throws Exception {
        String pacs008Message = createValidPacs008Message("INSTR456", "E2E456", "MSG456");
        
        // Process first time
        Exchange exchange1 = new DefaultExchange(camelContext);
        exchange1.getIn().setBody(pacs008Message);
        exchange1.getIn().setHeader("MessageId", "MSG456");
        
        processor.process(exchange1);
        
        // Verify first processing
        assertFalse(exchange1.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange1.getIn().getHeader("CanProcess", Boolean.class));
        
        // Process same message again
        Exchange exchange2 = new DefaultExchange(camelContext);
        exchange2.getIn().setBody(pacs008Message);
        exchange2.getIn().setHeader("MessageId", "MSG456_DUPLICATE");
        
        processor.process(exchange2);
        
        // Verify duplicate detection
        assertTrue(exchange2.getIn().getHeader("IsDuplicate", Boolean.class));
        assertFalse(exchange2.getIn().getHeader("CanProcess", Boolean.class));
        assertTrue(exchange2.getIn().getHeader("ShouldReject", Boolean.class));
        assertEquals("INSTR456", exchange2.getIn().getHeader("PrimaryIdentifier"));
        
        IdempotenceResult result = exchange2.getIn().getHeader("IdempotenceResult", IdempotenceResult.class);
        assertNotNull(result);
        assertTrue(result.isDuplicate());
        assertEquals("MSG456", result.getOriginalMessageId());
        assertEquals(2, result.getAccessCount());
    }
    
    @Test
    void testDuplicateActionIgnore() throws Exception {
        processor.setDuplicateAction("IGNORE");
        
        String pacs008Message = createValidPacs008Message("INSTR789", "E2E789", "MSG789");
        
        // Process first time
        Exchange exchange1 = new DefaultExchange(camelContext);
        exchange1.getIn().setBody(pacs008Message);
        processor.process(exchange1);
        
        // Process duplicate with IGNORE action
        Exchange exchange2 = new DefaultExchange(camelContext);
        exchange2.getIn().setBody(pacs008Message);
        processor.process(exchange2);
        
        assertTrue(exchange2.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange2.getIn().getHeader("CanProcess", Boolean.class));
        assertTrue(exchange2.getIn().getHeader("ShouldIgnore", Boolean.class));
        assertEquals("IGNORE", exchange2.getIn().getHeader("IdempotenceAction"));
    }
    
    @Test
    void testDuplicateActionWarn() throws Exception {
        processor.setDuplicateAction("WARN");
        
        String pacs008Message = createValidPacs008Message("INSTR999", "E2E999", "MSG999");
        
        // Process first time
        Exchange exchange1 = new DefaultExchange(camelContext);
        exchange1.getIn().setBody(pacs008Message);
        processor.process(exchange1);
        
        // Process duplicate with WARN action
        Exchange exchange2 = new DefaultExchange(camelContext);
        exchange2.getIn().setBody(pacs008Message);
        processor.process(exchange2);
        
        assertTrue(exchange2.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange2.getIn().getHeader("CanProcess", Boolean.class));
        assertTrue(exchange2.getIn().getHeader("DuplicateWarning", Boolean.class));
        assertEquals("WARN", exchange2.getIn().getHeader("IdempotenceAction"));
    }
    
    @Test
    void testMultipleIdentifierTypes() throws Exception {
        String pacs008Message = createValidPacs008Message("INSTR111", "E2E222", "MSG333");
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(pacs008Message);
        processor.process(exchange);
        
        // Should find multiple identifiers but use first one as primary
        assertEquals("INSTR111", exchange.getIn().getHeader("PrimaryIdentifier"));
        assertEquals("InstrId", exchange.getIn().getHeader("PrimaryIdentifierType"));
        
        // Repository should contain all tracked identifiers
        assertTrue(repository.size() >= 2); // At least InstrId and EndToEndId
    }
    
    @Test
    void testEmptyBody() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("");
        
        processor.process(exchange);
        
        assertFalse(exchange.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
        assertNull(exchange.getIn().getHeader("IdempotenceChecked")); // Should be null for empty body
    }
    
    @Test
    void testInvalidXml() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody("This is not XML");
        
        processor.process(exchange);
        
        assertTrue(exchange.getIn().getHeader("IdempotenceError", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
        assertNotNull(exchange.getIn().getHeader("IdempotenceErrorMessage"));
    }
    
    @Test
    void testPan001Message() throws Exception {
        String pan001Message = createValidPan001Message("INSTR555", "E2E555", "MSG555");
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(pan001Message);
        exchange.getIn().setHeader("MessageId", "MSG555");
        
        processor.process(exchange);
        
        assertFalse(exchange.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
        assertEquals("INSTR555", exchange.getIn().getHeader("PrimaryIdentifier"));
    }
    
    @Test
    void testMessageHashing() throws Exception {
        processor.setEnableHashing(true);
        processor.setTrackMessageHash(true);
        
        String pacs008Message = createValidPacs008Message("INSTR777", "E2E777", "MSG777");
        
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(pacs008Message);
        processor.process(exchange);
        
        // Verify processing completed successfully
        assertFalse(exchange.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
    }
    
    @Test
    void testSourceSystemTracking() throws Exception {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(createValidPacs008Message("INSTR888", "E2E888", "MSG888"));
        exchange.getIn().setHeader("SourceSystem", "TEST_SYSTEM");
        
        processor.process(exchange);
        
        assertFalse(exchange.getIn().getHeader("IsDuplicate", Boolean.class));
        assertTrue(exchange.getIn().getHeader("CanProcess", Boolean.class));
    }
    
    @Test
    void testDuplicateWithDifferentMessageId() throws Exception {
        String pacs008Message = createValidPacs008Message("SAMEINSTR", "SAMEENDTOEND", "MSG1");
        
        // Process first message
        Exchange exchange1 = new DefaultExchange(camelContext);
        exchange1.getIn().setBody(pacs008Message);
        exchange1.getIn().setHeader("MessageId", "MSG1");
        processor.process(exchange1);
        
        // Process same identifiers but different message
        String pacs008Message2 = createValidPacs008Message("SAMEINSTR", "SAMEENDTOEND", "MSG2");
        Exchange exchange2 = new DefaultExchange(camelContext);
        exchange2.getIn().setBody(pacs008Message2);
        exchange2.getIn().setHeader("MessageId", "MSG2");
        processor.process(exchange2);
        
        // Should detect duplicate based on identifiers, not message ID
        assertTrue(exchange2.getIn().getHeader("IsDuplicate", Boolean.class));
        assertEquals("MSG1", exchange2.getIn().getHeader("OriginalMessageId"));
    }
    
    private String createValidPacs008Message(String instrId, String endToEndId, String msgId) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>%s</MsgId>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <CdtTrfTxInf>
                        <PmtId>
                            <InstrId>%s</InstrId>
                            <EndToEndId>%s</EndToEndId>
                        </PmtId>
                        <Amt>
                            <InstdAmt Ccy="EUR">1000.00</InstdAmt>
                        </Amt>
                        <Dbtr><Nm>John Doe</Nm></Dbtr>
                        <Cdtr><Nm>Jane Smith</Nm></Cdtr>
                    </CdtTrfTxInf>
                </FIToFICstmrCdtTrf>
            </Document>
            """, msgId, instrId, endToEndId);
    }
    
    private String createValidPan001Message(String instrId, String endToEndId, String msgId) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
                <PmtInitn>
                    <GrpHdr>
                        <MsgId>%s</MsgId>
                        <CreDtTm>2023-10-19T10:30:00.000Z</CreDtTm>
                        <NbOfTxs>1</NbOfTxs>
                    </GrpHdr>
                    <PmtInf>
                        <PmtInfId>PMT123</PmtInfId>
                        <PmtMtd>TRF</PmtMtd>
                        <CdtTrfTxInf>
                            <PmtId>
                                <InstrId>%s</InstrId>
                                <EndToEndId>%s</EndToEndId>
                            </PmtId>
                            <Amt>
                                <InstdAmt Ccy="EUR">500.00</InstdAmt>
                            </Amt>
                        </CdtTrfTxInf>
                    </PmtInf>
                </PmtInitn>
            </Document>
            """, msgId, instrId, endToEndId);
    }
}