package com.pixel.v2.referentiel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixel.v2.referentiel.entity.RefFlow;
import com.pixel.v2.referentiel.model.RefFlowDto;
import com.pixel.v2.referentiel.repository.RefFlowRepository;

/**
 * Service for comprehensive referentiel data management
 */
@Service
@Transactional
public class ReferentielService {

    private static final Logger logger = LoggerFactory.getLogger(ReferentielService.class);



    @Autowired
    private RefFlowRepository refFlowRepository;


    /**
     * Get complete referentiel data by flow code Returns data structured like
     * referentiel-example.json
     */
    @Transactional(readOnly = true)
    public Optional<RefFlowDto> getCompleteReferentielByFlowCode(String flowCode) {
        logger.debug("Getting complete referentiel for flowCode: {}", flowCode);

        List<Map<String, Object>> results =
                refFlowRepository.findCompleteReferentielByFlowCode(flowCode);

        if (results.isEmpty()) {
            logger.warn("No referentiel data found for flowCode: {}", flowCode);
            return Optional.empty();
        }

        // Process the results and build the DTO
        RefFlowDto dto = buildRefFlowDto(results);

        return Optional.of(dto);
    }

    /**
     * Build the DTO from SQL query results
     */
    private RefFlowDto buildRefFlowDto(List<Map<String, Object>> results) {
        RefFlowDto dto = new RefFlowDto();

        // Since we may have multiple rows for different partners, get the first row for flow data
        Map<String, Object> firstRow = results.get(0);

        // Build flow information
        RefFlowDto.FlowInfo flowInfo = buildFlowInfo(firstRow);
        
        // Build functional properties from all rows
        List<RefFlowDto.FlowFuncPrtyInfo> funcProperties = buildFlowFunctionalProperties(results);
        flowInfo.setFlowFuncPrty(funcProperties);
        
        dto.setFlow(flowInfo);

        // Build partner information (IN and OUT)
        buildPartnerInfo(dto, results);

        // Build flow rules
        dto.setFlowRules(buildFlowRules(firstRow));

        // Build additional sections with defaults
        dto.setHttpTransport(buildHttpTransportInfo());
        dto.setVirtualAccount(buildVirtualAccountInfo());
        dto.setFunctionalModules(new ArrayList<>());

        return dto;
    }

    /**
     * Build flow information section
     */
    private RefFlowDto.FlowInfo buildFlowInfo(Map<String, Object> row) {
        RefFlowDto.FlowInfo flow = new RefFlowDto.FlowInfo();

        flow.setFlowId(String.valueOf(row.get("flow_id")));
        flow.setFlowCode((String) row.get("flow_code"));
        flow.setFlowName((String) row.get("flow_name"));
        flow.setFlowTypeName((String) row.get("flow_typ_name"));
        flow.setFlowDirection((String) row.get("flow_direction"));
        flow.setFlowEnabled(String.valueOf(row.get("enable_flg")));

        // Build application info
        RefFlowDto.ApplicationInfo app = new RefFlowDto.ApplicationInfo();
        app.setName((String) row.get("APPLICATION_NAME"));
        // Application code would need to be added to the SQL query or derived
        app.setCode("ITL"); // Default from example
        flow.setApplication(app);

        // Build countries list
        String countriesStr = (String) row.get("FLOW_COUNTRIES");
        if (countriesStr != null && !countriesStr.trim().isEmpty()) {
            List<String> countries = List.of(countriesStr.split(",\\s*"));
            flow.setCountries(countries);
        }

        // Functional properties will be set separately in buildRefFlowDto
        // flow.setFlowFuncPrty() is called from buildRefFlowDto with all results

        return flow;
    }

    /**
     * Build partner information for IN and OUT partners
     */
    private void buildPartnerInfo(RefFlowDto dto, List<Map<String, Object>> results) {
        for (Map<String, Object> row : results) {
            String partnerDirection = (String) row.get("partner_direction");

            if ("IN".equals(partnerDirection)) {
                dto.setPartnerIn(buildPartnerInfoFromRow(row));
            } else if ("OUT".equals(partnerDirection)) {
                dto.setPartnerOut(buildPartnerInfoFromRow(row));
            }
        }
    }

    /**
     * Build individual partner info from a row
     */
    private RefFlowDto.PartnerInfo buildPartnerInfoFromRow(Map<String, Object> row) {
        RefFlowDto.PartnerInfo partner = new RefFlowDto.PartnerInfo();

        partner.setPartnerCode((String) row.get("partner_code"));
        partner.setPartnerName((String) row.get("partner_name"));
        partner.setPartnerTypeName((String) row.get("partner_type_name"));
        partner.setCharsetCode((String) row.get("charset_code"));
        partner.setEnabled(String.valueOf(row.get("enable_out")));
        partner.setBmsaEnabled(String.valueOf(row.get("enable_bmsa")));

        // Build transport info
        partner.setTransport(buildTransportInfo(row));

        return partner;
    }

