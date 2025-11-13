package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.FlowReference;
import com.pixel.v2.persistence.model.FlowReference.FlowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FlowReference entity
 */
@Repository
public interface FlowReferenceRepository extends JpaRepository<FlowReference, String> {

    /**
     * Find flow reference by flow code
     */
    Optional<FlowReference> findByFlowCode(String flowCode);

    /**
     * Find all flows by status
     */
    List<FlowReference> findByStatus(FlowStatus status);

    /**
     * Find flows by source channel
     */
    List<FlowReference> findBySourceChannel(String sourceChannel);

    /**
     * Find flows by target system
     */
    List<FlowReference> findByTargetSystem(String targetSystem);

    /**
     * Find flows by source system and status
     */
    List<FlowReference> findBySourceSystemAndStatus(String sourceSystem, FlowStatus status);

    /**
     * Find active flows by source channel
     */
    @Query("SELECT f FROM FlowReference f WHERE f.sourceChannel = :sourceChannel AND f.status = 'ACTIVE'")
    List<FlowReference> findActiveFlowsBySourceChannel(
            @Param("sourceChannel") String sourceChannel);

    /**
     * Find flows with encryption required
     */
    @Query("SELECT f FROM FlowReference f WHERE f.encryptionRequired = 'TRUE'")
    List<FlowReference> findFlowsWithEncryptionRequired();

    /**
     * Find flows by flow type and priority
     */
    @Query("SELECT f FROM FlowReference f WHERE f.flowType = :flowType AND f.priority >= :minPriority ORDER BY f.priority DESC")
    List<FlowReference> findByFlowTypeAndMinPriority(@Param("flowType") String flowType,
            @Param("minPriority") Integer minPriority);

    /**
     * Find flows with split enabled
     */
    @Query("SELECT f FROM FlowReference f WHERE f.splitEnabled = 'TRUE'")
    List<FlowReference> findFlowsWithSplitEnabled();

    /**
     * Find flows by retention configuration
     */
    @Query("SELECT f FROM FlowReference f WHERE f.retentionInEnabled = 'TRUE' AND f.retentionInDays > :days")
    List<FlowReference> findFlowsWithRetentionPolicy(@Param("days") Integer days);

    /**
     * Check if flow code exists
     */
    boolean existsByFlowCode(String flowCode);

    /**
     * Count flows by status
     */
    long countByStatus(FlowStatus status);

    /**
     * Find flows by source format
     */
    List<FlowReference> findBySourceFormat(String sourceFormat);
}
