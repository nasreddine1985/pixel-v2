#!/bin/bash

# Hawt.io Startup Script for PIXEL-V2 Integration

set -e

echo "Starting Hawt.io Management Console..."
echo "Version: ${HAWTIO_VERSION}"
echo "Port: ${HAWTIO_PORT}"
echo "JVM Options: ${JAVA_OPTS}"

# Set JVM options for Hawt.io
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyWhitelist=${HAWTIO_PROXYWHITELIST:-*}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyHost=${HAWTIO_PROXY_HOST:-localhost}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyPort=${HAWTIO_PROXY_PORT:-8778}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.authenticationEnabled=${HAWTIO_AUTH_ENABLED:-false}"

# Additional JMX connection settings
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.realm=hawtio"
JAVA_OPTS="${JAVA_OPTS} -Djolokia.policyLocation=classpath:/jolokia-access.xml"

echo "=== Hawt.io Configuration ==="
echo "Proxy Whitelist: ${HAWTIO_PROXYWHITELIST:-*}"
echo "Proxy Host: ${HAWTIO_PROXY_HOST:-localhost}"
echo "Proxy Port: ${HAWTIO_PROXY_PORT:-8778}"
echo "Authentication: ${HAWTIO_AUTH_ENABLED:-false}"
echo "Java Options: ${JAVA_OPTS}"
echo "============================="

# Start Hawt.io application
exec java ${JAVA_OPTS} \
    -jar /opt/hawtio/hawtio-app.jar \
    -p ${HAWTIO_PORT} \
    -hst 0.0.0.0 \
    -j