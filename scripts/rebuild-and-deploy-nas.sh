#!/bin/bash

# PIXEL-V2 NAS Management Script
# Manages NAS container and lists CH files

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAS_CONTAINER="pixel-v2-nas"
DOCKER_COMPOSE_FILE="docker/docker-compose.yml"
SMB_HOST="localhost"
SMB_SHARE="CH"
SMB_USER="pixel"
SMB_PASS="pixel"

echo -e "${BLUE}🗄️  PIXEL-V2 NAS Management${NC}"
echo "=================================="

# Function to check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
        exit 1
    fi
}

# Function to restart NAS container
restart_nas() {
    echo -e "${YELLOW}🔄 Restarting NAS container...${NC}"
    
    # Stop the container if it's running
    if docker ps -q -f name=$NAS_CONTAINER | grep -q .; then
        echo "Stopping $NAS_CONTAINER..."
        docker-compose -f $DOCKER_COMPOSE_FILE stop nas
    fi
    
    # Remove the container if it exists
    if docker ps -aq -f name=$NAS_CONTAINER | grep -q .; then
        echo "Removing $NAS_CONTAINER..."
        docker-compose -f $DOCKER_COMPOSE_FILE rm -f nas
    fi
    
    # Start the NAS container
    echo "Starting $NAS_CONTAINER..."
    docker-compose -f $DOCKER_COMPOSE_FILE up -d nas
    
    # Wait for container to be ready
    echo "Waiting for NAS to be ready..."
    sleep 5
    
    # Fix permissions for application access (pixel user: UID=100, GID=101)
    echo "Fixing NAS directory permissions..."
    docker exec $NAS_CONTAINER chown -R 100:101 /CH /shared /data
    
    # Check if container is running
    if docker ps -q -f name=$NAS_CONTAINER | grep -q .; then
        echo -e "${GREEN}✅ NAS container restarted successfully${NC}"
    else
        echo -e "${RED}❌ Failed to start NAS container${NC}"
        exit 1
    fi
}

# Function to list CH files using smbclient
list_ch_files() {
    echo -e "${YELLOW}📁 Listing files in SMB CH share...${NC}"
    echo "SMB URL: smb://$SMB_USER:$SMB_PASS@$SMB_HOST/$SMB_SHARE"
    echo ""
    
    # Check if smbclient is available
    if ! command -v smbclient &> /dev/null; then
        echo -e "${RED}❌ smbclient not found. Installing via brew...${NC}"
        if command -v brew &> /dev/null; then
            brew install samba
        else
            echo -e "${RED}❌ Please install smbclient manually: brew install samba${NC}"
            echo "Alternative: Use docker exec method below"
            list_ch_files_docker
            return
        fi
    fi
    
    # List files using smbclient
    echo -e "${BLUE}📋 Files in CH share:${NC}"
    smbclient //$SMB_HOST/$SMB_SHARE -U $SMB_USER%$SMB_PASS -c "ls" 2>/dev/null || {
        echo -e "${YELLOW}⚠️  Direct smbclient failed, trying docker method...${NC}"
        list_ch_files_docker
    }
}

# Function to list CH files using docker exec
list_ch_files_docker() {
    echo -e "${BLUE}📋 Files in CH share (via docker):${NC}"
    
    # Check if NAS container is running
    if ! docker ps -q -f name=$NAS_CONTAINER | grep -q .; then
        echo -e "${RED}❌ NAS container is not running${NC}"
        return 1
    fi
    
    # List files inside the container
    docker exec $NAS_CONTAINER find /CH -type f -exec ls -la {} \; 2>/dev/null || {
        echo -e "${YELLOW}ℹ️  CH directory is empty or not accessible${NC}"
    }
    
    # Show directory structure
    echo ""
    echo -e "${BLUE}📁 CH directory structure:${NC}"
    docker exec $NAS_CONTAINER ls -la /CH 2>/dev/null || {
        echo -e "${YELLOW}ℹ️  CH directory not found${NC}"
    }
}

