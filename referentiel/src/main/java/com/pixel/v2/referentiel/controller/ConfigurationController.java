package com.pixel.v2.referentiel.controller;

import com.pixel.v2.referentiel.model.FlowConfiguration;
import com.pixel.v2.referentiel.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for configuration management
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ConfigurationController {
    
    private final ConfigurationService configurationService;
    
    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    
    /**
     * Get configuration by flow ID
     * 
     * @param flowId the flow identifier
     * @return FlowConfiguration for the given flow ID
     */
    @GetMapping("/config/{flowId}")
    public ResponseEntity<FlowConfiguration> getConfiguration(@PathVariable String flowId) {
        FlowConfiguration config = configurationService.getConfiguration(flowId);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Alternative endpoint for backward compatibility (matches the k-referentiel-data-loader kamelet)
     */
    @GetMapping("/config")
    public ResponseEntity<FlowConfiguration> getDefaultConfiguration(@RequestParam(required = false) String flowId) {
        String targetFlowId = flowId != null ? flowId : "pacs008"; // Default to pacs008 if no flowId provided
        FlowConfiguration config = configurationService.getConfiguration(targetFlowId);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Get all available configurations
     * 
     * @return Map of all configurations
     */
    @GetMapping("/configs")
    public ResponseEntity<Map<String, FlowConfiguration>> getAllConfigurations() {
        Map<String, FlowConfiguration> configs = configurationService.getAllConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "referentiel-service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    /**
     * Get service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "service", "PIXEL-V2 Referentiel Service",
            "version", "1.0.1-SNAPSHOT",
            "description", "Configuration management service for flow processing",
            "availableFlows", configurationService.getAllConfigurations().keySet()
        ));
    }
}