#!/bin/bash
# Quick start script for referentiel service

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
BUILD_ONLY=false
LOGS_FOLLOW=false
REBUILD=false
DOCKER_DIR="docker"

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  --build-only          Build the referentiel service without starting"
    echo "  --rebuild             Force rebuild the referentiel service"
    echo "  --logs                Follow logs after starting"
    echo "  --stop               Stop the referentiel service"
    echo "  --restart            Restart the referentiel service"
    echo "  -h, --help           Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0                    # Start referentiel service with dependencies"
    echo "  $0 --build-only      # Only build the service"
    echo "  $0 --rebuild --logs  # Rebuild and follow logs"
    echo "  $0 --stop            # Stop the service"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --build-only)
            BUILD_ONLY=true
            shift
            ;;
        --rebuild)
            REBUILD=true
            shift
            ;;
        --logs)
            LOGS_FOLLOW=true
            shift
            ;;
        --stop)
            echo -e "${YELLOW}🛑 Stopping referentiel service...${NC}"
            docker stop ${CONTAINER_NAME}
            exit 0
            ;;
        --restart)
            echo -e "${YELLOW}🔄 Restarting referentiel service...${NC}"
            docker restart ${CONTAINER_NAME}
            if [[ "$LOGS_FOLLOW" == true ]]; then
                docker logs -f ${CONTAINER_NAME}
            fi
            exit 0
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

echo "🐳 PIXEL-V2 Referentiel Service Manager"

# Check if we're in the correct directory
if [[ ! -f "${DOCKER_DIR}/referentiel-runtime/Dockerfile" ]]; then
    echo -e "${RED}Error: ${DOCKER_DIR}/referentiel-runtime/Dockerfile not found${NC}"
    echo -e "${YELLOW}Please run this script from the pixel-v2 root directory${NC}"
    exit 1
fi

# Docker image and container names
IMAGE_NAME="pixel-v2/referentiel-runtime:latest"
CONTAINER_NAME="pixel-v2-referentiel"

# Build options
BUILD_OPTS=""
if [[ "$REBUILD" == true ]]; then
    BUILD_OPTS="--no-cache"
    echo -e "${YELLOW}🔨 Force rebuilding referentiel service...${NC}"
else
    echo -e "${GREEN}🔨 Building referentiel service...${NC}"
fi

# Build the service
if [[ "$REBUILD" == true ]]; then
    docker build --no-cache -f ${DOCKER_DIR}/referentiel-runtime/Dockerfile -t ${IMAGE_NAME} .
else
    docker build -f ${DOCKER_DIR}/referentiel-runtime/Dockerfile -t ${IMAGE_NAME} .
fi

if [[ $? -ne 0 ]]; then
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build completed successfully${NC}"

# Exit if build-only mode
if [[ "$BUILD_ONLY" == true ]]; then
    echo -e "${BLUE}📦 Build complete. Use 'docker run -d --name ${CONTAINER_NAME} --network pixel-v2-network -p 8099:8099 ${IMAGE_NAME}' to start the service.${NC}"
    exit 0
fi

# Start dependencies first
echo -e "${BLUE}🚀 Starting dependencies (PostgreSQL)...${NC}"
docker-compose -f ${DOCKER_DIR}/docker-compose.yml up -d postgresql

# Wait for PostgreSQL to be ready
echo -e "${YELLOW}⏳ Waiting for PostgreSQL to be ready...${NC}"
sleep 10

# Remove existing container if it exists
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

# Start referentiel service
echo -e "${GREEN}🚀 Starting referentiel service...${NC}"
docker run -d \
  --name ${CONTAINER_NAME} \
  --network pixel-v2-network \
  -p 8099:8099 \
  -e DATABASE_URL=jdbc:postgresql://postgresql:5432/pixelv2 \
  -e DATABASE_USERNAME=pixelv2 \
  -e DATABASE_PASSWORD=pixelv2_secure_password \
  -e ALLOWED_ORIGINS="http://localhost:*,http://127.0.0.1:*" \
  --restart unless-stopped \
  ${IMAGE_NAME}

if [[ $? -eq 0 ]]; then
    echo -e "${GREEN}✅ Referentiel service started successfully${NC}"
    
    # Show service status
    echo -e "${BLUE}📊 Service status:${NC}"
    docker ps --filter name=${CONTAINER_NAME} --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    # Show useful URLs
    echo -e "${YELLOW}"
    echo "🔗 Service URLs:"
    echo "   Health Check: http://localhost:8099/actuator/health"
    echo "   Application Info: http://localhost:8099/actuator/info"
    echo "   Flow API Example: http://localhost:8099/api/flows/ICHSIC/complete"
    echo ""
    echo "📝 Useful commands:"
    echo "   View logs: docker logs -f ${CONTAINER_NAME}"
    echo "   Stop service: docker stop ${CONTAINER_NAME}"
    echo "   Restart service: docker restart ${CONTAINER_NAME}"
    echo "   Remove service: docker rm -f ${CONTAINER_NAME}"
    echo -e "${NC}"
    
    # Follow logs if requested
    if [[ "$LOGS_FOLLOW" == true ]]; then
        echo -e "${BLUE}📄 Following logs (Ctrl+C to stop)...${NC}"
        docker logs -f ${CONTAINER_NAME}
    fi
else
    echo -e "${RED}❌ Failed to start referentiel service${NC}"
    echo -e "${YELLOW}Check logs with: docker logs ${CONTAINER_NAME}${NC}"
    exit 1
fi