#!/bin/bash

# Build and Deploy PIXEL-V2 Camel Application
# This script builds the standalone Spring Boot Camel application

set -e

echo "Building PIXEL-V2 Camel Application..."

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
CAMEL_APP_DIR="$PROJECT_ROOT/flow-ch"

echo "Script directory: $SCRIPT_DIR"
echo "Project root: $PROJECT_ROOT"
echo "Flow-CH directory: $CAMEL_APP_DIR"

# Check if flow-ch directory exists
if [ ! -d "$CAMEL_APP_DIR" ]; then
    echo "Error: flow-ch directory not found at $CAMEL_APP_DIR"
    exit 1
fi

# Run technical framework setup
echo "Setting up technical framework..."
cd "$CAMEL_APP_DIR"
if [ -f "setup-technical-framework.sh" ]; then
    chmod +x setup-technical-framework.sh
    ./setup-technical-framework.sh
else
    echo "Warning: setup-technical-framework.sh not found, skipping..."
fi

# Build the application JAR
echo "Building flow-ch application..."
mvn clean package -DskipTests

# Build Docker image using project root as context
echo "Building Docker image..."
cd "$PROJECT_ROOT"
docker build -f docker/camel-runtime-spring/Dockerfile -t pixel-ch-app:latest .

echo "Application built successfully!"
echo ""
echo "To run the application:"
echo "1. cd ../  # Go to docker directory"  
echo "2. docker-compose up pixel-ch-app -d"
echo ""
echo "Available endpoints:"
echo "- Application: http://localhost:8082"
echo "- Health Check: http://localhost:8082/actuator/health" 
echo "- Metrics: http://localhost:8082/actuator/metrics"
echo "- Camel Routes: http://localhost:8082/actuator/camel"
echo "- JMX: localhost:8782"