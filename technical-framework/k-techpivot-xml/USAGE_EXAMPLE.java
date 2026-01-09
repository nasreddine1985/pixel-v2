// Exemple d'utilisation de la kamelet k-techpivot-xml dans ChRoute.java
// Le kamelet lit les données depuis les headers Camel et génère/met à jour un XML TechnicalPivot

// ==================================================================================
// MODE GENERATE : Créer un nouveau XML à partir des données JSON dans les headers
// ==================================================================================

// 1. Préparer les headers avec les données JSON et variables de traitement
.setHeader("FlowDataJson", simple("${file:content}")) // JSON complet du référentiel
// OU
.setHeader("RefFlowDataJson", simple("${file:content}")) // Alternative pour référentiel

// Variables de traitement
.setHeader("flowOccurId", simple("FLOW-${date:now:yyyyMMdd-HHmmss}"))
.setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader("MessageId", simple("MSG-${uuid}"))
.setHeader("CorrelationId", simple("CORR-${exchangeId}"))
.setHeader("ProcessingMode", constant("BATCH"))
.setHeader("BusinessStatus", constant("VALIDATED"))
.setHeader("TechnicalStatus", constant("SUCCESS"))

// Générer le XML TechnicalPivot
.to("kamelet:k-techpivot-xml?operation=generate")
.log("Generated TechnicalPivot XML: ${header.techPivotXml}")

// ==================================================================================
// MODE UPDATE : Mettre à jour un XML existant avec UNIQUEMENT les variables headers
// ==================================================================================

// 2. Préparer XML existant et variables de mise à jour
.setHeader("existingTechPivotXml", simple("${body}")) // XML existant à mettre à jour

// Variables de mise à jour (PAS de FlowDataJson/RefFlowDataJson en mode update)
.setHeader("flowOccurId", simple("UPDATED-${date:now:yyyyMMddHHmmss}"))
.setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
.setHeader("MessageId", simple("UPD-MSG-${uuid}"))
.setHeader("ProcessingMode", constant("REALTIME"))
.setHeader("BusinessStatus", constant("COMPLETED"))
.setHeader("TechnicalStatus", constant("FINALIZED"))

// Mettre à jour le XML (garde toutes les données existantes, met à jour uniquement les variables)
.to("kamelet:k-techpivot-xml?operation=update")
.log("Updated TechnicalPivot XML: ${header.techPivotXml}")

// ==================================================================================
// CONFIGURATION AVANCÉE
// ==================================================================================

// 3. Configuration complète avec headers personnalisés
.to("kamelet:k-techpivot-xml?" +
    "operation=generate&" +
    "xmlOutputHeader=myCustomXmlHeader&" +
    "existingXmlHeader=myExistingXmlHeader")

// 4. Exemple de route complète pour traitement de fichier JSON
from("file:data?include=*.json&noop=true")
    .log("Processing file: ${header.CamelFileName}")
    
    // Charger le JSON dans le header FlowDataJson
    .setHeader("FlowDataJson", simple("${body}"))
    
    // Préparer les variables de traitement
    .setHeader("flowOccurId", simple("${header.CamelFileName}-${date:now:yyyyMMddHHmmss}"))
    .setHeader("ReceivedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSS}"))
    .setHeader("MessageId", simple("FILE-${header.CamelFileName}"))
    .setHeader("CorrelationId", simple("${exchangeId}"))
    .setHeader("ProcessingMode", constant("FILE_BATCH"))
    .setHeader("BusinessStatus", constant("PROCESSING"))
    .setHeader("TechnicalStatus", constant("ACTIVE"))
    
    // Générer le XML TechnicalPivot
    .to("kamelet:k-techpivot-xml?operation=generate")
    
    // Le XML est maintenant disponible dans le header techPivotXml ET dans le body
    .log("Generated XML for file ${header.CamelFileName}")
    .to("file:output?fileName=${header.CamelFileName}.xml");

// ==================================================================================
// HEADERS SUPPORTÉS
// ==================================================================================

/*
INPUT HEADERS (pour operation=generate):
- FlowDataJson / RefFlowDataJson : JSON complet du référentiel (requis)
- flowOccurId / FlowOccurId : Identifiant d'occurrence du flow
- ReceivedTimestamp / receivedTimestamp : Timestamp de réception
- MessageId / messageId : Identifiant du message
- CorrelationId / correlationId : Identifiant de corrélation
- ProcessingMode / processingMode : Mode de traitement (défaut: NORMAL)
- BusinessStatus / businessStatus : Statut métier (défaut: PENDING)
- TechnicalStatus / technicalStatus : Statut technique (défaut: PROCESSING)

INPUT HEADERS (pour operation=update):
- existingTechPivotXml : XML existant à mettre à jour (requis)
- flowOccurId / FlowOccurId : Nouvel identifiant d'occurrence
- ReceivedTimestamp / receivedTimestamp : Nouveau timestamp
- MessageId / messageId : Nouvel identifiant de message
- CorrelationId / correlationId : Nouvel identifiant de corrélation
- ProcessingMode / processingMode : Nouveau mode de traitement
- BusinessStatus / businessStatus : Nouveau statut métier
- TechnicalStatus / technicalStatus : Nouveau statut technique
- NOTE: FlowDataJson/RefFlowDataJson sont IGNORÉS en mode update

OUTPUT HEADERS:
- techPivotXml (ou header personnalisé) : XML TechnicalPivot généré/mis à jour

OUTPUT BODY:
- XML TechnicalPivot généré/mis à jour
*/