-- ========================================
-- Sample Data Insert Script for TIB_AUDIT_TEC
-- Flow: OCHSIC with MQ Transport
-- ========================================

-- ========================================
-- GENERAL SAMPLE DATA INSERTS
-- ========================================

BEGIN;

-- 1. Sample Applications
INSERT INTO tib_audit_tec.ref_application (application_code, application_name) 
VALUES ('PIXEL', 'Pixel Integration Platform')
ON CONFLICT (application_code) DO NOTHING;

-- 2. Sample Flow Types
INSERT INTO tib_audit_tec.ref_flow_typ (flow_typ_name) 
VALUES 
    ('Payment Processing'),
    ('Data Synchronization'),
    ('Notification Service')
ON CONFLICT (flow_typ_name) DO NOTHING;

-- 3. Sample Partner Types
INSERT INTO tib_audit_tec.ref_partner_typ (partner_type_name)
VALUES 
    ('Internal System'),
    ('External Bank'),
    ('Third Party Service'),
    ('Government Agency'),
    ('Internal Partner')
ON CONFLICT (partner_type_name) DO NOTHING;

-- 4. Sample Countries
INSERT INTO tib_audit_tec.ref_country (country_name, country_iso_code, is_sepa, region)
VALUES 
    ('Switzerland', 'CH', 'Y', 'EMEA'),
    ('Germany', 'DE', 'Y', 'EMEA'),
    ('United States', 'US', 'N', 'AMER'),
    ('United Kingdom', 'GB', 'N', 'EMEA')
ON CONFLICT (country_iso_code) DO NOTHING;

-- 5. Sample Character Encodings
INSERT INTO tib_audit_tec.ref_charset_encoding (charset_code, charset_desc)
VALUES 
    ('UTF-8', 'Unicode UTF-8 encoding'),
    ('ISO-8859-1', 'Latin-1 Western European'),
    ('UTF-16', 'Unicode UTF-16 encoding')
ON CONFLICT (charset_code) DO NOTHING;

-- 6. Sample Route Rules
INSERT INTO tib_audit_tec.ref_route_rule (rule_name)
VALUES 
    ('DEFAULT_ROUTING'),
    ('CHIPS_RULE')
ON CONFLICT (rule_name) DO NOTHING;

-- ========================================
-- OCHSIC SPECIFIC SAMPLE DATA INSERTS
-- ========================================

-- 7. Insert Functional Process
INSERT INTO tib_audit_tec.ref_func_process (func_process_name, creation_dte, update_dte)
VALUES ('OCHSIC_PROCESSING', NOW(), NOW())
ON CONFLICT (func_process_name) DO NOTHING;

-- 8. Insert Technical Process
INSERT INTO tib_audit_tec.ref_tech_process (tech_process_name, creation_dte, update_dte)
VALUES ('MQ_TRANSPORT_PROCESS', NOW(), NOW())
ON CONFLICT (tech_process_name) DO NOTHING;

-- 9. Insert Transport (MQ, MQS, CFT)
INSERT INTO tib_audit_tec.ref_transport (transport_typ)
SELECT 'MQ' WHERE NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport WHERE transport_typ = 'MQ')
UNION ALL
SELECT 'MQS' WHERE NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport WHERE transport_typ = 'MQS')
UNION ALL  
SELECT 'CFT' WHERE NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport WHERE transport_typ = 'CFT');

-- Get the transport_id for MQ
-- Note: In a real scenario, you might want to store this in a variable

-- 10. Insert Partners (CHBANK01, CHSIC, DOME)
INSERT INTO tib_audit_tec.ref_partner (
    partner_type_id, 
    partner_name, 
    partner_code, 
    creation_dte, 
    update_dte
)
SELECT 
    pt.partner_type_id,
    'Swiss Bank Partner',
    'CHBANK01',
    NOW(),
    NOW()
FROM tib_audit_tec.ref_partner_typ pt
WHERE pt.partner_type_name = 'External Bank'
ON CONFLICT (partner_code) DO NOTHING;

