package com.pixel.v2.referentiel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pixel.v2.referentiel.entity.RefFlowRules;

/**
 * JPA Repository for RefFlowRules entity
 */
@Repository
public interface RefFlowRulesJpaRepository extends JpaRepository<RefFlowRules, String> {

    /**
     * Find rules by flow code
     */
    Optional<RefFlowRules> findByFlowCode(String flowCode);
}
