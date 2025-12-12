package com.pixel.v2.referentiel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pixel.v2.referentiel.entity.RefCharsetEncoding;

/**
 * JPA Repository for RefCharsetEncoding entity
 */
@Repository
public interface RefCharsetEncodingJpaRepository
        extends JpaRepository<RefCharsetEncoding, Integer> {

    /**
     * Find charset encodings used by partners for a specific flow
     */
    @Query("SELECT DISTINCT ce FROM RefCharsetEncoding ce " + "INNER JOIN ce.partners p "
            + "WHERE p.flowId = :flowId")
    List<RefCharsetEncoding> findByFlowId(@Param("flowId") Integer flowId);
}
