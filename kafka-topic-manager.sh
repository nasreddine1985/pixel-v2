#!/bin/bash

# Kafka Topic Manager Script
# Provides comprehensive Kafka topic management functionality

# Configuration
KAFKA_CONTAINER="pixel-v2-kafka"
KAFKA_BROKER="localhost:29092"
KAFKA_INTERNAL_BROKER="kafka:29092"

# Auto-detect Kafka executables path and extension
detect_kafka_executables() {
    # Try different common paths and executable patterns
    local paths=(
        "/opt/kafka/bin"
        "/usr/bin"
        "/bin" 
        "/opt/bitnami/kafka/bin"
        "/kafka/bin"
    )
    
    local extensions=(".sh" "")
    
    for path in "${paths[@]}"; do
        for ext in "${extensions[@]}"; do
            if docker exec $KAFKA_CONTAINER test -f "$path/kafka-topics$ext" 2>/dev/null; then
                echo "$path|$ext"
                return 0
            fi
        done
    done
    
    # If not found, try to find it with different patterns
    local found_path
    for ext in "${extensions[@]}"; do
        found_path=$(docker exec $KAFKA_CONTAINER find / -name "kafka-topics$ext" -type f 2>/dev/null | head -1)
        if [ -n "$found_path" ]; then
            echo "$(dirname "$found_path")|$ext"
            return 0
        fi
    done
    
    return 1
}

# Get Kafka bin path and extension
get_kafka_bin_path() {
    if [ -z "$KAFKA_BIN_PATH" ]; then
        local result=$(detect_kafka_executables)
        if [ -z "$result" ]; then
            echo -e "${RED}Error: Could not find Kafka executables in container${NC}"
            echo -e "${YELLOW}Available files in container:${NC}"
            docker exec $KAFKA_CONTAINER ls -la /usr/bin/ | grep kafka | head -10 2>/dev/null || true
            exit 1
        fi
        
        KAFKA_BIN_PATH=$(echo "$result" | cut -d'|' -f1)
        KAFKA_EXT=$(echo "$result" | cut -d'|' -f2)
        echo -e "${BLUE}Using Kafka executables from: $KAFKA_BIN_PATH (extension: '$KAFKA_EXT')${NC}"
    fi
}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_usage() {
    echo -e "${BLUE}Kafka Topic Manager${NC}"
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  list                    List all topics"
    echo "  create <topic-name>     Create a new topic"
    echo "  delete <topic-name>     Delete a specific topic"
    echo "  clear <topic-name>      Clear messages from a topic (recreate)"
    echo "  drop-all               Delete all topics"
    echo "  clear-all              Clear all topics"
    echo "  describe <topic-name>   Describe topic details"
    echo "  consumer <topic-name>   Start console consumer for topic"
    echo "  producer <topic-name>   Start console producer for topic"
    echo ""
    echo "Options for create:"
    echo "  --partitions <num>      Number of partitions (default: 3)"
    echo "  --replication <num>     Replication factor (default: 1)"
    echo ""
    echo "Examples:"
    echo "  $0 list"
    echo "  $0 create my-topic --partitions 5 --replication 1"
    echo "  $0 delete my-topic"
    echo "  $0 clear my-topic"
    echo "  $0 describe my-topic"
}

check_kafka_container() {
    if ! docker ps | grep -q "$KAFKA_CONTAINER"; then
        echo -e "${RED}Error: Kafka container '$KAFKA_CONTAINER' is not running${NC}"
        echo "Please start the Kafka container first with: docker-compose up -d"
        exit 1
    fi
    
    # Detect Kafka executables path
    get_kafka_bin_path
}

list_topics() {
    echo -e "${BLUE}📋 Listing Kafka Topics${NC}"
    docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --list
}

