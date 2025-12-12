#!/bin/bash

# Test Redis Cache Refresh for PIXEL-V2
# This script sends cache refresh events to test the caching mechanism

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Testing Redis Cache Refresh for PIXEL-V2${NC}"
echo "=================================================="

# Function to send refresh event
send_refresh_event() {
    local flow_code=$1
    local action=${2:-"REFRESH_FLOW"}
    
    echo -e "${YELLOW}📤 Sending $action event for flow: $flow_code${NC}"
    
    # Create refresh message JSON as single line
    local refresh_message="{\"action\":\"$action\",\"flowCode\":\"$flow_code\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\",\"source\":\"cache-refresh-test\"}"

    echo "Message: $refresh_message"
    
    # Send to Kafka ch-refresh topic
    echo "$refresh_message" | docker exec -i pixel-v2-kafka kafka-console-producer \
        --bootstrap-server kafka:29092 \
        --topic ch-refresh
        
    echo -e "${GREEN}✅ Refresh event sent successfully${NC}"
}

# Function to clear all cache
clear_all_cache() {
    echo -e "${YELLOW}🧹 Clearing all cache entries${NC}"
    
    # Create clear cache message JSON as single line
    local clear_message="{\"action\":\"CLEAR_ALL_CACHE\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\",\"source\":\"cache-refresh-test\"}"

    echo "Message: $clear_message"
    
    echo "$clear_message" | docker exec -i pixel-v2-kafka kafka-console-producer \
        --bootstrap-server kafka:29092 \
        --topic ch-refresh
        
    echo -e "${GREEN}✅ Clear cache event sent successfully${NC}"
}

# Function to check Redis cache
check_cache() {
    local flow_code=$1
    echo -e "${BLUE}🔍 Checking cache for flow: $flow_code${NC}"
    
    docker exec pixel-v2-redis redis-cli GET "flow:$flow_code:complete" || echo "No cache entry found"
}

# Main menu
show_menu() {
    echo ""
    echo -e "${BLUE}Select an action:${NC}"
    echo "1. Refresh ICHSIC flow cache"
    echo "2. Refresh OCHSIC flow cache" 
    echo "3. Clear all cache"
    echo "4. Check ICHSIC cache"
    echo "5. Check OCHSIC cache"
    echo "6. List all Redis keys"
    echo "7. Exit"
    echo ""
}

while true; do
    show_menu
    read -p "Enter your choice [1-7]: " choice
    
    case $choice in
        1)
            send_refresh_event "ICHSIC"
            ;;
        2)
            send_refresh_event "OCHSIC" 
            ;;
        3)
            clear_all_cache
            ;;
        4)
            check_cache "ICHSIC"
            ;;
        5)
            check_cache "OCHSIC"
            ;;
        6)
            echo -e "${BLUE}🔑 All Redis keys:${NC}"
            docker exec pixel-v2-redis redis-cli KEYS "*"
            ;;
        7)
            echo -e "${GREEN}👋 Goodbye!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Invalid choice. Please try again.${NC}"
            ;;
    esac
    
    echo ""
    read -p "Press Enter to continue..."
done