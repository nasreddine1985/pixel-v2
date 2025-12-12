package com.pixel.v2.referentiel.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.pixel.v2.referentiel.entity.RefFlow;
import com.pixel.v2.referentiel.entity.RefFlowRules;
import com.pixel.v2.referentiel.entity.RefFlowCountry;
import com.pixel.v2.referentiel.entity.RefFlowPartner;
import com.pixel.v2.referentiel.entity.RefCharsetEncoding;
import com.pixel.v2.referentiel.model.RefFlowCompleteDto;
import com.pixel.v2.referentiel.model.RefFlowCompleteDto.RefCharsetEncodingDto;
import com.pixel.v2.referentiel.model.RefFlowCompleteDto.RefFlowCountryDto;
import com.pixel.v2.referentiel.model.RefFlowCompleteDto.RefFlowPartnerDto;
import com.pixel.v2.referentiel.model.RefFlowCompleteDto.RefFlowRulesDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for accessing TIB_AUDIT_TEC schema tables using JPA Provides methods to retrieve
 * RefFlow data with related information
 */
@Repository
public class RefFlowRepository {

    private final RefFlowJpaRepository refFlowJpaRepository;
    private final RefFlowRulesJpaRepository refFlowRulesJpaRepository;
    private final RefCharsetEncodingJpaRepository refCharsetEncodingJpaRepository;

    @Autowired
    public RefFlowRepository(RefFlowJpaRepository refFlowJpaRepository,
            RefFlowRulesJpaRepository refFlowRulesJpaRepository,
            RefCharsetEncodingJpaRepository refCharsetEncodingJpaRepository) {
        this.refFlowJpaRepository = refFlowJpaRepository;
        this.refFlowRulesJpaRepository = refFlowRulesJpaRepository;
        this.refCharsetEncodingJpaRepository = refCharsetEncodingJpaRepository;
    }

    /**
     * Find RefFlow by flowCode
     */
    public Optional<RefFlowCompleteDto> findByFlowCode(String flowCode) {
        try {
            Optional<RefFlow> refFlowOpt =
                    refFlowJpaRepository.findByFlowCodeWithRelations(flowCode);
            if (refFlowOpt.isPresent()) {
                RefFlow refFlow = refFlowOpt.get();
                RefFlowCompleteDto dto = convertToDto(refFlow);

                // Load rules separately as it's not in entity relationships
                Optional<RefFlowRules> rulesOpt =
                        refFlowRulesJpaRepository.findByFlowCode(flowCode);
                rulesOpt.ifPresent(rules -> dto.setRules(convertRulesToDto(rules)));

                return Optional.of(dto);
            }
        } catch (Exception e) {
            // Log error and return empty
            System.err.println("Error finding RefFlow by flowCode: " + flowCode + ", error: "
                    + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Get all charset encodings (utility method)
     */
    public List<RefCharsetEncodingDto> findAllCharsetEncodings() {
        List<RefCharsetEncoding> entities = refCharsetEncodingJpaRepository.findAll();
        return entities.stream().map(this::convertCharsetEncodingToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert RefFlow entity to DTO
     */
    private RefFlowCompleteDto convertToDto(RefFlow refFlow) {
        RefFlowCompleteDto dto = new RefFlowCompleteDto();
        dto.setFlowId(refFlow.getFlowId());
        dto.setFuncProcessId(refFlow.getFuncProcessId());
        dto.setFlowTypId(refFlow.getFlowTypId());
        dto.setTechProcessId(refFlow.getTechProcessId());
        dto.setFlowName(refFlow.getFlowName());
        dto.setFlowDirection(refFlow.getFlowDirection());
        dto.setFlowCode(refFlow.getFlowCode());
        dto.setEnableFlg(refFlow.getEnableFlg());
        dto.setCreationDte(refFlow.getCreationDte());
        dto.setUpdateDte(refFlow.getUpdateDte());
        dto.setApplicationId(refFlow.getApplicationId());
        dto.setMaxFileSize(refFlow.getMaxFileSize());

        // Convert related entities
        if (refFlow.getCountries() != null) {
            dto.setCountries(refFlow.getCountries().stream().map(this::convertCountryToDto)
                    .collect(Collectors.toList()));
        }

        if (refFlow.getPartners() != null) {
            dto.setPartners(refFlow.getPartners().stream().map(this::convertPartnerToDto)
                    .collect(Collectors.toList()));

            // Get unique charset encodings from partners
            List<RefCharsetEncodingDto> charsetEncodings = refFlow.getPartners().stream()
                    .map(RefFlowPartner::getCharsetEncoding).filter(ce -> ce != null).distinct()
                    .map(this::convertCharsetEncodingToDto).collect(Collectors.toList());
            dto.setCharsetEncodings(charsetEncodings);
        }

        return dto;
    }

    /**
     * Convert RefFlowRules entity to DTO
     */
    private RefFlowRulesDto convertRulesToDto(RefFlowRules rules) {
        RefFlowRulesDto dto = new RefFlowRulesDto();
        dto.setFlowCode(rules.getFlowCode());
        dto.setTransportType(rules.getTransportType());
        dto.setIsUnitary(rules.getIsUnitary());
        dto.setPriority(rules.getPriority());
        dto.setUrgency(rules.getUrgency());
        dto.setFlowControlledEnabled(rules.getFlowControlledEnabled());
        dto.setFlowMaximum(rules.getFlowMaximum());
        dto.setFlowRetentionEnabled(rules.getFlowRetentionEnabled());
        dto.setRetentionCyclePeriod(rules.getRetentionCyclePeriod());
        dto.setWriteFile(rules.getWriteFile());
        dto.setMinRequiredFileSize(rules.getMinRequiredFileSize());
        dto.setIgnoreOutputDupCheck(rules.getIgnoreOutputDupCheck());
        dto.setLogAll(rules.getLogAll());
        return dto;
    }

    /**
     * Convert RefFlowCountry entity to DTO
     */
    private RefFlowCountryDto convertCountryToDto(RefFlowCountry country) {
        return new RefFlowCountryDto(country.getFlowId(), country.getCountryId());
    }

    /**
     * Convert RefFlowPartner entity to DTO
     */
    private RefFlowPartnerDto convertPartnerToDto(RefFlowPartner partner) {
        RefFlowPartnerDto dto = new RefFlowPartnerDto();
        dto.setPartnerId(partner.getPartnerId());
        dto.setFlowId(partner.getFlowId());
        dto.setTransportId(partner.getTransportId());
        dto.setPartnerDirection(partner.getPartnerDirection());
        dto.setCreationDte(partner.getCreationDte());
        dto.setUpdateDte(partner.getUpdateDte());
        dto.setRuleId(partner.getRuleId());
        dto.setCharsetEncodingId(partner.getCharsetEncodingId());
        dto.setEnableOut(partner.getEnableOut());
        dto.setEnableBmsa(partner.getEnableBmsa());
        return dto;
    }

    /**
     * Convert RefCharsetEncoding entity to DTO
     */
    private RefCharsetEncodingDto convertCharsetEncodingToDto(RefCharsetEncoding charsetEncoding) {
        return new RefCharsetEncodingDto(charsetEncoding.getCharsetEncodingId(),
                charsetEncoding.getCharsetCode(), charsetEncoding.getCharsetDesc());
    }
}
