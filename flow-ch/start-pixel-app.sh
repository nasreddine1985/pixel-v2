#!/bin/bash

# PIXEL-V2 Camel Application Startup Script
# This script starts the application with JVM arguments to suppress Kafka client reflection warnings

JAVA_OPTS="-Xms512m -Xmx2g"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.util=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS -Dkafka.client.security.reflection.warnings=false"
JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
JAVA_OPTS="$JAVA_OPTS -Dcamel.component.kafka.health-check-enabled=false"
JAVA_OPTS="$JAVA_OPTS -Dcamel.health.exclude-pattern=kafka*"

APP_JAR="target/pixel-camel-app-1.0.0.jar"
SPRING_PROFILES="prod"
SERVER_PORT="${SERVER_PORT:-8080}"

echo "🚀 Starting PIXEL-V2 Camel Application..."
echo "📋 Profile: $SPRING_PROFILES"
echo "🔌 Port: $SERVER_PORT"
echo "☕ Java Options: $JAVA_OPTS"
echo "📦 JAR: $APP_JAR"

java $JAVA_OPTS \
  -jar $APP_JAR \
  --spring.profiles.active=$SPRING_PROFILES \
  --server.port=$SERVER_PORT