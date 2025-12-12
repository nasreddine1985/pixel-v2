package com.pixel.v2.referentiel.service;

import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * Service for managing flow configurations
 */
@Service
public class ConfigurationService {

    private final Set<String> supportedFlows;

    public ConfigurationService() {
        this.supportedFlows = Set.of("pacs008", "pacs009", "pain001", "camt053","ICHSIC");
    }

    /**
     * Check if a flow is supported
     * 
     * @param flowId the flow identifier
     * @return true if flow is supported, false otherwise
     */
    public boolean hasConfiguration(String flowId) {
        return supportedFlows.contains(flowId);
    }

    /**
     * Get all supported flow codes
     * 
     * @return Set of supported flow codes
     */
    public Set<String> getAllSupportedFlows() {
        return supportedFlows;
    }
}
