# Apache Camel JBang Runtime Docker Image for PIXEL-V2
# JVM runtime with Camel 4.1.0 and JBang for deploying YAML routes

FROM eclipse-temurin:21-jdk-alpine

# Set environment variables
ENV CAMEL_VERSION=4.1.0
ENV JBANG_VERSION=0.115.0

# Install required packages
RUN apk add --no-cache bash curl unzip wget netcat-openbsd

# Create application directories
RUN mkdir -p /opt/pixel-v2/routes \
    /opt/pixel-v2/config \
    /opt/pixel-v2/logs \
    /opt/pixel-v2/lib

# Set working directory
WORKDIR /opt/pixel-v2

# Create non-root user for security
RUN addgroup -S pixel && adduser -S pixel -G pixel -h /opt/pixel-v2 -s /bin/bash && \
    chown -R pixel:pixel /opt/pixel-v2

# Switch to pixel user to install JBang for the correct user
USER pixel

# Install JBang and Camel for the pixel user
RUN curl -Ls https://sh.jbang.dev | bash -s - app setup && \
    ~/.jbang/bin/jbang trust add https://github.com/apache/camel/ && \
    ~/.jbang/bin/jbang app install camel@apache/camel

# Add JBang to PATH
ENV PATH="/opt/pixel-v2/.jbang/bin:$PATH"

# Switch back to root for file operations
USER root

# Copy runtime configuration files
COPY camel-application.properties /opt/pixel-v2/config/
COPY logback.xml /opt/pixel-v2/config/
COPY kamelet.properties /opt/pixel-v2/config/

# Route files are mounted as volume from docker-compose.yml

# Copy technical-framework JAR files (kamelets)
COPY technical-framework-jars/*.jar /opt/pixel-v2/lib/

# Copy application scripts
COPY entrypoint.sh /opt/pixel-v2/
COPY health-check.sh /opt/pixel-v2/

# Make scripts executable
RUN chmod +x /opt/pixel-v2/entrypoint.sh /opt/pixel-v2/health-check.sh

# JVM Performance tuning and JMX Configuration for Hawt.io
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=8778 -Dcom.sun.management.jmxremote.rmi.port=8778 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=pixel-v2-app"

# Camel specific environment variables
ENV CAMEL_MAIN_NAME=pixel-v2-camel-app
ENV CAMEL_MAIN_DURATION-MAX-SECONDS=0
ENV CAMEL_MAIN_SHUTDOWN_TIMEOUT=30
ENV CAMEL_MAIN_AUTO_STARTUP=true

# Expose management and application ports
EXPOSE 8080 8778 9779

# Health check using Camel health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /opt/pixel-v2/health-check.sh

# Switch to non-root user
USER pixel

# Set entrypoint
ENTRYPOINT ["/opt/pixel-v2/entrypoint.sh"]

# Default command to run PIXEL-V2 route
CMD ["pacs008-complete.yaml"]