    /**
     * Build functional properties list from query results
     */
    private List<RefFlowDto.FlowFuncPrtyInfo> buildFlowFunctionalProperties(List<Map<String, Object>> results) {
        List<RefFlowDto.FlowFuncPrtyInfo> properties = new ArrayList<>();
        
        for (Map<String, Object> row : results) {
            String prtyFlowName = (String) row.get("prty_flow_name");
            String flowPrtyValue = (String) row.get("flow_prty_value");
            
            // Only add if we have valid functional property data
            if (prtyFlowName != null && !prtyFlowName.trim().isEmpty() && 
                flowPrtyValue != null && !flowPrtyValue.trim().isEmpty()) {
                
                RefFlowDto.FlowFuncPrtyInfo property = new RefFlowDto.FlowFuncPrtyInfo();
                property.setKey(prtyFlowName);
                property.setType((String) row.get("prty_flow_typ"));
                property.setDesc((String) row.get("prty_flow_desc"));
                property.setValue(flowPrtyValue);
                
                properties.add(property);
            }
        }
        
        return properties;
    }

    /**
     * Build transport information
     */
    private RefFlowDto.TransportInfo buildTransportInfo(Map<String, Object> row) {
        RefFlowDto.TransportInfo transport = new RefFlowDto.TransportInfo();

        String transportType = (String) row.get("transport_typ");
        transport.setType(transportType);

        // Build transport-specific information based on type
        if ("MQS".equals(transportType)) {
            RefFlowDto.MqsInfo mqs = new RefFlowDto.MqsInfo();
            mqs.setQName((String) row.get("mqs_q_name"));
            mqs.setQManager((String) row.get("mqs_q_manager"));
            transport.setMqs(mqs);
        } else if ("CFT".equals(transportType)) {
            RefFlowDto.CftInfo cft = new RefFlowDto.CftInfo();
            cft.setIdf((String) row.get("cft_idf"));
            cft.setPartnerCode((String) row.get("cft_partner_code"));
            transport.setCft(cft);
        } else if ("HTTP".equals(transportType)) {
            RefFlowDto.HttpInfo http = new RefFlowDto.HttpInfo();
            http.setUri((String) row.get("http_uri"));
            transport.setHttp(http);
        } else if ("JMS".equals(transportType)) {
            RefFlowDto.JmsInfo jms = new RefFlowDto.JmsInfo();
            jms.setQName((String) row.get("jms_q_name"));
            transport.setJms(jms);
        } else if ("EMAIL".equals(transportType)) {
            RefFlowDto.EmailInfo email = new RefFlowDto.EmailInfo();
            email.setEmailName((String) row.get("email_name"));
            email.setFrom((String) row.get("email_from"));
            email.setTo((String) row.get("email_recipient_to"));
            email.setCc((String) row.get("email_recipient_cc"));
            email.setSubject((String) row.get("email_subject"));
            Boolean hasAttachment = (Boolean) row.get("has_attachment");
            email.setAttachment(hasAttachment != null && hasAttachment);
            transport.setEmail(email);
        }

        // Initialize unused transport types as null
        if (!"MQS".equals(transportType))
            transport.setMqs(null);
        if (!"CFT".equals(transportType))
            transport.setCft(null);
        if (!"HTTP".equals(transportType))
            transport.setHttp(null);
        if (!"JMS".equals(transportType))
            transport.setJms(null);
        if (!"EMAIL".equals(transportType))
            transport.setEmail(null);
        transport.setSftp(null);

        return transport;
    }

    /**
     * Build flow rules from row data
     */
    private List<RefFlowDto.FlowRuleInfo> buildFlowRules(Map<String, Object> row) {
        List<RefFlowDto.FlowRuleInfo> rules = new ArrayList<>();

        // Only create rule if we have rule data
        String ruleFlowCode = (String) row.get("rule_flowcode");
        if (ruleFlowCode != null && !ruleFlowCode.trim().isEmpty()) {
            RefFlowDto.FlowRuleInfo rule = new RefFlowDto.FlowRuleInfo();

            rule.setFlowCode(ruleFlowCode);
            rule.setTransportType((String) row.get("transporttype"));
            rule.setIsUnitary(getBooleanValue(row, "isunitary"));
            rule.setPriority(getIntegerValue(row, "priority"));
            rule.setUrgency((String) row.get("urgency"));
            rule.setFlowControlledEnabled(getBooleanValue(row, "flowcontrolledenabled"));
            rule.setFlowMaximum(getLongValue(row, "flowmaximum"));
            rule.setFlowRetentionEnabled(getBooleanValue(row, "flowretentionenabled"));
            rule.setRetentionCyclePeriod(getStringValue(row, "retentioncycleperiod"));
            rule.setWriteFile(getBooleanValue(row, "write_file"));
            rule.setMinRequiredFileSize(getLongValue(row, "minrequiredfilesize"));
            rule.setIgnoreOutputDupCheck(getBooleanValue(row, "ignoreoutputdupcheck"));
            rule.setLogAll(getBooleanValue(row, "logall"));
            rules.add(rule);
        }

        return rules;
    }

