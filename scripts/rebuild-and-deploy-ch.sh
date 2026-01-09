#!/bin/bash

# PIXEL-V2 Complete Rebuild and Deploy Script
# This script ensures fresh JAR build, proper Docker image rebuild, and deployment

set -e

echo "🚀 Starting PIXEL-V2 Complete Rebuild and Deploy Process..."
echo "=================================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Step 1: Clean and rebuild technical framework
echo "📦 Step 1: Rebuilding Technical Framework..."
cd technical-framework
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Technical framework build SUCCESS"
else
    echo "❌ Technical framework build FAILED"
    exit 1
fi

# Step 2: Clean and rebuild flow-ch application
echo "📦 Step 2: Rebuilding Flow-CH Application..."
cd "$PROJECT_ROOT/flow-ch"
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Flow-CH application build SUCCESS"
    JAR_SIZE=$(ls -lh target/pixel-ch-app-1.0.0.jar | awk '{print $5}')
    echo "📊 JAR info: $JAR_SIZE"
else
    echo "❌ Flow-CH application build FAILED"
    exit 1
fi

# Step 3: Copy JAR to Docker build context
echo "📋 Step 3: Updating Docker build context..."
cd "$PROJECT_ROOT"

# Copy fresh JAR to Docker build context
cp flow-ch/target/pixel-ch-app-1.0.0.jar docker/camel-runtime-spring/pixel-v2-app-spring-1-1.0.0.jar
echo "✅ Fresh JAR copied to Docker build context"

# Step 4: Stop and remove existing application container
echo "🛑 Step 4: Stopping existing Spring application container..."

# Stop only the application container (keep infrastructure running)
docker compose -f docker/docker-compose.yml stop pixel-v2-app-spring-1 2>/dev/null || true
docker compose -f docker/docker-compose.yml rm -f pixel-v2-app-spring-1 2>/dev/null || true

echo "✅ Spring application container stopped"

# Step 5: Rebuild Docker image (no cache to ensure fresh build)
echo "🐳 Step 5: Rebuilding Docker image..."
docker compose -f docker/docker-compose.yml build --no-cache pixel-v2-app-spring-1
if [ $? -eq 0 ]; then
    echo "✅ Docker image rebuild SUCCESS"
else
    echo "❌ Docker image rebuild FAILED"
    exit 1
fi

# Step 6: Deploy Spring application container
echo "🚀 Step 6: Deploying Spring application container..."

# Verify infrastructure is still running
echo "📋 Verifying infrastructure services are running..."
if ! docker ps | grep -q pixel-v2-postgresql; then
    echo "⚠️  PostgreSQL not running, starting infrastructure..."
    docker compose -f docker/docker-compose.yml up -d postgresql activemq kafka zookeeper redis
    sleep 10
fi

# Now start the application container
echo "🚀 Starting application container..."
docker compose -f docker/docker-compose.yml up -d pixel-v2-app-spring-1
if [ $? -eq 0 ]; then
    echo "✅ Container deployment SUCCESS"
else
    echo "❌ Container deployment FAILED"
    exit 1
fi

# Step 7: Wait for container startup and check health
echo "⏳ Step 7: Waiting for application startup..."
sleep 15

# Check if container is running
if docker ps | grep -q pixel-v2-app-spring-1; then
    echo "✅ Container is running"
    
    # Verify JAR was updated in container
    echo "📋 Verifying JAR in container:"
    docker exec pixel-v2-app-spring-1 ls -la /opt/pixel-v2-app-spring-1/pixel-v2-app-spring-1.jar
    
    # Verify TIB_AUDIT_TEC data including Switzerland flows
    echo "📋 Verifying TIB_AUDIT_TEC data:"
    SWISS_FLOWS=$(docker compose -f docker/docker-compose.yml exec -T postgresql psql -U pixelv2 -d pixelv2 -t -c "SELECT COUNT(*) FROM TIB_AUDIT_TEC.REF_FLOW WHERE FLOW_CODE IN ('ICHSIC', 'ICHSIC');" 2>/dev/null | tr -d ' ')
    if [ "$SWISS_FLOWS" = "2" ]; then
        echo "✅ Switzerland flows (ICHSIC/ICHSIC) verified in database"
    else
        echo "⚠️  Switzerland flows may be missing (found: $SWISS_FLOWS)"
    fi
    
    # Show recent logs to verify startup
    echo "📋 Recent startup logs:"
    echo "----------------------------------------"
    docker logs pixel-v2-app-spring-1 --tail 30
    echo "----------------------------------------"
    
    # Check for specific errors
    ERROR_COUNT=$(docker logs pixel-v2-app-spring-1 2>&1 | grep -i -c "templateid\|property.*key.*not found\|type.*conversion.*exception" || true)
    if [ "$ERROR_COUNT" -gt 0 ]; then
        echo "⚠️  Found $ERROR_COUNT kamelet/property errors in logs"
        echo "📋 Kamelet error details:"
        docker logs pixel-v2-app-spring-1 2>&1 | grep -i -A 3 -B 1 "templateid\|property.*key.*not found\|type.*conversion.*exception" | tail -20
    else
        echo "✅ No kamelet property errors detected"
    fi
else
    echo "❌ Container failed to start properly"
    echo "📋 Container logs:"
    docker logs pixel-v2-app-spring-1
    exit 1
fi

echo ""
echo "🎉 Rebuild and Deploy Process Complete!"
echo "=================================================="
echo "📊 Deployment Summary:"
echo "   - Technical Framework: ✅ Rebuilt with fresh kamelets"
echo "   - Flow-CH Application: ✅ Rebuilt ${JAR_SIZE} - kamelets extracted to classpath"
echo "   - Docker Image: ✅ Rebuilt (no cache) - fresh JAR deployed"
echo "   - Infrastructure: ✅ Kept running (PostgreSQL, Kafka, Redis, ActiveMQ)"
echo "   - Spring Container: ✅ Deployed with fresh JAR"
echo ""
echo "🔍 To monitor the application:"
echo "   docker logs -f pixel-v2-app-spring-1"
echo ""
echo "🌐 Application should be available at: http://localhost:8082"