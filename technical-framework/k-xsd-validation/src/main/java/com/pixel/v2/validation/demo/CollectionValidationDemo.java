package com.pixel.v2.validation.demo;

import com.pixel.v2.validation.processor.XsdValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;

import java.util.Arrays;
import java.util.List;

/**
 * Demo class showing how to use XsdValidationProcessor with collections
 * This is a simple demonstration of the collection validation functionality
 */
public class CollectionValidationDemo {

    private static final String VALID_XML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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

    private static final String VALID_XML_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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

    public static void main(String[] args) {
        System.out.println("=== XSD Validation Collection Demo ===\n");
        
        CollectionValidationDemo demo = new CollectionValidationDemo();
        
        // Demo 1: Single message validation
        System.out.println("1. Single Message Validation:");
        demo.demonstrateSingleValidation();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Demo 2: Collection validation with all valid messages
        System.out.println("2. Collection Validation (All Valid):");
        demo.demonstrateCollectionValidation();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Demo 3: Collection validation with mixed results
        System.out.println("3. Collection Validation (Mixed Results):");
        demo.demonstrateMixedCollectionValidation();
        
        System.out.println("\n=== Demo Complete ===");
    }

    public void demonstrateSingleValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure for single message validation
            exchange.getIn().setHeader("XsdFileName", "pacs.008.001.08.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(VALID_XML_1);
            
            // Process the validation
            processor.process(exchange);
            
            // Display results
            System.out.println("✅ Single validation successful!");
            System.out.println("   Validation Status: " + exchange.getIn().getHeader("ValidationStatus"));
            System.out.println("   Validation Scope: " + exchange.getIn().getHeader("ValidationScope"));
            System.out.println("   Message Count: " + exchange.getIn().getHeader("ValidationCount"));
            System.out.println("   Duration: " + exchange.getIn().getHeader("ValidationDuration") + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ Single validation failed: " + e.getMessage());
        }
    }

    public void demonstrateCollectionValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Create collection of valid XML messages
            List<String> xmlMessages = Arrays.asList(VALID_XML_1, VALID_XML_2);
            
            // Configure for collection validation
            exchange.getIn().setHeader("XsdFileName", "pacs.008.001.08.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(xmlMessages);
            
            // Process the validation
            processor.process(exchange);
            
            // Display results
            System.out.println("✅ Collection validation successful!");
            System.out.println("   Validation Status: " + exchange.getIn().getHeader("ValidationStatus"));
            System.out.println("   Validation Scope: " + exchange.getIn().getHeader("ValidationScope"));
            System.out.println("   Total Messages: " + exchange.getIn().getHeader("ValidationCount"));
            System.out.println("   Successful: " + exchange.getIn().getHeader("ValidationSuccessCount"));
            System.out.println("   Failed: " + exchange.getIn().getHeader("ValidationErrorCount"));
            System.out.println("   Duration: " + exchange.getIn().getHeader("ValidationDuration") + "ms");
            
        } catch (Exception e) {
            System.out.println("❌ Collection validation failed: " + e.getMessage());
        }
    }

    public void demonstrateMixedCollectionValidation() {
        try {
            XsdValidationProcessor processor = new XsdValidationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Create collection with one valid and one invalid XML
            String invalidXml = "<?xml version=\"1.0\"?><InvalidDocument>Not PACS.008 format</InvalidDocument>";
            List<String> xmlMessages = Arrays.asList(VALID_XML_1, invalidXml, VALID_XML_2);
            
            // Configure for collection validation
            exchange.getIn().setHeader("XsdFileName", "pacs.008.001.08.xsd");
            exchange.getIn().setHeader("ValidationMode", "STRICT");
            exchange.getIn().setBody(xmlMessages);
            
            // Process the validation
            processor.process(exchange);
            
            // This should not reach here if validation fails
            System.out.println("⚠️  Unexpected success with invalid data");
            
        } catch (Exception e) {
            System.out.println("❌ Collection validation failed as expected:");
            System.out.println("   Error: " + e.getMessage());
            
            // Note: In a real application, you would catch the specific exception
            // and extract the header information to see partial results
            System.out.println("   (In real usage, check ValidationSuccessCount and ValidationErrorCount headers)");
        }
    }
}

/*
Expected Output:

=== XSD Validation Collection Demo ===

1. Single Message Validation:
✅ Single validation successful!
   Validation Status: SUCCESS
   Validation Scope: SINGLE
   Message Count: 1
   Duration: 15ms

==================================================

2. Collection Validation (All Valid):
✅ Collection validation successful!
   Validation Status: SUCCESS
   Validation Scope: COLLECTION
   Total Messages: 2
   Successful: 2
   Failed: 0
   Duration: 12ms

==================================================

3. Collection Validation (Mixed Results):
❌ Collection validation failed as expected:
   Error: 1 out of 3 messages failed validation: [Message #2: cvc-elt.1.a: Cannot find the declaration of element 'InvalidDocument'.]
   (In real usage, check ValidationSuccessCount and ValidationErrorCount headers)

=== Demo Complete ===
*/