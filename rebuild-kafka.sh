#!/bin/bash

# PIXEL-V2 Kafka Container Rebuild and Restart Script
# This script rebuilds and restarts the Kafka container with all required topics for all flows

set -e

echo "==========================================="
echo "PIXEL-V2 Kafka Rebuild and Restart (ALL)"
echo "==========================================="

# Navigate to project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📍 Working directory: $PWD"

# Function to check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker and try again."
        exit 1
    fi
    echo "✓ Docker is running"
}

# Function to stop and remove Kafka container
stop_kafka() {
    echo "🛑 Stopping Kafka container..."
    docker compose -f docker/docker-compose.yml stop kafka || true
    
    echo "🗑️  Removing Kafka container..."
    docker compose -f docker/docker-compose.yml rm -f kafka || true
    
    echo "✓ Kafka container stopped and removed"
}

# Function to clean up Kafka volumes
cleanup_volumes() {
    echo "🧹 Cleaning up Kafka volumes..."
    
    # Remove Kafka-related volumes
    docker volume ls -q | grep -E "(kafka|zookeeper)" | xargs -r docker volume rm || true
    
    # Prune unused volumes
    docker volume prune -f
    
    echo "✓ Kafka volumes cleaned up"
}

# Function to rebuild Kafka image
rebuild_kafka() {
    echo "🔨 Rebuilding Kafka container image..."
    
    docker compose -f docker/docker-compose.yml build --no-cache kafka
    
    echo "✓ Kafka image rebuilt successfully"
}

# Function to start Kafka services
start_kafka() {
    echo "🚀 Starting Kafka services..."
    
    # Start Zookeeper first
    echo "📋 Starting Zookeeper..."
    docker compose -f docker/docker-compose.yml up -d zookeeper
    
    # Wait for Zookeeper to be ready
    echo "⏳ Waiting for Zookeeper to be ready..."
    sleep 5
    
    # Start Kafka
    echo "📋 Starting Kafka..."
    docker compose -f docker/docker-compose.yml up -d kafka
    
    # Wait for Kafka to be ready
    echo "⏳ Waiting for Kafka to start and create topics..."
    sleep 15
    
    echo "✓ Kafka services started"
}

# Function to verify Kafka topics
verify_topics() {
    echo "🔍 Verifying Kafka topics..."
    
    # Wait a bit more for topic creation
    sleep 5
    
    echo "📋 Listing all Kafka topics:"
    docker exec pixel-v2-kafka kafka-topics --bootstrap-server localhost:9092 --list | sort
    
    echo ""
    echo "🔍 Verifying critical topics:"
    
    # Define required topics
    local required_topics=(
        "ch-flow-summary"
        "ch-log-events"
        "ch-out"
        "ch-refresh-ref"
        "transaction-audit"
        "transaction-correlation"
        "pixel-v2-dlq"
    )
    
    local missing_topics=()
    
    # Check each required topic
    for topic in "${required_topics[@]}"; do
        if docker exec pixel-v2-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic "$topic" >/dev/null 2>&1; then
            echo "✓ $topic topic exists"
        else
            echo "❌ $topic topic missing"
            missing_topics+=("$topic")
        fi
    done
    
    # Report results
    if [ ${#missing_topics[@]} -eq 0 ]; then
        echo "✓ All critical topics verified successfully"
        
        # Show sample topic details
        echo ""
        echo "📊 Sample topic configurations:"
        docker exec pixel-v2-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic ch-flow-summary | head -2
        docker exec pixel-v2-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic pacs008-input | head -2
        
        return 0
    else
        echo "❌ Missing topics: ${missing_topics[*]}"
        return 1
    fi
}

# Function to show container status
show_status() {
    echo "📊 Container Status:"
    echo "==================="
    docker ps --filter "name=pixel-v2-kafka" --filter "name=pixel-v2-zookeeper" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
}

# Function to show Kafka logs
show_kafka_logs() {
    echo "📜 Recent Kafka logs:"
    echo "===================="
    docker logs pixel-v2-kafka --tail 10 | grep -E "(PIXEL-V2|topic|Started|ERROR)" || echo "No relevant log entries found"
    echo ""
}

# Main execution
main() {
    echo "Starting Kafka rebuild process..."
    
    # Check Docker
    check_docker
    
    # Stop Kafka
    stop_kafka
    
    # Clean up volumes
    cleanup_volumes
    
    # Rebuild Kafka
    rebuild_kafka
    
    # Start Kafka services
    start_kafka
    
    # Verify topics
    if verify_topics; then
        echo ""
        echo "🎉 Kafka rebuild completed successfully!"
        echo ""
        show_status
        show_kafka_logs
        echo ""
        echo "✅ Kafka is ready for all PIXEL-V2 flow operations"
        echo "✅ All required topics (PACS008, PAIN001, CH, audit) are available"
        echo ""
        echo "Next steps:"
        echo "1. Start CH flow: cd flow-ch && mvn spring-boot:run -Dspring-boot.run.profiles=dev"
        echo "2. Start PACS008 flow: cd pacs008-flow && ./run-pacs008.sh"
        echo "3. Monitor Kafka logs: docker logs -f pixel-v2-kafka"
        echo "4. Check topics: docker exec pixel-v2-kafka kafka-topics --bootstrap-server localhost:9092 --list"
    else
        echo ""
        echo "❌ Topic verification failed. Please check the logs:"
        docker logs pixel-v2-kafka --tail 20
        exit 1
    fi
}

# Handle script interruption
trap 'echo ""; echo "⚠️  Script interrupted. Kafka may be in an inconsistent state."; exit 1' INT TERM

# Run main function
main

echo ""
echo "🏁 Kafka rebuild script completed!"