package com.pixel.v2.persistence.service;

import com.pixel.v2.persistence.model.FlowReference;
import com.pixel.v2.persistence.model.FlowReference.FlowStatus;
import com.pixel.v2.persistence.repository.FlowReferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for FlowReference operations
 */
@Service
@Transactional
public class FlowReferenceService {

    private final FlowReferenceRepository flowReferenceRepository;

    public FlowReferenceService(FlowReferenceRepository flowReferenceRepository) {
        this.flowReferenceRepository = flowReferenceRepository;
    }

    /**
     * Save or update a flow reference
     */
    public FlowReference save(FlowReference flowReference) {
        flowReference.setLastUpdate(LocalDateTime.now());
        return flowReferenceRepository.save(flowReference);
    }

    /**
     * Find flow reference by ID
     */
    @Transactional(readOnly = true)
    public Optional<FlowReference> findById(String flowId) {
        return flowReferenceRepository.findById(flowId);
    }

    /**
     * Find flow reference by flow code
     */
    @Transactional(readOnly = true)
    public Optional<FlowReference> findByFlowCode(String flowCode) {
        return flowReferenceRepository.findByFlowCode(flowCode);
    }

    /**
     * Find all active flows
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findActiveFlows() {
        return flowReferenceRepository.findByStatus(FlowStatus.ACTIVE);
    }

    /**
     * Find flows by source channel
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findBySourceChannel(String sourceChannel) {
        return flowReferenceRepository.findBySourceChannel(sourceChannel);
    }

    /**
     * Find flows by target system
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findByTargetSystem(String targetSystem) {
        return flowReferenceRepository.findByTargetSystem(targetSystem);
    }

    /**
     * Find active flows by source channel
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findActiveFlowsBySourceChannel(String sourceChannel) {
        return flowReferenceRepository.findActiveFlowsBySourceChannel(sourceChannel);
    }

    /**
     * Find flows with encryption required
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findFlowsWithEncryptionRequired() {
        return flowReferenceRepository.findFlowsWithEncryptionRequired();
    }

    /**
     * Find flows by type and minimum priority
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findByFlowTypeAndMinPriority(String flowType, Integer minPriority) {
        return flowReferenceRepository.findByFlowTypeAndMinPriority(flowType, minPriority);
    }

    /**
     * Find flows with split enabled
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findFlowsWithSplitEnabled() {
        return flowReferenceRepository.findFlowsWithSplitEnabled();
    }

    /**
     * Find flows with retention policy
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findFlowsWithRetentionPolicy(Integer days) {
        return flowReferenceRepository.findFlowsWithRetentionPolicy(days);
    }

    /**
     * Check if flow code exists
     */
    @Transactional(readOnly = true)
    public boolean existsByFlowCode(String flowCode) {
        return flowReferenceRepository.existsByFlowCode(flowCode);
    }

    /**
     * Count flows by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(FlowStatus status) {
        return flowReferenceRepository.countByStatus(status);
    }

    /**
     * Update flow status
     */
    public void updateFlowStatus(String flowId, FlowStatus status) {
        Optional<FlowReference> flowOpt = flowReferenceRepository.findById(flowId);
        if (flowOpt.isPresent()) {
            FlowReference flow = flowOpt.get();
            flow.setStatus(status);
            flow.setLastUpdate(LocalDateTime.now());
            flowReferenceRepository.save(flow);
        }
    }

    /**
     * Delete flow reference
     */
    public void delete(String flowId) {
        flowReferenceRepository.deleteById(flowId);
    }

    /**
     * Find all flow references
     */
    @Transactional(readOnly = true)
    public List<FlowReference> findAll() {
        return flowReferenceRepository.findAll();
    }

    /**
     * Get flow configuration for processing
     */
    @Transactional(readOnly = true)
    public Optional<FlowReference> getFlowConfiguration(String flowCode) {
        return flowReferenceRepository.findByFlowCode(flowCode);
    }

    /**
     * Validate flow configuration
     */
    @Transactional(readOnly = true)
    public boolean isFlowConfigurationValid(String flowCode) {
        Optional<FlowReference> flow = flowReferenceRepository.findByFlowCode(flowCode);
        return flow.isPresent() && flow.get().getStatus() == FlowStatus.ACTIVE;
    }

    /**
     * Create new flow reference
     */
    public FlowReference createFlowReference(String flowCode, String flowName, FlowStatus status) {
        FlowReference flow = new FlowReference();
        flow.setFlowCode(flowCode);
        flow.setFlowName(flowName);
        flow.setStatus(status);
        return save(flow);
    }
}
