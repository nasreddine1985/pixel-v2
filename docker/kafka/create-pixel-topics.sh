#!/bin/bash

# PIXEL-V2 Kafka Topics Creation Script
# This script creates the necessary Kafka topics for PIXEL-V2 application

echo "Starting PIXEL-V2 Kafka topics creation..."

# Wait for Kafka to be ready
echo "Waiting for Kafka broker to be ready..."
while ! kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    echo "Kafka not ready yet, waiting 5 seconds..."
    sleep 5
done

echo "Kafka broker is ready, creating PIXEL-V2 topics..."

# Create PACS-008 processing topics
kafka-topics --bootstrap-server localhost:9092 --create \
    --topic pacs008-input \
    --partitions 3 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=1048576 \
    --if-not-exists

kafka-topics --bootstrap-server localhost:9092 --create \
    --topic pacs008-output \
    --partitions 3 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=1048576 \
    --if-not-exists

kafka-topics --bootstrap-server localhost:9092 --create \
    --topic pacs008-error \
    --partitions 2 \
    --replication-factor 1 \
    --config retention.ms=604800000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=1048576 \
    --if-not-exists

kafka-topics --bootstrap-server localhost:9092 --create \
    --topic pacs008-monitoring \
    --partitions 1 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=524288 \
    --if-not-exists

# Create audit and transaction tracking topics
kafka-topics --bootstrap-server localhost:9092 --create \
    --topic transaction-audit \
    --partitions 2 \
    --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=262144 \
    --if-not-exists

kafka-topics --bootstrap-server localhost:9092 --create \
    --topic transaction-correlation \
    --partitions 3 \
    --replication-factor 1 \
    --config retention.ms=86400000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=131072 \
    --if-not-exists

# Create dead letter topic for failed messages
kafka-topics --bootstrap-server localhost:9092 --create \
    --topic pixel-v2-dlq \
    --partitions 1 \
    --replication-factor 1 \
    --config retention.ms=2592000000 \
    --config compression.type=lz4 \
    --config cleanup.policy=delete \
    --config max.message.bytes=2097152 \
    --if-not-exists

echo "PIXEL-V2 Kafka topics created successfully!"

# List all topics to verify creation
echo "Listing all topics:"
kafka-topics --bootstrap-server localhost:9092 --list

echo "PIXEL-V2 topics setup completed!"