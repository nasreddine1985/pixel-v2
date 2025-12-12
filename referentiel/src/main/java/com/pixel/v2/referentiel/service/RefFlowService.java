package com.pixel.v2.referentiel.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixel.v2.referentiel.model.RefFlowCompleteDto;
import com.pixel.v2.referentiel.repository.RefFlowRepository;

/**
 * Service for managing RefFlow data operations Provides business logic for retrieving complete flow
 * information
 */
@Service
public class RefFlowService {

    private final RefFlowRepository refFlowRepository;

    @Autowired
    public RefFlowService(RefFlowRepository refFlowRepository) {
        this.refFlowRepository = refFlowRepository;
    }

    /**
     * Get complete flow information by flowCode
     * 
     * @param flowCode The flow code to search for
     * @return Optional containing RefFlowCompleteDto if found, empty otherwise
     */
    public Optional<RefFlowCompleteDto> getCompleteFlowByCode(String flowCode) {
        if (flowCode == null || flowCode.trim().isEmpty()) {
            return Optional.empty();
        }

        return refFlowRepository.findByFlowCode(flowCode.trim());
    }

    /**
     * Check if a flow exists by flowCode
     * 
     * @param flowCode The flow code to check
     * @return true if flow exists, false otherwise
     */
    public boolean flowExists(String flowCode) {
        return getCompleteFlowByCode(flowCode).isPresent();
    }
}
