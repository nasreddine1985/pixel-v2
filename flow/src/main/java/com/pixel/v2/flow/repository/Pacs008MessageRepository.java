package com.pixel.v2.flow.repository;

import com.pixel.v2.flow.model.Pacs008Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface Pacs008MessageRepository extends JpaRepository<Pacs008Message, Long> {
    
    /**
     * Find message by JMS Message ID
     */
    Optional<Pacs008Message> findByJmsMessageId(String jmsMessageId);
    
    /**
     * Find messages by message type
     */
    List<Pacs008Message> findByMessageType(String messageType);
    
    /**
     * Find messages by processing route
     */
    List<Pacs008Message> findByProcessingRoute(String processingRoute);
    
    /**
     * Find messages created after a specific date
     */
    List<Pacs008Message> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * Find messages by message type and processing route
     */
    @Query("SELECT m FROM Pacs008Message m WHERE m.messageType = :messageType AND m.processingRoute = :processingRoute")
    List<Pacs008Message> findByMessageTypeAndProcessingRoute(
        @Param("messageType") String messageType, 
        @Param("processingRoute") String processingRoute
    );
    
    /**
     * Count messages by message type
     */
    long countByMessageType(String messageType);
    
    /**
     * Count messages processed today
     */
    @Query("SELECT COUNT(m) FROM Pacs008Message m WHERE m.createdAt >= :startOfDay")
    long countTodaysMessages(@Param("startOfDay") LocalDateTime startOfDay);
}