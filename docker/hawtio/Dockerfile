# Custom Hawt.io Docker Image for PIXEL-V2 Camel Route Management
# Based on OpenJDK with Hawt.io web application

FROM eclipse-temurin:21-jdk-alpine

# Set Hawt.io version
ENV HAWTIO_VERSION=2.17.7
ENV HAWTIO_PORT=8080

# Install required packages
RUN apk add --no-cache curl bash wget unzip

# Create application directory
RUN mkdir -p /opt/hawtio

# Download Hawt.io application
RUN wget https://repo1.maven.org/maven2/io/hawt/hawtio-app/2.17.7/hawtio-app-2.17.7.jar -O /opt/hawtio/hawtio-app.jar

# Copy Hawt.io startup script from centralized scripts folder and configuration
COPY ../scripts/docker/hawtio/start-hawtio.sh /opt/hawtio/
COPY hawtio-config.json /opt/hawtio/
# Make script executable
RUN chmod +x /opt/hawtio/start-hawtio.sh

# Create non-root user
RUN addgroup -S hawtio && adduser -S hawtio -G hawtio -h /opt/hawtio

# Set ownership
RUN chown -R hawtio:hawtio /opt/hawtio

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/hawtio/ || exit 1

# Switch to non-root user
USER hawtio

# Set working directory
WORKDIR /opt/hawtio

# Start Hawt.io
ENTRYPOINT ["/opt/hawtio/start-hawtio.sh"]