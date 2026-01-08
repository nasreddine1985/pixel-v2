package com.pixel.v2.referential.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixel.v2.referential.model.RefFlowDto;
import com.pixel.v2.referential.service.ReferentialService;

/**
 * REST controller for comprehensive referential data access
 */
@RestController
@RequestMapping("/api/referential")
public class ReferentialController {

    private static final Logger logger = LoggerFactory.getLogger(ReferentialController.class);

    @Autowired
    private ReferentialService referentialService;


    /**
     * Get complete flow information by flow code including all related data Returns data structured
     * like referential-example.json
     * 
     * @param flowCode The flow code to retrieve complete information for
     * @return RefFlowDto containing all related flow data in the expected JSON structure
     */
    @GetMapping("/flows/{flowCode}/complete")
    public ResponseEntity<RefFlowDto> getCompleteFlowByCode(@PathVariable String flowCode) {
        logger.info("GET /api/referential/flows/{}/complete", flowCode);
        Optional<RefFlowDto> refFlow =
                referentialService.getCompleteReferentialByFlowCode(flowCode);

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
        logger.info("GET /api/referential/flows/{}/summary", flowId);
        String summary = referentialService.getFlowSummary(flowId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Validate flow configuration
     */
    @GetMapping("/flows/{flowId}/validate")
    public ResponseEntity<Boolean> validateFlowConfiguration(@PathVariable Integer flowId) {
        logger.info("GET /api/referential/flows/{}/validate", flowId);
        boolean isValid = referentialService.validateFlowConfiguration(flowId);
        return ResponseEntity.ok(isValid);
    }


}
