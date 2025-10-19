package com.pixel.v2.validation.processor;

import com.pixel.v2.validation.model.ValidationResult;
import com.pixel.v2.validation.model.ValidationError;
import com.pixel.v2.validation.model.ValidationWarning;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Payment Message Validator Processor
 * Validates payment messages for correctness, completeness, and adherence
 */
public class PaymentMessageValidator implements Processor {
    
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$");
    private static final Pattern INSTRUCTION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9/\\-?:().,'+\\s]{1,35}$");
    private static final Pattern END_TO_END_ID_PATTERN = Pattern.compile("^[A-Za-z0-9/\\-?:().,'+\\s]{1,35}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String xmlBody = exchange.getIn().getBody(String.class);
        String messageType = exchange.getIn().getHeader("MessageType", String.class);
        
        ValidationResult result = validatePaymentMessage(xmlBody, messageType);
        
        // Set validation result headers
        exchange.getIn().setHeader("ValidationResult", result);
        exchange.getIn().setHeader("IsValid", result.isValid());
        exchange.getIn().setHeader("ErrorCount", result.getErrorCount());
        exchange.getIn().setHeader("WarningCount", result.getWarningCount());
        
        // Set validation details for routing decisions
        if (!result.isValid()) {
            exchange.getIn().setHeader("ValidationFailed", true);
            exchange.getIn().setHeader("ValidationErrors", result.getErrors());
        }
        
        if (result.hasWarnings()) {
            exchange.getIn().setHeader("ValidationWarnings", result.getWarnings());
        }
    }
    
    private ValidationResult validatePaymentMessage(String xmlContent, String messageType) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Parse XML
            Document document = parseXml(xmlContent);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Determine message type if not provided
            if (messageType == null || messageType.isEmpty()) {
                messageType = detectMessageType(document, xpath);
            }
            result.setMessageType(messageType);
            
            // Extract message ID
            String messageId = extractMessageId(document, xpath, messageType);
            result.setMessageId(messageId);
            
            // Perform validations based on message type
            switch (messageType.toLowerCase()) {
                case "pacs.008":
                case "pacs008":
                    validatePacs008Message(document, xpath, result);
                    break;
                case "pan.001":
                case "pan001":
                    validatePan001Message(document, xpath, result);
                    break;
                default:
                    validateGenericPaymentMessage(document, xpath, result);
            }
            
