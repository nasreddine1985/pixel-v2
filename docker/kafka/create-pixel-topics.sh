#!/bin/bash

# PIXEL-V2 Kafka Topics Creation Script
# This script creates all necessary topics for PIXEL-V2 application
# It waits for Kafka to be ready before creating topics

echo "Starting PIXEL-V2 Kafka topics creation script..."

# Function to wait for Kafka to be ready
wait_for_kafka() {
    echo "Waiting for Kafka to be ready..."
    for i in {1..30}; do
        if kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
            echo "Kafka is ready!"
            return 0
        fi
        echo "Waiting for Kafka... attempt $i/30"
        sleep 2
    done
    echo "ERROR: Kafka is not ready after 60 seconds"
    return 1
}

# Function to create topic if it doesn't exist
create_topic() {
    local topic_name=$1
    local partitions=$2
    local replication_factor=$3
    
    echo "Creating topic: $topic_name (partitions: $partitions, replication: $replication_factor)"
    kafka-topics --bootstrap-server localhost:9092 \
        --create \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$replication_factor" \
        --if-not-exists
    
    if [ $? -eq 0 ]; then
        echo "✓ Topic $topic_name created successfully or already exists"
    else
        echo "✗ Failed to create topic $topic_name"
    fi
}

# Wait for Kafka to be ready
if wait_for_kafka; then
    echo "Creating PIXEL-V2 topics..."
    
    # Switzerland (CH) Flow Topics
    create_topic "ch-flow-summary" 1 1
    create_topic "ch-log-events" 1 1
    create_topic "ch-out" 1 1
    create_topic "ch-refresh-ref" 1 1
    
    # General PIXEL-V2 Topics
    create_topic "transaction-audit" 1 1
    create_topic "transaction-correlation" 1 1
    create_topic "pixel-v2-dlq" 1 1
    
    echo "PIXEL-V2 topics creation completed!"
    
    # List all topics to verify
    echo "Current topics:"
    kafka-topics --bootstrap-server localhost:9092 --list | sort
else
    echo "ERROR: Could not connect to Kafka, topics not created"
    exit 1
fi

echo "PIXEL-V2 Kafka topics creation script finished."