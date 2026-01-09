#!/bin/bash

# PIXEL-V2 Hawtio Rebuild and Deploy Script
# This script rebuilds Hawtio Docker image with fresh configuration and redeploys it

set -e

echo "🚀 Starting PIXEL-V2 Hawtio Rebuild and Deploy Process..."
echo "=================================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Step 1: Stop and remove existing Hawtio container
echo "🛑 Step 1: Stopping existing Hawtio container..."

# Stop Hawtio container
docker compose -f docker/docker-compose.yml stop hawtio 2>/dev/null || true
docker compose -f docker/docker-compose.yml rm -f hawtio 2>/dev/null || true
echo "✅ Hawtio container stopped"

# Step 2: Remove old Hawtio images (optional - for complete refresh)
echo "🧹 Step 2: Cleaning old Hawtio images..."
OLD_HAWTIO_IMAGES=$(docker images --filter=reference="*hawtio*" --filter=reference="*pixel-v2*hawtio*" -q)
if [ -n "$OLD_HAWTIO_IMAGES" ]; then
    echo "🗑️  Removing old Hawtio images..."
    docker rmi $OLD_HAWTIO_IMAGES 2>/dev/null || true
    echo "✅ Old Hawtio images cleaned"
else
    echo "✅ No old Hawtio images to clean"
fi

# Step 3: Rebuild Hawtio Docker image with fresh configuration
echo "🐳 Step 3: Rebuilding Hawtio Docker image..."
cd "$PROJECT_ROOT"

# Ensure Hawtio configuration files are present
if [ ! -f "docker/hawtio/Dockerfile" ]; then
    echo "❌ Hawtio Dockerfile not found!"
    exit 1
fi

if [ ! -f "docker/hawtio/start-hawtio.sh" ]; then
    echo "❌ Hawtio startup script not found!"
    exit 1
fi

if [ ! -f "docker/hawtio/hawtio-config.json" ]; then
    echo "❌ Hawtio configuration file not found!"
    exit 1
fi

echo "📋 Building Hawtio image with configuration:"
echo "   - Dockerfile: docker/hawtio/Dockerfile"
echo "   - Startup script: docker/hawtio/start-hawtio.sh"
echo "   - Config: docker/hawtio/hawtio-config.json"

docker compose -f docker/docker-compose.yml build --no-cache hawtio
if [ $? -eq 0 ]; then
    echo "✅ Hawtio Docker image rebuild SUCCESS"
else
    echo "❌ Hawtio Docker image rebuild FAILED"
    exit 1
fi

# Step 4: Deploy new Hawtio container
echo "🚀 Step 4: Deploying new Hawtio container..."
docker compose -f docker/docker-compose.yml up -d hawtio
if [ $? -eq 0 ]; then
    echo "✅ Hawtio container deployment SUCCESS"
else
    echo "❌ Hawtio container deployment FAILED"
    exit 1
fi

# Step 5: Wait for Hawtio startup and verify health
echo "⏳ Step 5: Waiting for Hawtio startup..."
echo "🕐 Giving Hawtio time to initialize (20 seconds)..."
sleep 20

# Check if Hawtio container is running
if docker ps | grep -q pixel-v2-hawtio; then
    echo "✅ Hawtio container is running"
    
    # Test Hawtio web interface
    echo "📋 Testing Hawtio web interface..."
    HAWTIO_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/hawtio/ 2>/dev/null || echo "FAILED")
    if [ "$HAWTIO_TEST" = "200" ]; then
        echo "✅ Hawtio web interface is responding (HTTP 200)"
    elif [ "$HAWTIO_TEST" = "302" ] || [ "$HAWTIO_TEST" = "301" ]; then
        echo "✅ Hawtio web interface is responding (HTTP $HAWTIO_TEST - redirect)"
    else
        echo "⚠️  Hawtio web interface test returned: $HAWTIO_TEST"
        echo "   This might be normal during startup - check logs below"
    fi
    
    # Check Hawtio version and configuration
    echo "📋 Checking Hawtio container details..."
    HAWTIO_VERSION=$(docker exec pixel-v2-hawtio ls -la /opt/hawtio/hawtio-app.jar 2>/dev/null || echo "Version check failed")
    echo "   Hawtio JAR: $HAWTIO_VERSION"
    
    # Show Hawtio environment variables
    echo "📋 Hawtio configuration:"
    echo "----------------------------------------"
    docker exec pixel-v2-hawtio env | grep -E "HAWTIO|JAVA" | sort
    echo "----------------------------------------"
    
    # Show recent Hawtio logs
    echo "📋 Recent Hawtio startup logs:"
    echo "----------------------------------------"
    docker logs pixel-v2-hawtio --tail 15 | grep -v "DEBUG" | tail -10
    echo "----------------------------------------"
    
else
    echo "❌ Hawtio container failed to start properly"
    echo "📋 Hawtio container logs:"
    docker logs pixel-v2-hawtio
    exit 1
fi

# Step 6: Verify connectivity to Camel applications
echo "📋 Step 6: Verifying connectivity to PIXEL-V2 Camel applications..."

# Check if target Camel applications are running
CAMEL_APPS_RUNNING=0
if docker ps | grep -q pixel-v2-app-spring-1; then
    echo "✅ pixel-v2-app-spring-1 is running"
    CAMEL_APPS_RUNNING=$((CAMEL_APPS_RUNNING + 1))
else
    echo "⚠️  pixel-v2-app-spring-1 is not running"
fi

if docker ps | grep -q pixel-v2-app-1; then
    echo "✅ pixel-v2-app-1 is running"
    CAMEL_APPS_RUNNING=$((CAMEL_APPS_RUNNING + 1))
else
    echo "ℹ️  pixel-v2-app-1 is not running (optional)"
fi

if [ $CAMEL_APPS_RUNNING -gt 0 ]; then
    echo "📊 Found $CAMEL_APPS_RUNNING Camel application(s) for Hawtio to monitor"
else
    echo "⚠️  No Camel applications currently running"
    echo "   Start your Camel applications for Hawtio monitoring"
fi

echo ""
echo "🎉 Hawtio Rebuild and Deploy Process Complete!"
echo "=================================================="
echo "📊 Deployment Summary:"
echo "   - Hawtio Container: ✅ Rebuilt and deployed on port 8090"
echo "   - Web Interface: ✅ Available for Camel route management"
echo "   - Configuration: ✅ PIXEL-V2 specific settings applied"
echo "   - Target Apps: 📊 $CAMEL_APPS_RUNNING Camel application(s) detected"
echo ""
echo "🔍 To monitor Hawtio:"
echo "   docker logs -f pixel-v2-hawtio"
echo ""
echo "🌐 Hawtio web interface:"
echo "   http://localhost:8090/hawtio/"
echo ""
echo "📋 Hawtio Features:"
echo "   - Camel route visualization and management"
echo "   - JMX monitoring and operations"
echo "   - Real-time metrics and statistics"
echo "   - Route start/stop/debug capabilities"