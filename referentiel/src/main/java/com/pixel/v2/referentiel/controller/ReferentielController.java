package com.pixel.v2.referentiel.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixel.v2.referentiel.model.RefFlowDto;
import com.pixel.v2.referentiel.service.ReferentielService;

/**
 * REST controller for comprehensive referentiel data access
 */
@RestController
@RequestMapping("/api/referentiel")
public class ReferentielController {

    private static final Logger logger = LoggerFactory.getLogger(ReferentielController.class);

    @Autowired
    private ReferentielService referentielService;

    
    /**
     * Get complete flow information by flow code including all related data Returns data structured
     * like referentiel-example.json
     * 
     * @param flowCode The flow code to retrieve complete information for
     * @return RefFlowDto containing all related flow data in the expected JSON structure
     */
    @GetMapping("/flows/{flowCode}/complete")
    public ResponseEntity<RefFlowDto> getCompleteFlowByCode(@PathVariable String flowCode) {
        logger.info("GET /api/referentiel/flows/{}/complete", flowCode);
        Optional<RefFlowDto> refFlow =
                referentielService.getCompleteReferentielByFlowCode(flowCode);

        if (refFlow.isPresent()) {
            return ResponseEntity.ok(refFlow.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get flow summary
     */
    @GetMapping("/flows/{flowId}/summary")
    public ResponseEntity<String> getFlowSummary(@PathVariable Integer flowId) {
        logger.info("GET /api/referentiel/flows/{}/summary", flowId);
        String summary = referentielService.getFlowSummary(flowId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Validate flow configuration
     */
    @GetMapping("/flows/{flowId}/validate")
    public ResponseEntity<Boolean> validateFlowConfiguration(@PathVariable Integer flowId) {
        logger.info("GET /api/referentiel/flows/{}/validate", flowId);
        boolean isValid = referentielService.validateFlowConfiguration(flowId);
        return ResponseEntity.ok(isValid);
    }

    
}
