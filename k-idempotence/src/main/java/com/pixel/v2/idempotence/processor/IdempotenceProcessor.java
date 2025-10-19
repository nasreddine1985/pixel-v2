package com.pixel.v2.idempotence.processor;

import com.pixel.v2.idempotence.model.IdempotenceResult;
import com.pixel.v2.idempotence.model.ProcessedIdentifier;
import com.pixel.v2.idempotence.repository.IdempotenceRepository;
import com.pixel.v2.idempotence.repository.impl.InMemoryIdempotenceRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Idempotence Processor for payment messages
 * Prevents duplicate transactions by tracking unique identifiers
 */
@Component("idempotenceProcessor")
public class IdempotenceProcessor implements Processor {
    
    private IdempotenceRepository repository;
    private boolean enableHashing = false;
    private String[] identifierXPaths = {
        "//*[local-name()='InstrId']",
        "//*[local-name()='EndToEndId']",
        "//*[local-name()='MsgId']"
    };
    private String[] identifierTypes = {"InstrId", "EndToEndId", "MsgId"};
    private String duplicateAction = "ERROR"; // ERROR, IGNORE, WARN
    private boolean trackMessageHash = true;
    
    public IdempotenceProcessor() {
        this.repository = new InMemoryIdempotenceRepository(); // Default to in-memory
    }
    
    public IdempotenceProcessor(IdempotenceRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String xmlBody = exchange.getIn().getBody(String.class);
        String messageId = exchange.getIn().getHeader("MessageId", String.class);
        String sourceSystem = exchange.getIn().getHeader("SourceSystem", String.class);
        
        if (xmlBody == null || xmlBody.trim().isEmpty()) {
            // No body to process - set minimal headers indicating no idempotence check
            exchange.getIn().setHeader("IsDuplicate", false);
            exchange.getIn().setHeader("CanProcess", true);
            // Do not set IdempotenceChecked for empty body
            return;
        }
        
        try {
            // Parse XML and extract identifiers
            Document document = parseXml(xmlBody);
            List<IdempotenceResult> results = checkIdempotence(document, messageId, sourceSystem, xmlBody);
            
            // Determine overall result
            boolean hasDuplicates = results.stream().anyMatch(IdempotenceResult::isDuplicate);
            IdempotenceResult primaryResult = results.isEmpty() ? null : results.get(0);
            
            // Set headers based on results
            setIdempotenceHeaders(exchange, primaryResult, hasDuplicates);
            
            // Set detailed results for complex scenarios
            if (results.size() > 1) {
                exchange.getIn().setHeader("IdempotenceResults", results);
            }
            
            // Handle duplicate action
            if (hasDuplicates) {
                handleDuplicateAction(exchange, results);
            }
            
        } catch (Exception e) {
            // Set error headers
            exchange.getIn().setHeader("IdempotenceError", true);
            exchange.getIn().setHeader("IdempotenceErrorMessage", e.getMessage());
            exchange.getIn().setHeader("CanProcess", true); // Allow processing on error by default
        }
    }
    
    private List<IdempotenceResult> checkIdempotence(Document document, String messageId, 
                                                    String sourceSystem, String xmlBody) throws XPathExpressionException {
        List<IdempotenceResult> results = new ArrayList<>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        for (int i = 0; i < identifierXPaths.length; i++) {
            String xpathExpr = identifierXPaths[i];
            String identifierType = identifierTypes[i];
            
            String identifier = (String) xpath.evaluate(xpathExpr, document, XPathConstants.STRING);
            
            if (identifier != null && !identifier.trim().isEmpty()) {
                IdempotenceResult result = checkSingleIdentifier(identifier, identifierType, 
                                                               messageId, sourceSystem, xmlBody);
                results.add(result);
            }
        }
        
        return results;
    }
    
