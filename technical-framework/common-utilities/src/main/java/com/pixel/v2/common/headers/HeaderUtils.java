package com.pixel.v2.common.headers;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.pixel.v2.common.headers.HeaderConstants.*;

/**
 * Utilitaires pour la gestion des headers dans les exchanges Camel.
 * Cette classe fournit des méthodes pratiques pour lire et écrire les headers
 * en utilisant les constantes définies dans HeaderConstants.
 */
public final class HeaderUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private HeaderUtils() {
        // Classe utilitaire - constructeur privé
    }

    // ==================================================================================
    // MÉTHODES DE LECTURE DES HEADERS (avec fallback)
    // ==================================================================================

    /**
     * Récupère les données JSON du flow depuis les headers avec fallback automatique.
     * Cherche dans l'ordre : FlowDataJson, flowDataJson, RefFlowDataJson, refFlowDataJson
     */
    public static String getFlowDataJson(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(FLOW_DATA_JSON, String.class);
        if (value != null) return value;
        
        value = message.getHeader(FLOW_DATA_JSON_LC, String.class);
        if (value != null) return value;
        
        value = message.getHeader(REF_FLOW_DATA_JSON, String.class);
        if (value != null) return value;
        
        return message.getHeader(REF_FLOW_DATA_JSON_LC, String.class);
    }

    /**
     * Récupère l'identifiant d'occurrence du flow avec fallback.
     */
    public static String getFlowOccurId(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(FLOW_OCCUR_ID, String.class);
        if (value != null) return value;
        
        return message.getHeader(FLOW_OCCUR_ID_CC, String.class);
    }

    /**
     * Récupère l'identifiant du message avec fallback.
     */
    public static String getMessageId(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(MESSAGE_ID, String.class);
        if (value != null) return value;
        
        return message.getHeader(MESSAGE_ID_LC, String.class);
    }

    /**
     * Récupère l'identifiant de corrélation avec fallback.
     */
    public static String getCorrelationId(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(CORRELATION_ID, String.class);
        if (value != null) return value;
        
        return message.getHeader(CORRELATION_ID_LC, String.class);
    }

    /**
     * Récupère le timestamp de réception avec fallback.
     */
    public static String getReceivedTimestamp(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(RECEIVED_TIMESTAMP, String.class);
        if (value != null) return value;
        
        return message.getHeader(RECEIVED_TIMESTAMP_LC, String.class);
    }

    /**
     * Récupère le mode de traitement avec fallback et valeur par défaut.
     */
    public static String getProcessingMode(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(PROCESSING_MODE, String.class);
        if (value != null) return value;
        
        value = message.getHeader(PROCESSING_MODE_LC, String.class);
        return value != null ? value : PROCESSING_MODE_NORMAL;
    }

    /**
     * Récupère le statut métier avec fallback et valeur par défaut.
     */
    public static String getBusinessStatus(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(BUSINESS_STATUS, String.class);
        if (value != null) return value;
        
        value = message.getHeader(BUSINESS_STATUS_LC, String.class);
        return value != null ? value : BUSINESS_STATUS_PENDING;
    }

    /**
     * Récupère le statut technique avec fallback et valeur par défaut.
     */
    public static String getTechnicalStatus(Exchange exchange) {
        Message message = exchange.getIn();
        String value = message.getHeader(TECHNICAL_STATUS, String.class);
        if (value != null) return value;
        
        value = message.getHeader(TECHNICAL_STATUS_LC, String.class);
        return value != null ? value : TECHNICAL_STATUS_PROCESSING;
    }

    // ==================================================================================
    // MÉTHODES D'ÉCRITURE DES HEADERS
    // ==================================================================================

    /**
     * Définit les données JSON du flow.
     */
    public static void setFlowDataJson(Exchange exchange, String flowDataJson) {
        exchange.getIn().setHeader(FLOW_DATA_JSON, flowDataJson);
    }

    /**
     * Définit l'identifiant d'occurrence du flow.
     */
    public static void setFlowOccurId(Exchange exchange, String flowOccurId) {
        exchange.getIn().setHeader(FLOW_OCCUR_ID, flowOccurId);
    }

    /**
     * Définit l'identifiant du message.
     */
    public static void setMessageId(Exchange exchange, String messageId) {
        exchange.getIn().setHeader(MESSAGE_ID, messageId);
    }

    /**
     * Définit l'identifiant de corrélation.
     */
    public static void setCorrelationId(Exchange exchange, String correlationId) {
        exchange.getIn().setHeader(CORRELATION_ID, correlationId);
    }

    /**
     * Définit le timestamp de réception.
     */
    public static void setReceivedTimestamp(Exchange exchange, String timestamp) {
        exchange.getIn().setHeader(RECEIVED_TIMESTAMP, timestamp);
    }

    /**
     * Définit le mode de traitement.
     */
    public static void setProcessingMode(Exchange exchange, String processingMode) {
        exchange.getIn().setHeader(PROCESSING_MODE, processingMode);
    }

    /**
     * Définit le statut métier.
     */
    public static void setBusinessStatus(Exchange exchange, String businessStatus) {
        exchange.getIn().setHeader(BUSINESS_STATUS, businessStatus);
    }

    /**
     * Définit le statut technique.
     */
    public static void setTechnicalStatus(Exchange exchange, String technicalStatus) {
        exchange.getIn().setHeader(TECHNICAL_STATUS, technicalStatus);
    }

    /**
     * Définit le XML TechnicalPivot généré.
     */
    public static void setTechPivotXml(Exchange exchange, String xml) {
        exchange.getIn().setHeader(TECH_PIVOT_XML, xml);
    }

    // ==================================================================================
    // MÉTHODES UTILITAIRES POUR GÉNÉRATION AUTOMATIQUE
    // ==================================================================================

    /**
     * Génère un nouvel identifiant d'occurrence basé sur un préfixe et timestamp.
     */
    public static String generateFlowOccurId(String prefix) {
        return prefix + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    /**
     * Génère un identifiant de message unique.
     */
    public static String generateMessageId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString();
    }

    /**
     * Génère un timestamp au format ISO avec millisecondes.
     */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    /**
     * Configure tous les headers de traitement standard avec génération automatique.
     */
    public static void setStandardProcessingHeaders(Exchange exchange, String flowOccurPrefix, 
                                                   String messagePrefix, String processingMode) {
        setFlowOccurId(exchange, generateFlowOccurId(flowOccurPrefix));
        setMessageId(exchange, generateMessageId(messagePrefix));
        setCorrelationId(exchange, exchange.getExchangeId());
        setReceivedTimestamp(exchange, generateTimestamp());
        setProcessingMode(exchange, processingMode);
        setBusinessStatus(exchange, BUSINESS_STATUS_PROCESSING);
        setTechnicalStatus(exchange, TECHNICAL_STATUS_ACTIVE);
    }

}