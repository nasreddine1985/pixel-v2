package com.pixel.v2.xsd;

import com.pixel.v2.validation.processor.XsdValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

/**
 * Simple test to debug ITL Pivot validation
 */
public class SimpleITLValidationTest {

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

    @Test
    void debugITLValidation() {
        try {
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            XsdValidationProcessor processor = new XsdValidationProcessor();
            
            // Set headers
            exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2-Simple.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(VALID_ITL_PIVOT_XML);
            
            System.out.println("=== TEST DEBUG INFO ===");
            System.out.println("XSD: " + exchange.getIn().getHeader("XsdFileName"));
            System.out.println("Mode: " + exchange.getIn().getHeader("ValidationMode"));
            System.out.println("XML: " + exchange.getIn().getBody());
            
            // Execute validation
            processor.process(exchange);
            
            // Print results
            System.out.println("=== VALIDATION RESULTS ===");
            System.out.println("Status: " + exchange.getIn().getHeader("ValidationStatus"));
            System.out.println("Scope: " + exchange.getIn().getHeader("ValidationScope"));
            System.out.println("Count: " + exchange.getIn().getHeader("ValidationCount"));
            System.out.println("Error: " + exchange.getIn().getHeader("ValidationError"));
            System.out.println("Duration: " + exchange.getIn().getHeader("ValidationDuration"));
            
            // Print all headers for debugging
            System.out.println("=== ALL HEADERS ===");
            exchange.getIn().getHeaders().forEach((k, v) -> System.out.println(k + ": " + v));
            
        } catch (Exception e) {
            System.out.println("=== EXCEPTION ===");
            e.printStackTrace();
        }
    }
}