-- Insert CHSIC Partner (IN direction)
INSERT INTO tib_audit_tec.ref_partner (
    partner_type_id, 
    partner_name, 
    partner_code, 
    creation_dte, 
    update_dte
)
SELECT 
    pt.partner_type_id,
    'CH - Switzerland ACH Clearing',
    'CHSIC',
    NOW(),
    NOW()
FROM tib_audit_tec.ref_partner_typ pt
WHERE pt.partner_type_name = 'Internal Partner'
ON CONFLICT (partner_code) DO NOTHING;

-- Insert DOME Partner (OUT direction)
INSERT INTO tib_audit_tec.ref_partner (
    partner_type_id, 
    partner_name, 
    partner_code, 
    creation_dte, 
    update_dte
)
SELECT 
    pt.partner_type_id,
    'Domestic payment engine',
    'DOME',
    NOW(),
    NOW()
FROM tib_audit_tec.ref_partner_typ pt
WHERE pt.partner_type_name = 'Internal Partner'
ON CONFLICT (partner_code) DO NOTHING;

-- 11. Insert Transport Configurations
-- MQ Transport Specific Configuration (original)
INSERT INTO tib_audit_tec.ref_transport_mqs (
    transport_id,
    mqs_q_name,
    mqs_q_manager
)
SELECT 
    t.transport_id,
    'OCHSIC.QUEUE',
    'QM_PIXEL_V2'
FROM tib_audit_tec.ref_transport t
WHERE t.transport_typ = 'MQ'
  AND NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport_mqs WHERE transport_id = t.transport_id);

-- MQS Transport Configuration for CHSIC
INSERT INTO tib_audit_tec.ref_transport_mqs (
    transport_id,
    mqs_q_name,
    mqs_q_manager
)
SELECT 
    t.transport_id,
    'OITARTMI01',
    'FRITL01Z'
FROM tib_audit_tec.ref_transport t
WHERE t.transport_typ = 'MQS'
  AND NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport_mqs WHERE transport_id = t.transport_id);

-- CFT Transport Configuration for DOME
INSERT INTO tib_audit_tec.ref_transport_cft (
    transport_id,
    cft_idf,
    cft_partner_code
)
SELECT 
    t.transport_id,
    'DOME_CFT_IDF',
    'DOME'
FROM tib_audit_tec.ref_transport t
WHERE t.transport_typ = 'CFT'
  AND NOT EXISTS (SELECT 1 FROM tib_audit_tec.ref_transport_cft WHERE transport_id = t.transport_id);

-- Note: CFT transport uses its own configuration table, not MQS
-- Mixed transport configurations are handled at the application level

