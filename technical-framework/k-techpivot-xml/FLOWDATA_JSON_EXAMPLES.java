// Exemples d'utilisation de la kamelet k-techpivot-xml avec FlowData JSON

// ===== MODE 1: GÉNÉRATION DEPUIS HEADERS (mode existant) =====
.setHeader("FlowCode", constant("ICHSIC"))
.setHeader("FlowID", constant(12345))
.setHeader("PartnerCode", constant("CHSIC"))
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=false")

// ===== MODE 2: GÉNÉRATION DEPUIS JSON FLOWDATA (nouveau) =====

// 2A. JSON dans le header RefFlowData
.setHeader("RefFlowData", constant("""
{
  "flow": {
    "FlowID": "584",
    "flowCode": "ICHSIC",
    "flowName": "Incoming payment from local clearing SIC",
    "flowTypeName": "Payment",
    "flowDirection": "IN",
    "flowEnabled": "Y",
    "flowFuncPrty": [
      {
        "key": "BIC",
        "type": "Enrichment",
        "value": "BPPBCHGGXXX"
      }
    ],
    "countries": ["SWITZERLAND"]
  },
  "partnerIn": {
    "partnerCode": "CHSIC",
    "partnerName": "CH - Switzerland ACH Clearing",
    "partnerTypeName": "CHClearing",
    "transport": {
      "type": "MQS",
      "mqs": {
        "qManager": "QM.CH.SIC",
        "qName": "CH.SIC.IN"
      }
    }
  },
  "partnerOut": [
    {
      "partnerCode": "ITLOUT",
      "partnerName": "ITL Output",
      "transport": {
        "type": "JMS",
        "jms": {
          "qName": "ITL.OUT.QUEUE"
        }
      }
    }
  ],
  "flowRules": [
    {
      "flowCode": "ICHSIC",
      "flowControlledEnabled": true,
      "flowMaximum": 1000,
      "flowRetentionEnabled": true,
      "retentionCyclePeriod": "30"
    }
  ]
}
"""))
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")

// 2B. JSON dans le body
.setBody(constant(flowDataJsonString))
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")

// 2C. JSON dans un header personnalisé
.setHeader("MyFlowData", simple("${exchangeProperty.refFlowJson}"))
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true&flowDataJsonHeader=MyFlowData")

// ===== MODE 3: MISE À JOUR AVEC JSON =====
// Mettre à jour un XML existant avec de nouvelles données JSON
.setHeader("RefFlowData", simple("${exchangeProperty.updatedFlowData}"))
.to("kamelet:k-techpivot-xml?operation=update&useFlowDataJson=true")

// ===== MODE 4: COMBINAISON HEADERS + JSON =====
// Les headers individuels sont prioritaires sur le JSON
.setHeader("RefFlowData", constant(flowDataJsonString))
.setHeader("FlowCode", constant("OVERRIDE_FLOW")) // Override le flowCode du JSON
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")

// ===== RÉSULTAT =====
// Le XML généré est disponible dans ${header.techPivotXml}
.log("Generated TechnicalPivot XML: ${header.techPivotXml}")

// ===== EXEMPLE DE WORKFLOW COMPLET =====
// 1. Récupérer les données référentielles
.to("kamelet:k-identification?flowCode={{pixel.flow.code}}")

// 2. Générer le TechnicalPivot XML à partir des données référentielles
.to("kamelet:k-techpivot-xml?operation=generate&useFlowDataJson=true")

// 3. Utiliser le XML généré pour la suite du traitement
.process(exchange -> {
    String techPivotXml = exchange.getIn().getHeader("techPivotXml", String.class);
    // Traiter le XML selon les besoins
})

// 4. Optionnel: Valider le XML généré
.to("kamelet:k-techpivot-xml?operation=validate")