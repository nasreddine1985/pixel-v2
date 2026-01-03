-- ========================================
-- TIB_AUDIT_TEC PostgreSQL Schema DDL
-- Converted from Oracle SQL DDL
-- Created: January 2, 2026
-- ========================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS tib_audit_tec;

-- ========================================
-- 1. REFERENCE TABLES (LOOKUP DATA)
-- ========================================

-- Applications
CREATE TABLE tib_audit_tec.ref_application (
    application_id BIGSERIAL PRIMARY KEY,
    application_code VARCHAR(10) UNIQUE NOT NULL,
    application_name VARCHAR(100) NOT NULL
);

-- Countries
CREATE TABLE tib_audit_tec.ref_country (
    country_id BIGSERIAL PRIMARY KEY,
    country_name VARCHAR(50) NOT NULL,
    country_iso_code VARCHAR(2) UNIQUE NOT NULL,
    is_sepa VARCHAR(1),
    region VARCHAR(5)
);

-- Character Set Encodings
CREATE TABLE tib_audit_tec.ref_charset_encoding (
    charset_encoding_id BIGSERIAL PRIMARY KEY,
    charset_code VARCHAR(30) UNIQUE NOT NULL,
    charset_desc VARCHAR(100)
);

-- Flow Types
CREATE TABLE tib_audit_tec.ref_flow_typ (
    flow_typ_id BIGSERIAL PRIMARY KEY,
    flow_typ_name VARCHAR(100) UNIQUE NOT NULL
);

-- Partner Types
CREATE TABLE tib_audit_tec.ref_partner_typ (
    partner_type_id BIGSERIAL PRIMARY KEY,
    partner_type_name VARCHAR(100) UNIQUE NOT NULL
);

-- Property Flow Types
CREATE TABLE tib_audit_tec.ref_prty_flow (
    prty_flow_id BIGSERIAL PRIMARY KEY,
    prty_flow_name VARCHAR(100) NOT NULL,
    prty_flow_desc VARCHAR(100) NOT NULL,
    prty_flow_typ VARCHAR(20) NOT NULL
);

-- Set Replacement Types
CREATE TABLE tib_audit_tec.ref_set_replacement (
    set_replacement_id BIGSERIAL PRIMARY KEY,
    set_replacement_name VARCHAR(50) UNIQUE NOT NULL,
    set_replacement_desc VARCHAR(100)
);

-- Route Rules
CREATE TABLE tib_audit_tec.ref_route_rule (
    rule_id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) UNIQUE NOT NULL
);

-- ========================================
-- 2. PROCESS DEFINITION TABLES
-- ========================================

-- Functional Processes
CREATE TABLE tib_audit_tec.ref_func_process (
    func_process_id BIGSERIAL PRIMARY KEY,
    func_process_name VARCHAR(50) UNIQUE NOT NULL,
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6)
);

-- Technical Processes
CREATE TABLE tib_audit_tec.ref_tech_process (
    tech_process_id BIGSERIAL PRIMARY KEY,
    creation_dte TIMESTAMP(6),
    tech_process_name VARCHAR(50) UNIQUE NOT NULL,
    update_dte TIMESTAMP(6)
);

-- ========================================
-- 3. PARTNER MANAGEMENT
-- ========================================

-- Partners
CREATE TABLE tib_audit_tec.ref_partner (
    partner_id BIGSERIAL PRIMARY KEY,
    partner_type_id BIGINT NOT NULL,
    partner_name VARCHAR(100),
    partner_code VARCHAR(10) UNIQUE NOT NULL,
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    FOREIGN KEY (partner_type_id) REFERENCES tib_audit_tec.ref_partner_typ(partner_type_id)
);

-- ========================================
-- 4. TRANSPORT CONFIGURATION
-- ========================================

-- Base Transport Table
CREATE TABLE tib_audit_tec.ref_transport (
    transport_id BIGSERIAL PRIMARY KEY,
    transport_typ VARCHAR(20)
);

-- HTTP Transport Configuration
CREATE TABLE tib_audit_tec.ref_transport_http (
    transport_id BIGINT PRIMARY KEY,
    http_uri VARCHAR(50) NOT NULL,
    http_partner_code VARCHAR(50) NOT NULL,
    client_port VARCHAR(10),
    client_host VARCHAR(100),
    client_method VARCHAR(20),
    client_body TEXT,
    issecure VARCHAR(10),
    expectedcode_succes VARCHAR(100),
    apigee_host VARCHAR(100),
    apigee_port VARCHAR(10),
    apigee_uri VARCHAR(100),
    apigee_method VARCHAR(30),
    throughput_max NUMERIC,
    nb_retry NUMERIC,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id),
    UNIQUE (http_uri, http_partner_code)
);

