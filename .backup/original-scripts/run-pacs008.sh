#!/bin/bash

# PACS-008 Flow - Development Runner
# Execute PACS-008 flow with JBang in development mode

echo "🚀 Starting PACS-008 Flow (Development Mode)..."

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

if [ ! -f "application-dev.properties" ]; then
    echo "❌ application-dev.properties not found"
    exit 1
fi

# Make executable and run with development properties
chmod +x pacs008-complete.yaml
echo "🔄 Running YAML route with development configuration..."
echo "📋 Using: application-dev.properties"
./pacs008-complete.yaml --properties=application-dev.properties

echo "✅ Development execution completed"