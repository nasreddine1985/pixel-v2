package com.pixel.v2.persistence.service;

import com.pixel.v2.persistence.model.FlowSummary;
import com.pixel.v2.persistence.repository.FlowSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for FlowSummary operations
 */
@Service
@Transactional
public class FlowSummaryService {

    private final FlowSummaryRepository flowSummaryRepository;

    public FlowSummaryService(FlowSummaryRepository flowSummaryRepository) {
        this.flowSummaryRepository = flowSummaryRepository;
    }

    /**
     * Save or update a flow summary
     */
    public FlowSummary save(FlowSummary flowSummary) {
        flowSummary.setLastUpdateDatetime(LocalDateTime.now());
        return flowSummaryRepository.save(flowSummary);
    }

    /**
     * Find flow summary by ID
     */
    @Transactional(readOnly = true)
    public Optional<FlowSummary> findById(String flowOccurId) {
        return flowSummaryRepository.findById(flowOccurId);
    }

    /**
     * Find flow summaries by flow code
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByFlowCode(String flowCode) {
        return flowSummaryRepository.findByFlowCodeOrderByBeginFlowDatetimeDesc(flowCode);
    }

    /**
     * Find flow summaries by status
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByStatus(String flowStatusCode) {
        return flowSummaryRepository
                .findByFlowStatusCodeOrderByBeginFlowDatetimeDesc(flowStatusCode);
    }

    /**
     * Find flow summaries by issuing partner
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByIssuingPartner(String issuingPartnerCode) {
        return flowSummaryRepository
                .findByIssuingPartnerCodeOrderByBeginFlowDatetimeDesc(issuingPartnerCode);
    }

    /**
     * Find flows by date range
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return flowSummaryRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Find flows with errors
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findFlowsWithErrors() {
        return flowSummaryRepository.findFlowsWithErrors();
    }

    /**
     * Find active flows (not completed)
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findActiveFlows() {
        return flowSummaryRepository.findActiveFlows();
    }

    /**
     * Find completed flows in date range
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findCompletedFlowsInDateRange(LocalDateTime startDate,
            LocalDateTime endDate) {
        return flowSummaryRepository.findCompletedFlowsInDateRange(startDate, endDate);
    }

    /**
     * Find flows by country and region
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByCountryAndRegion(String countryCode, String region) {
        return flowSummaryRepository
                .findByFlowCountryCodeAndRegionOrderByBeginFlowDatetimeDesc(countryCode, region);
    }

    /**
     * Find flows by replay ID
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByReplayId(String replayId) {
        return flowSummaryRepository.findByReplayIdOrderByBeginFlowDatetimeDesc(replayId);
    }

    /**
     * Get flow statistics by status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getFlowStatisticsByStatus() {
        return flowSummaryRepository.getFlowStatisticsByStatus();
    }

    /**
     * Get daily flow statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyFlowStatistics(LocalDate startDate, LocalDate endDate) {
        return flowSummaryRepository.getDailyFlowStatistics(startDate, endDate);
    }

    /**
     * Find flows by flow type and status
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByFlowTypeAndStatus(Integer flowTypeId, String statusCode) {
        return flowSummaryRepository.findByFlowTypeAndStatus(flowTypeId, statusCode);
    }

    /**
     * Find flows with high transaction count
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findFlowsWithHighTransactionCount(Integer minTransactions) {
        return flowSummaryRepository.findFlowsWithHighTransactionCount(minTransactions);
    }

    /**
     * Find recent flows by partner
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findRecentFlowsByPartner(String partnerCode, LocalDateTime since) {
        return flowSummaryRepository.findRecentFlowsByPartner(partnerCode, since);
    }

    /**
     * Count flows by status in date range
     */
    @Transactional(readOnly = true)
    public Long countByStatusInDateRange(String statusCode, LocalDateTime startDate,
            LocalDateTime endDate) {
        return flowSummaryRepository.countByStatusInDateRange(statusCode, startDate, endDate);
    }

    /**
     * Find flows by root error code
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findByRootErrorCode(String rootErrorCode) {
        return flowSummaryRepository.findByRootErrorCodeOrderByRootErrorDatetimeDesc(rootErrorCode);
    }

    /**
     * Find flows needing replay
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findFlowsNeedingReplay() {
        return flowSummaryRepository.findFlowsNeedingReplay();
    }

    /**
     * Get partner statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPartnerStatistics(LocalDate startDate, LocalDate endDate) {
        return flowSummaryRepository.getPartnerStatistics(startDate, endDate);
    }

    /**
     * Find latest flow summary for a flow code
     */
    @Transactional(readOnly = true)
    public Optional<FlowSummary> findLatestByFlowCode(String flowCode) {
        return flowSummaryRepository.findLatestByFlowCode(flowCode);
    }

    /**
     * Create a new flow summary
     */
    public FlowSummary createFlowSummary(String flowOccurId, String flowCode) {
        FlowSummary flowSummary = new FlowSummary(flowOccurId, flowCode);
        flowSummary.setBeginFlowDatetime(LocalDateTime.now());
        return save(flowSummary);
    }

    /**
     * Update flow summary with completion info
     */
    public FlowSummary completeFlow(String flowOccurId, String statusCode) {
        Optional<FlowSummary> summaryOpt = findById(flowOccurId);
        if (summaryOpt.isPresent()) {
            FlowSummary summary = summaryOpt.get();
            summary.setFlowStatusCode(statusCode);
            summary.setEndFlowDatetime(LocalDateTime.now());
            return save(summary);
        }
        return null;
    }

    /**
     * Update error count for a flow
     */
    public FlowSummary updateErrorCount(String flowOccurId, Integer errorCount, String errorCode) {
        Optional<FlowSummary> summaryOpt = findById(flowOccurId);
        if (summaryOpt.isPresent()) {
            FlowSummary summary = summaryOpt.get();
            summary.setNbError(errorCount);
            summary.setRootErrorCode(errorCode);
            summary.setRootErrorDatetime(LocalDateTime.now());
            return save(summary);
        }
        return null;
    }

    /**
     * Update transaction count for a flow
     */
    public FlowSummary updateTransactionCount(String flowOccurId, Integer transactionCount) {
        Optional<FlowSummary> summaryOpt = findById(flowOccurId);
        if (summaryOpt.isPresent()) {
            FlowSummary summary = summaryOpt.get();
            summary.setNbTransaction(transactionCount);
            return save(summary);
        }
        return null;
    }

    /**
     * Delete flow summary
     */
    public void delete(String flowOccurId) {
        flowSummaryRepository.deleteById(flowOccurId);
    }

    /**
     * Find all flow summaries
     */
    @Transactional(readOnly = true)
    public List<FlowSummary> findAll() {
        return flowSummaryRepository.findAll();
    }
}
