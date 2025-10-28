#!/bin/bash

# PostgreSQL Setup Script for Flow Application
# This script helps configure PostgreSQL for the Flow application

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Database configuration
DB_NAME="pixelv2"
DB_USER="postgres"
DB_PASSWORD="pixel"
DB_HOST="localhost"
DB_PORT="5432"

check_postgres_running() {
    log "Checking if PostgreSQL is running..."
    
    if pg_isready -h $DB_HOST -p $DB_PORT >/dev/null 2>&1; then
        success "PostgreSQL is running on $DB_HOST:$DB_PORT"
        return 0
    else
        error "PostgreSQL is not running on $DB_HOST:$DB_PORT"
        return 1
    fi
}

check_postgres_installation() {
    log "Checking PostgreSQL installation..."
    
    if command -v psql >/dev/null 2>&1; then
        success "PostgreSQL client (psql) is installed"
        return 0
    else
        error "PostgreSQL client (psql) is not installed"
        log "Install with: brew install postgresql"
        return 1
    fi
}

create_database_and_user() {
    log "Setting up database and user..."
    
    # Try to connect as superuser to create database and set password
    if psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres -c "SELECT 1;" >/dev/null 2>&1; then
        log "Connected as postgres superuser"
        
        # Create database if it doesn't exist
        log "Creating database '$DB_NAME'..."
        psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres -c "CREATE DATABASE $DB_NAME;" 2>/dev/null || warn "Database '$DB_NAME' already exists"
        
        # Set password for postgres user
        log "Setting password for postgres user..."
        psql -h $DB_HOST -p $DB_PORT -U postgres -d postgres -c "ALTER USER postgres PASSWORD '$DB_PASSWORD';" >/dev/null 2>&1
        
        success "Database setup completed"
        return 0
    else
        error "Cannot connect to PostgreSQL as superuser"
        warn "You may need to:"
        warn "1. Start PostgreSQL: brew services start postgresql"
        warn "2. Create user: createuser -s postgres"
        warn "3. Set password using psql"
        return 1
    fi
}

test_connection() {
    log "Testing database connection with application credentials..."
    
    if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT current_database(), current_user;" >/dev/null 2>&1; then
        success "Database connection test successful!"
        log "Connection details:"
        log "  Host: $DB_HOST:$DB_PORT"
        log "  Database: $DB_NAME" 
        log "  User: $DB_USER"
        log "  Password: $DB_PASSWORD"
        return 0
    else
        error "Database connection test failed"
        return 1
    fi
}

create_tables() {
    log "Creating application tables..."
    
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'SQL'
-- Create pacs008_messages table
CREATE TABLE IF NOT EXISTS pacs008_messages (
    id BIGSERIAL PRIMARY KEY,
    jms_message_id VARCHAR(100),
    jms_correlation_id VARCHAR(100),
    jms_timestamp BIGINT,
    jms_priority INTEGER,
    message_type VARCHAR(50),
    processing_route VARCHAR(50),
    message_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_timestamp TIMESTAMP
);

-- Create message_errors table  
CREATE TABLE IF NOT EXISTS message_errors (
    id BIGSERIAL PRIMARY KEY,
    jms_message_id VARCHAR(100),
    error_route VARCHAR(50),
    error_message TEXT,
    message_body TEXT,
    error_timestamp TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_pacs008_jms_message_id ON pacs008_messages(jms_message_id);
CREATE INDEX IF NOT EXISTS idx_pacs008_message_type ON pacs008_messages(message_type);
CREATE INDEX IF NOT EXISTS idx_pacs008_created_at ON pacs008_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_errors_jms_message_id ON message_errors(jms_message_id);
CREATE INDEX IF NOT EXISTS idx_errors_created_at ON message_errors(created_at);

\dt
SQL

    if [ $? -eq 0 ]; then
        success "Database tables created successfully"
        return 0
    else
        error "Failed to create tables"
        return 1
    fi
}

show_connection_info() {
    log "PostgreSQL Connection Information:"
    echo "=================================="
    echo "Host: $DB_HOST"
    echo "Port: $DB_PORT"  
    echo "Database: $DB_NAME"
    echo "Username: $DB_USER"
    echo "Password: $DB_PASSWORD"
    echo ""
    echo "JDBC URL: jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
    echo ""
    echo "Test connection:"
    echo "PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME"
}

main() {
    echo
    log "PostgreSQL Setup for Flow Application"
    log "====================================="
    echo
    
    case "${1:-setup}" in
        "check")
            check_postgres_installation
            check_postgres_running
            if test_connection; then
                success "PostgreSQL is properly configured!"
            else
                error "PostgreSQL configuration issues found"
                exit 1
            fi
            ;;
        "setup")
            check_postgres_installation || exit 1
            check_postgres_running || exit 1
            create_database_and_user || exit 1
            test_connection || exit 1
            create_tables || exit 1
            echo
            success "PostgreSQL setup completed successfully!"
            echo
            show_connection_info
            ;;
        "info")
            show_connection_info
            ;;
        "test")
            test_connection
            ;;
        "tables")
            create_tables
            ;;
        "help"|"-h"|"--help")
            cat << 'HELP'
PostgreSQL Setup Script for Flow Application

Usage: ./setup-postgres.sh [command]

Commands:
    setup     Complete PostgreSQL setup (default)
    check     Check PostgreSQL status and connection
    test      Test database connection only  
    tables    Create application tables only
    info      Show connection information
    help      Show this help

Examples:
    ./setup-postgres.sh           # Full setup
    ./setup-postgres.sh check     # Check status
    ./setup-postgres.sh test      # Test connection

Configuration:
    Database: pixelv2
    Username: postgres  
    Password: pixel
    Host: localhost:5432
HELP
            ;;
        *)
            error "Unknown command: $1"
            log "Use 'help' for usage information"
            exit 1
            ;;
    esac
    
    echo
}

main "$@"