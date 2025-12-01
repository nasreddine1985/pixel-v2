# PIXEL-V2 Camel Application Dockerfile
# Standalone Spring Boot application with Camel integration

FROM eclipse-temurin:21-jre-alpine

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Install required packages, create user and setup directories
RUN apk add --no-cache bash curl netcat-openbsd && \
    mkdir -p /opt/pixel-v2-app-spring-1 && \
    addgroup -S pixel && \
    adduser -S pixel -G pixel -h /opt/pixel-v2-app-spring-1 -s /bin/bash

# Set working directory
WORKDIR /opt/pixel-v2-app-spring-1

# Copy pre-built JAR from local context
COPY pixel-v2-app-spring-1-1.0.0.jar ./pixel-v2-app-spring-1.jar

# Setup directories, create entrypoint script, and set ownership
RUN mkdir -p config logs && \
    echo '#!/bin/bash' > entrypoint.sh && \
    echo 'set -e' >> entrypoint.sh && \
    echo 'echo "Starting PIXEL-V2 Camel Application..."' >> entrypoint.sh && \
    echo 'echo "Java Version: $(java -version 2>&1 | head -n 1)"' >> entrypoint.sh && \
    echo 'echo "Available Memory: $(free -h | head -2 | tail -1)"' >> entrypoint.sh && \
    echo 'echo "Environment: ${SPRING_PROFILES_ACTIVE}"' >> entrypoint.sh && \
    echo '' >> entrypoint.sh && \
    echo '# Wait for dependencies' >> entrypoint.sh && \
    echo 'if [ ! -z "$PIXEL_DB_HOST" ]; then' >> entrypoint.sh && \
    echo '  echo "Waiting for database..."' >> entrypoint.sh && \
    echo '  until nc -z $PIXEL_DB_HOST ${PIXEL_DB_PORT:-5432}; do' >> entrypoint.sh && \
    echo '    echo "Database not ready, waiting..."' >> entrypoint.sh && \
    echo '    sleep 2' >> entrypoint.sh && \
    echo '  done' >> entrypoint.sh && \
    echo '  echo "Database is ready!"' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo '' >> entrypoint.sh && \
    echo 'if [ ! -z "$PIXEL_ACTIVEMQ_HOST" ]; then' >> entrypoint.sh && \
    echo '  echo "Waiting for ActiveMQ..."' >> entrypoint.sh && \
    echo '  until nc -z $PIXEL_ACTIVEMQ_HOST ${PIXEL_ACTIVEMQ_PORT:-61616}; do' >> entrypoint.sh && \
    echo '    echo "ActiveMQ not ready, waiting..."' >> entrypoint.sh && \
    echo '    sleep 2' >> entrypoint.sh && \
    echo '  done' >> entrypoint.sh && \
    echo '  echo "ActiveMQ is ready!"' >> entrypoint.sh && \
    echo 'fi' >> entrypoint.sh && \
    echo '' >> entrypoint.sh && \
    echo 'echo "Starting Spring Boot application..."' >> entrypoint.sh && \
    echo 'exec java $JAVA_OPTS -jar pixel-v2-app-spring-1.jar "$@"' >> entrypoint.sh && \
    chmod +x entrypoint.sh && \
    chown -R pixel:pixel /opt/pixel-v2-app-spring-1

# Expose ports
EXPOSE 8080 8778

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user
USER pixel

# Set entrypoint
ENTRYPOINT ["./entrypoint.sh"]

# Default command
CMD []