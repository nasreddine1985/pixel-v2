-- TIB_AUDIT_TEC Schema Initialization Script
-- This script creates the TIB_AUDIT_TEC schema and referential tables based on FLOW.sql analysis
-- Date: 11 December 2025

-- Create TIB_AUDIT_TEC schema
CREATE SCHEMA IF NOT EXISTS TIB_AUDIT_TEC;

-- Grant permissions to pixelv2 user
GRANT ALL PRIVILEGES ON SCHEMA TIB_AUDIT_TEC TO pixelv2;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA TIB_AUDIT_TEC TO pixelv2;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA TIB_AUDIT_TEC TO pixelv2;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA TIB_AUDIT_TEC GRANT ALL ON TABLES TO pixelv2;
ALTER DEFAULT PRIVILEGES IN SCHEMA TIB_AUDIT_TEC GRANT ALL ON SEQUENCES TO pixelv2;

-- Create REF_CHARSET_ENCODING table
CREATE TABLE IF NOT EXISTS TIB_AUDIT_TEC.REF_CHARSET_ENCODING (
    CHARSET_ENCODING_ID NUMERIC(10) PRIMARY KEY,
    CHARSET_CODE VARCHAR(50) NOT NULL,
    CHARSET_DESC VARCHAR(255),
    
    -- Constraints
    CONSTRAINT uk_charset_code UNIQUE (CHARSET_CODE)
);

-- Create REF_FLOW table (main flow configuration table)
CREATE TABLE IF NOT EXISTS TIB_AUDIT_TEC.REF_FLOW (
    FLOW_ID NUMERIC(10) PRIMARY KEY,
    FUNC_PROCESS_ID NUMERIC(10),
    FLOW_TYP_ID NUMERIC(10),
    TECH_PROCESS_ID NUMERIC(10),
    FLOW_NAME VARCHAR(255),
    FLOW_DIRECTION VARCHAR(10) CHECK (FLOW_DIRECTION IN ('IN', 'OUT')),
    FLOW_CODE VARCHAR(50) NOT NULL,
    ENABLE_FLG VARCHAR(1) DEFAULT 'Y' CHECK (ENABLE_FLG IN ('Y', 'N')),
    CREATION_DTE TIMESTAMP,
    UPDATE_DTE TIMESTAMP,
    APPLICATION_ID NUMERIC(10),
    MAX_FILE_SIZE NUMERIC(15) DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uk_flow_code UNIQUE (FLOW_CODE)
);

-- Create REF_FLOW_RULES table (flow processing rules)
CREATE TABLE IF NOT EXISTS TIB_AUDIT_TEC.REF_FLOW_RULES (
    FLOWCODE VARCHAR(50) PRIMARY KEY,
    TRANSPORTTYPE VARCHAR(20) CHECK (TRANSPORTTYPE IN ('MQ', 'CFT')),
    ISUNITARY VARCHAR(10) CHECK (ISUNITARY IN ('true', 'false')),
    PRIORITY VARCHAR(10),
    URGENCY VARCHAR(20) CHECK (URGENCY IN ('Low', 'Medium', 'Urgent')),
    FLOWCONTROLLEDENABLED VARCHAR(10) CHECK (FLOWCONTROLLEDENABLED IN ('true', 'false')),
    FLOWMAXIMUM NUMERIC(10),
    FLOWRETENTIONENABLED VARCHAR(10) CHECK (FLOWRETENTIONENABLED IN ('true', 'false')),
    RETENTIONCYCLEPERIOD VARCHAR(50),
    WRITE_FILE VARCHAR(10) CHECK (WRITE_FILE IN ('true', 'false')),
    MINREQUIREDFILESIZE NUMERIC(15),
    IGNOREOUTPUTDUPCHECK VARCHAR(10) CHECK (IGNOREOUTPUTDUPCHECK IN ('true', 'false')),
    LOGALL VARCHAR(10) CHECK (LOGALL IN ('true', 'false')),
    
    -- Foreign key constraint
    CONSTRAINT fk_flow_rules_code FOREIGN KEY (FLOWCODE) REFERENCES TIB_AUDIT_TEC.REF_FLOW(FLOW_CODE)
);

-- Create REF_FLOW_COUNTRY table (flow-country mapping)
CREATE TABLE IF NOT EXISTS TIB_AUDIT_TEC.REF_FLOW_COUNTRY (
    FLOW_ID NUMERIC(10),
    COUNTRY_ID NUMERIC(10),
    
    -- Composite primary key
    PRIMARY KEY (FLOW_ID, COUNTRY_ID),
    
    -- Foreign key constraint
    CONSTRAINT fk_flow_country_flow FOREIGN KEY (FLOW_ID) REFERENCES TIB_AUDIT_TEC.REF_FLOW(FLOW_ID)
);

