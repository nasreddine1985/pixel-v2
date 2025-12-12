-- Simple TIB_AUDIT_TEC Data Import Script
-- This script imports data into the TIB_AUDIT_TEC schema with correct table structures

BEGIN;

-- Disable constraints temporarily for faster inserts
SET session_replication_role = replica;

-- Clear existing data first
TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_PARTNER CASCADE;
TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_COUNTRY CASCADE;
TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_RULES CASCADE;
TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW CASCADE;
TRUNCATE TABLE TIB_AUDIT_TEC.REF_CHARSET_ENCODING CASCADE;

-- Insert sample character encoding data
INSERT INTO TIB_AUDIT_TEC.REF_CHARSET_ENCODING (CHARSET_ENCODING_ID, CHARSET_CODE, CHARSET_DESC) VALUES
(1, 'UTF-8', 'UTF-8 Unicode encoding'),
(2, 'ISO-8859-1', 'ISO Latin-1 encoding'),
(3, 'ASCII', 'ASCII character encoding'),
(4, 'UTF-16', 'UTF-16 Unicode encoding'),
(5, 'WINDOWS-1252', 'Windows Western European encoding');

-- Insert sample flow data (based on actual table structure)
INSERT INTO TIB_AUDIT_TEC.REF_FLOW (FLOW_ID,FUNC_PROCESS_ID,FLOW_TYP_ID,TECH_PROCESS_ID,FLOW_NAME,FLOW_DIRECTION,FLOW_CODE,ENABLE_FLG,CREATION_DTE,UPDATE_DTE,APPLICATION_ID,MAX_FILE_SIZE) VALUES
(1,1,1,1,'France IN SEPA SCT DS02/DS03','IN','IFRDS0203','Y',to_timestamp('07/03/15 09:20:25','DD/MM/RR HH24:MI:SS'),to_timestamp('21/11/15 00:22:46','DD/MM/RR HH24:MI:SS'),1,0),
(2,2,1,42,'France OUT SEPA SCT DS02/DS03','OUT','OFRDS0203','Y',to_timestamp('07/03/15 09:20:26','DD/MM/RR HH24:MI:SS'),NULL,1,0),
(3,3,1,136,'France IN SWIFT MT','IN','IFRSWIFT','Y',NULL,to_timestamp('18/07/18 19:57:50','DD/MM/RR HH24:MI:SS'),1,0),
(4,4,1,5,'France OUT SWIFT MT','OUT','OFRSWIFT','Y',NULL,NULL,1,0),
(5,5,1,136,'Czech Republic IN SWIFT MT','IN','ICZSWIFT','Y',NULL,to_timestamp('18/07/18 19:57:50','DD/MM/RR HH24:MI:SS'),1,0),
(6,1,1,42,'Czech Republic OUT SEPA SCT DS02/DS03','OUT','OCZDS0203','Y',NULL,to_timestamp('09/05/15 08:31:45','DD/MM/RR HH24:MI:SS'),1,0),
(7,6,3,6,'Czech Republic IN SWIFT ACK/NACK','IN','ICZSWACK','Y',NULL,NULL,1,0),
(8,5,1,5,'Czech Republic OUT SWIFT MT','OUT','OCZSWIFT','Y',NULL,NULL,1,0),
(10,7,4,7,'Czech Republic OUT CFT ACK IN SWIFT MT','OUT','OCZSWCFT1','Y',NULL,NULL,1,0),
(11,7,4,7,'Czech Republic OUT CFT ACK IN SWIFT ACK/NACK','OUT','OCZSWCFT3','Y',NULL,NULL,1,0),
(66,40,1,42,'Denmark OUT SEPA SCT DS02/DS03','OUT','ODKDS0203','Y',NULL,to_timestamp('09/05/15 08:31:45','DD/MM/RR HH24:MI:SS'),1,0),
(67,42,1,136,'Denmark IN SWIFT MT','IN','IDKSWIFT','Y',NULL,to_timestamp('10/05/16 20:13:21','DD/MM/RR HH24:MI:SS'),1,0),
(68,44,4,7,'Denmark OUT CFT ACK IN SWIFT MT','OUT','ODKSWCFT1','Y',NULL,NULL,1,0),
(69,42,1,5,'Denmark OUT SWIFT MT','OUT','ODKSWIFT','Y',NULL,NULL,1,0),
(70,44,4,7,'Denmark IN CFT ACK OUT SWIFT MT','IN','IDKSWCFT2','Y',NULL,NULL,1,0),
(71,45,1,136,'Switzerland IN SEPA SIC','IN','ICHSIC','Y',to_timestamp('15/06/20 10:30:00','DD/MM/RR HH24:MI:SS'),NULL,1,0),
(72,46,1,42,'Switzerland OUT SEPA SIC','OUT','OCHSIC','Y',to_timestamp('15/06/20 10:30:01','DD/MM/RR HH24:MI:SS'),NULL,1,0);

