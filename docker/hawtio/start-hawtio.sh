#!/bin/bash

# Hawt.io Startup Script for PIXEL-V2 Integration

set -e

echo "Starting Hawt.io Management Console..."
echo "Version: ${HAWTIO_VERSION}"
echo "Port: ${HAWTIO_PORT}"
echo "JVM Options: ${JAVA_OPTS}"

# Set JVM options for Hawt.io - Use both proxyWhitelist and proxyAllowlist for compatibility
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyWhitelist=${HAWTIO_PROXYWHITELIST:-pixel-v2-app-1,pixel-v2-app-2,pixel-v2-app-3,localhost,127.0.0.1,172.19.0.*}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyAllowlist=${HAWTIO_PROXYWHITELIST:-pixel-v2-app-1,pixel-v2-app-2,pixel-v2-app-3,localhost,127.0.0.1,172.19.0.*}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyHost=${HAWTIO_PROXY_HOST:-localhost}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.proxyPort=${HAWTIO_PROXY_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.authenticationEnabled=${HAWTIO_AUTH_ENABLED:-false}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.localAddressProbing=${HAWTIO_LOCAL_ADDRESS_PROBING:-false}"
JAVA_OPTS="${JAVA_OPTS} -Dhawtio.disableProxy=false"

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