-- ========================================
-- TIB_AUDIT_TEC Data Initialization Script
-- Generated from actual pixelv2 database on 2026-01-05
-- ========================================

-- This script contains real production data extracted from the actual database
-- and creates a comprehensive initialization for the tib_audit_tec schema

BEGIN;

-- ========================================
-- 1. REFERENCE DATA - APPLICATIONS
-- ========================================

INSERT INTO tib_audit_tec.ref_application (application_id, application_code, application_name) 
VALUES (1, 'PIXEL', 'Pixel Integration Platform')
ON CONFLICT (application_code) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_application_application_id_seq', (SELECT COALESCE(MAX(application_id), 1) FROM tib_audit_tec.ref_application), true);

-- ========================================
-- 2. REFERENCE DATA - FLOW TYPES
-- ========================================

INSERT INTO tib_audit_tec.ref_flow_typ (flow_typ_id, flow_typ_name) 
VALUES 
    (1, 'Payment Processing'),
    (2, 'Data Synchronization'),
    (3, 'Notification Service')
ON CONFLICT (flow_typ_name) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_flow_typ_flow_typ_id_seq', (SELECT COALESCE(MAX(flow_typ_id), 1) FROM tib_audit_tec.ref_flow_typ), true);

-- ========================================
-- 3. REFERENCE DATA - PARTNER TYPES
-- ========================================

INSERT INTO tib_audit_tec.ref_partner_typ (partner_type_id, partner_type_name)
VALUES 
    (1, 'Internal System'),
    (2, 'External Bank'),
    (3, 'Third Party Service'),
    (4, 'Government Agency'),
    (5, 'Internal Partner')
ON CONFLICT (partner_type_name) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_partner_typ_partner_type_id_seq', (SELECT COALESCE(MAX(partner_type_id), 1) FROM tib_audit_tec.ref_partner_typ), true);

-- ========================================
-- 4. REFERENCE DATA - COUNTRIES
-- ========================================

INSERT INTO tib_audit_tec.ref_country (country_id, country_name, country_iso_code, is_sepa, region)
VALUES 
    (1, 'Switzerland', 'CH', 'Y', 'EMEA'),
    (2, 'Germany', 'DE', 'Y', 'EMEA'),
    (3, 'United States', 'US', 'N', 'AMER'),
    (4, 'United Kingdom', 'GB', 'N', 'EMEA')
ON CONFLICT (country_iso_code) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_country_country_id_seq', (SELECT COALESCE(MAX(country_id), 1) FROM tib_audit_tec.ref_country), true);

-- ========================================
-- 5. REFERENCE DATA - CHARACTER ENCODINGS
-- ========================================

INSERT INTO tib_audit_tec.ref_charset_encoding (charset_encoding_id, charset_code, charset_desc)
VALUES 
    (1, 'UTF-8', 'Unicode UTF-8 encoding'),
    (2, 'ISO-8859-1', 'Latin-1 Western European'),
    (3, 'UTF-16', 'Unicode UTF-16 encoding')
ON CONFLICT (charset_code) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_charset_encoding_charset_encoding_id_seq', (SELECT COALESCE(MAX(charset_encoding_id), 1) FROM tib_audit_tec.ref_charset_encoding), true);

-- ========================================
-- 6. REFERENCE DATA - ROUTE RULES
-- ========================================

INSERT INTO tib_audit_tec.ref_route_rule (rule_id, rule_name)
VALUES 
    (1, 'DEFAULT_ROUTING'),
    (2, 'CHIPS_RULE')
ON CONFLICT (rule_name) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_route_rule_rule_id_seq', (SELECT COALESCE(MAX(rule_id), 1) FROM tib_audit_tec.ref_route_rule), true);

-- ========================================
-- 7. FUNCTIONAL PROCESSES
-- ========================================

INSERT INTO tib_audit_tec.ref_func_process (func_process_id, func_process_name, creation_dte, update_dte)
VALUES (1, 'ICHSIC_PROCESSING', NOW(), NOW())
ON CONFLICT (func_process_name) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_func_process_func_process_id_seq', (SELECT COALESCE(MAX(func_process_id), 1) FROM tib_audit_tec.ref_func_process), true);

