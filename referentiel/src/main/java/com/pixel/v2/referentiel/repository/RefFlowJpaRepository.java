package com.pixel.v2.referentiel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pixel.v2.referentiel.entity.RefFlow;

/**
 * JPA Repository for RefFlow entity
 */
@Repository
public interface RefFlowJpaRepository extends JpaRepository<RefFlow, Integer> {

    /**
     * Find RefFlow by flow code
     */
    Optional<RefFlow> findByFlowCode(String flowCode);

    /**
     * Find RefFlow with all related data using separate queries to avoid MultipleBagFetchException
     */
    @Query("SELECT rf FROM RefFlow rf WHERE rf.flowCode = :flowCode")
    Optional<RefFlow> findByFlowCodeWithRelations(@Param("flowCode") String flowCode);
}
