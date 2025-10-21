package com.pixel.v2.outbound.controller;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for testing outbound message processing
 * 
 * Provides endpoints to submit messages directly to the outbound service
 * for testing and monitoring purposes.
 */
@RestController
@RequestMapping("/outbound")
public class OutboundController {

    private static final Logger logger = LoggerFactory.getLogger(OutboundController.class);

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    /**
     * Submit a message directly to the outbound service
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitMessage(@RequestBody String message) {
        try {
            logger.info("Received message submission request");
            
            // Send message to the direct endpoint
            producerTemplate.sendBody("direct:outbound-input", message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message submitted successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting message: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to submit message: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Submit a message with custom headers
     */
    @PostMapping("/submit-with-headers")
    public ResponseEntity<Map<String, Object>> submitMessageWithHeaders(
            @RequestBody String message,
            @RequestParam Map<String, String> headers) {
        try {
            logger.info("Received message submission request with headers: {}", headers);
            
            // Convert String headers to Object headers for Camel
            Map<String, Object> camelHeaders = new HashMap<>(headers);
            
            // Send message with headers to the direct endpoint
            producerTemplate.sendBodyAndHeaders("direct:outbound-input", message, camelHeaders);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message with headers submitted successfully");
            response.put("headers", headers);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting message with headers: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to submit message: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get service health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check Camel context status
            boolean camelHealthy = camelContext != null && camelContext.getStatus().isStarted();
            
            response.put("status", camelHealthy ? "healthy" : "unhealthy");
            response.put("camelContext", camelHealthy ? "started" : "not started");
            response.put("routes", camelContext != null ? camelContext.getRoutes().size() : 0);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking health: {}", e.getMessage());
            
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get information about available routes
     */
    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (camelContext != null) {
                response.put("totalRoutes", camelContext.getRoutes().size());
                response.put("routeIds", camelContext.getRoutes().stream()
                    .map(route -> route.getId())
                    .toList());
                response.put("camelVersion", camelContext.getVersion());
            } else {
                response.put("message", "Camel context not available");
            }
            
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting routes info: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}