-- ========================================
-- 8. TECHNICAL PROCESSES
-- ========================================

INSERT INTO tib_audit_tec.ref_tech_process (tech_process_id, creation_dte, tech_process_name, update_dte)
VALUES (1, NOW(), 'MQ_TRANSPORT_PROCESS', NOW())
ON CONFLICT (tech_process_name) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_tech_process_tech_process_id_seq', (SELECT COALESCE(MAX(tech_process_id), 1) FROM tib_audit_tec.ref_tech_process), true);

-- ========================================
-- 9. TRANSPORT TYPES
-- ========================================

INSERT INTO tib_audit_tec.ref_transport (transport_id, transport_typ)
VALUES 
    (1, 'MQ'),
    (2, 'MQS'),
    (3, 'CFT'),
    (4, 'KAFKA')
ON CONFLICT (transport_id) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_transport_transport_id_seq', (SELECT COALESCE(MAX(transport_id), 1) FROM tib_audit_tec.ref_transport), true);

-- ========================================
-- 10. PARTNERS
-- ========================================

INSERT INTO tib_audit_tec.ref_partner (
    partner_id,
    partner_type_id, 
    partner_name, 
    partner_code, 
    creation_dte, 
    update_dte
)
VALUES 
    (1, 2, 'Swiss Bank Partner', 'CHBANK01', NOW(), NOW()),
    (2, 5, 'CH - Switzerland ACH Clearing', 'CHSIC', NOW(), NOW()),
    (3, 5, 'Domestic payment engine', 'DOME', NOW(), NOW())
ON CONFLICT (partner_code) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_partner_partner_id_seq', (SELECT COALESCE(MAX(partner_id), 1) FROM tib_audit_tec.ref_partner), true);

-- ========================================
-- 11. TRANSPORT CONFIGURATIONS - MQS
-- ========================================

INSERT INTO tib_audit_tec.ref_transport_mqs (
    transport_id,
    mqs_q_name,
    mqs_q_manager
)
VALUES 
    (1, 'ICHSIC.QUEUE', 'QM_PIXEL_V2'),
    (2, 'OITARTMI01', 'FRITL01Z')
ON CONFLICT (transport_id) DO NOTHING;

-- ========================================
-- 12. TRANSPORT CONFIGURATIONS - CFT
-- ========================================

INSERT INTO tib_audit_tec.ref_transport_cft (
    transport_id,
    cft_idf,
    cft_partner_code
)
VALUES 
    (3, 'DOME_CFT_IDF', 'DOME')
ON CONFLICT (transport_id) DO NOTHING;

-- ========================================
-- 12.1. TRANSPORT CONFIGURATIONS - KAFKA
-- ========================================

INSERT INTO tib_audit_tec.ref_transport_kafka (
    transport_id,
    kafka_topic_name,
    kafka_group_id
)
VALUES 
    (4, 'pixel-v2-payments', 'pixel-consumer-group')
ON CONFLICT (transport_id) DO NOTHING;

-- ========================================
-- 13. FLOWS
-- ========================================

INSERT INTO tib_audit_tec.ref_flow (
    flow_id,
    func_process_id,
    flow_typ_id,
    tech_process_id,
    flow_name,
    flow_direction,
    flow_code,
    enable_flg,
    creation_dte,
    update_dte,
    application_id,
    max_file_size
)
VALUES (
    1,
    1,  -- ICHSIC_PROCESSING
    1,  -- Payment Processing
    1,  -- MQ_TRANSPORT_PROCESS
    'ICHSIC Payment Flow',
    'BID',
    'ICHSIC',
    'Y',
    NOW(),
    NOW(),
    1,  -- PIXEL
    10485760  -- 10MB in bytes
)
ON CONFLICT (flow_code) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_flow_flow_id_seq', (SELECT COALESCE(MAX(flow_id), 1) FROM tib_audit_tec.ref_flow), true);

-- ========================================
-- 14. FLOW-PARTNER ASSOCIATIONS
-- ========================================