-- Email Transport Configuration
CREATE TABLE tib_audit_tec.ref_transport_email (
    transport_id BIGINT PRIMARY KEY,
    email_name VARCHAR(50) UNIQUE NOT NULL,
    email_from VARCHAR(250) NOT NULL,
    email_recipient_to VARCHAR(250) NOT NULL,
    email_recipient_cc VARCHAR(250),
    email_subject VARCHAR(150) NOT NULL,
    email_body TEXT,
    email_signature TEXT,
    has_attachment VARCHAR(1) NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);

-- JMS Transport Configuration
CREATE TABLE tib_audit_tec.ref_transport_jms (
    transport_id BIGINT PRIMARY KEY,
    jms_q_name VARCHAR(128) UNIQUE NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);

-- MQ Series Transport Configuration
CREATE TABLE tib_audit_tec.ref_transport_mqs (
    transport_id BIGINT PRIMARY KEY,
    mqs_q_name VARCHAR(50) NOT NULL,
    mqs_q_manager VARCHAR(30) NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id),
    UNIQUE (mqs_q_name, mqs_q_manager)
);

-- CFT Transport Configuration
CREATE TABLE tib_audit_tec.ref_transport_cft (
    transport_id BIGINT PRIMARY KEY,
    cft_idf VARCHAR(30) NOT NULL,
    cft_partner_code VARCHAR(30) NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id),
    UNIQUE (cft_idf, cft_partner_code)
);

-- ========================================
-- 5. FLOW MANAGEMENT
-- ========================================

-- Main Flows Table
CREATE TABLE tib_audit_tec.ref_flow (
    flow_id BIGSERIAL PRIMARY KEY,
    func_process_id BIGINT NOT NULL,
    flow_typ_id BIGINT NOT NULL,
    tech_process_id BIGINT NOT NULL,
    flow_name VARCHAR(100),
    flow_direction VARCHAR(3),
    flow_code VARCHAR(50) UNIQUE NOT NULL,
    enable_flg VARCHAR(1),
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    application_id BIGINT,
    max_file_size BIGINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id),
    FOREIGN KEY (flow_typ_id) REFERENCES tib_audit_tec.ref_flow_typ(flow_typ_id),
    FOREIGN KEY (tech_process_id) REFERENCES tib_audit_tec.ref_tech_process(tech_process_id),
    FOREIGN KEY (application_id) REFERENCES tib_audit_tec.ref_application(application_id)
);

-- Flow Control Configuration
CREATE TABLE tib_audit_tec.ref_flow_control (
    flow_id BIGINT PRIMARY KEY,
    flow_control_type VARCHAR(100),
    release_hour INTEGER,
    release_min INTEGER,
    release_sec INTEGER,
    batch_count INTEGER,
    batch_duration_sec INTEGER,
    is_active VARCHAR(1),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id)
);

-- Flow Country Association
CREATE TABLE tib_audit_tec.ref_flow_country (
    flow_id BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    PRIMARY KEY (flow_id, country_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (country_id) REFERENCES tib_audit_tec.ref_country(country_id)
);

-- Flow Partner Association (Many-to-Many with Transport and Rules)
CREATE TABLE tib_audit_tec.ref_flow_partner (
    partner_id BIGINT NOT NULL,
    flow_id BIGINT NOT NULL,
    transport_id BIGINT NOT NULL,
    partner_direction VARCHAR(10),
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    rule_id BIGINT DEFAULT 1 NOT NULL,
    charset_encoding_id BIGINT,
    enable_out VARCHAR(1) DEFAULT 'Y',
    enable_bmsa VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (partner_id, flow_id, rule_id, transport_id),
    FOREIGN KEY (partner_id) REFERENCES tib_audit_tec.ref_partner(partner_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id),
    FOREIGN KEY (rule_id) REFERENCES tib_audit_tec.ref_route_rule(rule_id),
    FOREIGN KEY (charset_encoding_id) REFERENCES tib_audit_tec.ref_charset_encoding(charset_encoding_id)
);

-- Flow Rules Configuration
CREATE TABLE tib_audit_tec.ref_flow_rules (
    flowcode VARCHAR(200) PRIMARY KEY,
    transporttype VARCHAR(50) NOT NULL,
    isunitary VARCHAR(20) NOT NULL,
    priority NUMERIC,
    urgency VARCHAR(100),
    flowcontrolledenabled VARCHAR(100),
    flowmaximum NUMERIC,
    flowretentionenabled VARCHAR(100),
    retentioncycleperiod NUMERIC,
    write_file VARCHAR(20) DEFAULT 'false',
    minrequiredfilesize NUMERIC,
    ignoreoutputdupcheck VARCHAR(20) DEFAULT 'false',
    logall VARCHAR(20) DEFAULT 'false',
    CONSTRAINT chk_isunitary CHECK (isunitary IN ('true', 'false')),
    CONSTRAINT chk_flowcontrolled CHECK (flowcontrolledenabled IN ('true', 'false')),
    CONSTRAINT chk_retention_positive CHECK (retentioncycleperiod > 0),
    CONSTRAINT chk_flowmax_positive CHECK (flowmaximum > 0)
);

-- ========================================
-- 6. FUNCTION MANAGEMENT
-- ========================================

-- Functions
CREATE TABLE tib_audit_tec.ref_function (
    function_id BIGSERIAL PRIMARY KEY,
    function_typ VARCHAR(20),
    function_name VARCHAR(20),
    function_tech_id VARCHAR(30)
);

-- External Services Configuration
CREATE TABLE tib_audit_tec.ref_external_service (
    function_id BIGINT PRIMARY KEY,
    service_code VARCHAR(64) UNIQUE NOT NULL,
    transport_request BIGINT NOT NULL,
    transport_reply BIGINT,
    transport_feedback BIGINT,
    transport_feedback_reply BIGINT,
    transport_late_reply BIGINT,
    timeout_soft INTEGER,
    timeout_hard INTEGER,
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id) ON DELETE CASCADE
);

