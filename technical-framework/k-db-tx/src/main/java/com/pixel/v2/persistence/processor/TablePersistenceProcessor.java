package com.pixel.v2.persistence.processor;

import com.pixel.v2.persistence.model.FlowReference;
import com.pixel.v2.persistence.model.FlowSummary;
import com.pixel.v2.persistence.model.LogEvent;
import com.pixel.v2.persistence.model.PaymentAuditDetails;
import com.pixel.v2.persistence.service.FlowReferenceService;
import com.pixel.v2.persistence.service.FlowSummaryService;
import com.pixel.v2.persistence.service.LogEventService;
import com.pixel.v2.persistence.service.PaymentAuditDetailsService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Processor for handling table-based persistence operations Supports all four main tables:
 * FLOW_REFERENCE, LOG_EVENT, FLOW_SUMMARY, PAYMENT_AUDIT_DETAILS
 */
@Component
@Transactional
public class TablePersistenceProcessor implements Processor {

    private final FlowReferenceService flowReferenceService;
    private final LogEventService logEventService;
    private final FlowSummaryService flowSummaryService;
    private final PaymentAuditDetailsService paymentAuditDetailsService;

    public TablePersistenceProcessor(FlowReferenceService flowReferenceService,
            LogEventService logEventService, FlowSummaryService flowSummaryService,
            PaymentAuditDetailsService paymentAuditDetailsService) {
        this.flowReferenceService = flowReferenceService;
        this.logEventService = logEventService;
        this.flowSummaryService = flowSummaryService;
        this.paymentAuditDetailsService = paymentAuditDetailsService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String operationType = exchange.getIn().getHeader("operation", String.class);
        String tableType = exchange.getIn().getHeader("table", String.class);

        if (operationType == null || tableType == null) {
            throw new IllegalArgumentException("Both 'operation' and 'table' headers are required");
        }

        switch (tableType.toUpperCase()) {
            case "FLOW_REFERENCE":
                handleFlowReference(exchange, operationType);
                break;
            case "LOG_EVENT":
                handleLogEvent(exchange, operationType);
                break;
            case "FLOW_SUMMARY":
                handleFlowSummary(exchange, operationType);
                break;
            case "PAYMENT_AUDIT_DETAILS":
                handlePaymentAuditDetails(exchange, operationType);
                break;
            default:
                throw new IllegalArgumentException("Unsupported table type: " + tableType);
        }
    }

    private void handleFlowReference(Exchange exchange, String operationType) {
        Object body = exchange.getIn().getBody();

        switch (operationType.toLowerCase()) {
            case "save":
                if (body instanceof FlowReference flowReference) {
                    FlowReference saved = flowReferenceService.save(flowReference);
                    exchange.getMessage().setBody(saved);
                } else {
                    throw new IllegalArgumentException(
                            "Body must be FlowReference for save operation");
                }
                break;
            case "findByFlowCode":
                String flowCode = exchange.getIn().getHeader("flowCode", String.class);
                if (flowCode != null) {
                    exchange.getMessage().setBody(flowReferenceService.findByFlowCode(flowCode));
                } else {
                    throw new IllegalArgumentException(
                            "flowCode header is required for findByFlowCode operation");
                }
                break;
            case "findActiveFlows":
                exchange.getMessage().setBody(flowReferenceService.findActiveFlows());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported operation for FlowReference: " + operationType);
        }
    }

    private void handleLogEvent(Exchange exchange, String operationType) {
        Object body = exchange.getIn().getBody();

        switch (operationType.toLowerCase()) {
            case "save":
                if (body instanceof LogEvent logEvent) {
                    LogEvent saved = logEventService.save(logEvent);
                    exchange.getMessage().setBody(saved);
                } else {
                    throw new IllegalArgumentException("Body must be LogEvent for save operation");
                }
                break;
            case "findByFlowId":
                String flowId = exchange.getIn().getHeader("flowId", String.class);
                if (flowId != null) {
                    exchange.getMessage().setBody(logEventService.findByFlowId(flowId));
                } else {
                    throw new IllegalArgumentException(
                            "flowId header is required for findByFlowId operation");
                }
                break;
            case "findErrorLogs":
                String flowCodeForErrors = exchange.getIn().getHeader("flowCode", String.class);
                if (flowCodeForErrors != null) {
                    exchange.getMessage()
                            .setBody(logEventService.findErrorLogsByFlowCode(flowCodeForErrors));
                } else {
                    throw new IllegalArgumentException(
                            "flowCode header is required for findErrorLogs operation");
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported operation for LogEvent: " + operationType);
        }
    }

    private void handleFlowSummary(Exchange exchange, String operationType) {
        Object body = exchange.getIn().getBody();

        switch (operationType.toLowerCase()) {
            case "save":
                if (body instanceof FlowSummary flowSummary) {
                    FlowSummary saved = flowSummaryService.save(flowSummary);
                    exchange.getMessage().setBody(saved);
                } else {
                    throw new IllegalArgumentException(
                            "Body must be FlowSummary for save operation");
                }
                break;
            case "findByFlowCode":
                String flowCode = exchange.getIn().getHeader("flowCode", String.class);
                if (flowCode != null) {
                    exchange.getMessage().setBody(flowSummaryService.findByFlowCode(flowCode));
                } else {
                    throw new IllegalArgumentException(
                            "flowCode header is required for findByFlowCode operation");
                }
                break;
            case "findActiveFlows":
                exchange.getMessage().setBody(flowSummaryService.findActiveFlows());
                break;
            case "findFlowsWithErrors":
                exchange.getMessage().setBody(flowSummaryService.findFlowsWithErrors());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported operation for FlowSummary: " + operationType);
        }
    }

    private void handlePaymentAuditDetails(Exchange exchange, String operationType) {
        Object body = exchange.getIn().getBody();

        switch (operationType.toLowerCase()) {
            case "save":
                if (body instanceof PaymentAuditDetails paymentAuditDetails) {
                    PaymentAuditDetails saved =
                            paymentAuditDetailsService.save(paymentAuditDetails);
                    exchange.getMessage().setBody(saved);
                } else {
                    throw new IllegalArgumentException(
                            "Body must be PaymentAuditDetails for save operation");
                }
                break;
            case "findByFlowOccurId":
                String flowOccurId = exchange.getIn().getHeader("flowOccurId", String.class);
                if (flowOccurId != null) {
                    exchange.getMessage()
                            .setBody(paymentAuditDetailsService.findByFlowOccurId(flowOccurId));
                } else {
                    throw new IllegalArgumentException(
                            "flowOccurId header is required for findByFlowOccurId operation");
                }
                break;
            case "findByCurrency":
                String currency = exchange.getIn().getHeader("currency", String.class);
                if (currency != null) {
                    exchange.getMessage()
                            .setBody(paymentAuditDetailsService.findByCurrency(currency));
                } else {
                    throw new IllegalArgumentException(
                            "currency header is required for findByCurrency operation");
                }
                break;
            case "findHighValuePayments":
                String threshold = exchange.getIn().getHeader("threshold", String.class);
                if (threshold != null) {
                    exchange.getMessage().setBody(paymentAuditDetailsService
                            .findHighValuePayments(new BigDecimal(threshold)));
                } else {
                    throw new IllegalArgumentException(
                            "threshold header is required for findHighValuePayments operation");
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported operation for PaymentAuditDetails: " + operationType);
        }
    }
}