INSERT INTO tib_audit_tec.ref_flow_partner (
    partner_id,
    flow_id,
    transport_id,
    partner_direction,
    creation_dte,
    update_dte,
    rule_id,
    charset_encoding_id,
    enable_out,
    enable_bmsa
)
VALUES 
    -- CHBANK01 - INOUT with MQ transport
    (1, 1, 1, 'INOUT', NOW(), NOW(), 1, 1, 'Y', 'N'),
    -- CHSIC - IN with MQS transport 
    (2, 1, 2, 'IN', NOW(), NOW(), 1, 1, 'Y', 'N'),
    -- CHSIC - OUT with MQ transport 
    (2, 1, 1, 'OUT', NOW(), NOW(), 1, 1, 'Y', 'N'),
    -- CHSIC - OUT with KAFKA transport 
    (2, 1, 4, 'OUT', NOW(), NOW(), 1, 1, 'Y', 'N')
    -- DOME - OUT with CFT transport
--    (3, 1, 3, 'OUT', NOW(), NOW(), 2, 1, 'Y', 'N')
ON CONFLICT (partner_id, flow_id, rule_id, transport_id) DO NOTHING;

-- ========================================
-- 15. FLOW-COUNTRY ASSOCIATIONS
-- ========================================

INSERT INTO tib_audit_tec.ref_flow_country (
    flow_id,
    country_id
)
VALUES (1, 1)  -- ICHSIC flow with Switzerland
ON CONFLICT (flow_id, country_id) DO NOTHING;

-- ========================================
-- 16. FLOW RULES
-- ========================================

INSERT INTO tib_audit_tec.ref_flow_rules (
    flowcode,
    transporttype,
    isunitary,
    priority,
    urgency,
    flowcontrolledenabled,
    flowmaximum,
    flowretentionenabled,
    retentioncycleperiod,
    write_file,
    minrequiredfilesize,
    ignoreoutputdupcheck,
    logall
)
VALUES 
    ('ICHSIC', 'MQ', 'true', 1, 'HIGH', 'false', 100, 'true', 7, 'false', 1024, 'false', 'false')
ON CONFLICT (flowcode, transporttype) DO NOTHING;

INSERT INTO tib_audit_tec.ref_flow_rules (
    flowcode,
    transporttype,
    isunitary,
    priority,
    urgency,
    flowcontrolledenabled,
    flowmaximum,
    flowretentionenabled,
    retentioncycleperiod,
    write_file,
    minrequiredfilesize,
    ignoreoutputdupcheck,
    logall
)
VALUES 
    ('ICHSIC', 'KAFKA', 'true', 1, 'MEDIUM', 'false', 100, 'true', 7, 'false', 1024, 'false', 'false')
ON CONFLICT (flowcode, transporttype) DO NOTHING;

-- ========================================
-- 17. PROPERTY FLOW DEFINITIONS
-- ========================================

INSERT INTO tib_audit_tec.ref_prty_flow (prty_flow_id, prty_flow_name, prty_flow_desc, prty_flow_typ) 
VALUES
    (1, 'BIC', 'FP for CH SIC CLEARING Incoming payments', 'Enrichment'),
    (2, 'PcssA2Bank', 'FP for CH SIC CLEARING Incoming payments', 'Enrichment'),
    (3, 'XmlSchema', 'FP for CH SIC CLEARING Incoming payments', 'Validation'),
    (4, 'Application', 'FP for CH SIC CLEARING Incoming payments', 'Application')
ON CONFLICT (prty_flow_id) DO NOTHING;

-- Reset sequence to match actual data
SELECT setval('tib_audit_tec.ref_prty_flow_prty_flow_id_seq', (SELECT COALESCE(MAX(prty_flow_id), 1) FROM tib_audit_tec.ref_prty_flow), true);

-- ========================================
-- 18. FUNCTIONAL PROCESS PROPERTIES
-- ========================================

INSERT INTO tib_audit_tec.ref_func_process_prty (func_process_id, prty_flow_id, flow_prty_value) 
VALUES
    (1, 1, 'BPPBCHGGXXX'),
    (1, 2, 'P01'),
    (1, 3, 'pacs.008.001.08.ch.02.itl.v2.xsd'),
    (1, 4, 'ITL')
ON CONFLICT (func_process_id, prty_flow_id) DO NOTHING;

COMMIT;

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Comprehensive flow view with all relationships
SELECT 
    f.flow_id,
    f.flow_code,
    f.flow_name,
    f.flow_direction,
    f.enable_flg,
    ft.flow_typ_name,
    fp.func_process_name,
    tp.tech_process_name,
    app.application_name,
    f.max_file_size,
    f.creation_dte
