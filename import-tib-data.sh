#!/bin/bash

# TIB_AUDIT_TEC Data Import Script
# This script imports all referential data from FLOW.sql into the TIB_AUDIT_TEC tables
# Run this after the database is initialized and running
# Date: 11 December 2025

set -e

# Configuration
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="pixelv2"
DB_USER="pixelv2"
DB_PASSWORD="pixelv2_secure_password"
CONTAINER_NAME="pixel-v2-postgresql"
DATA_FILE="/docker-entrypoint-initdb.d/fix-tib-data-import.sql"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo "=============================================="
echo "   TIB_AUDIT_TEC Data Import Script          "
echo "=============================================="
echo ""

# Check if container is running
print_status "Checking if PostgreSQL container is running..."
if ! docker ps --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    print_error "PostgreSQL container is not running!"
    print_status "Please start it with: docker-compose up -d postgresql"
    exit 1
fi
print_success "PostgreSQL container is running"

# Test database connection
print_status "Testing database connection..."
if ! PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "SELECT 1;" >/dev/null 2>&1; then
    print_error "Cannot connect to database!"
    print_status "Make sure PostgreSQL is fully initialized"
    exit 1
fi
print_success "Database connection successful"

# Check if TIB_AUDIT_TEC schema exists
print_status "Checking TIB_AUDIT_TEC schema..."
SCHEMA_EXISTS=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'TIB_AUDIT_TEC';" | xargs)
if [ "${SCHEMA_EXISTS}" != "1" ]; then
    print_error "TIB_AUDIT_TEC schema does not exist!"
    print_status "Please run the schema initialization first"
    exit 1
fi
print_success "TIB_AUDIT_TEC schema found"

# Check current data counts
print_status "Checking current data in tables..."
REF_FLOW_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW;" 2>/dev/null | xargs || echo "0")
print_status "Current REF_FLOW records: ${REF_FLOW_COUNT}"

if [ "${REF_FLOW_COUNT}" -gt 0 ]; then
    print_warning "Tables already contain data!"
    print_status "Do you want to clear existing data and reimport? (y/N)"
    read -r CLEAR_DATA
    CLEAR_DATA_LOWER=$(echo "${CLEAR_DATA}" | tr '[:upper:]' '[:lower:]')
    if [ "${CLEAR_DATA_LOWER}" = "y" ] || [ "${CLEAR_DATA_LOWER}" = "yes" ]; then
        print_status "Clearing existing data..."
        PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
        TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_PARTNER CASCADE;
        TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_COUNTRY CASCADE;
        TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW_RULES CASCADE;
        TRUNCATE TABLE TIB_AUDIT_TEC.REF_FLOW CASCADE;
        TRUNCATE TABLE TIB_AUDIT_TEC.REF_CHARSET_ENCODING CASCADE;
        "
        print_success "Existing data cleared"
    else
        print_status "Skipping import - keeping existing data"
        exit 0
    fi
fi

# Create a clean data file without transaction management
print_status "Preparing data for import..."
TEMP_DATA_FILE="/tmp/tib_audit_tec_data.sql"

# Extract just the INSERT statements from the processed file
docker exec "${CONTAINER_NAME}" sh -c "cat /docker-entrypoint-initdb.d/fix-tib-data-import.sql" > "${TEMP_DATA_FILE}"

# Import the data
print_status "Importing referential data..."
PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -f "${TEMP_DATA_FILE}"

# Clean up temp file
rm -f "${TEMP_DATA_FILE}"

# Verify import
print_status "Verifying data import..."
CHARSET_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_CHARSET_ENCODING;" | xargs)
FLOW_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW;" | xargs)
RULES_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW_RULES;" | xargs)
COUNTRY_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW_COUNTRY;" | xargs)
PARTNER_COUNT=$(PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW_PARTNER;" | xargs)

TOTAL_COUNT=$((CHARSET_COUNT + FLOW_COUNT + RULES_COUNT + COUNTRY_COUNT + PARTNER_COUNT))

echo ""
print_success "Data import completed successfully!"
echo ""
echo "Import Statistics:"
echo "  - REF_CHARSET_ENCODING: ${CHARSET_COUNT} records"
echo "  - REF_FLOW: ${FLOW_COUNT} records"
echo "  - REF_FLOW_RULES: ${RULES_COUNT} records"
echo "  - REF_FLOW_COUNTRY: ${COUNTRY_COUNT} records"
echo "  - REF_FLOW_PARTNER: ${PARTNER_COUNT} records"
echo "  - Total: ${TOTAL_COUNT} records"
echo ""

# Test some sample queries
print_status "Testing sample queries..."
echo ""
echo "Sample flows by direction:"
PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
SELECT flow_direction, COUNT(*) as count 
FROM TIB_AUDIT_TEC.REF_FLOW 
GROUP BY flow_direction 
ORDER BY flow_direction;
"

echo ""
echo "Sample character encodings:"
PGPASSWORD="${DB_PASSWORD}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
SELECT charset_code, charset_desc 
FROM TIB_AUDIT_TEC.REF_CHARSET_ENCODING 
ORDER BY charset_encoding_id 
LIMIT 5;
"

print_success "TIB_AUDIT_TEC data import completed successfully!"
echo ""
echo "=============================================="
echo "   Data Import Complete!                     "
echo "=============================================="

exit 0