#!/bin/bash
# PIXEL-V2 Referential Service Manager
# Combined script to start/stop the referential service

set -e

# Get the script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

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
ACTION=""

# Docker image and container names
IMAGE_NAME="pixel-v2/referential-runtime:latest"
CONTAINER_NAME="pixel-v2-referential"

# Function to display usage
usage() {
    echo "Usage: $0 [start|stop|restart] [OPTIONS]"
    echo ""
    echo "Actions:"
    echo "  start                Start the referential service"
    echo "  stop                 Stop the referential service"
    echo "  restart              Restart the referential service"
    echo ""
    echo "Options (for start/restart):"
    echo "  --build-only         Build the referential service without starting"
    echo "  --rebuild            Force rebuild the referential service"
    echo "  --logs               Follow logs after starting"
    echo "  -h, --help           Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start             # Start referential service with dependencies"
    echo "  $0 stop              # Stop the service"
    echo "  $0 restart --logs    # Restart and follow logs"
    echo "  $0 start --rebuild   # Rebuild and start"
}

# Function to stop referential service
stop_referential() {
    echo -e "${BLUE}=== PIXEL-V2 Referential Shutdown ===${NC}"
    
    # Stop docker container
    if docker ps --filter name=${CONTAINER_NAME} --format "{{.Names}}" | grep -q ${CONTAINER_NAME}; then
        echo -e "${YELLOW}🛑 Stopping referential docker container...${NC}"
        docker stop ${CONTAINER_NAME}
        echo -e "${GREEN}✅ Docker container stopped${NC}"
    else
        echo -e "${BLUE}ℹ️  No running docker container found${NC}"
    fi
    
    # Check and kill processes on port 8099
    if lsof -ti:8099 > /dev/null 2>&1; then
        echo -e "${YELLOW}🔍 Found processes on port 8099 - stopping them...${NC}"
        lsof -ti:8099 | xargs kill -9 2>/dev/null || true
        sleep 2
        
        # Double check
        if lsof -ti:8099 > /dev/null 2>&1; then
            echo -e "${RED}⚠️  Port 8099 still in use after kill attempt${NC}"
            lsof -ti:8099 | xargs kill -9 2>/dev/null || true
            sleep 3
        fi
    fi
    
    # Kill any remaining referential java processes
    echo -e "${YELLOW}🔍 Looking for Referential Java processes...${NC}"
    local pids=$(ps aux | grep -i "referentialapplication\|referential.*spring-boot" | grep -v grep | awk '{print $2}')
    
    if [ ! -z "$pids" ]; then
        echo -e "${YELLOW}Found Referential processes with PIDs: $pids${NC}"
        for pid in $pids; do
            echo -e "${YELLOW}Killing process $pid${NC}"
            kill -9 $pid 2>/dev/null || true
        done
    else
        echo -e "${BLUE}ℹ️  No Referential Java processes found${NC}"
    fi
    
    # Final verification
    sleep 2
    if lsof -ti:8099 > /dev/null 2>&1; then
        echo -e "${RED}  WARNING: Port 8099 is still in use!${NC}"
        lsof -ti:8099 | head -5
    else
        echo -e "${GREEN}✅ Port 8099 is now free${NC}"
    fi
    
    echo -e "${GREEN} Referential shutdown complete${NC}"
}

# Function to start referential service
start_referential() {
    echo -e "${BLUE} PIXEL-V2 Referential Service Startup${NC}"
    
    # Check if we're in the correct directory
    if [[ ! -f "${DOCKER_DIR}/referential-runtime/Dockerfile" ]]; then
        echo -e "${RED} Error: ${DOCKER_DIR}/referential-runtime/Dockerfile not found${NC}"
        echo -e "${YELLOW}Please run this script from the pixel-v2 root directory${NC}"
        exit 1
    fi
    
    # Stop any existing instance first
    stop_referential
    sleep 2
    
    # Build the service
    if [[ "$REBUILD" == true ]]; then
        echo -e "${YELLOW}🔨 Force rebuilding referential service...${NC}"
        docker build --no-cache -f ${DOCKER_DIR}/referential-runtime/Dockerfile -t ${IMAGE_NAME} .
    else
        echo -e "${GREEN}🔨 Building referential service...${NC}"
        docker build -f ${DOCKER_DIR}/referential-runtime/Dockerfile -t ${IMAGE_NAME} .
    fi
    
    if [[ $? -ne 0 ]]; then
        echo -e "${RED} Build failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN} Build completed successfully${NC}"
    
    # Exit if build-only mode
    if [[ "$BUILD_ONLY" == true ]]; then
        echo -e "${BLUE} Build complete. Use '$0 start' to start the service.${NC}"
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
    
    # Start referential service
    echo -e "${GREEN} Starting referential service...${NC}"
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
        echo -e "${GREEN} Referential service started successfully${NC}"
        
        # Show service status
        echo -e "${BLUE} Service status:${NC}"
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
        echo "   Stop service: $0 stop"
        echo "   Restart service: $0 restart"
        echo -e "${NC}"
        
        # Follow logs if requested
        if [[ "$LOGS_FOLLOW" == true ]]; then
            echo -e "${BLUE}📄 Following logs (Ctrl+C to stop)...${NC}"
            docker logs -f ${CONTAINER_NAME}
        fi
    else
        echo -e "${RED} Failed to start referential service${NC}"
        echo -e "${YELLOW}Check logs with: docker logs ${CONTAINER_NAME}${NC}"
        exit 1
    fi
}

# Function to restart referential service
restart_referential() {
    echo -e "${YELLOW}🔄 Restarting referential service...${NC}"
    stop_referential
    sleep 3
    start_referential
}

# Parse command line arguments
if [[ $# -eq 0 ]]; then
    usage
    exit 1
fi

# First argument should be the action
case $1 in
    start|stop|restart)
        ACTION=$1
        shift
        ;;
    -h|--help)
        usage
        exit 0
        ;;
    *)
        echo -e "${RED} Unknown action: $1${NC}"
        echo -e "${YELLOW}Valid actions: start, stop, restart${NC}"
        usage
        exit 1
        ;;
esac

# Parse remaining options
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
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED} Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Execute the requested action
case $ACTION in
    start)
        start_referential
        ;;
    stop)
        stop_referential
        ;;
    restart)
        restart_referential
        ;;
esac