#!/bin/bash

# Hawt.io Startup Script for PIXEL-V2 Integration

set -e

echo "Starting Hawt.io Management Console..."
echo "Version: ${HAWTIO_VERSION}"
echo "Port: ${HAWTIO_PORT}"

echo "=== Hawt.io Configuration ==="
echo "HAWTIO_PROXY_ALLOWLIST: ${HAWTIO_PROXY_ALLOWLIST}"
echo "HAWTIO_PROXY_WHITELIST: ${HAWTIO_PROXY_WHITELIST}" 
echo "JAVA_OPTS: ${JAVA_OPTS}"
echo "============================="

# Start Hawt.io WAR with older Jetty (more stable)
echo "Starting Hawt.io WAR with Jetty runner..."
exec java ${JAVA_OPTS} \
    -jar /opt/hawtio/jetty-runner.jar \
    --port ${HAWTIO_PORT} \
    --host 0.0.0.0 \
    /opt/hawtio/hawtio.war