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

-- Create referential data table for validation
CREATE TABLE IF NOT EXISTS pixel_v2.referential_data (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_code VARCHAR(100) NOT NULL,
    entity_name VARCHAR(255),
    entity_description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint for entity type and code
    CONSTRAINT unique_entity_type_code UNIQUE (entity_type, entity_code)
);

-- Create index for referential data lookups
CREATE INDEX IF NOT EXISTS idx_referential_entity_type ON pixel_v2.referential_data(entity_type);
CREATE INDEX IF NOT EXISTS idx_referential_entity_code ON pixel_v2.referential_data(entity_code);
CREATE INDEX IF NOT EXISTS idx_referential_active ON pixel_v2.referential_data(is_active);

-- Insert sample referential data for PACS-008 validation
INSERT INTO pixel_v2.referential_data (entity_type, entity_code, entity_name, entity_description) VALUES
    ('BANK_CODE', 'FR1420041', 'BNP Paribas', 'BNP Paribas Bank Code'),
    ('BANK_CODE', 'FR1430002', 'Crédit Agricole', 'Crédit Agricole Bank Code'),
    ('CURRENCY', 'EUR', 'Euro', 'European Union Euro Currency'),
    ('CURRENCY', 'USD', 'US Dollar', 'United States Dollar Currency'),
    ('COUNTRY', 'FR', 'France', 'French Republic'),
    ('COUNTRY', 'DE', 'Germany', 'Federal Republic of Germany'),
    ('PAYMENT_TYPE', 'SEPA', 'SEPA Credit Transfer', 'Single Euro Payments Area Credit Transfer'),
    ('PAYMENT_TYPE', 'INSTANT', 'Instant Payment', 'Real-time Payment Processing')
ON CONFLICT (entity_type, entity_code) DO NOTHING;

-- Create audit log table for message monitoring
CREATE TABLE IF NOT EXISTS pixel_v2.audit_log (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    operation VARCHAR(100) NOT NULL,
    operation_details TEXT,
    user_context VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to messages table
    FOREIGN KEY (message_id) REFERENCES pixel_v2.tb_messages(message_id) ON DELETE CASCADE
);

-- Create index for audit log queries
CREATE INDEX IF NOT EXISTS idx_audit_message_id ON pixel_v2.audit_log(message_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON pixel_v2.audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_operation ON pixel_v2.audit_log(operation);

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

CREATE TRIGGER update_referential_data_updated_at
    BEFORE UPDATE ON pixel_v2.referential_data
    FOR EACH ROW EXECUTE FUNCTION pixel_v2.update_updated_at_column();

-- Grant execute permissions on functions
GRANT EXECUTE ON FUNCTION pixel_v2.update_updated_at_column() TO pixelv2;

-- Display setup completion message
DO $$
BEGIN
    RAISE NOTICE 'PIXEL-V2 PostgreSQL database initialization completed successfully!';
    RAISE NOTICE 'Database: %, Schema: pixel_v2', current_database();
    RAISE NOTICE 'Tables created: tb_messages, referential_data, audit_log';
END $$;