create_topic() {
    local topic_name=$1
    local partitions=${2:-3}
    local replication=${3:-1}
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${BLUE}🆕 Creating topic: ${GREEN}$topic_name${NC} (partitions: $partitions, replication: $replication)"
    docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --create \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$replication"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Topic '$topic_name' created successfully${NC}"
    else
        echo -e "${RED}❌ Failed to create topic '$topic_name'${NC}"
    fi
}

delete_topic() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}🗑️  Deleting topic: $topic_name${NC}"
    read -p "Are you sure you want to delete topic '$topic_name'? (y/N): " confirm
    
    if [[ $confirm =~ ^[Yy]$ ]]; then
        docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
            --bootstrap-server $KAFKA_INTERNAL_BROKER \
            --delete \
            --topic "$topic_name"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Topic '$topic_name' deleted successfully${NC}"
        else
            echo -e "${RED}❌ Failed to delete topic '$topic_name'${NC}"
        fi
    else
        echo -e "${YELLOW}Operation cancelled${NC}"
    fi
}

clear_topic() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}🧹 Clearing topic: $topic_name${NC}"
    
    # Get topic configuration
    local partitions=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --describe \
        --topic "$topic_name" 2>/dev/null | grep "PartitionCount" | awk '{print $4}')
    
    local replication=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --describe \
        --topic "$topic_name" 2>/dev/null | grep "ReplicationFactor" | awk '{print $6}')
    
    if [ -z "$partitions" ] || [ -z "$replication" ]; then
        echo -e "${RED}❌ Topic '$topic_name' does not exist${NC}"
        return 1
    fi
    
    read -p "Are you sure you want to clear all messages from topic '$topic_name'? (y/N): " confirm
    
    if [[ $confirm =~ ^[Yy]$ ]]; then
        # Delete and recreate topic to clear messages
        docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
            --bootstrap-server $KAFKA_INTERNAL_BROKER \
            --delete \
            --topic "$topic_name" > /dev/null 2>&1
        
        sleep 2
        
        docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
            --bootstrap-server $KAFKA_INTERNAL_BROKER \
            --create \
            --topic "$topic_name" \
            --partitions "$partitions" \
            --replication-factor "$replication" > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Topic '$topic_name' cleared successfully${NC}"
        else
            echo -e "${RED}❌ Failed to clear topic '$topic_name'${NC}"
        fi
    else
        echo -e "${YELLOW}Operation cancelled${NC}"
    fi
}

drop_all_topics() {
    echo -e "${RED}💀 DROP ALL TOPICS${NC}"
    echo -e "${YELLOW}⚠️  This will DELETE ALL Kafka topics permanently!${NC}"
    read -p "Are you ABSOLUTELY SURE you want to delete ALL topics? Type 'YES' to confirm: " confirm
    
    if [ "$confirm" = "YES" ]; then
        local topics=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
            --bootstrap-server $KAFKA_INTERNAL_BROKER \
            --list 2>/dev/null | grep -v "^__")
        
        if [ -z "$topics" ]; then
            echo -e "${YELLOW}No user topics found${NC}"
            return
        fi
        
        echo -e "${RED}Deleting all topics...${NC}"
        for topic in $topics; do
            echo -e "Deleting: $topic"
            docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
                --bootstrap-server $KAFKA_INTERNAL_BROKER \
                --delete \
                --topic "$topic" > /dev/null 2>&1
        done
        
        echo -e "${GREEN}✅ All topics deleted${NC}"
    else
        echo -e "${YELLOW}Operation cancelled${NC}"
    fi
}

