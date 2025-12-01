#!/bin/bash

# PIXEL-V2 Health Check Script
# Checks if the Camel application is healthy

set -e

# Check if Camel health endpoint is responding
HEALTH_URL="http://localhost:8080/observe/health"
TIMEOUT=10

# Check HTTP health endpoint
if command -v curl >/dev/null 2>&1; then
    response=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$HEALTH_URL" || echo "000")
    if [ "$response" = "200" ]; then
        echo "Health check passed - Camel application is healthy"
        exit 0
    else
        echo "Health check failed - HTTP status: $response"
        exit 1
    fi
elif command -v wget >/dev/null 2>&1; then
    if wget -q --timeout=$TIMEOUT --tries=1 -O /dev/null "$HEALTH_URL"; then
        echo "Health check passed - Camel application is healthy"
        exit 0
    else
        echo "Health check failed - wget failed"
        exit 1
    fi
else
    # Fallback: check if Java process is running
    if pgrep -f "jbang.*camel" >/dev/null 2>&1; then
        echo "Health check passed - Camel process is running"
        exit 0
    else
        echo "Health check failed - Camel process not found"
        exit 1
    fi
fi