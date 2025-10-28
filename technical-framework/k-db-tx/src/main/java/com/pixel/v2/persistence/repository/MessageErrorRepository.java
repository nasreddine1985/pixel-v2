package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.MessageError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageErrorRepository extends JpaRepository<MessageError, Long> {

    List<MessageError> findByJmsMessageId(String jmsMessageId);

    List<MessageError> findByErrorRoute(String errorRoute);

    List<MessageError> findByErrorMessageContainingOrderByErrorTimestampDesc(String errorType);

    @Query("SELECT COUNT(e) FROM MessageError e WHERE e.errorRoute = :route AND e.errorTimestamp BETWEEN :startTime AND :endTime")
    Long countByErrorRouteAndErrorTimestampBetween(@Param("route") String route,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT e FROM MessageError e WHERE e.errorRoute = :route ORDER BY e.errorTimestamp DESC")
    List<MessageError> findErrorsByRouteOrderByErrorTimestampDesc(@Param("route") String route);

    @Query("SELECT e FROM MessageError e WHERE e.errorTimestamp >= :since ORDER BY e.errorTimestamp DESC")
    List<MessageError> findRecentErrors(@Param("since") LocalDateTime since);
}
