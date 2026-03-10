#!/bin/bash

# Quick Start Script for BH Flow Testing
# This script helps you quickly test the Bahrain WPS flow

set -e

echo "======================================"
echo " PIXEL-V2 BH Flow - Quick Start"
echo "======================================"
echo ""

# Function to check if service is running
check_service() {
    local service=$1
    local port=$2
    echo -n "Checking $service on port $port... "
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health 2>/dev/null | grep -q "200"; then
        echo "✓ Running"
        return 0
    else
        echo "✗ Not running"
        return 1
    fi
}

# Check prerequisites
echo "Step 1: Checking prerequisites..."
echo ""

if ! command -v docker &> /dev/null; then
    echo "✗ Docker not found. Please install Docker first."
    exit 1
fi

if ! command -v curl &> /dev/null; then
    echo "✗ curl not found. Please install curl first."
    exit 1
fi

echo "✓ Docker found"
echo "✓ curl found"
echo ""

# Build the application
echo "Step 2: Building BH Flow application..."
echo ""
cd "$(dirname "$0")"
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "✗ Build failed"
    exit 1
fi

echo "✓ Build successful"
echo ""

# Start Docker services
echo "Step 3: Starting Docker services..."
echo ""

cd ../docker
docker-compose up -d postgresql kafka

echo "Waiting for services to be ready (30 seconds)..."
sleep 30

# Start BH application
echo ""
echo "Step 4: Starting BH Flow application..."
echo ""

cd ../flow-bh
docker-compose -f docker-compose-bh.yml up -d pixel-bh-app

echo "Waiting for BH app to start (60 seconds)..."
sleep 60

# Check services
echo ""
echo "Step 5: Checking service health..."
echo ""

check_service "PostgreSQL" "5432"
check_service "Kafka" "9092"  
check_service "BH Application" "8081"

echo ""

# Test account validation
echo "Step 6: Testing account validation endpoint..."
echo ""

RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8081/wps/account-validation \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "BH67BMAG00001299123456",
    "clientId": "CLIENT123",
    "requestId": "TEST001"
  }' -o /tmp/bh-test-response.txt)

if [ "$RESPONSE" = "200" ]; then
    echo "✓ Account validation endpoint working"
    echo "Response:"
    cat /tmp/bh-test-response.txt
    echo ""
else
    echo "✗ Account validation endpoint failed (HTTP $RESPONSE)"
fi

echo ""

# Display useful commands
echo "======================================"
echo " Quick Start Complete!"
echo "======================================"
echo ""
echo "Useful commands:"
echo ""
echo "1. View logs:"
echo "   docker logs pixel-v2-bh-app -f"
echo ""
echo "2. Test account validation:"
echo "   curl -X POST http://localhost:8081/wps/account-validation \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"accountNumber\":\"BH67BMAG00001299123456\",\"clientId\":\"CLIENT123\",\"requestId\":\"TEST001\"}'"
echo ""
echo "3. Check health:"
echo "   curl http://localhost:8081/actuator/health"
echo ""
echo "4. Access Hawtio console:"
echo "   http://localhost:8081/actuator/hawtio"
echo ""
echo "5. Monitor Kafka events:"
echo "   docker exec -it pixel-v2-kafka kafka-console-consumer \\"
echo "     --bootstrap-server localhost:9092 \\"
echo "     --topic BHWPS-log-event-topic \\"
echo "     --from-beginning"
echo ""
echo "6. Stop services:"
echo "   docker-compose -f docker-compose-bh.yml down"
echo "   cd ../docker && docker-compose down"
echo ""
