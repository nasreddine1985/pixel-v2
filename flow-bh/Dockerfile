# Multi-stage build for PIXEL-V2 BH Application

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM first for better caching
COPY pom.xml ../pom.xml

# Copy technical framework modules
COPY technical-framework ../technical-framework

# Copy referential module
COPY referential ../referential

# Copy the flow-bh module
COPY flow-bh .

# Build the application
RUN mvn clean install -DskipTests -f ../pom.xml
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Create pixel user and group
RUN groupadd -g 101 pixel && \
    useradd -r -u 100 -g pixel pixel

# Install required utilities
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create NAS directories with proper permissions
RUN mkdir -p /opt/nas/BH/IN /opt/nas/BH/OUT /opt/nas/BH/ARCHIVE && \
    chown -R pixel:pixel /opt/nas && \
    chmod -R 755 /opt/nas

# Switch to pixel user
USER pixel

# Expose application port
EXPOSE 8081

# JVM options for production
ENV JAVA_OPTS="-XX:+UseG1GC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/opt/nas/BH/heapdumps \
    -Djava.security.egd=file:/dev/./urandom \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
