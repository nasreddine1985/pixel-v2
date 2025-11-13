package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.FlowSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FlowSummary entity
 */
@Repository
public interface FlowSummaryRepository extends JpaRepository<FlowSummary, String> {

    /**
     * Find flow summary by flow code
     */
    List<FlowSummary> findByFlowCodeOrderByBeginFlowDatetimeDesc(String flowCode);

    /**
     * Find flow summary by status
     */
    List<FlowSummary> findByFlowStatusCodeOrderByBeginFlowDatetimeDesc(String flowStatusCode);

    /**
     * Find flow summary by issuing partner
     */
    List<FlowSummary> findByIssuingPartnerCodeOrderByBeginFlowDatetimeDesc(
            String issuingPartnerCode);

    /**
     * Find flows by date range
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.beginFlowDatetime BETWEEN :startDate AND :endDate ORDER BY f.beginFlowDatetime DESC")
    List<FlowSummary> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find flows with errors
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.nbError > 0 ORDER BY f.lastUpdateDatetime DESC")
    List<FlowSummary> findFlowsWithErrors();

    /**
     * Find active flows (not completed)
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.endFlowDatetime IS NULL ORDER BY f.beginFlowDatetime DESC")
    List<FlowSummary> findActiveFlows();

    /**
     * Find completed flows in date range
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.endFlowDatetime BETWEEN :startDate AND :endDate ORDER BY f.endFlowDatetime DESC")
    List<FlowSummary> findCompletedFlowsInDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find flows by country and region
     */
    List<FlowSummary> findByFlowCountryCodeAndRegionOrderByBeginFlowDatetimeDesc(String countryCode,
            String region);

    /**
     * Find flows by replay ID
     */
    List<FlowSummary> findByReplayIdOrderByBeginFlowDatetimeDesc(String replayId);

    /**
     * Get flow statistics by status
     */
    @Query("SELECT f.flowStatusCode, COUNT(f) FROM FlowSummary f GROUP BY f.flowStatusCode")
    List<Object[]> getFlowStatisticsByStatus();

    /**
     * Get daily flow statistics
     */
    @Query("SELECT f.beginFlowDate, COUNT(f), SUM(f.nbTransaction), SUM(f.nbError) FROM FlowSummary f WHERE f.beginFlowDate BETWEEN :startDate AND :endDate GROUP BY f.beginFlowDate ORDER BY f.beginFlowDate")
    List<Object[]> getDailyFlowStatistics(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find flows by flow type and status
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.flowTypeId = :flowTypeId AND f.flowStatusCode = :statusCode ORDER BY f.beginFlowDatetime DESC")
    List<FlowSummary> findByFlowTypeAndStatus(@Param("flowTypeId") Integer flowTypeId,
            @Param("statusCode") String statusCode);

    /**
     * Find flows with high transaction count
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.nbTransaction >= :minTransactions ORDER BY f.nbTransaction DESC")
    List<FlowSummary> findFlowsWithHighTransactionCount(
            @Param("minTransactions") Integer minTransactions);

    /**
     * Find recent flows by partner
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.issuingPartnerCode = :partnerCode AND f.beginFlowDatetime >= :since ORDER BY f.beginFlowDatetime DESC")
    List<FlowSummary> findRecentFlowsByPartner(@Param("partnerCode") String partnerCode,
            @Param("since") LocalDateTime since);

    /**
     * Count flows by status in date range
     */
    @Query("SELECT COUNT(f) FROM FlowSummary f WHERE f.flowStatusCode = :statusCode AND f.beginFlowDatetime BETWEEN :startDate AND :endDate")
    Long countByStatusInDateRange(@Param("statusCode") String statusCode,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find flows by root error code
     */
    List<FlowSummary> findByRootErrorCodeOrderByRootErrorDatetimeDesc(String rootErrorCode);

    /**
     * Find flows needing replay
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.nbReplay > 0 ORDER BY f.lastUpdateDatetime DESC")
    List<FlowSummary> findFlowsNeedingReplay();

    /**
     * Get partner statistics
     */
    @Query("SELECT f.issuingPartnerCode, COUNT(f), SUM(f.nbTransaction) FROM FlowSummary f WHERE f.beginFlowDate BETWEEN :startDate AND :endDate GROUP BY f.issuingPartnerCode")
    List<Object[]> getPartnerStatistics(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find latest flow summary for a flow code
     */
    @Query("SELECT f FROM FlowSummary f WHERE f.flowCode = :flowCode ORDER BY f.beginFlowDatetime DESC LIMIT 1")
    Optional<FlowSummary> findLatestByFlowCode(@Param("flowCode") String flowCode);
}
