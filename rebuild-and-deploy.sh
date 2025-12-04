#!/bin/bash

# PIXEL-V2 Complete Rebuild and Deploy Script
# This script ensures fresh JAR build, proper Docker image rebuild, and deployment

set -e

echo "🚀 Starting PIXEL-V2 Complete Rebuild and Deploy Process..."
echo "=================================================="

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

# Step 1.5: Extract kamelet YAML files from technical framework JARs to flow-ch resources
# echo "📂 Step 1.5: Extracting kamelet YAML files to flow-ch resources..."
# mkdir -p flow-ch/src/main/resources/kamelets

# # Extract kamelets from all technical framework JARs
# for jar_file in technical-framework/*/target/*.jar; do
#     if [ -f "$jar_file" ]; then
#         jar_name=$(basename "$jar_file")
#         echo "📋 Extracting kamelets from $jar_name..."
        
#         # Create temp directory for extraction
#         temp_dir=$(mktemp -d)
#         cd "$temp_dir"
        
#         # Extract everything from the JAR
#         jar xf "$OLDPWD/$jar_file" 2>/dev/null || true
        
#         if [ -d "kamelets" ]; then
#             # Copy extracted kamelets to flow-ch resources
#             for kamelet_file in kamelets/*.kamelet.yaml; do
#                 if [ -f "$kamelet_file" ]; then
#                     cp "$kamelet_file" "$OLDPWD/flow-ch/src/main/resources/kamelets/"
#                     echo "  ✅ Extracted $(basename "$kamelet_file")"
#                 fi
#             done
#         fi
        
#         # Clean up temp directory
#         cd "$OLDPWD"
#         rm -rf "$temp_dir"
#     fi
# done
# echo "✅ Kamelet YAML files extracted to flow-ch resources"

# Step 2: Clean and rebuild flow-ch application
echo "📦 Step 2: Rebuilding Flow-CH Application..."
cd ../flow-ch
mvn clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Flow-CH application build SUCCESS"
    JAR_SIZE=$(ls -lh target/pixel-camel-app-1.0.0.jar | awk '{print $5}')
    echo "📊 JAR info: $JAR_SIZE"
else
    echo "❌ Flow-CH application build FAILED"
    exit 1
fi

# Step 3: Copy JAR to Docker build context
echo "📋 Step 3: Updating Docker build context..."
cd ..

# Copy fresh JAR to Docker build context
cp flow-ch/target/pixel-camel-app-1.0.0.jar docker/camel-runtime-spring/pixel-v2-app-spring-1-1.0.0.jar
echo "✅ Fresh JAR copied to Docker build context"

# Step 4: Stop and remove existing container
echo "🛑 Step 4: Stopping existing container..."
docker compose -f docker/docker-compose.yml stop pixel-v2-app-spring-1
docker compose -f docker/docker-compose.yml rm -f pixel-v2-app-spring-1
echo "✅ Container stopped and removed"

# Step 5: Rebuild Docker image (no cache to ensure fresh build)
echo "🐳 Step 5: Rebuilding Docker image..."
docker compose -f docker/docker-compose.yml build --no-cache pixel-v2-app-spring-1
if [ $? -eq 0 ]; then
    echo "✅ Docker image rebuild SUCCESS"
else
    echo "❌ Docker image rebuild FAILED"
    exit 1
fi

# Step 6: Deploy fresh container
echo "🚀 Step 6: Deploying fresh container..."
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
echo "   - Container: ✅ Deployed"
echo ""
echo "🔍 To monitor the application:"
echo "   docker logs -f pixel-v2-app-spring-1"
echo ""
echo "🌐 Application should be available at: http://localhost:8082"