#!/bin/bash

# PACS-008 Flow - Production Runner
# Execute PACS-008 production flow with JBang

echo "🚀 Starting PACS-008 Production Flow..."

# Check JBang installation
if ! command -v jbang &> /dev/null; then
    echo "❌ JBang not found. Install with:"
    echo "   curl -Ls https://sh.jbang.dev | bash"
    exit 1
fi

# Check required files
if [ ! -f "pacs008-complete.yaml" ]; then
    echo "❌ pacs008-complete.yaml not found"
    exit 1
fi

if [ ! -f "application-prod.properties" ]; then
    echo "❌ application-prod.properties not found"
    exit 1
fi

# Check environment variables
if [ -z "$DB_PASSWORD" ]; then
    echo "❌ DB_PASSWORD environment variable is required"
    exit 1
fi

if [ -z "$ARTEMIS_PASSWORD" ]; then
    echo "❌ ARTEMIS_PASSWORD environment variable is required"
    exit 1
fi

# Make executable and run with production properties
chmod +x pacs008-complete.yaml
echo "🔄 Running production YAML route..."
echo "📋 Using production configuration: application-prod.properties"

./pacs008-complete.yaml --properties=application-prod.properties

echo "✅ Production execution completed"