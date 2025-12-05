#!/bin/bash

# PIXEL-V2 Camel Runtime Spring Container Restart Script
# =====================================================
# This script restarts the camel-runtime-spring container (pixel-v2-app-spring-1)
# without rebuilding the image or affecting other containers

set -e

CONTAINER_NAME="pixel-v2-app-spring-1"
IMAGE_NAME="pixel-v2-app-spring-1"

echo "🔄 Restarting PIXEL-V2 Camel Runtime Spring Container..."
echo "=================================================="

# Check if container exists
if ! docker ps -a --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    echo "❌ Container ${CONTAINER_NAME} not found!"
    echo "Run './rebuild-and-deploy.sh' to create and deploy the container first."
    exit 1
fi

# Check if image exists
if ! docker images --format "table {{.Repository}}" | grep -q "^${IMAGE_NAME}$"; then
    echo "❌ Image ${IMAGE_NAME} not found!"
    echo "Run './rebuild-and-deploy.sh' to build the image first."
    exit 1
fi

echo "🛑 Step 1: Stopping container..."
docker stop ${CONTAINER_NAME} || echo "Container was already stopped"

echo "🗑️ Step 2: Removing container..."
docker rm ${CONTAINER_NAME} || echo "Container was already removed"

echo "🚀 Step 3: Starting fresh container..."
# Use docker compose to restart with proper network and dependencies
cd "$(dirname "$0")/docker"
docker compose up -d ${CONTAINER_NAME}

echo "⏳ Step 4: Waiting for container startup..."
sleep 3

# Check container status
if docker ps --format "table {{.Names}}\t{{.Status}}" | grep -q "${CONTAINER_NAME}.*Up"; then
    echo "✅ Container restart SUCCESS"
    
    echo "📋 Container Status:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(NAMES|${CONTAINER_NAME})"
    
    echo ""
    echo "📋 Recent startup logs:"
    echo "----------------------------------------"
    docker logs --tail 10 ${CONTAINER_NAME}
    echo "----------------------------------------"
    
    echo ""
    echo "🔍 To monitor the application:"
    echo "   docker logs -f ${CONTAINER_NAME}"
    echo ""
    echo "🌐 Application should be available at: http://localhost:8082"
else
    echo "❌ Container restart FAILED"
    echo "📋 Container logs:"
    docker logs --tail 20 ${CONTAINER_NAME}
    exit 1
fi

echo ""
echo "🎉 Camel Runtime Spring Container Restart Complete!"
echo "=================================================="