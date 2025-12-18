package com.pixel.v2.referentiel.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixel.v2.referentiel.model.RefFlowCompleteDto;
import com.pixel.v2.referentiel.service.ConfigurationService;
import com.pixel.v2.referentiel.service.RefFlowService;

/**
 * REST controller for configuration management
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final RefFlowService refFlowService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService,
            RefFlowService refFlowService) {
        this.configurationService = configurationService;
        this.refFlowService = refFlowService;
    }



    /**
     * Get complete flow information including country, partner, rules and charset encoding data
     * 
     * @param flowCode The flow code to retrieve complete information for
     * @return RefFlowCompleteDto containing all related flow data
     */
    @GetMapping("/flows/{flowCode}/complete")
    public ResponseEntity<RefFlowCompleteDto> getCompleteFlowByCode(@PathVariable String flowCode) {
        Optional<RefFlowCompleteDto> refFlow = refFlowService.getCompleteFlowByCode(flowCode);

        if (refFlow.isPresent()) {
            return ResponseEntity.ok(refFlow.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a flow exists by flowCode
     * 
     * @param flowCode The flow code to check
     * @return boolean indicating if flow exists
     */
    @GetMapping("/flows/{flowCode}/exists")
    public ResponseEntity<Map<String, Boolean>> checkFlowExists(@PathVariable String flowCode) {
        boolean exists = refFlowService.flowExists(flowCode);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "referentiel-service"));
    }

    /**
     * Get service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity
                .ok(Map.of("service", "PIXEL-V2 Referentiel Service", "version", "1.0.1-SNAPSHOT",
                        "description", "Configuration management service for flow processing",
                        "availableFlows", configurationService.getAllSupportedFlows()));
    }
}