            // Set valid flag based on error count
            result.setValid(!result.hasErrors());
            
        } catch (Exception e) {
            result.addError("PARSE_ERROR", "XML_INVALID", "Failed to parse XML: " + e.getMessage());
        }
        
        return result;
    }
    
    private Document parseXml(String xmlContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
    }
    
    private String detectMessageType(Document document, XPath xpath) throws XPathExpressionException {
        // Try to detect PACS.008 (ignore namespaces for detection)
        NodeList pacs008Nodes = (NodeList) xpath.evaluate("//*[local-name()='FIToFICstmrCdtTrf']", document, XPathConstants.NODESET);
        if (pacs008Nodes.getLength() > 0) {
            return "pacs.008";
        }
        
        // Try to detect PAN.001 (ignore namespaces for detection)
        NodeList pan001Nodes = (NodeList) xpath.evaluate("//*[local-name()='PmtInitn']", document, XPathConstants.NODESET);
        if (pan001Nodes.getLength() > 0) {
            return "pan.001";
        }
        
        return "unknown";
    }
    
    private String extractMessageId(Document document, XPath xpath, String messageType) throws XPathExpressionException {
        String messageId = null;
        
        switch (messageType.toLowerCase()) {
            case "pacs.008":
            case "pacs008":
                messageId = (String) xpath.evaluate("//*[local-name()='FIToFICstmrCdtTrf']/*[local-name()='GrpHdr']/*[local-name()='MsgId']", document, XPathConstants.STRING);
                break;
            case "pan.001":
            case "pan001":
                messageId = (String) xpath.evaluate("//*[local-name()='PmtInitn']/*[local-name()='GrpHdr']/*[local-name()='MsgId']", document, XPathConstants.STRING);
                break;
        }
        
        return messageId != null ? messageId : "UNKNOWN";
    }
    
    private void validatePacs008Message(Document document, XPath xpath, ValidationResult result) throws XPathExpressionException {
        // Validate Group Header
        validateGroupHeader(document, xpath, result, "//*[local-name()='FIToFICstmrCdtTrf']/*[local-name()='GrpHdr']");
        
        // Validate Credit Transfer Transaction Information
        validateCreditTransferInfo(document, xpath, result);
        
        // Validate specific PACS.008 fields
        validatePacs008SpecificFields(document, xpath, result);
    }
    
    private void validatePan001Message(Document document, XPath xpath, ValidationResult result) throws XPathExpressionException {
        // Validate Group Header
        validateGroupHeader(document, xpath, result, "//*[local-name()='PmtInitn']/*[local-name()='GrpHdr']");
        
        // Validate Payment Information
        validatePaymentInformation(document, xpath, result);
    }
    
    private void validateGenericPaymentMessage(Document document, XPath xpath, ValidationResult result) {
        // Basic XML structure validation
        result.addWarning("MESSAGE_TYPE", "UNKNOWN_TYPE", "Unknown message type - performing basic validation only");
    }
    
    private void validateGroupHeader(Document document, XPath xpath, ValidationResult result, String headerPath) throws XPathExpressionException {
        // Message ID validation
        String msgId = (String) xpath.evaluate(headerPath + "/*[local-name()='MsgId']", document, XPathConstants.STRING);
        if (msgId == null || msgId.trim().isEmpty()) {
            result.addError("MsgId", "REQUIRED_FIELD", "Message ID is required");
        } else if (!INSTRUCTION_ID_PATTERN.matcher(msgId).matches()) {
            result.addError("MsgId", "INVALID_FORMAT", "Message ID format is invalid");
        }
        
        // Creation Date Time validation
        String creDtTm = (String) xpath.evaluate(headerPath + "/*[local-name()='CreDtTm']", document, XPathConstants.STRING);
        if (creDtTm == null || creDtTm.trim().isEmpty()) {
            result.addError("CreDtTm", "REQUIRED_FIELD", "Creation Date Time is required");
        } else {
            validateDateTime(creDtTm, "CreDtTm", result);
        }
        
        // Number of Transactions validation
        String nbOfTxs = (String) xpath.evaluate(headerPath + "/*[local-name()='NbOfTxs']", document, XPathConstants.STRING);
        if (nbOfTxs == null || nbOfTxs.trim().isEmpty()) {
            result.addError("NbOfTxs", "REQUIRED_FIELD", "Number of Transactions is required");
        } else {
            try {
                int txCount = Integer.parseInt(nbOfTxs);
                if (txCount <= 0) {
                    result.addError("NbOfTxs", "INVALID_VALUE", "Number of Transactions must be greater than 0");
                }
                if (txCount > 99999) {
                    result.addWarning("NbOfTxs", "HIGH_VOLUME", "High number of transactions may impact performance");
                }
            } catch (NumberFormatException e) {
                result.addError("NbOfTxs", "INVALID_FORMAT", "Number of Transactions must be numeric");
            }
        }
        
        // Instructing Agent validation (if present)
        String instgAgt = (String) xpath.evaluate(headerPath + "/*[local-name()='InstgAgt']/*[local-name()='FinInstnId']/*[local-name()='BIC']", document, XPathConstants.STRING);
        if (instgAgt != null && !instgAgt.trim().isEmpty()) {
            validateBIC(instgAgt, "InstgAgt.BIC", result);
        }
        
        // Instructed Agent validation (if present)
        String instdAgt = (String) xpath.evaluate(headerPath + "/*[local-name()='InstdAgt']/*[local-name()='FinInstnId']/*[local-name()='BIC']", document, XPathConstants.STRING);
        if (instdAgt != null && !instdAgt.trim().isEmpty()) {
            validateBIC(instdAgt, "InstdAgt.BIC", result);
        }
    }
    
    private void validateCreditTransferInfo(Document document, XPath xpath, ValidationResult result) throws XPathExpressionException {
        NodeList txInfoNodes = (NodeList) xpath.evaluate("//*[local-name()='CdtTrfTxInf']", document, XPathConstants.NODESET);
        
        for (int i = 0; i < txInfoNodes.getLength(); i++) {
            String txPath = "//*[local-name()='CdtTrfTxInf'][" + (i + 1) + "]";
            
            // Payment ID validation
            String pmtId = (String) xpath.evaluate(txPath + "/*[local-name()='PmtId']/*[local-name()='InstrId']", document, XPathConstants.STRING);
            if (pmtId == null || pmtId.trim().isEmpty()) {
                result.addError("PmtId.InstrId", "REQUIRED_FIELD", "Instruction ID is required for transaction " + (i + 1));
            } else if (!INSTRUCTION_ID_PATTERN.matcher(pmtId).matches()) {
                result.addError("PmtId.InstrId", "INVALID_FORMAT", "Instruction ID format is invalid for transaction " + (i + 1));
            }
            
            // End to End ID validation
            String endToEndId = (String) xpath.evaluate(txPath + "/*[local-name()='PmtId']/*[local-name()='EndToEndId']", document, XPathConstants.STRING);
            if (endToEndId == null || endToEndId.trim().isEmpty()) {
                result.addError("PmtId.EndToEndId", "REQUIRED_FIELD", "End to End ID is required for transaction " + (i + 1));
            } else if (!END_TO_END_ID_PATTERN.matcher(endToEndId).matches()) {
                result.addError("PmtId.EndToEndId", "INVALID_FORMAT", "End to End ID format is invalid for transaction " + (i + 1));
            }
            
            // Amount validation
            validateAmount(document, xpath, result, txPath, i + 1);
            
            // Debtor validation
            validateParty(document, xpath, result, txPath + "/*[local-name()='Dbtr']", "Debtor", i + 1);
            
            // Creditor validation  
            validateParty(document, xpath, result, txPath + "/*[local-name()='Cdtr']", "Creditor", i + 1);
            
            // Debtor Account validation
            validateAccount(document, xpath, result, txPath + "/*[local-name()='DbtrAcct']", "Debtor Account", i + 1);
            
            // Creditor Account validation
            validateAccount(document, xpath, result, txPath + "/*[local-name()='CdtrAcct']", "Creditor Account", i + 1);
        }
    }
    
    private void validatePaymentInformation(Document document, XPath xpath, ValidationResult result) throws XPathExpressionException {
        // Similar validation for PAN.001 payment information
        NodeList pmtInfoNodes = (NodeList) xpath.evaluate("//Document/PmtInitn/PmtInf", document, XPathConstants.NODESET);
        
        for (int i = 0; i < pmtInfoNodes.getLength(); i++) {
            String pmtPath = "//Document/PmtInitn/PmtInf[" + (i + 1) + "]";
            
            // Payment Information ID
            String pmtInfId = (String) xpath.evaluate(pmtPath + "/PmtInfId", document, XPathConstants.STRING);
            if (pmtInfId == null || pmtInfId.trim().isEmpty()) {
                result.addError("PmtInfId", "REQUIRED_FIELD", "Payment Information ID is required for payment " + (i + 1));
            }
            
            // Payment Method
            String pmtMtd = (String) xpath.evaluate(pmtPath + "/PmtMtd", document, XPathConstants.STRING);
            if (pmtMtd == null || pmtMtd.trim().isEmpty()) {
                result.addError("PmtMtd", "REQUIRED_FIELD", "Payment Method is required for payment " + (i + 1));
            } else if (!"TRF".equals(pmtMtd) && !"CHK".equals(pmtMtd) && !"TRA".equals(pmtMtd)) {
                result.addWarning("PmtMtd", "UNUSUAL_VALUE", "Unusual payment method: " + pmtMtd);
            }
            
            // Requested Execution Date
            String reqdExctnDt = (String) xpath.evaluate(pmtPath + "/ReqdExctnDt", document, XPathConstants.STRING);
            if (reqdExctnDt != null && !reqdExctnDt.trim().isEmpty()) {
                validateDate(reqdExctnDt, "ReqdExctnDt", result);
            }
        }
    }
    
    private void validatePacs008SpecificFields(Document document, XPath xpath, ValidationResult result) throws XPathExpressionException {
        // Validate Settlement Information
        String sttlmMtd = (String) xpath.evaluate("//Document/FIToFICstmrCdtTrf/CdtTrfTxInf/SttlmInf/SttlmMtd", document, XPathConstants.STRING);
        if (sttlmMtd != null && !sttlmMtd.trim().isEmpty()) {
            if (!"INDA".equals(sttlmMtd) && !"INGA".equals(sttlmMtd) && !"COVE".equals(sttlmMtd)) {
                result.addWarning("SttlmMtd", "UNUSUAL_VALUE", "Unusual settlement method: " + sttlmMtd);
            }
        }
        
        // Validate Channel Code
        String chnlTp = (String) xpath.evaluate("//Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/CtgyPurp/Cd", document, XPathConstants.STRING);
        if (chnlTp != null && !chnlTp.trim().isEmpty()) {
            validateChannelCode(chnlTp, result);
        }
    }
    
    private void validateAmount(Document document, XPath xpath, ValidationResult result, String txPath, int txIndex) throws XPathExpressionException {
        String amount = (String) xpath.evaluate(txPath + "/*[local-name()='Amt']/*[local-name()='InstdAmt']", document, XPathConstants.STRING);
        String currency = (String) xpath.evaluate(txPath + "/*[local-name()='Amt']/*[local-name()='InstdAmt']/@Ccy", document, XPathConstants.STRING);
        
        if (amount == null || amount.trim().isEmpty()) {
            result.addError("Amt.InstdAmt", "REQUIRED_FIELD", "Instructed Amount is required for transaction " + txIndex);
        } else {
            try {
                BigDecimal amountValue = new BigDecimal(amount);
                if (amountValue.compareTo(BigDecimal.ZERO) <= 0) {
                    result.addError("Amt.InstdAmt", "INVALID_VALUE", "Amount must be greater than 0 for transaction " + txIndex);
                }
                if (amountValue.compareTo(new BigDecimal("999999999.99")) > 0) {
                    result.addWarning("Amt.InstdAmt", "HIGH_AMOUNT", "High amount detected for transaction " + txIndex);
                }
            } catch (NumberFormatException e) {
                result.addError("Amt.InstdAmt", "INVALID_FORMAT", "Amount format is invalid for transaction " + txIndex);
            }
        }
        
        if (currency == null || currency.trim().isEmpty()) {
            result.addError("Amt.InstdAmt.Ccy", "REQUIRED_FIELD", "Currency is required for transaction " + txIndex);
        } else if (currency.length() != 3) {
            result.addError("Amt.InstdAmt.Ccy", "INVALID_FORMAT", "Currency code must be 3 characters for transaction " + txIndex);
        }
    }
    
    private void validateParty(Document document, XPath xpath, ValidationResult result, String partyPath, String partyType, int txIndex) throws XPathExpressionException {
        String name = (String) xpath.evaluate(partyPath + "/*[local-name()='Nm']", document, XPathConstants.STRING);
        if (name == null || name.trim().isEmpty()) {
            result.addError(partyType + ".Nm", "REQUIRED_FIELD", partyType + " name is required for transaction " + txIndex);
        } else if (name.length() > 70) {
            result.addError(partyType + ".Nm", "INVALID_LENGTH", partyType + " name exceeds maximum length for transaction " + txIndex);
        }
        
        // Address validation (if present)
        String country = (String) xpath.evaluate(partyPath + "/*[local-name()='PstlAdr']/*[local-name()='Ctry']", document, XPathConstants.STRING);
        if (country != null && !country.trim().isEmpty() && country.length() != 2) {
            result.addError(partyType + ".PstlAdr.Ctry", "INVALID_FORMAT", "Country code must be 2 characters for transaction " + txIndex);
        }
    }
    
    private void validateAccount(Document document, XPath xpath, ValidationResult result, String accountPath, String accountType, int txIndex) throws XPathExpressionException {
        String iban = (String) xpath.evaluate(accountPath + "/*[local-name()='Id']/*[local-name()='IBAN']", document, XPathConstants.STRING);
        String otherAccount = (String) xpath.evaluate(accountPath + "/*[local-name()='Id']/*[local-name()='Othr']/*[local-name()='Id']", document, XPathConstants.STRING);
        
        if ((iban == null || iban.trim().isEmpty()) && (otherAccount == null || otherAccount.trim().isEmpty())) {
            result.addError(accountType + ".Id", "REQUIRED_FIELD", accountType + " identification is required for transaction " + txIndex);
        } else if (iban != null && !iban.trim().isEmpty()) {
            validateIBAN(iban, accountType + ".IBAN", result, txIndex);
        }
    }
    
    private void validateBIC(String bic, String fieldName, ValidationResult result) {
        if (!BIC_PATTERN.matcher(bic).matches()) {
            result.addError(fieldName, "INVALID_FORMAT", "BIC format is invalid: " + bic);
        }
    }
    
    private void validateIBAN(String iban, String fieldName, ValidationResult result, int txIndex) {
        String cleanIban = iban.replaceAll("\\s", "");
        if (!IBAN_PATTERN.matcher(cleanIban).matches()) {
            result.addError(fieldName, "INVALID_FORMAT", "IBAN format is invalid for transaction " + txIndex);
        }
        
        // Basic IBAN check digit validation could be added here
        if (cleanIban.length() < 15 || cleanIban.length() > 34) {
            result.addError(fieldName, "INVALID_LENGTH", "IBAN length is invalid for transaction " + txIndex);
        }
    }
    
    private void validateDateTime(String dateTime, String fieldName, ValidationResult result) {
        try {
            // Basic ISO datetime validation
            if (!dateTime.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?([+-]\\d{2}:\\d{2}|Z)?")) {
                result.addError(fieldName, "INVALID_FORMAT", "DateTime format should be ISO 8601");
            }
        } catch (Exception e) {
            result.addError(fieldName, "INVALID_FORMAT", "DateTime format is invalid");
        }
    }
    
    private void validateDate(String date, String fieldName, ValidationResult result) {
        try {
            LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            result.addError(fieldName, "INVALID_FORMAT", "Date format is invalid");
        }
    }
    
    private void validateChannelCode(String channelCode, ValidationResult result) {
        // Example channel codes validation
        String[] validCodes = {"CASH", "CORT", "RTGS", "BOOK", "HOLD"};
        boolean isValid = false;
        for (String code : validCodes) {
            if (code.equals(channelCode)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            result.addWarning("CtgyPurp.Cd", "UNKNOWN_CODE", "Unknown channel code: " + channelCode);
        }
    }
}