-- Function Process Definition (Many-to-Many)
CREATE TABLE tib_audit_tec.ref_func_process_def (
    function_id BIGINT NOT NULL,
    func_process_id BIGINT NOT NULL,
    function_process_order INTEGER,
    PRIMARY KEY (function_id, func_process_id),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id),
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id)
);

-- Function Process Properties
CREATE TABLE tib_audit_tec.ref_func_process_prty (
    func_process_id BIGINT NOT NULL,
    prty_flow_id BIGINT NOT NULL,
    flow_prty_value VARCHAR(512),
    PRIMARY KEY (func_process_id, prty_flow_id),
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id),
    FOREIGN KEY (prty_flow_id) REFERENCES tib_audit_tec.ref_prty_flow(prty_flow_id)
);

-- ========================================
-- 7. CHARACTER REPLACEMENT SYSTEM
-- ========================================

-- Character Replacement Definitions
CREATE TABLE tib_audit_tec.ref_char_replace (
    set_replacement_id BIGINT NOT NULL,
    char_old_version VARCHAR(50) NOT NULL,
    char_new_version VARCHAR(50),
    char_to_replace_typ VARCHAR(20),
    PRIMARY KEY (set_replacement_id, char_old_version),
    FOREIGN KEY (set_replacement_id) REFERENCES tib_audit_tec.ref_set_replacement(set_replacement_id)
);

-- Function Character Replace Configuration
CREATE TABLE tib_audit_tec.ref_func_char_rplc (
    function_id BIGINT PRIMARY KEY,
    char_replace_typ VARCHAR(20),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id)
);

