package com.pixel.v2.transformation.demo;

import com.pixel.v2.transformation.processor.XslTransformationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;

import java.util.Arrays;
import java.util.List;

/**
 * Demo class showing how to use the XSL Transformation processor
 */
public class XslTransformationDemo {

    // Sample PACS.008 XML message
    private static final String PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">" +
            "<FIToFICstmrCdtTrf>" +
            "<GrpHdr>" +
            "<MsgId>DEMO001</MsgId>" +
            "<CreDtTm>2025-11-12T20:30:00</CreDtTm>" +
            "<NbOfTxs>1</NbOfTxs>" +
            "<TtlIntrBkSttlmAmt Ccy=\"EUR\">150.75</TtlIntrBkSttlmAmt>" +
            "</GrpHdr>" +
            "</FIToFICstmrCdtTrf>" +
            "</Document>";

    // Sample ITL Pivot XML message  
    private static final String ITL_PIVOT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<Document xmlns=\"http://bnpparibas.com/G02\">" +
            "<ITLHdr>" +
            "<ITLFlow>" +
            "<ITLFlowCd>DEMO_FLOW</ITLFlowCd>" +
            "<ITLFlowOcc>001</ITLFlowOcc>" +
            "</ITLFlow>" +
            "</ITLHdr>" +
            "<PmtPvt>" +
            "<InstrInf>" +
            "<InstrId>DEMO_INSTR</InstrId>" +
            "<EndToEndId>DEMO_E2E</EndToEndId>" +
            "</InstrInf>" +
            "<TxInf>" +
            "<TxId>DEMO_TX</TxId>" +
            "<IntrBkSttlmAmt Ccy=\"USD\">250.50</IntrBkSttlmAmt>" +
            "</TxInf>" +
            "</PmtPvt>" +
            "</Document>";

    public static void main(String[] args) {
        System.out.println("=== XSL TRANSFORMATION DEMO ===\n");

        XslTransformationDemo demo = new XslTransformationDemo();
        
        // Demo 1: Single PACS.008 transformation
        demo.demonstrateSinglePacs008Transformation();
        
        // Demo 2: Collection PACS.008 transformation
        demo.demonstrateCollectionPacs008Transformation();
        
        // Demo 3: Single ITL Pivot transformation
        demo.demonstrateSingleITLPivotTransformation();
        
        // Demo 4: Mixed collection with error handling
        demo.demonstrateMixedCollectionWithErrors();
        
        System.out.println("=== DEMO COMPLETED ===");
    }

    private void demonstrateSinglePacs008Transformation() {
        System.out.println("--- Demo 1: Single PACS.008 Transformation ---");
        
        try {
            XslTransformationProcessor processor = new XslTransformationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure transformation
            exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
            exchange.getIn().setHeader("TransformationMode", "STRICT");
            exchange.getIn().setBody(PACS008_XML);
            
            System.out.println("Input XML: " + PACS008_XML.substring(0, 100) + "...");
            
            // Execute transformation
            processor.process(exchange);
            
            // Show results
            String transformedXml = exchange.getIn().getBody(String.class);
            System.out.println("Transformation Status: " + exchange.getIn().getHeader("TransformationStatus"));
            System.out.println("Transformation Duration: " + exchange.getIn().getHeader("TransformationDuration") + "ms");
            System.out.println("Transformed XML: " + transformedXml.substring(0, Math.min(200, transformedXml.length())) + "...");
            
        } catch (Exception e) {
            System.err.println("Transformation failed: " + e.getMessage());
        }
        
        System.out.println();
    }