    private IdempotenceResult checkSingleIdentifier(String identifier, String identifierType, 
                                                   String messageId, String sourceSystem, String xmlBody) {
        IdempotenceResult result = new IdempotenceResult(identifier, identifierType);
        
        try {
            // Check if identifier exists
            Optional<ProcessedIdentifier> existing = repository.findByIdentifierAndType(identifier, identifierType);
            
            if (existing.isPresent()) {
                // Duplicate found
                ProcessedIdentifier processed = existing.get();
                result.markAsDuplicate(processed.getFirstProcessedAt(), processed.getMessageId());
                
                // Update access count and get the updated object
                ProcessedIdentifier updated = repository.update(processed);
                result.setAccessCount(updated.getAccessCount());
                
                // Set action based on configuration
                result.setAction(IdempotenceResult.IdempotenceAction.valueOf(duplicateAction));
                
            } else {
                // First time processing
                result.markAsFirstTime();
                result.setAccessCount(1); // Set initial access count
                
                // Save new identifier
                ProcessedIdentifier newProcessed = new ProcessedIdentifier(identifier, identifierType, messageId);
                if (sourceSystem != null) {
                    newProcessed.setSourceSystem(sourceSystem);
                }
                
                if (trackMessageHash && xmlBody != null) {
                    newProcessed.setMessageHash(generateHash(xmlBody));
                }
                
                repository.save(newProcessed);
                result.setAction(IdempotenceResult.IdempotenceAction.PROCESS);
            }
            
        } catch (Exception e) {
            // On error, allow processing but log the issue
            result.setAction(IdempotenceResult.IdempotenceAction.PROCESS);
            result.setDuplicate(false);
        }
        
        return result;
    }
    
    private void setIdempotenceHeaders(Exchange exchange, IdempotenceResult result, boolean hasDuplicates) {
        exchange.getIn().setHeader("IsDuplicate", hasDuplicates);
        exchange.getIn().setHeader("CanProcess", !hasDuplicates || !duplicateAction.equals("ERROR"));
        exchange.getIn().setHeader("IdempotenceChecked", true);
        
        if (result != null) {
            exchange.getIn().setHeader("IdempotenceResult", result);
            exchange.getIn().setHeader("PrimaryIdentifier", result.getIdentifier());
            exchange.getIn().setHeader("PrimaryIdentifierType", result.getIdentifierType());
            exchange.getIn().setHeader("IdempotenceAction", result.getAction().toString());
            
            if (result.isDuplicate()) {
                exchange.getIn().setHeader("FirstProcessedAt", result.getFirstProcessedAt());
                exchange.getIn().setHeader("OriginalMessageId", result.getOriginalMessageId());
                exchange.getIn().setHeader("AccessCount", result.getAccessCount());
            }
        }
    }
    
    private void handleDuplicateAction(Exchange exchange, List<IdempotenceResult> results) {
        switch (duplicateAction.toUpperCase()) {
            case "ERROR":
                exchange.getIn().setHeader("DuplicateDetected", true);
                exchange.getIn().setHeader("ShouldReject", true);
                exchange.getIn().setHeader("RejectReason", "Duplicate identifier detected");
                break;
                
            case "IGNORE":
                exchange.getIn().setHeader("DuplicateDetected", true);
                exchange.getIn().setHeader("ShouldIgnore", true);
                exchange.getIn().setHeader("IgnoreReason", "Duplicate identifier - ignoring message");
                break;
                
            case "WARN":
                exchange.getIn().setHeader("DuplicateDetected", true);
                exchange.getIn().setHeader("DuplicateWarning", true);
                exchange.getIn().setHeader("WarningMessage", "Duplicate identifier detected but allowing processing");
                break;
        }
        
        // Set duplicate details for all found duplicates
        StringBuilder duplicateDetails = new StringBuilder();
        for (IdempotenceResult result : results) {
            if (result.isDuplicate()) {
                duplicateDetails.append(String.format("%s:%s (first seen: %s, access count: %d); ", 
                    result.getIdentifierType(), result.getIdentifier(), 
                    result.getFirstProcessedAt(), result.getAccessCount()));
            }
        }
        exchange.getIn().setHeader("DuplicateDetails", duplicateDetails.toString());
    }
    
    private Document parseXml(String xmlContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // Security settings
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }
    
    private String generateHash(String content) {
        if (!enableHashing) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    // Configuration setters
    public void setRepository(IdempotenceRepository repository) {
        this.repository = repository;
    }
    
    public void setEnableHashing(boolean enableHashing) {
        this.enableHashing = enableHashing;
    }
    
    public void setIdentifierXPaths(String[] identifierXPaths) {
        this.identifierXPaths = identifierXPaths;
    }
    
    public void setIdentifierTypes(String[] identifierTypes) {
        this.identifierTypes = identifierTypes;
        // Update XPath expressions to match the identifier types
        this.identifierXPaths = new String[identifierTypes.length];
        for (int i = 0; i < identifierTypes.length; i++) {
            this.identifierXPaths[i] = "//*[local-name()='" + identifierTypes[i] + "']";
        }
    }
    
    public void setDuplicateAction(String duplicateAction) {
        this.duplicateAction = duplicateAction;
    }
    
    public void setTrackMessageHash(boolean trackMessageHash) {
        this.trackMessageHash = trackMessageHash;
    }
}