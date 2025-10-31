package com.pixel.v2.referentiel.service;

import com.pixel.v2.referentiel.model.FlowConfiguration;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing flow configurations
 */
@Service
public class ConfigurationService {
    
    private static final String CDM_XSD_FILE = "cdm.xsd";
    private static final String DEFAULT_KAFKA_BROKER = "localhost:9092";
    
    private final Map<String, FlowConfiguration> configurations;
    
    public ConfigurationService() {
        this.configurations = initializeConfigurations();
    }
    
    /**
     * Get configuration by flow ID
     * 
     * @param flowId the flow identifier
     * @return FlowConfiguration for the given flow ID, or default configuration if not found
     */
    public FlowConfiguration getConfiguration(String flowId) {
        return configurations.getOrDefault(flowId, getDefaultConfiguration());
    }
    
    /**
     * Get all available configurations
     * 
     * @return Map of all configurations
     */
    public Map<String, FlowConfiguration> getAllConfigurations() {
        return new HashMap<>(configurations);
    }
    
    /**
     * Check if a configuration exists for the given flow ID
     * 
     * @param flowId the flow identifier
     * @return true if configuration exists, false otherwise
     */
    public boolean hasConfiguration(String flowId) {
        return configurations.containsKey(flowId);
    }
    
    /**
     * Initialize predefined configurations
     */
    private Map<String, FlowConfiguration> initializeConfigurations() {
        Map<String, FlowConfiguration> configs = new HashMap<>();
        
        // PACS008 configuration
        FlowConfiguration pacs008Config = new FlowConfiguration(
            "pacs008_mapping",
            "instant_payments",
            "real_time",
            true,
            "//Document",
            100,
            Arrays.asList("queue1", "queue2")
        );
        pacs008Config.setXsltFileToCdm("pacs008-to-cdm.xslt");
        pacs008Config.setXsltFileFromCdm("cdm-to-pacs008.xslt");
        pacs008Config.setXsdFlowFile("pacs008.xsd");
        pacs008Config.setXsdCdmFile(CDM_XSD_FILE);
        pacs008Config.setKafkaBroker(DEFAULT_KAFKA_BROKER);
        pacs008Config.setKafkaTopic("pacs008-topic");
        configs.put("pacs008", pacs008Config);
        
        // PACS009 configuration
        FlowConfiguration pacs009Config = new FlowConfiguration(
            "pacs009_mapping",
            "instant_payments",
            "real_time",
            true,
            "//Document",
            50,
            Arrays.asList("queue3", "queue4")
        );
        pacs009Config.setXsltFileToCdm("pacs009-to-cdm.xslt");
        pacs009Config.setXsltFileFromCdm("cdm-to-pacs009.xslt");
        pacs009Config.setXsdFlowFile("pacs009.xsd");
        pacs009Config.setXsdCdmFile(CDM_XSD_FILE);
        pacs009Config.setKafkaBroker(DEFAULT_KAFKA_BROKER);
        pacs009Config.setKafkaTopic("pacs009-topic");
        configs.put("pacs009", pacs009Config);
        
        // PAIN001 configuration
        FlowConfiguration pain001Config = new FlowConfiguration(
            "pain001_mapping",
            "sepa_credit_transfer",
            "batch",
            true,
            "//CstmrCdtTrfInitn",
            200,
            Arrays.asList("queue5", "queue6")
        );
        pain001Config.setXsltFileToCdm("pain001-to-cdm.xslt");
        pain001Config.setXsltFileFromCdm("cdm-to-pain001.xslt");
        pain001Config.setXsdFlowFile("pain001.xsd");
        pain001Config.setXsdCdmFile("cdm.xsd");
        pain001Config.setKafkaBroker("localhost:9092");
        pain001Config.setKafkaTopic("pain001-topic");
        configs.put("pain001", pain001Config);
        
        // CAMT053 configuration
        FlowConfiguration camt053Config = new FlowConfiguration(
            "camt053_mapping",
            "bank_statement",
            "daily",
            false,
            "",
            1000,
            Arrays.asList("queue7")
        );
        camt053Config.setXsltFileToCdm("camt053-to-cdm.xslt");
        camt053Config.setXsltFileFromCdm("cdm-to-camt053.xslt");
        camt053Config.setXsdFlowFile("camt053.xsd");
        camt053Config.setXsdCdmFile("cdm.xsd");
        camt053Config.setKafkaBroker("localhost:9092");
        camt053Config.setKafkaTopic("camt053-topic");
        configs.put("camt053", camt053Config);
        
        return configs;
    }
    
    /**
     * Get default configuration for unknown flow IDs
     */
    private FlowConfiguration getDefaultConfiguration() {
        FlowConfiguration defaultConfig = new FlowConfiguration(
            "default_mapping",
            "standard",
            "normal",
            false,
            "",
            1000,
            Arrays.asList("default_queue")
        );
        defaultConfig.setXsltFileToCdm("default-to-cdm.xslt");
        defaultConfig.setXsltFileFromCdm("cdm-to-default.xslt");
        defaultConfig.setXsdFlowFile("default.xsd");
        defaultConfig.setXsdCdmFile("cdm.xsd");
        defaultConfig.setKafkaBroker("localhost:9092");
        defaultConfig.setKafkaTopic("default-topic");
        return defaultConfig;
    }
}