-- PIXEL-V2 Database Initialization Script
-- This script creates the necessary database schema for PIXEL-V2 application

-- Create PIXEL-V2 application user and database
DO $$
BEGIN
    -- Create schema for PIXEL-V2 application
    CREATE SCHEMA IF NOT EXISTS pixel_v2;
    
    -- Grant permissions to pixelv2 user
    GRANT ALL PRIVILEGES ON SCHEMA pixel_v2 TO pixelv2;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pixel_v2 TO pixelv2;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA pixel_v2 TO pixelv2;
    
    -- Set default privileges for future objects
    ALTER DEFAULT PRIVILEGES IN SCHEMA pixel_v2 GRANT ALL ON TABLES TO pixelv2;
    ALTER DEFAULT PRIVILEGES IN SCHEMA pixel_v2 GRANT ALL ON SEQUENCES TO pixelv2;
END $$;

-- Create sequence for flow occurrence ID generation
CREATE SEQUENCE IF NOT EXISTS pixel_v2.flow_occurence_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Grant usage permissions on the sequence
GRANT USAGE, SELECT ON SEQUENCE pixel_v2.flow_occurence_id_seq TO pixelv2;

-- Set comment on the sequence
COMMENT ON SEQUENCE pixel_v2.flow_occurence_id_seq IS 'Sequence for generating unique flow occurrence identifiers in PIXEL-V2 payment processing';

