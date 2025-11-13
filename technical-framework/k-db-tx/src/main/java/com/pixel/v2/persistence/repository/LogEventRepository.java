package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.LogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for LogEvent entity
 */
@Repository
public interface LogEventRepository extends JpaRepository<LogEvent, String> {

    /**
     * Find log events by flow ID
     */
    List<LogEvent> findByFlowIdOrderByDatatsDesc(String flowId);

    /**
     * Find log events by flow code
     */
    List<LogEvent> findByFlowCodeOrderByDatatsDesc(String flowCode);

    /**
     * Find log events by component
     */
    List<LogEvent> findByComponentOrderByDatatsDesc(String component);

    /**
     * Find log events by log role
     */
    List<LogEvent> findByLogRoleOrderByDatatsDesc(String logRole);

    /**
     * Find log events by date range
     */
    @Query("SELECT l FROM LogEvent l WHERE l.datats BETWEEN :startDate AND :endDate ORDER BY l.datats DESC")
    List<LogEvent> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find log events by flow ID and date range
     */
    @Query("SELECT l FROM LogEvent l WHERE l.flowId = :flowId AND l.datats BETWEEN :startDate AND :endDate ORDER BY l.datats DESC")
    List<LogEvent> findByFlowIdAndDateRange(@Param("flowId") String flowId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find log events by log day
     */
    List<LogEvent> findByLogDay(LocalDate logDay);

    /**
     * Find error logs by flow code
     */
    @Query("SELECT l FROM LogEvent l WHERE l.flowCode = :flowCode AND l.logRole = 'ERROR' ORDER BY l.datats DESC")
    List<LogEvent> findErrorLogsByFlowCode(@Param("flowCode") String flowCode);

    /**
     * Find recent logs for a component
     */
    @Query("SELECT l FROM LogEvent l WHERE l.component = :component AND l.datats >= :since ORDER BY l.datats DESC")
    List<LogEvent> findRecentLogsByComponent(@Param("component") String component,
            @Param("since") LocalDateTime since);

    /**
     * Find logs by context ID
     */
    List<LogEvent> findByContextIdOrderByDatatsAsc(String contextId);

    /**
     * Find logs by message ID
     */
    List<LogEvent> findByMsgIdOrderByDatatsDesc(String msgId);

    /**
     * Count logs by flow code and date range
     */
    @Query("SELECT COUNT(l) FROM LogEvent l WHERE l.flowCode = :flowCode AND l.datats BETWEEN :startDate AND :endDate")
    Long countByFlowCodeAndDateRange(@Param("flowCode") String flowCode,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs with errors in a date range
     */
    @Query("SELECT l FROM LogEvent l WHERE l.logRole IN ('ERROR', 'FATAL') AND l.datats BETWEEN :startDate AND :endDate ORDER BY l.datats DESC")
    List<LogEvent> findErrorLogsInDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs by batch information
     */
    @Query("SELECT l FROM LogEvent l WHERE l.msgBatchName = :batchName ORDER BY l.msgBatchMsgNo ASC")
    List<LogEvent> findByBatchName(@Param("batchName") String batchName);

    /**
     * Find logs by instance ID and component
     */
    List<LogEvent> findByInstanceIdAndComponentOrderByDatatsDesc(String instanceId,
            String component);

    /**
     * Get latest log for a flow
     */
    @Query("SELECT l FROM LogEvent l WHERE l.flowId = :flowId ORDER BY l.datats DESC LIMIT 1")
    LogEvent findLatestByFlowId(@Param("flowId") String flowId);

    /**
     * Find logs by service path pattern
     */
    @Query("SELECT l FROM LogEvent l WHERE l.servicePath LIKE %:pathPattern% ORDER BY l.datats DESC")
    List<LogEvent> findByServicePathPattern(@Param("pathPattern") String pathPattern);

    /**
     * Get flow statistics by date
     */
    @Query("SELECT l.flowCode, COUNT(l), l.logDay FROM LogEvent l WHERE l.logDay = :date GROUP BY l.flowCode, l.logDay")
    List<Object[]> getFlowStatisticsByDate(@Param("date") LocalDate date);
}
