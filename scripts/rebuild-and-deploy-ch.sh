#!/bin/bash

# PIXEL-V2 Complete Rebuild and Deploy Script
# This script ensures fresh JAR build, proper Docker image rebuild, and deployment

set -e

echo "Starting PIXEL-V2 Complete Rebuild and Deploy Process..."
echo "=================================================="

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Step 1: Clean and rebuild technical framework
echo "Step 1: Rebuilding Technical Framework..."
cd technical-framework
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "Technical framework build SUCCESS"
else
    echo "Technical framework build FAILED"
    exit 1
fi

# Step 2: Clean and rebuild flow-ch application
echo "📦 Step 2: Rebuilding Flow-CH Application..."
cd "$PROJECT_ROOT/flow-ch"
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "Flow-CH application build SUCCESS"
    JAR_SIZE=$(ls -lh target/pixel-ch-app-1.0.0.jar | awk '{print $5}')
    echo "JAR info: $JAR_SIZE"
else
    echo "Flow-CH application build FAILED"
    exit 1
fi

# Step 3: Copy JAR to Docker build context
echo "Step 3: Updating Docker build context..."
cd "$PROJECT_ROOT"

# Copy fresh JAR to Docker build context
cp flow-ch/target/pixel-ch-app-1.0.0.jar docker/camel-runtime-spring/pixel-v2-app-spring-1-1.0.0.jar
echo "Fresh JAR copied to Docker build context"

# Step 4: Stop and remove existing application container
echo "Step 4: Stopping existing Spring application container..."

# Stop only the application container (keep infrastructure running)
docker compose -f docker/docker-compose.yml stop pixel-v2-app-spring-1 2>/dev/null || true
docker compose -f docker/docker-compose.yml rm -f pixel-v2-app-spring-1 2>/dev/null || true

echo "✅ Spring application container stopped"

# Step 5: Rebuild Docker image (no cache to ensure fresh build)
echo "Step 5: Rebuilding Docker image..."
docker compose -f docker/docker-compose.yml build --no-cache pixel-v2-app-spring-1
if [ $? -eq 0 ]; then
    echo "Docker image rebuild SUCCESS"
else
    echo "Docker image rebuild FAILED"
    exit 1
fi

# Step 6: Deploy Spring application container
echo "Step 6: Deploying Spring application container..."



# Now start the application container
echo "Starting application container..."
docker compose -f docker/docker-compose.yml up -d --no-deps pixel-v2-app-spring-1
if [ $? -eq 0 ]; then
    echo "Container deployment SUCCESS"
else
    echo "Container deployment FAILED"
    exit 1
fi





echo ""
echo "Rebuild and Deploy Process Complete!"
echo "==================================="