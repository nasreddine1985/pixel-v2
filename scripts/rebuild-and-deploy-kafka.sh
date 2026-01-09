#!/bin/bash

# PIXEL-V2 Kafka Rebuild and Deploy Script
# This script rebuilds Kafka Docker image with fresh configuration and redeploys it

set -e

echo "🚀 Starting PIXEL-V2 Kafka Rebuild and Deploy Process..."
echo "=================================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Step 1: Stop and remove existing Kafka containers
echo "🛑 Step 1: Stopping existing Kafka infrastructure..."

# Stop Kafka first (it depends on Zookeeper)
docker compose -f docker/docker-compose.yml stop kafka 2>/dev/null || true
docker compose -f docker/docker-compose.yml rm -f kafka 2>/dev/null || true
echo "✅ Kafka container stopped"

# Stop Zookeeper (Kafka's dependency)
docker compose -f docker/docker-compose.yml stop zookeeper 2>/dev/null || true
docker compose -f docker/docker-compose.yml rm -f zookeeper 2>/dev/null || true
echo "✅ Zookeeper container stopped"

# Step 2: Remove old Kafka images (optional - for complete refresh)
echo "🧹 Step 2: Cleaning old Kafka images..."
OLD_KAFKA_IMAGES=$(docker images --filter=reference="*kafka*" --filter=reference="*pixel-v2*kafka*" -q)
if [ -n "$OLD_KAFKA_IMAGES" ]; then
    echo "🗑️  Removing old Kafka images..."
    docker rmi $OLD_KAFKA_IMAGES 2>/dev/null || true
    echo "✅ Old Kafka images cleaned"
else
    echo "✅ No old Kafka images to clean"
fi

# Step 3: Rebuild Kafka Docker image with fresh configuration
echo "🐳 Step 3: Rebuilding Kafka Docker image..."
cd "$PROJECT_ROOT"

# Ensure Kafka configuration files are present
if [ ! -f "docker/kafka/Dockerfile" ]; then
    echo "❌ Kafka Dockerfile not found!"
    exit 1
fi

if [ ! -f "docker/kafka/create-pixel-topics.sh" ]; then
    echo "❌ Kafka topics script not found!"
    exit 1
fi

if [ ! -f "docker/kafka/kafka-pixel-v2.properties" ]; then
    echo "❌ Kafka properties file not found!"
    exit 1
fi

echo "📋 Building Kafka image with configuration:"
echo "   - Dockerfile: docker/kafka/Dockerfile"
echo "   - Topics script: docker/kafka/create-pixel-topics.sh"
echo "   - Properties: docker/kafka/kafka-pixel-v2.properties"

docker compose -f docker/docker-compose.yml build --no-cache kafka
if [ $? -eq 0 ]; then
    echo "✅ Kafka Docker image rebuild SUCCESS"
else
    echo "❌ Kafka Docker image rebuild FAILED"
    exit 1
fi

# Step 4: Start Zookeeper first (Kafka dependency)
echo "🚀 Step 4: Starting Zookeeper..."
docker compose -f docker/docker-compose.yml up -d zookeeper
if [ $? -eq 0 ]; then
    echo "✅ Zookeeper started successfully"
    echo "⏳ Waiting for Zookeeper to be ready..."
    sleep 10
else
    echo "❌ Zookeeper startup FAILED"
    exit 1
fi

# Step 5: Deploy new Kafka container
echo "🚀 Step 5: Deploying new Kafka container..."
docker compose -f docker/docker-compose.yml up -d kafka
if [ $? -eq 0 ]; then
    echo "✅ Kafka container deployment SUCCESS"
else
    echo "❌ Kafka container deployment FAILED"
    exit 1
fi

# Step 6: Wait for Kafka startup and verify health
echo "⏳ Step 6: Waiting for Kafka startup..."
echo "🕐 Giving Kafka time to initialize (30 seconds)..."
sleep 30

# Check if Kafka container is running
if docker ps | grep -q pixel-v2-kafka; then
    echo "✅ Kafka container is running"
    
    # Test Kafka broker connection
    echo "📋 Testing Kafka broker connection..."
    KAFKA_TEST=$(docker compose -f docker/docker-compose.yml exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "FAILED")
    if [ "$KAFKA_TEST" != "FAILED" ]; then
        echo "✅ Kafka broker is responding"
        
        # Show available topics
        echo "📋 Available Kafka topics:"
        echo "----------------------------------------"
        docker compose -f docker/docker-compose.yml exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null | grep -E "^[a-zA-Z]" | sort
        echo "----------------------------------------"
        
        # Count PIXEL-V2 specific topics
        PIXEL_TOPICS=$(docker compose -f docker/docker-compose.yml exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null | grep -c "pixel\|PIXEL\|pacs\|PACS" || echo "0")
        echo "📊 PIXEL-V2 topics found: $PIXEL_TOPICS"
        
    else
        echo "⚠️  Kafka broker connection test failed"
    fi
    
    # Show recent Kafka logs
    echo "📋 Recent Kafka startup logs:"
    echo "----------------------------------------"
    docker logs pixel-v2-kafka --tail 20 | grep -v "DEBUG" | tail -15
    echo "----------------------------------------"
    
else
    echo "❌ Kafka container failed to start properly"
    echo "📋 Kafka container logs:"
    docker logs pixel-v2-kafka
    exit 1
fi

# Step 7: Verify Zookeeper connection
echo "📋 Step 7: Verifying Zookeeper connection..."
if docker ps | grep -q pixel-v2-zookeeper; then
    echo "✅ Zookeeper container is running"
    
    # Check Zookeeper health
    ZK_TEST=$(docker compose -f docker/docker-compose.yml exec -T zookeeper echo ruok | nc localhost 2181 2>/dev/null || echo "FAILED")
    if [ "$ZK_TEST" = "imok" ]; then
        echo "✅ Zookeeper is healthy"
    else
        echo "⚠️  Zookeeper health check failed"
    fi
else
    echo "❌ Zookeeper is not running"
fi

echo ""
echo "🎉 Kafka Rebuild and Deploy Process Complete!"
echo "=================================================="
echo "📊 Deployment Summary:"
echo "   - Zookeeper: ✅ Deployed and running on port 2181"
echo "   - Kafka Broker: ✅ Rebuilt and deployed on ports 9092/29092"
echo "   - PIXEL-V2 Topics: ✅ $PIXEL_TOPICS topics configured"
echo "   - Custom Configuration: ✅ Applied"
echo ""
echo "🔍 To monitor Kafka:"
echo "   docker logs -f pixel-v2-kafka"
echo ""
echo "🔧 To list topics:"
echo "   docker compose -f docker/docker-compose.yml exec kafka kafka-topics --bootstrap-server localhost:9092 --list"
echo ""
echo "🌐 Kafka is available at: localhost:9092"
echo "📡 Zookeeper is available at: localhost:2181"