    /**
     * Build default HTTP transport info (typically null values)
     */
    private RefFlowDto.HttpTransportInfo buildHttpTransportInfo() {
        RefFlowDto.HttpTransportInfo httpTransport = new RefFlowDto.HttpTransportInfo();

        RefFlowDto.HttpInfo inHttp = new RefFlowDto.HttpInfo();
        inHttp.setUri(null);
        inHttp.setPartnerCode(null);

        RefFlowDto.HttpInfo outHttp = new RefFlowDto.HttpInfo();
        outHttp.setUri(null);
        outHttp.setPartnerCode(null);

        httpTransport.setIn(inHttp);
        httpTransport.setOut(outHttp);

        return httpTransport;
    }

    /**
     * Build default virtual account info (typically null values)
     */
    private RefFlowDto.VirtualAccountInfo buildVirtualAccountInfo() {
        RefFlowDto.VirtualAccountInfo virtualAccount = new RefFlowDto.VirtualAccountInfo();
        virtualAccount.setFormat(null);
        virtualAccount.setPath(null);
        return virtualAccount;
    }

    /**
     * Helper method to safely convert string values to Boolean
     */
    private Boolean getBooleanValue(Map<String, Object> row, String columnName) {
        Object value = row.get(columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String strValue = value.toString().trim().toLowerCase();
        return "true".equals(strValue) || "1".equals(strValue) || "y".equals(strValue);
    }

    /**
     * Helper method to safely get Integer values from SQL result map
     */
    private Integer getIntegerValue(Map<String, Object> row, String columnName) {
        Object value = row.get(columnName);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Helper method to safely convert values to String
     */
    private String getStringValue(Map<String, Object> row, String columnName) {
        Object value = row.get(columnName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Utility method to safely get Long values from SQL result
     */
    private Long getLongValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null)
            return null;
        if (value instanceof Long)
            return (Long) value;
        if (value instanceof Integer)
            return ((Integer) value).longValue();
        if (value instanceof Number)
            return ((Number) value).longValue();
        return null;
    }

    /**
     * Validate flow configuration completeness
     */
    @Transactional(readOnly = true)
    public boolean validateFlowConfiguration(Integer flowId) {
        logger.debug("Validating flow configuration for flowId: {}", flowId);

        Optional<RefFlow> flowOpt = refFlowRepository.findById(flowId);
        if (flowOpt.isEmpty()) {
            logger.warn("Flow not found with ID: {}", flowId);
            return false;
        }

        RefFlow flow = flowOpt.get();

        // Check essential configurations
        boolean isValid = flow.getFlowName() != null && !flow.getFlowName().trim().isEmpty()
                && flow.getFlowCode() != null && !flow.getFlowCode().trim().isEmpty()
                && flow.getApplicationId() != null && flow.getFuncProcessId() != null
                && flow.getTechProcessId() != null && "Y".equals(flow.getEnableFlg());

        logger.debug("Flow {} validation result: {}", flowId, isValid);
        return isValid;
    }

    /**
     * Get flow summary with basic information
     */
    @Transactional(readOnly = true)
    public String getFlowSummary(Integer flowId) {
        Optional<RefFlow> flowOpt = refFlowRepository.findById(flowId);
        if (flowOpt.isEmpty()) {
            return "Flow not found";
        }

        RefFlow flow = flowOpt.get();
        StringBuilder summary = new StringBuilder();
        summary.append("Flow: ").append(flow.getFlowName()).append(" (").append(flow.getFlowCode())
                .append(")").append("\nDirection: ").append(flow.getFlowDirection())
                .append("\nStatus: ")
                .append("Y".equals(flow.getEnableFlg()) ? "Enabled" : "Disabled")
                .append("\nApplication ID: ").append(flow.getApplicationId())
                .append("\nFunc Process ID: ").append(flow.getFuncProcessId())
                .append("\nTech Process ID: ").append(flow.getTechProcessId())
                .append("\nCountries: ")
                .append(flow.getCountries() != null ? flow.getCountries().size() : 0)
                .append("\nPartners: ")
                .append(flow.getPartners() != null ? flow.getPartners().size() : 0);

        return summary.toString();
    }
}
