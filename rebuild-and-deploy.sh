#!/bin/bash

echo "🏗️  Building new application JAR..."
cd flow-ch && mvn clean package -DskipTests

echo "📦  Copying JAR and rebuilding Docker image..."
cd ..
cp flow-ch/target/pixel-camel-app-1.0.0.jar docker/camel-runtime-spring/pixel-v2-app-spring-1-1.0.0.jar
docker compose -f docker/docker-compose.yml build --no-cache pixel-v2-app-spring-1

echo "🚀  Deploying new container..."
docker compose -f docker/docker-compose.yml up -d pixel-v2-app-spring-1

echo "✅  Deployment complete!"
echo "JAR info:"
docker exec pixel-v2-app-spring-1 ls -la /opt/pixel-v2-app-spring-1/pixel-v2-app-spring-1.jar