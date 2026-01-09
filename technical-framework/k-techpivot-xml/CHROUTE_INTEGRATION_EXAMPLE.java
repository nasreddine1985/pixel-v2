// ===== EXEMPLE D'INTÉGRATION DANS ChRoute.java =====

// Option 1: Utiliser les données du référentiel récupérées par k-identification
from(K_MQ_STARTER_ENDPOINT)
    .log("Starting CH payment processing...")
    
    // Step 2: Récupérer les données référentielles (contient RefFlowData JSON)
    .to(K_IDENTIFICATION_ENDPOINT)
    
    // Step 2b: Générer le TechnicalPivot XML à partir des données référentielles
    .to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")
    .log("Generated TechnicalPivot XML: ${header.techPivotXml}")
    
    // Step 3: Continuer le traitement...
    .to(K_DUPLICATE_CHECK_ENDPOINT)
    // ...

// Option 2: Créer un TechnicalPivot XML à partir des headers existants dans votre route
from(K_MQ_STARTER_ENDPOINT)
    .log("Starting CH payment processing...")
    
    // Enrichir avec des informations spécifiques
    .setHeader("FlowCode", constant("ICHSIC"))
    .setHeader("FlowID", simple("${header.ProcessingFlowId}"))
    .setHeader("FileName", simple("${header.MessageFileName}"))
    .setHeader("PartnerCode", simple("${header.SenderBIC}"))
    .setHeader("QueueName", constant("CH.SIC.INPUT"))
    
    // Générer le XML TechnicalPivot
    .to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=false")
    
    // Le XML est maintenant disponible pour usage
    .process(exchange -> {
        String techPivotXml = exchange.getIn().getHeader("techPivotXml", String.class);
        // Utiliser le XML selon vos besoins
        System.out.println("TechnicalPivot XML ready: " + (techPivotXml != null ? "YES" : "NO"));
    })
    
    // Continuer le pipeline...
    .to(K_IDENTIFICATION_ENDPOINT)
    .to(K_DUPLICATE_CHECK_ENDPOINT)
    // ...

// Option 3: Construire dynamiquement un JSON FlowData et générer le XML
from(K_MQ_STARTER_ENDPOINT)
    .log("Starting CH payment processing...")
    
    // Construire un JSON FlowData personnalisé
    .process(exchange -> {
        String flowCode = exchange.getIn().getHeader("FlowCode", "ICHSIC", String.class);
        String partnerCode = exchange.getIn().getHeader("SenderBIC", String.class);
        String queueName = exchange.getIn().getHeader("InputQueue", String.class);
        
        String flowDataJson = String.format("""
            {
              "flow": {
                "flowCode": "%s",
                "flowName": "Dynamic CH Payment Processing",
                "flowDirection": "IN",
                "flowEnabled": "Y"
              },
              "partnerIn": {
                "partnerCode": "%s",
                "transport": {
                  "type": "JMS",
                  "jms": {
                    "qName": "%s"
                  }
                }
              }
            }
            """, flowCode, partnerCode, queueName);
        
        exchange.getIn().setHeader("RefFlowData", flowDataJson);
    })
    
    // Générer le XML à partir du JSON construit
    .to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")
    .log("Generated TechnicalPivot XML from dynamic FlowData")
    
    // Continuer le traitement...
    .to(K_DUPLICATE_CHECK_ENDPOINT)
    // ...

// ===== EXEMPLE DE CONSTANTES À AJOUTER DANS ChRoute =====
// Ajoutez cette constante dans votre classe ChRoute
private static final String K_TECHPIVOT_XML_ENDPOINT = 
    "kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true&validateAgainstXSD=true";

// Puis utilisez-la dans votre pipeline :
.to(K_IDENTIFICATION_ENDPOINT)
.to(K_TECHPIVOT_XML_ENDPOINT)  // Génère le TechnicalPivot XML
.to(K_DUPLICATE_CHECK_ENDPOINT)