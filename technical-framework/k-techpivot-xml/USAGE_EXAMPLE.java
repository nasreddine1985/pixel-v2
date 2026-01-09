// Exemple d'utilisation de la kamelet k-techpivot-xml dans ChRoute.java

// 1. Générer un nouveau XML TechnicalPivot à partir des headers
.to("kamelet:k-techpivot-xml?operation=generate&flowCodeHeader=FlowCode&partnerCodeHeader=PartnerCode&fileNameHeader=FileName")

// 2. Mettre à jour un XML existant
.setHeader("FlowCode", constant("NEW_FLOW_CODE"))
.to("kamelet:k-techpivot-xml?operation=update&flowCodeHeader=FlowCode")

// 3. Valider un XML existant
.to("kamelet:k-techpivot-xml?operation=validate")

// Configuration complète avec tous les paramètres :
.to("kamelet:k-techpivot-xml?" +
    "operation=generate&" +
    "flowCodeHeader=FlowCode&" +
    "flowIdHeader=FlowID&" +
    "flowNameHeader=FlowName&" +
    "fileNameHeader=FileName&" +
    "partnerCodeHeader=PartnerCode&" +
    "outputPartnerCodeHeader=OutputPartnerCode&" +
    "queueNameHeader=QueueName&" +
    "validateAgainstXSD=true&" +
    "xmlOutputHeader=techPivotXml&" +
    "prettifyXml=true")

// Le XML généré sera disponible dans le header "techPivotXml"
.log("Generated TechnicalPivot XML: ${header.techPivotXml}")