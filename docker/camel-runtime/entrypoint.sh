#!/bin/bash

# PIXEL-V2 Camel Runtime Entrypoint Script
# This script initializes and runs Camel JBang routes

set -e

echo "Starting PIXEL-V2 Camel Runtime..."

# Set default values
# Default route file (can be overridden)
ROUTE_FILE=${1:-"pacs008-complete.yaml"}
CONFIG_DIR="/opt/pixel-v2/config"
ROUTES_DIR="/opt/pixel-v2/routes"
LOGS_DIR="/opt/pixel-v2/logs"

# Ensure directories exist
mkdir -p "$LOGS_DIR"

# Environment validation
echo "Environment: ${PIXEL_ENV:-dev}"
echo "Java Version: $(java -version 2>&1 | head -n 1)"
echo "JBang Version: $(jbang version)"
echo "Camel Version: $CAMEL_VERSION"

# Set JVM options for JBang
JVM_OPTS="$JAVA_OPTS -Djava.util.logging.config.file=$CONFIG_DIR/logging.properties -Dlogback.configurationFile=$CONFIG_DIR/logback.xml -Dcamel.component.properties.location=$CONFIG_DIR/camel-application.properties"

# Check if route file exists
if [ -f "$ROUTES_DIR/$ROUTE_FILE" ]; then
    ROUTE_PATH="$ROUTES_DIR/$ROUTE_FILE"
elif [ -f "$ROUTE_FILE" ]; then
    ROUTE_PATH="$ROUTE_FILE"
else
    echo "Error: Route file '$ROUTE_FILE' not found in current directory or $ROUTES_DIR"
    echo "Available routes in $ROUTES_DIR:"
    ls -la "$ROUTES_DIR" 2>/dev/null || echo "No routes directory found"
    exit 1
fi

echo "Using route file: $ROUTE_PATH"

# Wait for dependencies to be ready
echo "Checking dependencies..."

# Check PostgreSQL
if [ -n "$PIXEL_DB_URL" ]; then
    echo "Waiting for PostgreSQL to be ready..."
    until java -cp /opt/pixel-v2/lib/postgresql.jar org.postgresql.util.PSQLException 2>/dev/null || true; do
        echo "PostgreSQL not ready, waiting 5 seconds..."
        sleep 5
    done
fi


# Log startup information
echo "=== PIXEL-V2 Camel Runtime Startup ==="
echo "Timestamp: $(date)"
echo "Route: $ROUTE_PATH"
echo "Config: $CONFIG_DIR/camel-application.properties"
echo "Logs: $LOGS_DIR"
echo "JVM Options: $JVM_OPTS"
echo "======================================"

# Add technical-framework JARs to classpath
TECHNICAL_FRAMEWORK_JARS="/opt/pixel-v2/lib/*.jar"
CLASSPATH_JARS=$(ls /opt/pixel-v2/lib/*.jar 2>/dev/null | tr '\n' ':' | sed 's/:$//')

echo "Technical Framework JARs found: $(ls /opt/pixel-v2/lib/*.jar 2>/dev/null | wc -l)"

# Run Camel route with JBang and technical-framework JARs in classpath
exec jbang \
    --cp="$CLASSPATH_JARS" \
    -R="-Xms512m" \
    -R="-Xmx2g" \
    -R="-XX:+UseG1GC" \
    -R="-XX:MaxGCPauseMillis=200" \
    -R="-Djava.security.egd=file:/dev/./urandom" \
    -R="-Dfile.encoding=UTF-8" \
    -R="-Dcom.sun.management.jmxremote" \
    -R="-Dcom.sun.management.jmxremote.port=8778" \
    -R="-Dcom.sun.management.jmxremote.rmi.port=8778" \
    -R="-Dcom.sun.management.jmxremote.authenticate=false" \
    -R="-Dcom.sun.management.jmxremote.ssl=false" \
    -R="-Djava.rmi.server.hostname=$(hostname)" \
    -R="-Djava.util.logging.config.file=$CONFIG_DIR/logging.properties" \
    -R="-Dlogback.configurationFile=$CONFIG_DIR/logback.xml" \
    camel@apache/camel run \
    --logging-level=INFO \
    --health \
    --console \
    --reload \
    --property=camel.component.properties.location="file:$CONFIG_DIR/camel-application.properties,file:$CONFIG_DIR/kamelet.properties" \
    "$ROUTE_PATH"