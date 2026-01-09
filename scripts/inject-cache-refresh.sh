#!/bin/bash

# Script to inject cache refresh message to Kafka ch-refresh topic
# This will trigger cache invalidation and force referentiel service calls

FLOW_CODE="${1:-ICHSIC}"
KAFKA_CONTAINER="pixel-v2-kafka"
TOPIC="ch-refresh"

echo "🔄 Injecting cache refresh message for flow: $FLOW_CODE"

# Create refresh message JSON
REFRESH_MESSAGE="{\"flowCode\":\"$FLOW_CODE\",\"action\":\"refresh\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)\"}"

echo "📤 Sending refresh message: $REFRESH_MESSAGE"

# Send message to Kafka topic
docker exec $KAFKA_CONTAINER kafka-console-producer \
    --bootstrap-server localhost:29092 \
    --topic $TOPIC << EOF
$REFRESH_MESSAGE
EOF

if [ $? -eq 0 ]; then
    echo "✅ Cache refresh message sent successfully!"
    echo "📋 The cache for flow $FLOW_CODE should now be invalidated"
    echo "🔍 Next message processing will call the referentiel service"
else
    echo "❌ Failed to send cache refresh message"
    exit 1
fi