-- Function Set Replace (Many-to-Many Complex Association)
CREATE TABLE tib_audit_tec.ref_func_set_rplc (
    partner_id BIGINT NOT NULL,
    flow_id BIGINT NOT NULL,
    function_id BIGINT NOT NULL,
    set_replacement_id BIGINT NOT NULL,
    PRIMARY KEY (partner_id, flow_id, function_id, set_replacement_id),
    FOREIGN KEY (partner_id) REFERENCES tib_audit_tec.ref_partner(partner_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_func_char_rplc(function_id),
    FOREIGN KEY (set_replacement_id) REFERENCES tib_audit_tec.ref_set_replacement(set_replacement_id)
);

-- ========================================
-- 8. EMAIL AND NOTIFICATION MANAGEMENT
-- ========================================

-- Email Recipients Configuration
CREATE TABLE tib_audit_tec.ref_email_recipient (
    email_recipient_id BIGSERIAL PRIMARY KEY,
    owner_app VARCHAR(50) NOT NULL,
    param VARCHAR(100),
    transport_id BIGINT NOT NULL,
    mail_from VARCHAR(250),
    mail_to VARCHAR(250),
    mail_cc VARCHAR(250),
    mail_bcc VARCHAR(250),
    is_html CHAR(1),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport_email(transport_id)
);

-- Post-Flow Notification Configuration
CREATE TABLE tib_audit_tec.ref_postflow_notify (
    functionality VARCHAR(64),
    channel VARCHAR(64),
    destination VARCHAR(512)
);

-- ========================================
-- 9. PERFORMANCE INDEXES
-- ========================================

-- Flow-related indexes for performance
CREATE INDEX idx_flow_partner_flow_id ON tib_audit_tec.ref_flow_partner(flow_id);
CREATE INDEX idx_flow_partner_partner_id ON tib_audit_tec.ref_flow_partner(partner_id);
CREATE INDEX idx_flow_partner_transport_id ON tib_audit_tec.ref_flow_partner(transport_id);
CREATE INDEX idx_flow_application_id ON tib_audit_tec.ref_flow(application_id);
CREATE INDEX idx_flow_creation_dte ON tib_audit_tec.ref_flow(creation_dte);
CREATE INDEX idx_flow_func_process_id ON tib_audit_tec.ref_flow(func_process_id);
CREATE INDEX idx_flow_tech_process_id ON tib_audit_tec.ref_flow(tech_process_id);

-- Partner-related indexes
CREATE INDEX idx_partner_type_id ON tib_audit_tec.ref_partner(partner_type_id);
CREATE INDEX idx_partner_creation_dte ON tib_audit_tec.ref_partner(creation_dte);

-- Function-related indexes
CREATE INDEX idx_func_process_def_func_id ON tib_audit_tec.ref_func_process_def(function_id);
CREATE INDEX idx_func_process_def_proc_id ON tib_audit_tec.ref_func_process_def(func_process_id);
CREATE INDEX idx_func_set_rplc_partner_flow ON tib_audit_tec.ref_func_set_rplc(partner_id, flow_id);

-- Transport-related indexes
CREATE INDEX idx_email_recipient_transport_id ON tib_audit_tec.ref_email_recipient(transport_id);

-- ========================================
-- 10. COMMENTS FOR DOCUMENTATION
-- ========================================

COMMENT ON SCHEMA tib_audit_tec IS 'Technical audit and configuration schema for message flow management';

COMMENT ON TABLE tib_audit_tec.ref_application IS 'Application registry for flow ownership';
COMMENT ON TABLE tib_audit_tec.ref_flow IS 'Central flow configuration table linking functional and technical processes';
COMMENT ON TABLE tib_audit_tec.ref_flow_partner IS 'Many-to-many association between flows and partners with transport configuration';
COMMENT ON TABLE tib_audit_tec.ref_transport IS 'Base transport configuration table';
COMMENT ON TABLE tib_audit_tec.ref_transport_http IS 'HTTP/REST transport-specific configuration';
COMMENT ON TABLE tib_audit_tec.ref_transport_email IS 'Email transport-specific configuration';
COMMENT ON TABLE tib_audit_tec.ref_transport_jms IS 'JMS transport-specific configuration';
COMMENT ON TABLE tib_audit_tec.ref_transport_mqs IS 'MQ Series transport-specific configuration';
COMMENT ON TABLE tib_audit_tec.ref_transport_cft IS 'CFT (Cross File Transfer) transport-specific configuration';
COMMENT ON TABLE tib_audit_tec.ref_partner IS 'Partner/system registry for integration';
COMMENT ON TABLE tib_audit_tec.ref_function IS 'Processing function definitions';
COMMENT ON TABLE tib_audit_tec.ref_char_replace IS 'Character replacement rules for data transformation';
COMMENT ON TABLE tib_audit_tec.ref_flow_rules IS 'Business rules for flow processing and control';

COMMENT ON COLUMN tib_audit_tec.ref_flow.flow_direction IS 'IN/OUT/BID - Flow direction indicator';
COMMENT ON COLUMN tib_audit_tec.ref_flow.enable_flg IS 'Y/N - Whether flow is enabled';
COMMENT ON COLUMN tib_audit_tec.ref_flow.max_file_size IS 'Maximum file size in bytes for this flow';
COMMENT ON COLUMN tib_audit_tec.ref_flow_partner.partner_direction IS 'Direction of partner in flow context';
COMMENT ON COLUMN tib_audit_tec.ref_flow_partner.enable_out IS 'Y/N - Enable outbound processing';
COMMENT ON COLUMN tib_audit_tec.ref_flow_partner.enable_bmsa IS 'Y/N - Enable BMSA processing';
COMMENT ON COLUMN tib_audit_tec.ref_country.is_sepa IS 'Y/N - SEPA zone indicator';
COMMENT ON COLUMN tib_audit_tec.ref_transport_email.has_attachment IS 'Y/N - Email has attachment capability';
COMMENT ON COLUMN tib_audit_tec.ref_email_recipient.is_html IS 'Y/N - Email format is HTML';

COMMIT;

-- ========================================
-- END OF SCHEMA CREATION
-- ========================================