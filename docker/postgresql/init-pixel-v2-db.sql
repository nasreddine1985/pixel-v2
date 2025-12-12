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
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    correlation_id VARCHAR(255),
    message_type VARCHAR(50),
    source VARCHAR(100),
    payload TEXT,
    processing_status VARCHAR(50) DEFAULT 'LOGGED',
    log_level VARCHAR(20) DEFAULT 'INFO',
    route_id VARCHAR(255),
    exchange_id VARCHAR(255),
    error_message TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Index for better performance
    CONSTRAINT idx_log_event_message_id UNIQUE (message_id, timestamp)
);

-- Create indexes for log events queries
CREATE INDEX IF NOT EXISTS idx_log_event_correlation_id ON pixel_v2.log_event(correlation_id);
CREATE INDEX IF NOT EXISTS idx_log_event_status ON pixel_v2.log_event(processing_status);
CREATE INDEX IF NOT EXISTS idx_log_event_type ON pixel_v2.log_event(message_type);
CREATE INDEX IF NOT EXISTS idx_log_event_source ON pixel_v2.log_event(source);
CREATE INDEX IF NOT EXISTS idx_log_event_timestamp ON pixel_v2.log_event(timestamp);
CREATE INDEX IF NOT EXISTS idx_log_event_route_id ON pixel_v2.log_event(route_id);
CREATE INDEX IF NOT EXISTS idx_log_event_log_level ON pixel_v2.log_event(log_level);

-- Create flow summary table for flow tracking and monitoring
CREATE TABLE IF NOT EXISTS pixel_v2.flow_summary (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    correlation_id VARCHAR(255),
    message_type VARCHAR(50),
    source VARCHAR(100),
    payload TEXT,
    processing_status VARCHAR(50) DEFAULT 'RECEIVED',
    flow_code VARCHAR(100),
    step VARCHAR(100),
    queue VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Index for better performance
    CONSTRAINT idx_flow_summary_message_id_timestamp UNIQUE (message_id, timestamp)
);

-- Create indexes for flow summary queries
CREATE INDEX IF NOT EXISTS idx_flow_summary_correlation_id ON pixel_v2.flow_summary(correlation_id);
CREATE INDEX IF NOT EXISTS idx_flow_summary_status ON pixel_v2.flow_summary(processing_status);
CREATE INDEX IF NOT EXISTS idx_flow_summary_type ON pixel_v2.flow_summary(message_type);
CREATE INDEX IF NOT EXISTS idx_flow_summary_source ON pixel_v2.flow_summary(source);
CREATE INDEX IF NOT EXISTS idx_flow_summary_timestamp ON pixel_v2.flow_summary(timestamp);
CREATE INDEX IF NOT EXISTS idx_flow_summary_flow_code ON pixel_v2.flow_summary(flow_code);
CREATE INDEX IF NOT EXISTS idx_flow_summary_step ON pixel_v2.flow_summary(step);
CREATE INDEX IF NOT EXISTS idx_flow_summary_queue ON pixel_v2.flow_summary(queue);



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

CREATE TRIGGER update_flow_summary_updated_at
    BEFORE UPDATE ON pixel_v2.flow_summary
    FOR EACH ROW EXECUTE FUNCTION pixel_v2.update_updated_at_column();



-- Grant execute permissions on functions
GRANT EXECUTE ON FUNCTION pixel_v2.update_updated_at_column() TO pixelv2;

-- Include TIB_AUDIT_TEC schema initialization
\i /docker-entrypoint-initdb.d/init-tib-audit-tec-schema.sql

-- Display setup completion message
DO $$
BEGIN
    RAISE NOTICE 'PIXEL-V2 PostgreSQL database initialization completed successfully!';
    RAISE NOTICE 'Database: %, Schema: pixel_v2', current_database();
    RAISE NOTICE 'Schemas created: pixel_v2, TIB_AUDIT_TEC';
    RAISE NOTICE 'Tables created: tb_messages, log_event, flow_summary';
    RAISE NOTICE 'Referential tables created: REF_CHARSET_ENCODING, REF_FLOW, REF_FLOW_RULES, REF_FLOW_COUNTRY, REF_FLOW_PARTNER';
END $$;