#!/bin/bash

# PIXEL-V2 Application Log Viewer Script
# Shows logs for pixel-v2-app-spring-1 container with various options

CONTAINER_NAME="pixel-v2-app-spring-1"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to display help
show_help() {
    echo -e "${BLUE}PIXEL-V2 Log Viewer${NC}"
    echo -e "${BLUE}===================${NC}"
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -f, --follow         Follow log output (like tail -f)"
    echo "  -t, --tail N         Show last N lines (default: 50)"
    echo "  -s, --since TIME     Show logs since timestamp (e.g., '1h', '30m', '2m')"
    echo "  -g, --grep PATTERN   Filter logs containing pattern"
    echo "  -e, --errors         Show only ERROR and WARN messages"
    echo "  -k, --kafka          Show Kafka-related logs"
    echo "  -m, --mq             Show MQ/JMS-related logs"
    echo "  -p, --processing     Show message processing logs"
    echo "  -r, --routes         Show route startup and processing logs"
    echo "  -a, --all            Show all logs (no tail limit)"
    echo "  -h, --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                   # Show last 50 lines"
    echo "  $0 -f                # Follow logs in real-time"
    echo "  $0 -t 100            # Show last 100 lines"
    echo "  $0 -s 5m             # Show logs from last 5 minutes"
    echo "  $0 -e                # Show only errors and warnings"
    echo "  $0 -k -f             # Follow Kafka-related logs"
    echo "  $0 -g 'PACS008'      # Show logs containing 'PACS008'"
    echo "  $0 -p -s 2m          # Show processing logs from last 2 minutes"
}

# Check if container is running
check_container() {
    if ! docker ps --format "table {{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        echo -e "${RED}❌ Container ${CONTAINER_NAME} is not running!${NC}"
        echo -e "${YELLOW}💡 Try: docker ps | grep pixel-v2${NC}"
        exit 1
    fi
}

# Default values
FOLLOW=false
TAIL_LINES=50
SINCE=""
GREP_PATTERN=""
SHOW_ALL=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--follow)
            FOLLOW=true
            shift
            ;;
        -t|--tail)
            TAIL_LINES="$2"
            shift 2
            ;;
        -s|--since)
            SINCE="$2"
            shift 2
            ;;
        -g|--grep)
            GREP_PATTERN="$2"
            shift 2
            ;;
        -e|--errors)
            GREP_PATTERN="ERROR|WARN|Exception|Failed"
            shift
            ;;
        -k|--kafka)
            GREP_PATTERN="kafka|Kafka|Publishing|Published|topic|k-kafka-message-publisher|flow-summary"
            shift
            ;;
        -m|--mq)
            GREP_PATTERN="JMS|ActiveMQ|k-mq-starter|Message received|queue"
            shift
            ;;
        -p|--processing)
            GREP_PATTERN="Processing|Received PACS008|K-MQ-Starter|Message Body|k-log-flow-summary"
            shift
            ;;
        -r|--routes)
            GREP_PATTERN="Route.*started|Route.*consuming|Routes startup|kamelet"
            shift
            ;;
        -a|--all)
            SHOW_ALL=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Check if container is running
check_container

# Build docker logs command
CMD="docker logs"

if [ "$FOLLOW" = true ]; then
    CMD="$CMD -f"
fi

if [ -n "$SINCE" ]; then
    CMD="$CMD --since=$SINCE"
elif [ "$SHOW_ALL" = false ]; then
    CMD="$CMD --tail=$TAIL_LINES"
fi

CMD="$CMD $CONTAINER_NAME"

# Add grep filter if specified
if [ -n "$GREP_PATTERN" ]; then
    CMD="$CMD | grep -E \"$GREP_PATTERN\""
fi

# Display header
echo -e "${GREEN}🚀 PIXEL-V2 Application Logs${NC}"
echo -e "${GREEN}============================${NC}"
echo -e "${BLUE}Container:${NC} $CONTAINER_NAME"

if [ "$FOLLOW" = true ]; then
    echo -e "${BLUE}Mode:${NC} Following logs (Ctrl+C to stop)"
else
    if [ -n "$SINCE" ]; then
        echo -e "${BLUE}Time Range:${NC} Since $SINCE"
    elif [ "$SHOW_ALL" = false ]; then
        echo -e "${BLUE}Lines:${NC} Last $TAIL_LINES"
    else
        echo -e "${BLUE}Lines:${NC} All logs"
    fi
fi

if [ -n "$GREP_PATTERN" ]; then
    echo -e "${BLUE}Filter:${NC} $GREP_PATTERN"
fi

echo -e "${GREEN}============================${NC}"
echo ""

# Execute the command
if [ -n "$GREP_PATTERN" ]; then
    eval "$CMD"
else
    eval "$CMD"
fi