package com.pixel.v2.persistence.service;

import com.pixel.v2.persistence.model.LogEvent;
import com.pixel.v2.persistence.repository.LogEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for LogEvent operations
 */
@Service
@Transactional
public class LogEventService {

    private final LogEventRepository logEventRepository;

    public LogEventService(LogEventRepository logEventRepository) {
        this.logEventRepository = logEventRepository;
    }

    /**
     * Save a log event
     */
    public LogEvent save(LogEvent logEvent) {
        return logEventRepository.save(logEvent);
    }

    /**
     * Find log event by ID
     */
    @Transactional(readOnly = true)
    public Optional<LogEvent> findById(String logId) {
        return logEventRepository.findById(logId);
    }

    /**
     * Find log events by flow ID
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByFlowId(String flowId) {
        return logEventRepository.findByFlowIdOrderByDatatsDesc(flowId);
    }

    /**
     * Find log events by flow code
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByFlowCode(String flowCode) {
        return logEventRepository.findByFlowCodeOrderByDatatsDesc(flowCode);
    }

    /**
     * Find log events by component
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByComponent(String component) {
        return logEventRepository.findByComponentOrderByDatatsDesc(component);
    }

    /**
     * Find log events by log role
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByLogRole(String logRole) {
        return logEventRepository.findByLogRoleOrderByDatatsDesc(logRole);
    }

    /**
     * Find log events by date range
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logEventRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Find log events by flow ID and date range
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByFlowIdAndDateRange(String flowId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return logEventRepository.findByFlowIdAndDateRange(flowId, startDate, endDate);
    }

    /**
     * Find log events by log day
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByLogDay(LocalDate logDay) {
        return logEventRepository.findByLogDay(logDay);
    }

    /**
     * Find error logs by flow code
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findErrorLogsByFlowCode(String flowCode) {
        return logEventRepository.findErrorLogsByFlowCode(flowCode);
    }

    /**
     * Find recent logs for a component
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findRecentLogsByComponent(String component, LocalDateTime since) {
        return logEventRepository.findRecentLogsByComponent(component, since);
    }

    /**
     * Find logs by context ID
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByContextId(String contextId) {
        return logEventRepository.findByContextIdOrderByDatatsAsc(contextId);
    }

    /**
     * Find logs by message ID
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByMsgId(String msgId) {
        return logEventRepository.findByMsgIdOrderByDatatsDesc(msgId);
    }

    /**
     * Count logs by flow code and date range
     */
    @Transactional(readOnly = true)
    public Long countByFlowCodeAndDateRange(String flowCode, LocalDateTime startDate,
            LocalDateTime endDate) {
        return logEventRepository.countByFlowCodeAndDateRange(flowCode, startDate, endDate);
    }

    /**
     * Find logs with errors in a date range
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findErrorLogsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logEventRepository.findErrorLogsInDateRange(startDate, endDate);
    }

    /**
     * Find logs by batch name
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByBatchName(String batchName) {
        return logEventRepository.findByBatchName(batchName);
    }

    /**
     * Find logs by instance ID and component
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByInstanceIdAndComponent(String instanceId, String component) {
        return logEventRepository.findByInstanceIdAndComponentOrderByDatatsDesc(instanceId,
                component);
    }

    /**
     * Get latest log for a flow
     */
    @Transactional(readOnly = true)
    public LogEvent findLatestByFlowId(String flowId) {
        return logEventRepository.findLatestByFlowId(flowId);
    }

    /**
     * Find logs by service path pattern
     */
    @Transactional(readOnly = true)
    public List<LogEvent> findByServicePathPattern(String pathPattern) {
        return logEventRepository.findByServicePathPattern(pathPattern);
    }

    /**
     * Get flow statistics by date
     */
    @Transactional(readOnly = true)
    public List<Object[]> getFlowStatisticsByDate(LocalDate date) {
        return logEventRepository.getFlowStatisticsByDate(date);
    }

    /**
     * Create and save a log event
     */
    public LogEvent createLogEvent(String logId, String flowId, String flowCode, String component,
            String txt, String logRole) {
        LogEvent logEvent = new LogEvent(logId, flowId, flowCode, component);
        logEvent.setTxt(txt);
        logEvent.setLogRole(logRole);
        logEvent.setHalfFlowId(flowId); // Default to flowId
        logEvent.setHalfFlowCode(flowCode); // Default to flowCode
        logEvent.setInstanceId("DEFAULT"); // Default instance ID
        return save(logEvent);
    }

    /**
     * Log an error event
     */
    public LogEvent logError(String flowId, String flowCode, String component,
            String errorMessage) {
        String logId = generateLogId();
        return createLogEvent(logId, flowId, flowCode, component, errorMessage, "ERROR");
    }

    /**
     * Log an info event
     */
    public LogEvent logInfo(String flowId, String flowCode, String component, String message) {
        String logId = generateLogId();
        return createLogEvent(logId, flowId, flowCode, component, message, "INFO");
    }

    /**
     * Delete old log events
     */
    public void deleteOldLogs(LocalDateTime beforeDate) {
        List<LogEvent> oldLogs =
                logEventRepository.findByDateRange(LocalDateTime.of(2020, 1, 1, 0, 0), beforeDate);
        logEventRepository.deleteAll(oldLogs);
    }

    /**
     * Generate a unique log ID
     */
    private String generateLogId() {
        return "LOG_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}
