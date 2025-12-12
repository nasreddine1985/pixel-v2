#!/bin/bash

# PostgreSQL Container Rebuild and Restart Script
# This script rebuilds and restarts the PostgreSQL container for PIXEL-V2
# Date: 11 December 2025

set -e

# Configuration
CONTAINER_NAME="pixel-v2-postgresql"
SERVICE_NAME="postgresql"
COMPOSE_FILE="docker-compose.yml"

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

# Check if we're in the correct directory
if [ ! -f "docker/${COMPOSE_FILE}" ]; then
    print_error "docker-compose.yml not found. Please run this script from the PIXEL-V2 root directory."
    exit 1
fi

echo "======================================"
echo "   PIXEL-V2 PostgreSQL Container     "
echo "   Rebuild and Restart Script        "
echo "======================================"
echo ""

# Step 1: Check if container exists
print_status "Checking if PostgreSQL container exists..."
if docker ps -a --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    print_warning "Container ${CONTAINER_NAME} found. Stopping and removing..."
    
    # Stop the container if running
    if docker ps --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        print_status "Stopping running container..."
        docker stop "${CONTAINER_NAME}" || true
    fi
    
    # Remove the container
    print_status "Removing existing container..."
    docker rm "${CONTAINER_NAME}" || true
    
    print_success "Container removed successfully"
else
    print_status "No existing container found"
fi

# Step 2: Remove PostgreSQL image to force rebuild
print_status "Checking for existing PostgreSQL image..."
IMAGE_ID=$(docker images --format "table {{.Repository}}:{{.Tag}} {{.ID}}" | grep "pixel-v2.*postgresql" | awk '{print $2}' | head -1 || true)
if [ ! -z "${IMAGE_ID}" ]; then
    print_warning "Removing existing PostgreSQL image (${IMAGE_ID}) to force rebuild..."
    docker rmi "${IMAGE_ID}" || true
    print_success "Image removed successfully"
fi

# Step 3: Remove PostgreSQL volumes for fresh initialization
print_status "Removing PostgreSQL volumes for fresh initialization..."
docker compose -f "${COMPOSE_FILE}" down -v 2>/dev/null || true
docker volume rm pixel-v2-postgres-data pixel-v2-postgres-logs 2>/dev/null || true
print_success "Volumes removed successfully"

# Step 4: Rebuild PostgreSQL service
print_status "Rebuilding PostgreSQL container from scratch..."
docker compose -f "docker/${COMPOSE_FILE}" build --no-cache --pull "${SERVICE_NAME}"
print_success "Container rebuilt successfully"

# Step 5: Start PostgreSQL service
print_status "Starting PostgreSQL container..."
docker compose -f "docker/${COMPOSE_FILE}" up -d "${SERVICE_NAME}"

# Step 7: Wait for container to be ready
print_status "Waiting for PostgreSQL to be ready..."
TIMEOUT=60
COUNTER=0

while [ $COUNTER -lt $TIMEOUT ]; do
    if docker exec "${CONTAINER_NAME}" pg_isready -h localhost -p 5432 -U pixelv2 -d pixelv2 >/dev/null 2>&1; then
        print_success "PostgreSQL is ready and accepting connections!"
        break
    fi
    
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER + 2))
done

echo ""

if [ $COUNTER -ge $TIMEOUT ]; then
    print_error "PostgreSQL failed to start within ${TIMEOUT} seconds"
    print_status "Container logs:"
    docker logs "${CONTAINER_NAME}" --tail 20
    exit 1
fi

# Step 8: Verify database initialization
print_status "Verifying database initialization..."
print_status "Waiting for initialization scripts to complete..."
sleep 15  # Give more time for initialization scripts

# Check schemas with retry logic
print_status "Checking database schemas..."
RETRY_COUNT=0
MAX_RETRIES=5
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    SCHEMAS=$(docker exec "${CONTAINER_NAME}" psql -U pixelv2 -d pixelv2 -t -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('pixel_v2', 'TIB_AUDIT_TEC');" 2>/dev/null | wc -l || echo "0")
    if [ "${SCHEMAS}" -ge 2 ]; then
        print_success "Database schemas created successfully"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            print_status "Schemas not ready yet, retrying in 5 seconds... (${RETRY_COUNT}/${MAX_RETRIES})"
            sleep 5
        else
            print_warning "Database schemas may not be fully initialized after ${MAX_RETRIES} attempts"
        fi
    fi
done

# Check referential data with retry logic
print_status "Checking referential data..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    REF_RECORDS=$(docker exec "${CONTAINER_NAME}" psql -U pixelv2 -d pixelv2 -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW;" 2>/dev/null | xargs || echo "0")
    if [ "${REF_RECORDS}" -gt 0 ]; then
        print_success "Referential data imported successfully (${REF_RECORDS} flows)"
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            print_status "Referential data not ready yet, retrying in 5 seconds... (${RETRY_COUNT}/${MAX_RETRIES})"
            sleep 5
        else
            print_warning "Referential data not loaded automatically, importing manually..."
            docker exec -i "${CONTAINER_NAME}" psql -U pixelv2 -d pixelv2 < docker/postgresql/simple-tib-data-import.sql
            
            # Verify after manual import
            REF_RECORDS=$(docker exec "${CONTAINER_NAME}" psql -U pixelv2 -d pixelv2 -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW;" 2>/dev/null | xargs || echo "0")
            if [ "${REF_RECORDS}" -gt 0 ]; then
                print_success "Referential data manually imported (${REF_RECORDS} flows)"
            else
                print_error "Failed to import referential data"
            fi
            break
        fi
    fi
done

# Step 9: Display container information
print_status "Container information:"
echo "Container Name: ${CONTAINER_NAME}"
echo "Service Name: ${SERVICE_NAME}"
echo "Port: 5432"
echo "Database: pixelv2"
echo "Username: pixelv2"
echo "Schemas: pixel_v2 (default), TIB_AUDIT_TEC (referential data)"

# Step 10: Display connection information
print_status "Connection details:"
echo "Host: localhost"
echo "Port: 5432"
echo "Database: pixelv2"
echo "Username: pixelv2"
echo "Password: pixelv2_secure_password"
echo ""
echo "JDBC URL: jdbc:postgresql://localhost:5432/pixelv2"

# Step 11: Offer to run verification script
print_status "Would you like to run the database verification script? (y/N)"
read -r RUN_VERIFICATION
RUN_VERIFICATION_LOWER=$(echo "${RUN_VERIFICATION}" | tr '[:upper:]' '[:lower:]')
if [ "${RUN_VERIFICATION_LOWER}" = "y" ] || [ "${RUN_VERIFICATION_LOWER}" = "yes" ]; then
    if [ -f "postgresql/verify-database.sh" ]; then
        print_status "Running database verification..."
        ./postgresql/verify-database.sh
    else
        print_warning "Database verification script not found at postgresql/verify-database.sh"
    fi
fi

# Step 12: Display logs option
print_status "To view PostgreSQL logs, run:"
echo "docker logs ${CONTAINER_NAME} -f"

print_success "PostgreSQL container rebuild and restart completed successfully!"
echo ""
echo "======================================"
echo "   PostgreSQL Container Ready!       "
echo "======================================"

exit 0