-- Create generic messages table for all message types
CREATE TABLE IF NOT EXISTS pixel_v2.tb_messages (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    correlation_id VARCHAR(255),
    message_type VARCHAR(50),
    source VARCHAR(100),
    payload TEXT,
    processing_status VARCHAR(50) DEFAULT 'RECEIVED',
    error_message TEXT,
    file_name VARCHAR(500),
    line_number BIGINT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    
    -- Indexes for better performance
    CONSTRAINT unique_message_id UNIQUE (message_id)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_tb_messages_correlation_id ON pixel_v2.tb_messages(correlation_id);
CREATE INDEX IF NOT EXISTS idx_tb_messages_status ON pixel_v2.tb_messages(processing_status);
CREATE INDEX IF NOT EXISTS idx_tb_messages_type ON pixel_v2.tb_messages(message_type);
CREATE INDEX IF NOT EXISTS idx_tb_messages_source ON pixel_v2.tb_messages(source);
CREATE INDEX IF NOT EXISTS idx_tb_messages_received_at ON pixel_v2.tb_messages(received_at);
CREATE INDEX IF NOT EXISTS idx_tb_messages_created_at ON pixel_v2.tb_messages(created_at);

-- Create log events table for Camel route logging
CREATE TABLE IF NOT EXISTS pixel_v2.log_event (
    LOGID VARCHAR(64) PRIMARY KEY NOT NULL,
    DATATS TIMESTAMP(6) NOT NULL,
    FLOWID VARCHAR(64) NOT NULL,
    HALFFLOWID VARCHAR(64) NOT NULL,
    FLOWCODE VARCHAR(64) NOT NULL,
    HALFFLOWCODE VARCHAR(64) NOT NULL,
    CONTEXTID VARCHAR(64),
    CLIENTLOGTIMESTAMP TIMESTAMP(6) NOT NULL,
    DBLOGTIMESTAMP TIMESTAMP(6) NOT NULL,
    TXT VARCHAR(2048) NOT NULL,
    LONGTXT TEXT,
    LOGROLE VARCHAR(16) NOT NULL,
    CODE VARCHAR(32),
    CUSTOMSTEP VARCHAR(128),
    COMPONENT VARCHAR(64) NOT NULL,
    INSTANCEID VARCHAR(16) NOT NULL,
    SERVICEPATH VARCHAR(512),
    PROCESSPATH VARCHAR(512),
    REFFLOWID DECIMAL(10,0),
    BEGINPROCESS TIMESTAMP(6),
    ENDPROCESS TIMESTAMP(6),
    CONTEXTTIMESTAMP TIMESTAMP(6),
    MSGSENTTIMESTAMP TIMESTAMP(6),
    MESSAGINGTYPE VARCHAR(10),
    MSGID VARCHAR(64),
    MSGPRIORITY INTEGER,
    MSGCORRELATIONID VARCHAR(256),
    MSGSOURCESYSTEM VARCHAR(128),
    MSGPRIVATECONTEXT TEXT,
    MSGTRANSACTIONID VARCHAR(64),
    MSGPROPERTIES TEXT,
    MSGBATCHNAME VARCHAR(256),
    MSGBATCHMSGNO INTEGER,
    MSGBATCHSIZE INTEGER,
    XMLMSGACTION VARCHAR(256),
    MSGRESUBMITIND VARCHAR(64),
    MSGBODY TEXT,
    LOG_DAY DATE GENERATED ALWAYS AS (DATATS::DATE) STORED
);

-- Create indexes for log events queries (matching JPA @Index annotations)
CREATE INDEX IF NOT EXISTS idx_log_datats ON pixel_v2.log_event(DATATS);
CREATE INDEX IF NOT EXISTS idx_log_flowid ON pixel_v2.log_event(FLOWID);
CREATE INDEX IF NOT EXISTS idx_log_flowcode ON pixel_v2.log_event(FLOWCODE);
CREATE INDEX IF NOT EXISTS idx_log_component ON pixel_v2.log_event(COMPONENT);
CREATE INDEX IF NOT EXISTS idx_log_day ON pixel_v2.log_event(LOG_DAY);
CREATE INDEX IF NOT EXISTS idx_log_logrole ON pixel_v2.log_event(LOGROLE);
-- Additional useful indexes
CREATE INDEX IF NOT EXISTS idx_log_event_msgcorrelationid ON pixel_v2.log_event(MSGCORRELATIONID);
CREATE INDEX IF NOT EXISTS idx_log_event_msgid ON pixel_v2.log_event(MSGID);
CREATE INDEX IF NOT EXISTS idx_log_event_contextid ON pixel_v2.log_event(CONTEXTID);
CREATE INDEX IF NOT EXISTS idx_log_event_refflowid ON pixel_v2.log_event(REFFLOWID);

-- Create flow summary table for flow tracking and monitoring
CREATE TABLE IF NOT EXISTS pixel_v2.flow_summary (
    FLOW_OCCUR_ID VARCHAR(64) PRIMARY KEY,
    FLOW_CODE VARCHAR(64),
    FLOW_STATUS_CODE VARCHAR(20),
    FLOW_COUNTRY_CODE VARCHAR(59),
    FLOW_COUNTRY_ID INTEGER,
    FLOW_TYPE_ID INTEGER,
    FLOW_COMMENT VARCHAR(255),
    NB_OUT_EXPECTED INTEGER,
    NB_OUT_COMPLETED INTEGER,
    NB_ERROR INTEGER,
    NB_REMITANCE INTEGER,
    NB_TRANSACTION INTEGER,
    NB_REPLAY INTEGER,
    ISSUING_PARTNER_CODE VARCHAR(64),
    ISSUING_PARTNER_LINK VARCHAR(128),
    RECIPIENT_PARTNER_CODE VARCHAR(255),
    RECIPIENT_PARTNER_LINK VARCHAR(510),
    LAST_LOG_ID VARCHAR(64),
    LAST_LOG_COMPONENT VARCHAR(255),
    LAST_LOG_DATETIME TIMESTAMP(6),
    LAST_LOG_STATUS_CODE VARCHAR(20),
    LAST_UPDATE_DATETIME TIMESTAMP(6),
    LAST_UPDATE_USER VARCHAR(50),
    ROOT_ERROR_CODE VARCHAR(50),
    ROOT_ERROR_LOG_ID VARCHAR(50),
    ROOT_ERROR_DATETIME TIMESTAMP(6),
    INPUT_FILE_PATH VARCHAR(500),
    INPUT_FILE_SIZE VARCHAR(50),
    REF_FLOW_ID DECIMAL(10,0),
    BEGIN_FLOW_DATETIME TIMESTAMP(6),
    END_FLOW_DATETIME TIMESTAMP(6),
    CURRENT_CLIENT_DATETIME TIMESTAMP(6),
    BEGIN_FLOW_DATE DATE GENERATED ALWAYS AS (BEGIN_FLOW_DATETIME::DATE) STORED,
    END_FLOW_DATE DATE GENERATED ALWAYS AS (END_FLOW_DATETIME::DATE) STORED,
    LAST_UPDATE_DATE DATE GENERATED ALWAYS AS (LAST_UPDATE_DATETIME::DATE) STORED,
    REPLAY_ID VARCHAR(20),
    REGION VARCHAR(5)
);

-- Create indexes for flow summary queries
CREATE INDEX IF NOT EXISTS idx_flow_summary_flow_code ON pixel_v2.flow_summary(FLOW_CODE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_status_code ON pixel_v2.flow_summary(FLOW_STATUS_CODE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_country_code ON pixel_v2.flow_summary(FLOW_COUNTRY_CODE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_country_id ON pixel_v2.flow_summary(FLOW_COUNTRY_ID);
CREATE INDEX IF NOT EXISTS idx_flow_summary_type_id ON pixel_v2.flow_summary(FLOW_TYPE_ID);
CREATE INDEX IF NOT EXISTS idx_flow_summary_begin_flow_datetime ON pixel_v2.flow_summary(BEGIN_FLOW_DATETIME);
CREATE INDEX IF NOT EXISTS idx_flow_summary_end_flow_datetime ON pixel_v2.flow_summary(END_FLOW_DATETIME);
CREATE INDEX IF NOT EXISTS idx_flow_summary_last_update_datetime ON pixel_v2.flow_summary(LAST_UPDATE_DATETIME);
CREATE INDEX IF NOT EXISTS idx_flow_summary_begin_flow_date ON pixel_v2.flow_summary(BEGIN_FLOW_DATE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_issuing_partner ON pixel_v2.flow_summary(ISSUING_PARTNER_CODE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_recipient_partner ON pixel_v2.flow_summary(RECIPIENT_PARTNER_CODE);
CREATE INDEX IF NOT EXISTS idx_flow_summary_ref_flow_id ON pixel_v2.flow_summary(REF_FLOW_ID);
CREATE INDEX IF NOT EXISTS idx_flow_summary_region ON pixel_v2.flow_summary(REGION);



-- Create function to update timestamp on record modification
CREATE OR REPLACE FUNCTION pixel_v2.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at columns
CREATE TRIGGER update_tb_messages_updated_at
    BEFORE UPDATE ON pixel_v2.tb_messages
    FOR EACH ROW EXECUTE FUNCTION pixel_v2.update_updated_at_column();

-- Create trigger to automatically update LAST_UPDATE_DATETIME
CREATE OR REPLACE FUNCTION pixel_v2.update_flow_summary_last_update()
RETURNS TRIGGER AS $$
BEGIN
    NEW.LAST_UPDATE_DATETIME = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_flow_summary_last_update_datetime
    BEFORE UPDATE ON pixel_v2.flow_summary
    FOR EACH ROW EXECUTE FUNCTION pixel_v2.update_flow_summary_last_update();



-- Grant execute permissions on functions
GRANT EXECUTE ON FUNCTION pixel_v2.update_updated_at_column() TO pixelv2;

-- Include TIB_AUDIT_TEC schema initialization
\i /docker-entrypoint-initdb.d/init_tib_audit_tec_schema.sql

-- Include sample data for TIB_AUDIT_TEC schema
\i /docker-entrypoint-initdb.d/insert_ochsic_sample_data.sql

-- Display setup completion message
DO $$
BEGIN
    RAISE NOTICE 'PIXEL-V2 PostgreSQL database initialization completed successfully!';
    RAISE NOTICE 'Database: %, Schema: pixel_v2', current_database();
    RAISE NOTICE 'Schemas created: pixel_v2, TIB_AUDIT_TEC';
    RAISE NOTICE 'Tables created: tb_messages, log_event, flow_summary';
    RAISE NOTICE 'Referential tables created: REF_CHARSET_ENCODING, REF_FLOW, REF_FLOW_RULES, REF_FLOW_COUNTRY, REF_FLOW_PARTNER';
END $$;