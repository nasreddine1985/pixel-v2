#!/bin/bash

# PACS-008 Flow - Test Setup Validation
# Check prerequisites for running PACS-008 YAML flow with JBang

echo "🔍 PACS-008 Flow Test Setup Validation"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
    echo "✅ Java $JAVA_VERSION found"
    if [ "$JAVA_VERSION" -lt "11" ]; then
        echo "❌ Java 11+ required"
        exit 1
    fi
else
    echo "❌ Java not found"
    exit 1
fi

# Check JBang
if command -v jbang &> /dev/null; then
    echo "✅ JBang found"
else
    echo "❌ JBang not found. Install: curl -Ls https://sh.jbang.dev | bash"
    exit 1
fi

# Check required files
if [ ! -f "pacs008-complete.yaml" ]; then
    echo "❌ pacs008-complete.yaml missing"
    exit 1
fi

if [ ! -f "run-pacs008.sh" ]; then
    echo "❌ run-pacs008.sh missing"
    exit 1
fi

echo "✅ All prerequisites ready"
echo ""
echo "🚀 Test PACS-008 flow:"
echo "  ./pacs008-complete.yaml    # Direct execution"
echo "  ./run-pacs008.sh          # Helper script"