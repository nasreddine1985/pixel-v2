#!/bin/bash
# PIXEL-V2 Referentiel Local Service Manager
# Script to start/stop the referentiel service locally using Maven

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REFERENTIEL_DIR="referentiel"
REFERENTIEL_PORT=8099
PID_FILE="/tmp/referentiel-local.pid"
LOG_FILE="/tmp/referentiel-local.log"
ACTION=""
BACKGROUND=false
CLEAN_BUILD=false

# Function to display usage
usage() {
    echo "Usage: $0 [start|stop|restart|status] [OPTIONS]"
    echo ""
    echo "Actions:"
    echo "  start                Start the referentiel service locally"
    echo "  stop                 Stop the referentiel service"
    echo "  restart              Restart the referentiel service"
    echo "  status               Show service status"
    echo ""
    echo "Options (for start/restart):"
    echo "  --background, -b     Run in background (daemon mode)"
    echo "  --clean              Clean build before starting"
    echo "  --logs               Show logs after starting (only in background mode)"
    echo "  -h, --help           Display this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start             # Start referentiel service in foreground"
    echo "  $0 start -b          # Start in background"
    echo "  $0 stop              # Stop the service"
    echo "  $0 restart --clean   # Clean rebuild and restart"
    echo "  $0 status            # Check service status"
}

