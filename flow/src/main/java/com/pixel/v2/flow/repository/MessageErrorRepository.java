package com.pixel.v2.flow.repository;

import com.pixel.v2.flow.model.MessageError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageErrorRepository extends JpaRepository<MessageError, Long> {
    
    /**
     * Find errors by JMS Message ID
     */
    List<MessageError> findByJmsMessageId(String jmsMessageId);
    
    /**
     * Find errors by error route
     */
    List<MessageError> findByErrorRoute(String errorRoute);
    
    /**
     * Find errors created after a specific date
     */
    List<MessageError> findByCreatedAtAfter(LocalDateTime createdAt);
    
    /**
     * Find errors by error route and date range
     */
    @Query("SELECT e FROM MessageError e WHERE e.errorRoute = :errorRoute AND e.createdAt BETWEEN :startDate AND :endDate")
    List<MessageError> findByErrorRouteAndDateRange(
        @Param("errorRoute") String errorRoute, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count errors by route
     */
    long countByErrorRoute(String errorRoute);
    
    /**
     * Count errors today
     */
    @Query("SELECT COUNT(e) FROM MessageError e WHERE e.createdAt >= :startOfDay")
    long countTodaysErrors(@Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * Find recent errors (last 24 hours)
     */
    @Query("SELECT e FROM MessageError e WHERE e.createdAt >= :since ORDER BY e.createdAt DESC")
    List<MessageError> findRecentErrors(@Param("since") LocalDateTime since);
}