# Function to list files in /opt/nas/CH/IN directory
list_in_files() {
    echo -e "${YELLOW}📁 Listing files in /opt/nas/CH/IN directory...${NC}"
    echo ""
    
    # Check if application container is running
    if docker ps -q -f name=pixel-v2-app-spring-1 | grep -q .; then
        echo -e "${BLUE}📋 Files in /opt/nas/CH/IN:${NC}"
        docker exec pixel-v2-app-spring-1 find /opt/nas/CH/IN -type f 2>/dev/null | head -20 || {
            echo -e "${YELLOW}📂 Directory /opt/nas/CH/IN is empty or doesn't exist${NC}"
        }
        echo ""
        echo -e "${BLUE}📊 Directory structure:${NC}"
        docker exec pixel-v2-app-spring-1 ls -la /opt/nas/CH/IN/ 2>/dev/null || {
            echo -e "${YELLOW}📂 IN directory not created yet${NC}"
        }
    else
        echo -e "${RED}❌ Application container (pixel-v2-app-spring-1) is not running${NC}"
        return 1
    fi
}

# Function to list files in /opt/nas/CH/OUT directory
list_out_files() {
    echo -e "${YELLOW}📁 Listing files in /opt/nas/CH/OUT directory...${NC}"
    echo ""
    
    # Check if application container is running
    if docker ps -q -f name=pixel-v2-app-spring-1 | grep -q .; then
        echo -e "${BLUE}📋 Files in /opt/nas/CH/OUT:${NC}"
        docker exec pixel-v2-app-spring-1 find /opt/nas/CH/OUT -type f 2>/dev/null | head -20 || {
            echo -e "${YELLOW}📂 Directory /opt/nas/CH/OUT is empty or doesn't exist${NC}"
        }
        echo ""
        echo -e "${BLUE}📊 Directory structure:${NC}"
        docker exec pixel-v2-app-spring-1 ls -la /opt/nas/CH/OUT/ 2>/dev/null || {
            echo -e "${YELLOW}📂 OUT directory not created yet${NC}"
        }
    else
        echo -e "${RED}❌ Application container (pixel-v2-app-spring-1) is not running${NC}"
        return 1
    fi
}

# Function to show NAS status
show_nas_status() {
    echo -e "${BLUE}📊 NAS Container Status:${NC}"
    if docker ps -q -f name=$NAS_CONTAINER | grep -q .; then
        echo -e "${GREEN}✅ Status: Running${NC}"
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" -f name=$NAS_CONTAINER
    else
        echo -e "${RED}❌ Status: Not Running${NC}"
    fi
    echo ""
}

# Function to test SMB connection
test_smb_connection() {
    echo -e "${YELLOW}🔗 Testing SMB connection...${NC}"
    
    # Test using smbclient if available
    if command -v smbclient &> /dev/null; then
        if smbclient -L $SMB_HOST -U $SMB_USER%$SMB_PASS &>/dev/null; then
            echo -e "${GREEN}✅ SMB connection successful${NC}"
            echo "Available shares:"
            smbclient -L $SMB_HOST -U $SMB_USER%$SMB_PASS 2>/dev/null | grep "Disk" || echo "No shares found"
        else
            echo -e "${RED}❌ SMB connection failed${NC}"
        fi
    else
        echo -e "${YELLOW}ℹ️  smbclient not available, skipping connection test${NC}"
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  restart    Restart the NAS container"
    echo "  list       List files in CH SMB share"
    echo "  list-in    List files in /opt/nas/CH/IN directory"
    echo "  list-out   List files in /opt/nas/CH/OUT directory"
    echo "  status     Show NAS container status"
    echo "  test       Test SMB connection"
    echo "  all        Restart NAS and list files (default)"
    echo "  help       Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                # Restart NAS and list files"
    echo "  $0 restart        # Only restart NAS"
    echo "  $0 list           # Only list CH files"
    echo "  $0 list-in        # List files in IN directory"
    echo "  $0 list-out       # List files in OUT directory"
    echo "  $0 status         # Show NAS status"
}

# Main execution
main() {
    check_docker
    
    case ${1:-all} in
        "restart")
            restart_nas
            ;;
        "list")
            show_nas_status
            list_ch_files
            ;;
        "list-in")
            list_in_files
            ;;
        "list-out")
            list_out_files
            ;;
        "status")
            show_nas_status
            ;;
        "test")
            show_nas_status
            test_smb_connection
            ;;
        "all")
            show_nas_status
            restart_nas
            echo ""
            list_ch_files
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            echo -e "${RED}❌ Invalid option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"

echo ""
echo -e "${GREEN}🎉 NAS management completed!${NC}"