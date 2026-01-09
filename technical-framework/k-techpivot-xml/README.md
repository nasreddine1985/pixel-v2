# K-TechPivot-XML Kamelet

Kamelet pour générer, mettre à jour et valider des fichiers XML TechnicalPivot conformes au schéma `TechnicalPivot.xsd`.

## Description

Cette kamelet permet de :

- **Générer** un nouveau XML TechnicalPivot à partir des headers Camel **OU** d'un JSON FlowData (RefFlowDto)
- **Mettre à jour** un XML TechnicalPivot existant avec de nouveaux headers ou JSON FlowData
- **Valider** un XML TechnicalPivot contre le schéma XSD

### Modes de fonctionnement

1. **Mode Headers** : Génération basée sur des headers Camel individuels (mode original)
2. **Mode FlowData JSON** : Génération basée sur un JSON structuré (RefFlowDto du référentiel) ⭐ **NOUVEAU**
3. **Mode Mixte** : Combinaison des deux avec priorité aux headers

## Configuration

### Opérations supportées

| Opération  | Description                                         |
| ---------- | --------------------------------------------------- |
| `generate` | Génère un nouveau XML à partir des headers          |
| `update`   | Met à jour un XML existant avec de nouveaux headers |
| `validate` | Valide un XML existant contre le XSD                |

### Paramètres de configuration

#### Headers mapping pour la génération XML

| Paramètre                 | Description                                      | Défaut              |
| ------------------------- | ------------------------------------------------ | ------------------- |
| `flowCodeHeader`          | Nom du header contenant le FlowCode              | `FlowCode`          |
| `flowIdHeader`            | Nom du header contenant le FlowID                | `FlowID`            |
| `flowNameHeader`          | Nom du header contenant le FlowName              | `FlowName`          |
| `fileNameHeader`          | Nom du header contenant le FileName              | `FileName`          |
| `partnerCodeHeader`       | Nom du header contenant le PartnerCode (Input)   | `PartnerCode`       |
| `outputPartnerCodeHeader` | Nom du header contenant le PartnerCode de sortie | `OutputPartnerCode` |
| `queueNameHeader`         | Nom du header contenant le QName pour JMS        | `QueueName`         |

#### Configuration XSD et sortie

| Paramètre            | Description                                        | Défaut                         |
| -------------------- | -------------------------------------------------- | ------------------------------ |
| `validateAgainstXSD` | Valider le XML généré contre le TechnicalPivot.xsd | `true`                         |
| `xsdPath`            | Chemin vers le fichier TechnicalPivot.xsd          | `classpath:TechnicalPivot.xsd` |
| `xmlOutputHeader`    | Nom du header où placer le XML généré/mis à jour   | `techPivotXml`                 |
| `prettifyXml`        | Formatter le XML en sortie                         | `true`                         |

## Exemples d'utilisation

### 1. Générer un nouveau XML

```java
// Définir les headers nécessaires
.setHeader("FlowCode", constant("ICHSIC"))
.setHeader("FlowID", constant(12345))
.setHeader("FlowName", constant("CH Payment Processing"))
.setHeader("FileName", constant("payment.xml"))
.setHeader("PartnerCode", constant("INPUT_PARTNER"))
.setHeader("QueueName", constant("CH.INPUT.QUEUE"))

// Générer le XML
.to("kamelet:k-techpivot-xml?operation=generate")

// Le XML est maintenant disponible dans le header 'techPivotXml'
.log("Generated XML: ${header.techPivotXml}")
```

### 2. Mettre à jour un XML existant

```java
// Un XML existe déjà dans le header 'techPivotXml'
.setHeader("FlowCode", constant("NEW_FLOW_CODE"))

// Mettre à jour le XML
.to("kamelet:k-techpivot-xml?operation=update&flowCodeHeader=FlowCode")

// Le XML mis à jour est dans le header 'techPivotXml'
```

### 3. Valider un XML

```java
// Un XML existe déjà dans le header 'techPivotXml'
.to("kamelet:k-techpivot-xml?operation=validate")

// Le résultat de la validation est dans le header 'xmlValidationResult'
.choice()
  .when(header("xmlValidationResult").isEqualTo("VALID"))
    .log("XML is valid!")
  .otherwise()
    .log("XML validation failed: ${header.xmlValidationResult}")
```

### 4. Configuration complète

```java
.to("kamelet:k-techpivot-xml?" +
    "operation=generate&" +
    "flowCodeHeader=MyFlowCode&" +
    "flowIdHeader=MyFlowID&" +
    "flowNameHeader=MyFlowName&" +
    "fileNameHeader=MyFileName&" +
    "partnerCodeHeader=MyPartnerCode&" +
    "outputPartnerCodeHeader=MyOutputPartnerCode&" +
    "queueNameHeader=MyQueueName&" +
    "validateAgainstXSD=true&" +
    "xmlOutputHeader=myTechPivotXml&" +
    "prettifyXml=true")
```

## Structure XML générée

Le XML généré suit la structure du schéma `TechnicalPivot.xsd` :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<TechPivotRoot xmlns="http://bnpp.com/TechPivot">
  <FileName>payment.xml</FileName>
  <Flow>
    <FlowID>12345</FlowID>
    <FlowCode>ICHSIC</FlowCode>
    <FlowName>CH Payment Processing</FlowName>
    <FlowDirection>INBOUND</FlowDirection>
    <FlowEnabled>true</FlowEnabled>
  </Flow>
  <Input>
    <PartnerCode>INPUT_PARTNER</PartnerCode>
    <Transport>
      <JMS>
        <QName>CH.INPUT.QUEUE</QName>
      </JMS>
    </Transport>
  </Input>
  <Output>
    <PartnerCode>OUTPUT_PARTNER</PartnerCode>
  </Output>
</TechPivotRoot>
```

## Headers de sortie

| Header                                          | Description                                                 |
| ----------------------------------------------- | ----------------------------------------------------------- |
| `techPivotXml` (ou paramètre `xmlOutputHeader`) | Contient le XML généré/mis à jour                           |
| `xmlValidationResult`                           | Résultat de la validation (`VALID` ou `INVALID: <message>`) |

## Tests

Exécuter les tests unitaires :

```bash
mvn test
```

## Dépendances

- `camel-kamelet`
- `camel-jackson`
- `camel-jaxb`
- `camel-bean`
- `camel-groovy`

## Notes

- Le XML généré est automatiquement validé contre le XSD si `validateAgainstXSD=true`
- Le XML peut être formatté (indenté) si `prettifyXml=true`
- Les headers manquants sont ignorés (éléments XML optionnels)
- En cas d'erreur, une exception est levée avec les détails