FROM tib_audit_tec.ref_flow f
JOIN tib_audit_tec.ref_flow_typ ft ON f.flow_typ_id = ft.flow_typ_id
JOIN tib_audit_tec.ref_func_process fp ON f.func_process_id = fp.func_process_id
JOIN tib_audit_tec.ref_tech_process tp ON f.tech_process_id = tp.tech_process_id
JOIN tib_audit_tec.ref_application app ON f.application_id = app.application_id
WHERE f.flow_code = 'ICHSIC';

-- Flow-partner relationships with transport details
SELECT 
    f.flow_code,
    p.partner_code,
    p.partner_name,
    pt.partner_type_name,
    t.transport_typ,
    CASE 
        WHEN t.transport_typ IN ('MQ', 'MQS') THEN tmqs.mqs_q_name 
        WHEN t.transport_typ = 'CFT' THEN tcft.cft_idf 
        WHEN t.transport_typ = 'KAFKA' THEN tkafka.kafka_topic_name 
        ELSE 'N/A' 
    END as queue_name,
    CASE 
        WHEN t.transport_typ IN ('MQ', 'MQS') THEN tmqs.mqs_q_manager 
        WHEN t.transport_typ = 'CFT' THEN tcft.cft_partner_code 
        WHEN t.transport_typ = 'KAFKA' THEN tkafka.kafka_group_id 
        ELSE 'N/A' 
    END as manager_or_partner,
    ce.charset_code,
    rfp.partner_direction,
    rr.rule_name,
    rfp.enable_out,
    rfp.enable_bmsa
FROM tib_audit_tec.ref_flow_partner rfp
JOIN tib_audit_tec.ref_flow f ON rfp.flow_id = f.flow_id
JOIN tib_audit_tec.ref_partner p ON rfp.partner_id = p.partner_id
JOIN tib_audit_tec.ref_partner_typ pt ON p.partner_type_id = pt.partner_type_id
JOIN tib_audit_tec.ref_transport t ON rfp.transport_id = t.transport_id
JOIN tib_audit_tec.ref_charset_encoding ce ON rfp.charset_encoding_id = ce.charset_encoding_id
JOIN tib_audit_tec.ref_route_rule rr ON rfp.rule_id = rr.rule_id
LEFT JOIN tib_audit_tec.ref_transport_mqs tmqs ON t.transport_id = tmqs.transport_id
LEFT JOIN tib_audit_tec.ref_transport_cft tcft ON t.transport_id = tcft.transport_id
LEFT JOIN tib_audit_tec.ref_transport_kafka tkafka ON t.transport_id = tkafka.transport_id
WHERE f.flow_code = 'ICHSIC'
ORDER BY rfp.partner_direction, p.partner_code;

-- Flow-country relationships
SELECT 
    f.flow_code,
    c.country_name,
    c.country_iso_code,
    c.is_sepa,
    c.region
FROM tib_audit_tec.ref_flow_country fc
JOIN tib_audit_tec.ref_flow f ON fc.flow_id = f.flow_id
JOIN tib_audit_tec.ref_country c ON fc.country_id = c.country_id
WHERE f.flow_code = 'ICHSIC';

-- Flow rules
SELECT 
    flowcode,
    transporttype,
    isunitary,
    priority,
    urgency,
    flowcontrolledenabled,
    flowmaximum,
    flowretentionenabled,
    retentioncycleperiod,
    write_file,
    minrequiredfilesize,
    ignoreoutputdupcheck,
    logall
FROM tib_audit_tec.ref_flow_rules
WHERE flowcode = 'ICHSIC';

-- Functional properties for ICHSIC flow
SELECT 
    fp.func_process_name,
    rfpp.func_process_id,
    rfpp.prty_flow_id,
    rfpp.flow_prty_value,
    rpf.prty_flow_name,
    rpf.prty_flow_desc,
    rpf.prty_flow_typ