-- Create REF_FLOW_PARTNER table (partner configuration)
CREATE TABLE IF NOT EXISTS TIB_AUDIT_TEC.REF_FLOW_PARTNER (
    PARTNER_ID NUMERIC(10),
    FLOW_ID NUMERIC(10),
    TRANSPORT_ID NUMERIC(10),
    PARTNER_DIRECTION VARCHAR(10) CHECK (PARTNER_DIRECTION IN ('IN', 'OUT')),
    CREATION_DTE TIMESTAMP,
    UPDATE_DTE TIMESTAMP,
    RULE_ID NUMERIC(10),
    CHARSET_ENCODING_ID NUMERIC(10),
    ENABLE_OUT VARCHAR(1) DEFAULT 'Y' CHECK (ENABLE_OUT IN ('Y', 'N')),
    ENABLE_BMSA VARCHAR(1) DEFAULT 'N' CHECK (ENABLE_BMSA IN ('Y', 'N')),
    
    -- Composite primary key
    PRIMARY KEY (PARTNER_ID, FLOW_ID, TRANSPORT_ID),
    
    -- Foreign key constraints
    CONSTRAINT fk_flow_partner_flow FOREIGN KEY (FLOW_ID) REFERENCES TIB_AUDIT_TEC.REF_FLOW(FLOW_ID),
    CONSTRAINT fk_flow_partner_charset FOREIGN KEY (CHARSET_ENCODING_ID) REFERENCES TIB_AUDIT_TEC.REF_CHARSET_ENCODING(CHARSET_ENCODING_ID)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_ref_flow_direction ON TIB_AUDIT_TEC.REF_FLOW(FLOW_DIRECTION);
CREATE INDEX IF NOT EXISTS idx_ref_flow_enable ON TIB_AUDIT_TEC.REF_FLOW(ENABLE_FLG);
CREATE INDEX IF NOT EXISTS idx_ref_flow_app_id ON TIB_AUDIT_TEC.REF_FLOW(APPLICATION_ID);
CREATE INDEX IF NOT EXISTS idx_ref_flow_country_country ON TIB_AUDIT_TEC.REF_FLOW_COUNTRY(COUNTRY_ID);
CREATE INDEX IF NOT EXISTS idx_ref_flow_partner_direction ON TIB_AUDIT_TEC.REF_FLOW_PARTNER(PARTNER_DIRECTION);
CREATE INDEX IF NOT EXISTS idx_ref_flow_partner_rule ON TIB_AUDIT_TEC.REF_FLOW_PARTNER(RULE_ID);
CREATE INDEX IF NOT EXISTS idx_ref_flow_rules_transport ON TIB_AUDIT_TEC.REF_FLOW_RULES(TRANSPORTTYPE);
CREATE INDEX IF NOT EXISTS idx_ref_flow_rules_priority ON TIB_AUDIT_TEC.REF_FLOW_RULES(PRIORITY);

-- Create function to update timestamp on record modification for TIB_AUDIT_TEC schema
CREATE OR REPLACE FUNCTION TIB_AUDIT_TEC.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.UPDATE_DTE = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update UPDATE_DTE columns
CREATE TRIGGER update_ref_flow_updated_at
    BEFORE UPDATE ON TIB_AUDIT_TEC.REF_FLOW
    FOR EACH ROW EXECUTE FUNCTION TIB_AUDIT_TEC.update_updated_at_column();

CREATE TRIGGER update_ref_flow_partner_updated_at
    BEFORE UPDATE ON TIB_AUDIT_TEC.REF_FLOW_PARTNER
    FOR EACH ROW EXECUTE FUNCTION TIB_AUDIT_TEC.update_updated_at_column();

-- Grant execute permissions on functions
GRANT EXECUTE ON FUNCTION TIB_AUDIT_TEC.update_updated_at_column() TO pixelv2;

-- Add comments to tables for documentation
COMMENT ON SCHEMA TIB_AUDIT_TEC IS 'Technical audit schema for PIXEL-V2 referential data management';
COMMENT ON TABLE TIB_AUDIT_TEC.REF_CHARSET_ENCODING IS 'Character encoding definitions for file processing';
COMMENT ON TABLE TIB_AUDIT_TEC.REF_FLOW IS 'Main flow configuration and metadata for payment processing flows';
COMMENT ON TABLE TIB_AUDIT_TEC.REF_FLOW_RULES IS 'Flow processing rules and configuration parameters';
COMMENT ON TABLE TIB_AUDIT_TEC.REF_FLOW_COUNTRY IS 'Junction table linking flows to specific countries';
COMMENT ON TABLE TIB_AUDIT_TEC.REF_FLOW_PARTNER IS 'Partner configuration for flows including transport and encoding settings';

-- Add column comments for key fields
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW.FLOW_CODE IS 'Unique flow identifier code (e.g., IPLLOCAL, ODKBECICFP8)';
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW.FLOW_DIRECTION IS 'Flow direction: IN for incoming, OUT for outgoing';
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW.ENABLE_FLG IS 'Enable/disable flag: Y for enabled, N for disabled';
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW_RULES.TRANSPORTTYPE IS 'Transport mechanism: MQ for Message Queue, CFT for file transfer';
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW_RULES.URGENCY IS 'Processing urgency level: Low, Medium, or Urgent';
COMMENT ON COLUMN TIB_AUDIT_TEC.REF_FLOW_COUNTRY.COUNTRY_ID IS 'ISO numeric country code identifier';

-- Insert referential data directly (sample from FLOW.sql)
-- Character encodings
INSERT INTO TIB_AUDIT_TEC.REF_CHARSET_ENCODING (CHARSET_ENCODING_ID, CHARSET_CODE, CHARSET_DESC) VALUES
(1, 'UTF8', 'Eight-bit UCS Transformation Format'),
(2, 'CP852', 'MS-DOS Latin-2'),
(3, 'ISO-8859-2', 'Latin Alphabet No. 2'),
(4, 'ASCII', 'American Standard Code for Information Interchange'),
(5, 'CP1250', 'Windows Eastern European'),
(6, 'ISO-8859-1', 'Latin Alphabet No. 1'),
(7, 'CP1047', 'Latin-1 character set for EBCDIC hosts'),
(8, 'NA', 'Not applicable'),
(9, 'ISO-8859-5', 'Latin/Cyrillic Alphabet No. 5 (Russia and the countries formerly included into URSS)'),
(10, 'CP1251', 'Code Page for Windows Latin-5 (Russia and the countries formerly included into URSS)'),
(11, 'UTF-8', 'UTF-8 without a CRLF at the end of file')
ON CONFLICT (CHARSET_ENCODING_ID) DO NOTHING;

-- Note: For complete data import from FLOW.sql, run:
-- psql -U pixelv2 -d pixelv2 -f /path/to/processed-flow-data.sql

-- Display setup completion message with data import statistics
DO $$
DECLARE
    charset_count INTEGER;
    flow_count INTEGER;
    rules_count INTEGER;
    country_count INTEGER;
    partner_count INTEGER;
    total_count INTEGER;
BEGIN
    -- Get record counts
    SELECT COUNT(*) INTO charset_count FROM TIB_AUDIT_TEC.REF_CHARSET_ENCODING;
    SELECT COUNT(*) INTO flow_count FROM TIB_AUDIT_TEC.REF_FLOW;
    SELECT COUNT(*) INTO rules_count FROM TIB_AUDIT_TEC.REF_FLOW_RULES;
    SELECT COUNT(*) INTO country_count FROM TIB_AUDIT_TEC.REF_FLOW_COUNTRY;
    SELECT COUNT(*) INTO partner_count FROM TIB_AUDIT_TEC.REF_FLOW_PARTNER;
    
    total_count := charset_count + flow_count + rules_count + country_count + partner_count;
    
    RAISE NOTICE 'TIB_AUDIT_TEC schema initialization and data import completed successfully!';
    RAISE NOTICE 'Schema: TIB_AUDIT_TEC';
    RAISE NOTICE 'Tables created: REF_CHARSET_ENCODING, REF_FLOW, REF_FLOW_RULES, REF_FLOW_COUNTRY, REF_FLOW_PARTNER';
    RAISE NOTICE 'Data import statistics:';
    RAISE NOTICE '  - REF_CHARSET_ENCODING: % records', charset_count;
    RAISE NOTICE '  - REF_FLOW: % records', flow_count;
    RAISE NOTICE '  - REF_FLOW_RULES: % records', rules_count;
    RAISE NOTICE '  - REF_FLOW_COUNTRY: % records', country_count;
    RAISE NOTICE '  - REF_FLOW_PARTNER: % records', partner_count;
    RAISE NOTICE 'Total referential records imported: %', total_count;
END $$;