-- 12. Insert the main Flow record
INSERT INTO tib_audit_tec.ref_flow (
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
SELECT 
    fp.func_process_id,
    ft.flow_typ_id,
    tp.tech_process_id,
    'OCHSIC Payment Flow',
    'BID',
    'OCHSIC',
    'Y',
    NOW(),
    NOW(),
    app.application_id,
    10485760  -- 10MB in bytes
FROM tib_audit_tec.ref_func_process fp
CROSS JOIN tib_audit_tec.ref_flow_typ ft
CROSS JOIN tib_audit_tec.ref_tech_process tp
CROSS JOIN tib_audit_tec.ref_application app
WHERE fp.func_process_name = 'OCHSIC_PROCESSING'
  AND ft.flow_typ_name = 'Payment Processing'
  AND tp.tech_process_name = 'MQ_TRANSPORT_PROCESS'
  AND app.application_code = 'PIXEL'
ON CONFLICT (flow_code) DO NOTHING;

-- 13. Insert Flow-Partner Associations
-- Original CHBANK01 partner
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
SELECT 
    p.partner_id,
    f.flow_id,
    t.transport_id,
    'INOUT',
    NOW(),
    NOW(),
    rr.rule_id,
    ce.charset_encoding_id,
    'Y',
    'N'
FROM tib_audit_tec.ref_partner p
CROSS JOIN tib_audit_tec.ref_flow f
CROSS JOIN tib_audit_tec.ref_transport t
CROSS JOIN tib_audit_tec.ref_route_rule rr
CROSS JOIN tib_audit_tec.ref_charset_encoding ce
WHERE p.partner_code = 'CHBANK01'
  AND f.flow_code = 'OCHSIC'
  AND t.transport_typ = 'MQ'
  AND rr.rule_name = 'DEFAULT_ROUTING'
  AND ce.charset_code = 'UTF-8'
ON CONFLICT (partner_id, flow_id, rule_id, transport_id) DO NOTHING;

-- CHSIC IN Partner Association
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
SELECT 
    p.partner_id,
    f.flow_id,
    t.transport_id,
    'IN',
    NOW(),
    NOW(),
    rr.rule_id,
    ce.charset_encoding_id,
    'Y',
    'N'
FROM tib_audit_tec.ref_partner p
CROSS JOIN tib_audit_tec.ref_flow f
CROSS JOIN tib_audit_tec.ref_transport t
CROSS JOIN tib_audit_tec.ref_route_rule rr
CROSS JOIN tib_audit_tec.ref_charset_encoding ce
WHERE p.partner_code = 'CHSIC'
  AND f.flow_code = 'OCHSIC'
  AND t.transport_typ = 'MQS'
  AND rr.rule_name = 'DEFAULT_ROUTING'
  AND ce.charset_code = 'UTF-8'
ON CONFLICT (partner_id, flow_id, rule_id, transport_id) DO NOTHING;

-- DOME OUT Partner Association
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
SELECT 
    p.partner_id,
    f.flow_id,
    t.transport_id,
    'OUT',
    NOW(),
    NOW(),
    rr.rule_id,
    ce.charset_encoding_id,
    'Y',
    'N'
FROM tib_audit_tec.ref_partner p
CROSS JOIN tib_audit_tec.ref_flow f
CROSS JOIN tib_audit_tec.ref_transport t
CROSS JOIN tib_audit_tec.ref_route_rule rr
CROSS JOIN tib_audit_tec.ref_charset_encoding ce
WHERE p.partner_code = 'DOME'
  AND f.flow_code = 'OCHSIC'
  AND t.transport_typ = 'CFT'
  AND rr.rule_name = 'CHIPS_RULE'
  AND ce.charset_code = 'UTF-8'
ON CONFLICT (partner_id, flow_id, rule_id, transport_id) DO NOTHING;

-- 14. Insert Flow-Country Association
INSERT INTO tib_audit_tec.ref_flow_country (
    flow_id,
    country_id
)
SELECT 
    f.flow_id,
    c.country_id
FROM tib_audit_tec.ref_flow f
CROSS JOIN tib_audit_tec.ref_country c
WHERE f.flow_code = 'OCHSIC'
  AND c.country_iso_code = 'CH'
ON CONFLICT (flow_id, country_id) DO NOTHING;

-- 15. Insert Flow Rules
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
VALUES (
    'OCHSIC',
    'MQ',
    'true',
    1,
    'HIGH',
    'false',
    100,
    'true',
    7,
    'false',
    1024,
    'false',
    'false'
)
ON CONFLICT (flowcode) DO NOTHING;

-- 16. Insert Functional Properties for OCHSIC flow
-- Insert property flow definitions
INSERT INTO tib_audit_tec.ref_prty_flow (prty_flow_id, prty_flow_name, prty_flow_desc, prty_flow_typ) VALUES
(1, 'BIC', 'FP for CH SIC CLEARING Incoming payments', 'Enrichment'),
(2, 'PcssA2Bank', 'FP for CH SIC CLEARING Incoming payments', 'Enrichment'),
(3, 'XmlSchema', 'FP for CH SIC CLEARING Incoming payments', 'Validation'),
(4, 'Application', 'FP for CH SIC CLEARING Incoming payments', 'Application')
ON CONFLICT (prty_flow_id) DO NOTHING;

-- 17. Insert functional process properties with values for OCHSIC flow
INSERT INTO tib_audit_tec.ref_func_process_prty (func_process_id, prty_flow_id, flow_prty_value) VALUES
(1, 1, 'BPPBCHGGXXX'),
(1, 2, 'P01'),
(1, 3, 'pacs.008.001.08.ch.02.itl.v2.xsd'),
(1, 4, 'ITL')
ON CONFLICT (func_process_id, prty_flow_id) DO NOTHING;

COMMIT;

-- ========================================
-- Verification Queries
-- ========================================

-- Show the created flow with all its relationships
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
WHERE f.flow_code = 'OCHSIC';

-- Show flow-partner relationships (enhanced)
SELECT 
    f.flow_code,
    p.partner_code,
    p.partner_name,
    pt.partner_type_name,
    t.transport_typ,
    ce.charset_code,
    fp.partner_direction,
    rr.rule_name,
    fp.enable_out,
    fp.enable_bmsa
FROM tib_audit_tec.ref_flow_partner fp
JOIN tib_audit_tec.ref_flow f ON fp.flow_id = f.flow_id
JOIN tib_audit_tec.ref_partner p ON fp.partner_id = p.partner_id
JOIN tib_audit_tec.ref_partner_typ pt ON p.partner_type_id = pt.partner_type_id
JOIN tib_audit_tec.ref_transport t ON fp.transport_id = t.transport_id
JOIN tib_audit_tec.ref_charset_encoding ce ON fp.charset_encoding_id = ce.charset_encoding_id
JOIN tib_audit_tec.ref_route_rule rr ON fp.rule_id = rr.rule_id
WHERE f.flow_code = 'OCHSIC'
ORDER BY fp.partner_direction, p.partner_code;

-- Show flow-country relationships
SELECT 
    f.flow_code,
    c.country_name,
    c.country_iso_code,
    c.is_sepa,
    c.region
FROM tib_audit_tec.ref_flow_country fc
JOIN tib_audit_tec.ref_flow f ON fc.flow_id = f.flow_id
JOIN tib_audit_tec.ref_country c ON fc.country_id = c.country_id
WHERE f.flow_code = 'OCHSIC';

-- Show flow rules
SELECT 
    flowcode,
    transporttype,
    isunitary,
    priority,
    urgency,
    flowcontrolledenabled,
    flowmaximum,
    flowretentionenabled,
    retentioncycleperiod
FROM tib_audit_tec.ref_flow_rules
WHERE flowcode = 'OCHSIC';

-- Show MQ transport configuration
SELECT 
    t.transport_typ,
    mqs.mqs_q_name,
    mqs.mqs_q_manager
FROM tib_audit_tec.ref_transport_mqs mqs
JOIN tib_audit_tec.ref_transport t ON mqs.transport_id = t.transport_id
WHERE t.transport_typ = 'MQ';

-- Show all transport configurations (enhanced)
SELECT 
    t.transport_typ,
    tmqs.mqs_q_name,
    tmqs.mqs_q_manager,
    'MQS Config' as config_type
FROM tib_audit_tec.ref_transport_mqs tmqs
JOIN tib_audit_tec.ref_transport t ON tmqs.transport_id = t.transport_id
UNION ALL
SELECT 
    t.transport_typ,
    tcft.cft_idf as mqs_q_name,
    tcft.cft_part_code as mqs_q_manager,
    'CFT Config' as config_type
FROM tib_audit_tec.ref_transport_cft tcft
JOIN tib_audit_tec.ref_transport t ON tcft.transport_id = t.transport_id;

-- Show functional properties for OCHSIC flow
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
WHERE fp.func_process_name = 'OCHSIC_PROCESSING'
ORDER BY rfpp.prty_flow_id;

RAISE NOTICE 'Sample data for OCHSIC flow inserted successfully!';
RAISE NOTICE 'Flow Code: OCHSIC';
RAISE NOTICE 'Transport: MQ';
RAISE NOTICE 'Country: Switzerland (CH)';
RAISE NOTICE 'Charset: UTF-8';
RAISE NOTICE 'Partner Type: External Bank';
RAISE NOTICE 'Flow Type: Payment Processing';
RAISE NOTICE 'CHSIC (IN): MQS Transport - Queue: OITARTMI01, Manager: FRITL01Z';
RAISE NOTICE 'DOME (OUT): CFT Transport with MQS - Queue: DOMCHI02MQS, Manager: CHIPS_MGR';