FROM apache/activemq-artemis:2.31.2-alpine

# Set environment variables for PIXEL-V2
ENV ARTEMIS_USER=admin
ENV ARTEMIS_PASSWORD=admin
ENV ANONYMOUS_LOGIN=false
ENV EXTRA_ARGS="--http-host 0.0.0.0 --relax-jolokia"

# Expose ports
# 61616 - Core protocol
# 5672  - AMQP
# 1883  - MQTT  
# 8161  - Web console
EXPOSE 61616 5672 1883 8161

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"artemis\"/Started || exit 1