    private void demonstrateCollectionPacs008Transformation() {
        System.out.println("--- Demo 2: Collection PACS.008 Transformation ---");
        
        try {
            XslTransformationProcessor processor = new XslTransformationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure transformation for collection
            exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
            exchange.getIn().setHeader("TransformationMode", "STRICT");
            
            List<String> xmlCollection = Arrays.asList(
                PACS008_XML,
                PACS008_XML.replace("DEMO001", "DEMO002").replace("150.75", "300.25")
            );
            exchange.getIn().setBody(xmlCollection);
            
            System.out.println("Input Collection Size: " + xmlCollection.size());
            
            // Execute transformation
            processor.process(exchange);
            
            // Show results
            System.out.println("Transformation Status: " + exchange.getIn().getHeader("TransformationStatus"));
            System.out.println("Transformation Count: " + exchange.getIn().getHeader("TransformationCount"));
            System.out.println("Success Count: " + exchange.getIn().getHeader("TransformationSuccessCount"));
            System.out.println("Error Count: " + exchange.getIn().getHeader("TransformationErrorCount"));
            System.out.println("Duration: " + exchange.getIn().getHeader("TransformationDuration") + "ms");
            
            @SuppressWarnings("unchecked")
            List<String> transformedCollection = (List<String>) exchange.getIn().getBody();
            System.out.println("Transformed Collection Size: " + transformedCollection.size());
            
        } catch (Exception e) {
            System.err.println("Collection transformation failed: " + e.getMessage());
        }
        
        System.out.println();
    }

    private void demonstrateSingleITLPivotTransformation() {
        System.out.println("--- Demo 3: Single ITL Pivot Transformation ---");
        
        try {
            XslTransformationProcessor processor = new XslTransformationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure transformation for ITL Pivot
            exchange.getIn().setHeader("XslFileName", "itl-pivot-to-cdm.xsl");
            exchange.getIn().setHeader("TransformationMode", "STRICT");
            exchange.getIn().setBody(ITL_PIVOT_XML);
            
            System.out.println("Input ITL XML: " + ITL_PIVOT_XML.substring(0, 100) + "...");
            
            // Execute transformation
            processor.process(exchange);
            
            // Show results
            String transformedXml = exchange.getIn().getBody(String.class);
            System.out.println("Transformation Status: " + exchange.getIn().getHeader("TransformationStatus"));
            System.out.println("Transformation Duration: " + exchange.getIn().getHeader("TransformationDuration") + "ms");
            System.out.println("Transformed CDM XML: " + transformedXml.substring(0, Math.min(200, transformedXml.length())) + "...");
            
        } catch (Exception e) {
            System.err.println("ITL transformation failed: " + e.getMessage());
        }
        
        System.out.println();
    }

    private void demonstrateMixedCollectionWithErrors() {
        System.out.println("--- Demo 4: Mixed Collection with Error Handling ---");
        
        try {
            XslTransformationProcessor processor = new XslTransformationProcessor();
            Exchange exchange = new DefaultExchange(new DefaultCamelContext());
            
            // Configure transformation in LENIENT mode
            exchange.getIn().setHeader("XslFileName", "pacs-008-to-simplified.xsl");
            exchange.getIn().setHeader("TransformationMode", "LENIENT");
            
            // Create collection with valid and invalid messages
            List<String> mixedCollection = Arrays.asList(
                PACS008_XML,
                "<?xml version=\"1.0\"?><InvalidXML><UnclosedTag></InvalidXML>", // Invalid XML
                PACS008_XML.replace("DEMO001", "DEMO003")
            );
            exchange.getIn().setBody(mixedCollection);
            
            System.out.println("Input Mixed Collection Size: " + mixedCollection.size());
            
            // Execute transformation
            processor.process(exchange);
            
            // Show results
            System.out.println("Transformation Status: " + exchange.getIn().getHeader("TransformationStatus"));
            System.out.println("Transformation Count: " + exchange.getIn().getHeader("TransformationCount"));
            System.out.println("Success Count: " + exchange.getIn().getHeader("TransformationSuccessCount"));
            System.out.println("Error Count: " + exchange.getIn().getHeader("TransformationErrorCount"));
            System.out.println("Error Details: " + exchange.getIn().getHeader("TransformationError"));
            System.out.println("Duration: " + exchange.getIn().getHeader("TransformationDuration") + "ms");
            
        } catch (Exception e) {
            System.err.println("Mixed collection transformation failed: " + e.getMessage());
        }
        
        System.out.println();
    }
}