clear_all_topics() {
    echo -e "${YELLOW}🧹 CLEAR ALL TOPICS${NC}"
    echo -e "${YELLOW}⚠️  This will CLEAR ALL messages from all Kafka topics!${NC}"
    read -p "Are you sure you want to clear ALL topics? Type 'YES' to confirm: " confirm
    
    if [ "$confirm" = "YES" ]; then
        local topics=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
            --bootstrap-server $KAFKA_INTERNAL_BROKER \
            --list 2>/dev/null | grep -v "^__")
        
        if [ -z "$topics" ]; then
            echo -e "${YELLOW}No user topics found${NC}"
            return
        fi
        
        echo -e "${YELLOW}Clearing all topics...${NC}"
        for topic in $topics; do
            echo -e "Clearing: $topic"
            
            # Get topic configuration
            local partitions=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
                --bootstrap-server $KAFKA_INTERNAL_BROKER \
                --describe \
                --topic "$topic" 2>/dev/null | grep "PartitionCount" | awk '{print $4}')
            
            local replication=$(docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
                --bootstrap-server $KAFKA_INTERNAL_BROKER \
                --describe \
                --topic "$topic" 2>/dev/null | grep "ReplicationFactor" | awk '{print $6}')
            
            if [ ! -z "$partitions" ] && [ ! -z "$replication" ]; then
                # Delete and recreate
                docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
                    --bootstrap-server $KAFKA_INTERNAL_BROKER \
                    --delete \
                    --topic "$topic" > /dev/null 2>&1
                
                sleep 1
                
                docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
                    --bootstrap-server $KAFKA_INTERNAL_BROKER \
                    --create \
                    --topic "$topic" \
                    --partitions "$partitions" \
                    --replication-factor "$replication" > /dev/null 2>&1
            fi
        done
        
        echo -e "${GREEN}✅ All topics cleared${NC}"
    else
        echo -e "${YELLOW}Operation cancelled${NC}"
    fi
}

describe_topic() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${BLUE}🔍 Describing topic: ${GREEN}$topic_name${NC}"
    docker exec $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-topics$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --describe \
        --topic "$topic_name"
}

start_consumer() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${BLUE}👂 Starting console consumer for topic: ${GREEN}$topic_name${NC}"
    echo -e "${YELLOW}Press Ctrl+C to stop${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-console-consumer$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --topic "$topic_name" \
        --from-beginning
}

start_producer() {
    local topic_name=$1
    
    if [ -z "$topic_name" ]; then
        echo -e "${RED}Error: Topic name is required${NC}"
        return 1
    fi
    
    echo -e "${BLUE}📤 Starting console producer for topic: ${GREEN}$topic_name${NC}"
    echo -e "${YELLOW}Type messages and press Enter. Press Ctrl+C to stop${NC}"
    docker exec -it $KAFKA_CONTAINER $KAFKA_BIN_PATH/kafka-console-producer$KAFKA_EXT \
        --bootstrap-server $KAFKA_INTERNAL_BROKER \
        --topic "$topic_name"
}

# Parse arguments
case "$1" in
    "list")
        check_kafka_container
        list_topics
        ;;
    "create")
        check_kafka_container
        topic_name=$2
        partitions=3
        replication=1
        
        # Parse optional parameters
        shift 2
        while [[ $# -gt 0 ]]; do
            case $1 in
                --partitions)
                    partitions="$2"
                    shift 2
                    ;;
                --replication)
                    replication="$2"
                    shift 2
                    ;;
                *)
                    echo -e "${RED}Unknown option: $1${NC}"
                    print_usage
                    exit 1
                    ;;
            esac
        done
        
        create_topic "$topic_name" "$partitions" "$replication"
        ;;
    "delete")
        check_kafka_container
        delete_topic "$2"
        ;;
    "clear")
        check_kafka_container
        clear_topic "$2"
        ;;
    "drop-all")
        check_kafka_container
        drop_all_topics
        ;;
    "clear-all")
        check_kafka_container
        clear_all_topics
        ;;
    "describe")
        check_kafka_container
        describe_topic "$2"
        ;;
    "consumer")
        check_kafka_container
        start_consumer "$2"
        ;;
    "producer")
        check_kafka_container
        start_producer "$2"
        ;;
    "help"|"-h"|"--help"|"")
        print_usage
        ;;
    *)
        echo -e "${RED}Unknown command: $1${NC}"
        print_usage
        exit 1
        ;;
esac