package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.Pacs008Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface Pacs008MessageRepository extends JpaRepository<Pacs008Message, Long> {

    List<Pacs008Message> findByJmsMessageId(String jmsMessageId);

    List<Pacs008Message> findByJmsCorrelationId(String jmsCorrelationId);

    List<Pacs008Message> findByProcessingRoute(String processingRoute);

    List<Pacs008Message> findByMessageTypeAndProcessedTimestampBetween(String messageType,
            LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(p) FROM Pacs008Message p WHERE p.messageType = :messageType AND p.processedTimestamp BETWEEN :startTime AND :endTime")
    Long countByMessageTypeAndProcessedTimestampBetween(@Param("messageType") String messageType,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT p FROM Pacs008Message p WHERE p.processingRoute = :route AND p.processedTimestamp >= :since ORDER BY p.processedTimestamp DESC")
    List<Pacs008Message> findRecentMessagesByRoute(@Param("route") String route,
            @Param("since") LocalDateTime since);
}
