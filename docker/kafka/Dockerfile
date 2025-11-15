# Apache Kafka Docker Image for PIXEL-V2
# Based on Confluent Kafka with PIXEL-V2 specific configuration

FROM confluentinc/cp-kafka:7.5.0

# Set environment variables for Kafka
ENV KAFKA_BROKER_ID=1
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
ENV KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
ENV KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
ENV KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0
ENV KAFKA_AUTO_CREATE_TOPICS_ENABLE=true

# PIXEL-V2 specific Kafka configuration
ENV KAFKA_NUM_PARTITIONS=3
ENV KAFKA_DEFAULT_REPLICATION_FACTOR=1
ENV KAFKA_MIN_INSYNC_REPLICAS=1
ENV KAFKA_LOG_RETENTION_HOURS=24
ENV KAFKA_LOG_RETENTION_BYTES=1073741824
ENV KAFKA_LOG_SEGMENT_BYTES=1073741824
ENV KAFKA_LOG_CLEANUP_POLICY=delete

# Performance tuning
ENV KAFKA_SOCKET_SEND_BUFFER_BYTES=102400
ENV KAFKA_SOCKET_RECEIVE_BUFFER_BYTES=102400
ENV KAFKA_SOCKET_REQUEST_MAX_BYTES=104857600
ENV KAFKA_NUM_NETWORK_THREADS=8
ENV KAFKA_NUM_IO_THREADS=8
ENV KAFKA_BACKGROUND_THREADS=10

# JVM Settings for optimal performance
ENV KAFKA_HEAP_OPTS="-Xmx1G -Xms1G"
ENV KAFKA_JVM_PERFORMANCE_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true"

# Create directory for custom configuration and topics
USER root
RUN mkdir -p /etc/kafka-pixel-v2/topics
# Copy configuration and scripts from centralized scripts folder
COPY kafka-pixel-v2.properties /etc/kafka-pixel-v2/
COPY ../scripts/docker/kafka/create-pixel-topics.sh /etc/kafka-pixel-v2/
RUN chmod +x /etc/kafka-pixel-v2/create-pixel-topics.sh

# Expose Kafka ports
EXPOSE 9092 29092

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD kafka-topics --bootstrap-server localhost:9092 --list || exit 1

# Switch back to appuser
USER appuser

# Create PIXEL-V2 topics on startup
CMD ["/bin/bash", "-c", "/etc/kafka-pixel-v2/create-pixel-topics.sh & /etc/confluent/docker/run"]