-- Insert sample flow rules data (based on actual table structure)
INSERT INTO TIB_AUDIT_TEC.REF_FLOW_RULES (FLOWCODE, TRANSPORTTYPE, ISUNITARY, PRIORITY, URGENCY, FLOWCONTROLLEDENABLED, FLOWMAXIMUM, FLOWRETENTIONENABLED, RETENTIONCYCLEPERIOD, WRITE_FILE, MINREQUIREDFILESIZE, IGNOREOUTPUTDUPCHECK, LOGALL) VALUES
('IFRDS0203', 'MQ', 'false', 'Medium', 'Medium', 'true', 1000, 'true', '30days', 'true', 1024, 'false', 'true'),
('OFRDS0203', 'MQ', 'false', 'Medium', 'Medium', 'true', 1000, 'true', '30days', 'true', 1024, 'false', 'true'),
('IFRSWIFT', 'CFT', 'true', 'High', 'Urgent', 'true', 500, 'true', '60days', 'true', 2048, 'false', 'true'),
('OFRSWIFT', 'CFT', 'true', 'High', 'Urgent', 'true', 500, 'true', '60days', 'true', 2048, 'false', 'true'),
('ICZSWIFT', 'CFT', 'true', 'High', 'Urgent', 'true', 500, 'true', '60days', 'true', 2048, 'false', 'true'),
('OCZDS0203', 'MQ', 'false', 'Medium', 'Medium', 'true', 1000, 'true', '30days', 'true', 1024, 'false', 'true'),
('ICZSWACK', 'CFT', 'true', 'Low', 'Low', 'false', 100, 'false', '7days', 'false', 512, 'true', 'false'),
('OCZSWIFT', 'CFT', 'true', 'High', 'Urgent', 'true', 500, 'true', '60days', 'true', 2048, 'false', 'true'),
('OCZSWCFT1', 'CFT', 'true', 'Medium', 'Medium', 'true', 200, 'true', '30days', 'true', 1024, 'false', 'true'),
('OCZSWCFT3', 'CFT', 'true', 'Medium', 'Medium', 'true', 200, 'true', '30days', 'true', 1024, 'false', 'true'),
('ICHSIC', 'MQ', 'false', 'High', 'Medium', 'true', 800, 'true', '45days', 'true', 1536, 'false', 'true'),
('OCHSIC', 'MQ', 'false', 'High', 'Medium', 'true', 800, 'true', '45days', 'true', 1536, 'false', 'true');

-- Insert sample flow country data (based on actual table structure - just flow_id and country_id)
INSERT INTO TIB_AUDIT_TEC.REF_FLOW_COUNTRY (FLOW_ID, COUNTRY_ID) VALUES
(1, 250),   -- France
(2, 250),   -- France
(3, 250),   -- France
(4, 250),   -- France
(5, 203),   -- Czech Republic
(6, 203),   -- Czech Republic
(7, 203),   -- Czech Republic
(8, 203),   -- Czech Republic
(10, 203),  -- Czech Republic
(11, 203),  -- Czech Republic
(66, 208),  -- Denmark
(67, 208),  -- Denmark
(68, 208),  -- Denmark
(69, 208),  -- Denmark
(70, 208),  -- Denmark
(71, 756),  -- Switzerland
(72, 756);  -- Switzerland

-- Insert sample flow partner data (based on actual table structure)
INSERT INTO TIB_AUDIT_TEC.REF_FLOW_PARTNER (PARTNER_ID, FLOW_ID, TRANSPORT_ID, PARTNER_DIRECTION, CREATION_DTE, UPDATE_DTE, RULE_ID, CHARSET_ENCODING_ID, ENABLE_OUT, ENABLE_BMSA) VALUES
(1, 1, 1, 'IN', NOW(), NOW(), 1, 1, 'Y', 'N'),
(2, 2, 1, 'OUT', NOW(), NOW(), 2, 1, 'Y', 'N'),
(3, 3, 2, 'IN', NOW(), NOW(), 3, 1, 'Y', 'N'),
(4, 4, 2, 'OUT', NOW(), NOW(), 4, 1, 'Y', 'N'),
(5, 5, 2, 'IN', NOW(), NOW(), 5, 1, 'Y', 'N'),
(6, 6, 1, 'OUT', NOW(), NOW(), 6, 1, 'Y', 'N'),
(7, 7, 2, 'IN', NOW(), NOW(), 7, 1, 'Y', 'N'),
(8, 8, 2, 'OUT', NOW(), NOW(), 8, 1, 'Y', 'N'),
(10, 10, 2, 'OUT', NOW(), NOW(), 9, 1, 'Y', 'N'),
(11, 11, 2, 'OUT', NOW(), NOW(), 10, 1, 'Y', 'N'),
(12, 71, 3, 'IN', NOW(), NOW(), 11, 1, 'Y', 'N'),
(13, 72, 3, 'OUT', NOW(), NOW(), 12, 1, 'Y', 'N');

-- Re-enable constraints
SET session_replication_role = DEFAULT;

-- Display import statistics
DO $$
DECLARE
    charset_count INTEGER;
    flow_count INTEGER;
    rules_count INTEGER;
    country_count INTEGER;
    partner_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO charset_count FROM TIB_AUDIT_TEC.REF_CHARSET_ENCODING;
    SELECT COUNT(*) INTO flow_count FROM TIB_AUDIT_TEC.REF_FLOW;
    SELECT COUNT(*) INTO rules_count FROM TIB_AUDIT_TEC.REF_FLOW_RULES;
    SELECT COUNT(*) INTO country_count FROM TIB_AUDIT_TEC.REF_FLOW_COUNTRY;
    SELECT COUNT(*) INTO partner_count FROM TIB_AUDIT_TEC.REF_FLOW_PARTNER;
    
    RAISE NOTICE 'TIB_AUDIT_TEC Data Import Complete!';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Records imported per table:';
    RAISE NOTICE '  - REF_CHARSET_ENCODING: % records', charset_count;
    RAISE NOTICE '  - REF_FLOW: % records', flow_count;
    RAISE NOTICE '  - REF_FLOW_RULES: % records', rules_count;
    RAISE NOTICE '  - REF_FLOW_COUNTRY: % records', country_count;
    RAISE NOTICE '  - REF_FLOW_PARTNER: % records', partner_count;
    RAISE NOTICE 'Total records imported: %', (charset_count + flow_count + rules_count + country_count + partner_count);
END $$;

COMMIT;