# Function to check if port is in use
check_port() {
    if lsof -ti:${REFERENTIEL_PORT} > /dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to get process PID by port
get_pid_by_port() {
    lsof -ti:${REFERENTIEL_PORT} 2>/dev/null || echo ""
}

# Function to check if referentiel process is running
is_referentiel_running() {
    if [[ -f "$PID_FILE" ]]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            # Check if it's actually our Java process
            if ps -p "$pid" -o command= | grep -q "referentiel"; then
                return 0  # Running
            fi
        fi
        # PID file exists but process is not running, clean up
        rm -f "$PID_FILE"
    fi
    
    # Also check by port
    if check_port; then
        local port_pid=$(get_pid_by_port)
        if [[ -n "$port_pid" ]] && ps -p "$port_pid" -o command= | grep -q "referentiel"; then
            return 0  # Running
        fi
    fi
    
    return 1  # Not running
}

# Function to stop referentiel service
stop_referentiel() {
    echo -e "${BLUE}=== PIXEL-V2 Referentiel Local Shutdown ===${NC}"
    
    local stopped=false
    
    # Check PID file first
    if [[ -f "$PID_FILE" ]]; then
        local pid=$(cat "$PID_FILE")
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${YELLOW}🛑 Stopping referentiel process (PID: $pid)...${NC}"
            kill -TERM "$pid" 2>/dev/null || true
            sleep 3
            
            # Check if still running, force kill
            if ps -p "$pid" > /dev/null 2>&1; then
                echo -e "${YELLOW}⚡ Force killing referentiel process...${NC}"
                kill -KILL "$pid" 2>/dev/null || true
                sleep 1
            fi
            
            stopped=true
        fi
        rm -f "$PID_FILE"
    fi
    
    # Check by port and kill any remaining processes
    if check_port; then
        echo -e "${YELLOW}🔍 Looking for processes on port ${REFERENTIEL_PORT}...${NC}"
        local port_pids=$(get_pid_by_port)
        if [[ -n "$port_pids" ]]; then
            echo -e "${YELLOW}⚡ Killing processes on port ${REFERENTIEL_PORT}: $port_pids${NC}"
            echo "$port_pids" | xargs kill -9 2>/dev/null || true
            sleep 2
            stopped=true
        fi
    fi
    
    # Final port check
    if check_port; then
        echo -e "${YELLOW}ℹ️  Port ${REFERENTIEL_PORT} still in use${NC}"
    else
        echo -e "${GREEN}✅ Port ${REFERENTIEL_PORT} is now free${NC}"
    fi
    
    if [[ "$stopped" == true ]]; then
        echo -e "${GREEN}🎉 Referentiel local service stopped${NC}"
    else
        echo -e "${BLUE}ℹ️  No running referentiel process found${NC}"
    fi
    
    # Clean up log file if exists
    if [[ -f "$LOG_FILE" ]]; then
        rm -f "$LOG_FILE"
    fi
}

# Function to show service status
show_status() {
    echo -e "${BLUE}=== PIXEL-V2 Referentiel Local Status ===${NC}"
    
    if is_referentiel_running; then
        if [[ -f "$PID_FILE" ]]; then
            local pid=$(cat "$PID_FILE")
            echo -e "${GREEN}✅ Referentiel is running (PID: $pid)${NC}"
        else
            local port_pid=$(get_pid_by_port)
            echo -e "${GREEN}✅ Referentiel is running on port ${REFERENTIEL_PORT} (PID: $port_pid)${NC}"
        fi
        
        echo -e "${BLUE}📊 Process information:${NC}"
        if check_port; then
            ps -p "$(get_pid_by_port)" -o pid,ppid,etime,pcpu,pmem,cmd 2>/dev/null || echo "Process details unavailable"
        fi
        
        echo -e "${BLUE}🔗 Service URL: http://localhost:${REFERENTIEL_PORT}/api/referentiel${NC}"
    else
        echo -e "${YELLOW}❌ Referentiel is not running${NC}"
        echo -e "${BLUE}📊 Port ${REFERENTIEL_PORT} status: $(check_port && echo "In use by another process" || echo "Free")${NC}"
    fi
}

# Function to start referentiel service
start_referentiel() {
    echo -e "${BLUE}🚀 PIXEL-V2 Referentiel Local Service Startup${NC}"
    
    # Determine the correct project directory
    local project_dir=""
    if [[ -f "referentiel/pom.xml" ]]; then
        # We're in the pixel-v2 root directory
        project_dir="."
    elif [[ -f "../referentiel/pom.xml" ]]; then
        # We're in a subdirectory (like scripts)
        project_dir=".."
    elif [[ -f "../../referentiel/pom.xml" ]]; then
        # We're in a deeper subdirectory
        project_dir="../.."
    else
        echo -e "${RED}❌ Error: Cannot find referentiel/pom.xml${NC}"
        echo -e "${YELLOW}Please ensure you're running this script from the pixel-v2 directory or its subdirectories${NC}"
        exit 1
    fi
    
    # Change to project root directory
    cd "$project_dir"
    project_dir=$(pwd)
    echo -e "${BLUE}📁 Working directory: $project_dir${NC}"
    
    # Update referentiel directory path
    REFERENTIEL_DIR="referentiel"
    
    # Check if already running
    if is_referentiel_running; then
        echo -e "${YELLOW}⚠️  Referentiel service is already running${NC}"
        show_status
        return 0
    fi
    
    # Stop any existing instance first
    stop_referentiel
    sleep 2
    
    # Build the service
    echo -e "${GREEN}🔨 Building referentiel service...${NC}"
    cd "${REFERENTIEL_DIR}"
    
    if [[ "$CLEAN_BUILD" == true ]]; then
        echo -e "${YELLOW}🧹 Performing clean build...${NC}"
        mvn clean compile -q
    else
        mvn compile -q
    fi
    
    if [[ $? -ne 0 ]]; then
        echo -e "${RED}❌ Build failed${NC}"
        cd ..
        exit 1
    fi
    
    echo -e "${GREEN}✅ Build completed successfully${NC}"
    
    # Check PostgreSQL dependency
    echo -e "${BLUE}🔍 Checking PostgreSQL dependency...${NC}"
    if ! docker ps | grep -q "pixel-v2-postgresql"; then
        echo -e "${YELLOW}⚠️  PostgreSQL container not running. Starting...${NC}"
        current_dir=$(pwd)
        docker-compose -f docker/docker-compose.yml up -d postgresql
        echo -e "${YELLOW}⏳ Waiting for PostgreSQL to be ready...${NC}"
        sleep 10
        cd "$current_dir/${REFERENTIEL_DIR}"
    else
        echo -e "${GREEN}✅ PostgreSQL is running${NC}"
    fi
    
    # Start the application
    echo -e "${GREEN}🚀 Starting referentiel service locally...${NC}"
    
    if [[ "$BACKGROUND" == true ]]; then
        echo -e "${BLUE}🔄 Starting in background mode...${NC}"
        nohup mvn spring-boot:run > "$LOG_FILE" 2>&1 &
        local pid=$!
        echo "$pid" > "$PID_FILE"
        
        # Wait a bit and check if process started successfully
        sleep 5
        if ps -p "$pid" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Referentiel service started in background (PID: $pid)${NC}"
            echo -e "${BLUE}📋 Log file: $LOG_FILE${NC}"
            echo -e "${BLUE}🔗 Service URL: http://localhost:${REFERENTIEL_PORT}/api/referentiel${NC}"
            
            # Wait a bit more for startup
            echo -e "${YELLOW}⏳ Waiting for service to be ready...${NC}"
            sleep 10
            
            # Test the service
            if curl -s -f "http://localhost:${REFERENTIEL_PORT}/api/referentiel/flows/ICHSIC/complete" > /dev/null 2>&1; then
                echo -e "${GREEN}✅ Service is responding correctly${NC}"
            else
                echo -e "${YELLOW}⚠️  Service may still be starting up${NC}"
            fi
        else
            echo -e "${RED}❌ Failed to start referentiel service${NC}"
            rm -f "$PID_FILE"
            exit 1
        fi
    else
        echo -e "${BLUE}🔄 Starting in foreground mode...${NC}"
        echo -e "${YELLOW}📝 Press Ctrl+C to stop the service${NC}"
        echo -e "${BLUE}🔗 Service URL: http://localhost:${REFERENTIEL_PORT}/api/referentiel${NC}"
        echo ""
        
        # Trap Ctrl+C to clean shutdown
        trap 'echo -e "\n${YELLOW}🛑 Shutting down...${NC}"; stop_referentiel; exit 0' SIGINT SIGTERM
        
        mvn spring-boot:run
    fi
}

# Function to restart referentiel service
restart_referentiel() {
    echo -e "${BLUE}🔄 PIXEL-V2 Referentiel Local Service Restart${NC}"
    stop_referentiel
    sleep 3
    start_referentiel
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        start)
            ACTION="start"
            shift
            ;;
        stop)
            ACTION="stop"
            shift
            ;;
        restart)
            ACTION="restart"
            shift
            ;;
        status)
            ACTION="status"
            shift
            ;;
        --background|-b)
            BACKGROUND=true
            shift
            ;;
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --logs)
            SHOW_LOGS=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Validate action
if [[ -z "$ACTION" ]]; then
    echo -e "${RED}❌ No action specified${NC}"
    usage
    exit 1
fi

# Execute action
case $ACTION in
    start)
        start_referentiel
        if [[ "$BACKGROUND" == true ]] && [[ "$SHOW_LOGS" == true ]]; then
            echo -e "${BLUE}📋 Following logs...${NC}"
            tail -f "$LOG_FILE"
        fi
        ;;
    stop)
        stop_referentiel
        ;;
    restart)
        restart_referentiel
        if [[ "$BACKGROUND" == true ]] && [[ "$SHOW_LOGS" == true ]]; then
            echo -e "${BLUE}📋 Following logs...${NC}"
            tail -f "$LOG_FILE"
        fi
        ;;
    status)
        show_status
        ;;
    *)
        echo -e "${RED}❌ Invalid action: $ACTION${NC}"
        usage
        exit 1
        ;;
esac