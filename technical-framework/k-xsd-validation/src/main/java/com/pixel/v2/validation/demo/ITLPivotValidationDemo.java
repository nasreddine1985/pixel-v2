package com.pixel.v2.validation.demo;

import com.pixel.v2.validation.processor.XsdValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;

import java.util.Arrays;
import java.util.List;

/**
 * Demo class showing ITL Pivot validation functionality
 */
public class ITLPivotValidationDemo {

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

    private static final String VALID_ITL_PIVOT_XML_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"http://bnpparibas.com/G02\">" +
            "<ITLHdr>" +
            "<ITLFlow>" +
            "<ITLFlowCd>TEST_FLOW_2</ITLFlowCd>" +
            "<ITLFlowOcc>002</ITLFlowOcc>" +
            "</ITLFlow>" +
            "</ITLHdr>" +
            "<PmtPvt>" +
            "<InstrInf>" +
            "<InstrId>INSTR002</InstrId>" +
            "<EndToEndId>E2E002</EndToEndId>" +
            "</InstrInf>" +
            "<TxInf>" +
            "<TxId>TX002</TxId>" +
            "<IntrBkSttlmAmt Ccy=\"USD\">250.00</IntrBkSttlmAmt>" +
            "</TxInf>" +
            "</PmtPvt>" +
            "</Document>";

    private static final String INVALID_ITL_PIVOT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"http://bnpparibas.com/G02\">" +
            "<ITLHdr>" +
            "<!-- Missing required ITLFlow element -->" +
            "</ITLHdr>" +
            "<PmtPvt>" +
            "<InstrInf>" +
            "<InstrId>INSTR001</InstrId>" +
            "</InstrInf>" +
            "</PmtPvt>" +
            "</Document>";

    public static void main(String[] args) {
        System.out.println("=== ITL Pivot XSD Validation Demo ===\n");
        
        ITLPivotValidationDemo demo = new ITLPivotValidationDemo();
        
        // Demo 1: Single ITL Pivot validation
        System.out.println("1. Single ITL Pivot Validation:");
        demo.demonstrateSingleITLValidation();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Demo 2: Collection validation with all valid ITL messages
        System.out.println("2. ITL Pivot Collection Validation (All Valid):");
        demo.demonstrateITLCollectionValidation();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Demo 3: Collection validation with mixed results
        System.out.println("3. ITL Pivot Collection Validation (Mixed Results):");
        demo.demonstrateMixedITLCollectionValidation();
        
        System.out.println("\n=== ITL Pivot Demo Complete ===");
    }

    public void demonstrateSingleITLValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure for ITL Pivot validation
            exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(VALID_ITL_PIVOT_XML);
            
            // Process the validation
            processor.process(exchange);
            
            // Display results
            System.out.println("✅ ITL Pivot single validation successful!");
            System.out.println("   Validation Status: " + exchange.getIn().getHeader("ValidationStatus"));
            System.out.println("   Validation Scope: " + exchange.getIn().getHeader("ValidationScope"));
            System.out.println("   Message Count: " + exchange.getIn().getHeader("ValidationCount"));
            System.out.println("   Duration: " + exchange.getIn().getHeader("ValidationDuration") + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ ITL Pivot single validation failed: " + e.getMessage());
        }
    }

    public void demonstrateITLCollectionValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Create collection of valid ITL Pivot messages
            List<String> itlMessages = Arrays.asList(VALID_ITL_PIVOT_XML, VALID_ITL_PIVOT_XML_2);
            
            // Configure for ITL Pivot collection validation
            exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(itlMessages);
            
            // Process the validation
            processor.process(exchange);
            
            // Display results
            System.out.println("✅ ITL Pivot collection validation successful!");
            System.out.println("   Validation Status: " + exchange.getIn().getHeader("ValidationStatus"));
            System.out.println("   Validation Scope: " + exchange.getIn().getHeader("ValidationScope"));
            System.out.println("   Total Messages: " + exchange.getIn().getHeader("ValidationCount"));
            System.out.println("   Successful: " + exchange.getIn().getHeader("ValidationSuccessCount"));
            System.out.println("   Failed: " + exchange.getIn().getHeader("ValidationErrorCount"));
            System.out.println("   Duration: " + exchange.getIn().getHeader("ValidationDuration") + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ ITL Pivot collection validation failed: " + e.getMessage());
        }
    }

    public void demonstrateMixedITLCollectionValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Create collection with one valid and one invalid ITL Pivot message
            List<String> mixedMessages = Arrays.asList(VALID_ITL_PIVOT_XML, INVALID_ITL_PIVOT_XML, VALID_ITL_PIVOT_XML_2);
            
            // Configure for ITL Pivot collection validation
            exchange.getIn().setHeader("XsdFileName", "ISO_Business_ITL_Pivot-v2.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(mixedMessages);
            
            // Process the validation
            processor.process(exchange);
            
            // This should not reach here if validation fails
            System.out.println("⚠️  Unexpected success with invalid ITL Pivot data");
            
        } catch (Exception e) {
            System.out.println("❌ ITL Pivot collection validation failed as expected:");
            System.out.println("   Error: " + e.getMessage());
            
            // Note: In a real application, you would catch the specific exception
            // and extract the header information to see partial results
            System.out.println("   (In real usage, check ValidationSuccessCount and ValidationErrorCount headers)");
        }
    }
}

/*
Expected Output:

=== ITL Pivot XSD Validation Demo ===

1. Single ITL Pivot Validation:
✅ ITL Pivot single validation successful!
   Validation Status: SUCCESS
   Validation Scope: SINGLE
   Message Count: 1
   Duration: 25ms

==================================================

2. ITL Pivot Collection Validation (All Valid):
✅ ITL Pivot collection validation successful!
   Validation Status: SUCCESS
   Validation Scope: COLLECTION
   Total Messages: 2
   Successful: 2
   Failed: 0
   Duration: 18ms

==================================================

3. ITL Pivot Collection Validation (Mixed Results):
❌ ITL Pivot collection validation failed as expected:
   Error: 1 out of 3 messages failed validation: [Message #2: cvc-complex-type.2.4.b: The content of element 'ITLHdr' is not complete...]
   (In real usage, check ValidationSuccessCount and ValidationErrorCount headers)

=== ITL Pivot Demo Complete ===
*/