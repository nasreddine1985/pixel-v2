#!/bin/bash

# Build Technical Framework JARs for Camel Spring App
# This script builds all technical framework components and copies the JARs

set -e

echo "Building Technical Framework JARs for Camel Spring App..."

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$SCRIPT_DIR"
TECHNICAL_FRAMEWORK_DIR="$(cd "$SCRIPT_DIR/../technical-framework" && pwd)"

# Change to technical framework directory
cd "$TECHNICAL_FRAMEWORK_DIR"

# Build all technical framework modules
echo "Building technical framework modules..."
mvn clean install -DskipTests

# Create technical-jars directory if it doesn't exist
mkdir -p "$APP_DIR/technical-jars"

# Copy built JARs to Camel Spring app
echo "Copying JARs to Camel Spring app..."

# List of technical framework modules to copy
modules=(
    "k-mq-message-receiver"
    "k-kafka-starter"
    "k-http-message-receiver"
    "k-cft-data-receiver"
    "k-db-tx"
    "k-log-tx"
    "k-referentiel-data-loader"
    "k-xsd-validation"
    "k-xsl-transformation"
    "k-message-concat"
    "k-message-split"
    "k-log-events"
)

for module in "${modules[@]}"; do
    if [ -d "$module" ]; then
        jar_file="$module/target/$module-1.0.1-SNAPSHOT.jar"
        if [ -f "$jar_file" ]; then
            echo "Copying $jar_file"
            cp "$jar_file" "$APP_DIR/technical-jars/"
        else
            echo "Warning: $jar_file not found, skipping..."
        fi
    else
        echo "Warning: Module $module not found, skipping..."
    fi
done

# Also copy kamelets from JARs to resources
echo "Extracting kamelets from JARs..."
mkdir -p "$APP_DIR/src/main/resources/kamelets"

for jar_file in "$APP_DIR/technical-jars"/*.jar; do
    if [ -f "$jar_file" ]; then
        jar_name=$(basename "$jar_file" .jar)
        echo "Extracting kamelets from $jar_name..."
        
        # Extract kamelet YAML files
        unzip -j -o "$jar_file" "kamelets/*.kamelet.yaml" -d "$APP_DIR/src/main/resources/kamelets/" 2>/dev/null || true
    fi
done

echo "Technical Framework JARs copied successfully!"
echo "Available JARs in technical-jars directory:"
ls -la "$APP_DIR/technical-jars/"

echo ""
echo "Extracted kamelets:"
ls -la "$APP_DIR/src/main/resources/kamelets/" 2>/dev/null || echo "No kamelets extracted"

echo ""
echo "To build the Camel Spring app, run:"
echo "cd $APP_DIR"
echo "./build.sh"