FROM tib_audit_tec.ref_func_process_prty rfpp
JOIN tib_audit_tec.ref_func_process fp ON fp.func_process_id = rfpp.func_process_id
JOIN tib_audit_tec.ref_prty_flow rpf ON rpf.prty_flow_id = rfpp.prty_flow_id
WHERE fp.func_process_name = 'ICHSIC_PROCESSING'
ORDER BY rfpp.prty_flow_id;

-- Transport configurations summary
SELECT 
    t.transport_id,
    t.transport_typ,
    COALESCE(tmqs.mqs_q_name, tcft.cft_idf, tkafka.kafka_topic_name, 'N/A') as identifier,
    COALESCE(tmqs.mqs_q_manager, tcft.cft_partner_code, tkafka.kafka_group_id, 'N/A') as manager_partner
FROM tib_audit_tec.ref_transport t
LEFT JOIN tib_audit_tec.ref_transport_mqs tmqs ON t.transport_id = tmqs.transport_id
LEFT JOIN tib_audit_tec.ref_transport_cft tcft ON t.transport_id = tcft.transport_id
LEFT JOIN tib_audit_tec.ref_transport_kafka tkafka ON t.transport_id = tkafka.transport_id
ORDER BY t.transport_id;

-- Data counts verification
SELECT 
    'ref_application' as table_name, COUNT(*) as record_count FROM tib_audit_tec.ref_application
UNION ALL
SELECT 'ref_flow_typ', COUNT(*) FROM tib_audit_tec.ref_flow_typ
UNION ALL
SELECT 'ref_partner_typ', COUNT(*) FROM tib_audit_tec.ref_partner_typ
UNION ALL
SELECT 'ref_country', COUNT(*) FROM tib_audit_tec.ref_country
UNION ALL
SELECT 'ref_charset_encoding', COUNT(*) FROM tib_audit_tec.ref_charset_encoding
UNION ALL
SELECT 'ref_route_rule', COUNT(*) FROM tib_audit_tec.ref_route_rule
UNION ALL
SELECT 'ref_func_process', COUNT(*) FROM tib_audit_tec.ref_func_process
UNION ALL
SELECT 'ref_tech_process', COUNT(*) FROM tib_audit_tec.ref_tech_process
UNION ALL
SELECT 'ref_transport', COUNT(*) FROM tib_audit_tec.ref_transport
UNION ALL
SELECT 'ref_partner', COUNT(*) FROM tib_audit_tec.ref_partner
UNION ALL
SELECT 'ref_transport_mqs', COUNT(*) FROM tib_audit_tec.ref_transport_mqs
UNION ALL
SELECT 'ref_transport_cft', COUNT(*) FROM tib_audit_tec.ref_transport_cft
UNION ALL
SELECT 'ref_transport_kafka', COUNT(*) FROM tib_audit_tec.ref_transport_kafka
UNION ALL
SELECT 'ref_flow', COUNT(*) FROM tib_audit_tec.ref_flow
UNION ALL
SELECT 'ref_flow_partner', COUNT(*) FROM tib_audit_tec.ref_flow_partner
UNION ALL
SELECT 'ref_flow_country', COUNT(*) FROM tib_audit_tec.ref_flow_country
UNION ALL
SELECT 'ref_flow_rules', COUNT(*) FROM tib_audit_tec.ref_flow_rules
UNION ALL
SELECT 'ref_prty_flow', COUNT(*) FROM tib_audit_tec.ref_prty_flow
UNION ALL
SELECT 'ref_func_process_prty', COUNT(*) FROM tib_audit_tec.ref_func_process_prty
ORDER BY table_name;

DO $$ 
BEGIN
    RAISE NOTICE '=== TIB_AUDIT_TEC Data Initialization Complete ===';
    RAISE NOTICE 'Generated from actual pixelv2 database data on 2026-01-05';
    RAISE NOTICE 'Flow: ICHSIC Payment Flow (BID)';
    RAISE NOTICE 'Partners: CHBANK01 (INOUT/MQ), CHSIC (IN/MQS), DOME (OUT/CFT)';
    RAISE NOTICE 'Country: Switzerland (CH)';
    RAISE NOTICE 'Application: PIXEL Integration Platform';
    RAISE NOTICE 'Transport Types: MQ, MQS, CFT, KAFKA with actual configurations';
    RAISE NOTICE '=== All sequences reset to match actual data ===';
END $$;