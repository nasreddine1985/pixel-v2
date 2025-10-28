#!/bin/bash

# Test script for kamelet-only PACS.008 message processing
# Flow module now relies entirely on k-mq-message-receiver kamelet

echo "🚀 Testing PACS.008 Processing with Kamelet-Only Approach"
echo "========================================================="

cd /Users/n.abassi/sources/pixel-v2

echo ""
echo "📋 Configuration Summary:"
echo "- Using ONLY k-mq-message-receiver kamelet"
echo "- YAML-based route configuration"
echo "- Zero JMS configuration in flow module"
echo "- All JMS handling delegated to kamelet"
echo "- Batch processing: 1000 messages or 1-second timeout"
echo ""

# Build the project
echo "🔨 Building project..."
mvn clean compile -q -f flow/pom.xml
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"
echo ""

# Start the application with kamelet profile
echo "🚀 Starting Flow application with JMS kamelet profile..."
echo "Profile: jms (uses YAML routes with k-mq-message-receiver kamelet)"
echo ""
echo "Expected behavior:"
echo "- Messages consumed ONLY via k-mq-message-receiver kamelet"
echo "- Batch aggregation (1000 messages or 1-second timeout)"
echo "- JMS configuration handled entirely by kamelet"
echo "- Zero JMS duplication - clean architecture"
echo ""

# Run with jms profile to use kamelet-only routes
mvn spring-boot:run -f flow/pom.xml -Dspring-boot.run.profiles=jms