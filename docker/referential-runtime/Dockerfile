# Multi-stage build for Spring Boot Referential application
FROM maven:3.9.9-amazoncorretto-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy parent pom.xml first for better layer caching
COPY pom.xml ./

# Copy referential module
COPY referential ./referential

# Build the application (only referential module since it's standalone)
RUN mvn clean package spring-boot:repackage -f referential/pom.xml -DskipTests

# Runtime stage
FROM amazoncorretto:21-alpine-jdk

# Install wget for health checks
RUN apk add --no-cache wget

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Copy the built JAR from builder stage
COPY --from=builder /app/referential/target/*.jar app.jar

# Copy application properties for production
COPY --from=builder /app/referential/src/main/resources/application-prod.properties application-prod.properties

# Change ownership to spring user
RUN chown -R spring:spring /app
USER spring:spring

# Expose the application port
EXPOSE 8099

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8099/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Run the application with production profile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]