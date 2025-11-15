# PIXEL-V2 Docker Infrastructure

This directory contains Docker images and orchestration files for the complete PIXEL-V2 infrastructure stack.

## Architecture Overview

The PIXEL-V2 Docker infrastructure consists of 4 main components:

1. **ActiveMQ Artemis** (`artemis-mq/`) - Message broker for PACS-008 queue processing
2. **PostgreSQL** (`postgresql/`) - Database for transaction storage and referential data
3. **Apache Kafka** (`kafka/`) - Event streaming platform for audit and monitoring
4. **Camel Runtime** (`camel-runtime/`) - JBang-based Camel runtime for YAML route execution

## Quick Start

### Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB+ available RAM
- 10GB+ available disk space

### Start Complete Stack

```bash
# Start all services
cd docker
docker-compose up -d

# Start with management tools (Adminer, Kafdrop)
docker-compose --profile tools up -d

# View logs
docker-compose logs -f pixel-v2-app
```

### Deploy PIXEL-V2 Route

```bash
# Copy route to shared volume
cp ../pacs008-flow/pacs008-complete.yaml ./routes/

# Restart application to pick up new route
docker-compose restart pixel-v2-app
```

## Service Details

### ActiveMQ Artemis (`artemis-mq`)

- **Ports**: 61616 (Core), 5672 (AMQP), 1883 (MQTT), 8161 (Web Console)
- **Credentials**: admin/admin
- **Queues**: Pre-configured PACS-008 processing queues
- **Web Console**: http://localhost:8161/console

### PostgreSQL (`postgresql`)

- **Port**: 5432
- **Database**: pixelv2
- **Credentials**: pixelv2/pixelv2_secure_password
- **Schema**: Auto-initialized with PIXEL-V2 tables
- **Management**: http://localhost:8090 (Adminer, with --profile tools)

### Apache Kafka (`kafka`)

- **Port**: 9092 (External), 29092 (Internal)
- **Topics**: Auto-created PACS-008 topics
- **Management**: http://localhost:9000 (Kafdrop, with --profile tools)

### Camel Runtime (`camel-runtime`)

- **Port**: 8080 (HTTP API), 8778 (JMX), 9779 (Metrics)
- **Runtime**: OpenJDK 21 + JBang + Camel 4.1.0
- **Health**: http://localhost:8080/q/health
- **Console**: http://localhost:8080/q/dev

## Environment Configuration

### Production Environment Variables

```bash
# Database
PIXEL_DB_URL=jdbc:postgresql://postgresql:5432/pixelv2
PIXEL_DB_USERNAME=pixelv2
PIXEL_DB_PASSWORD=pixelv2_secure_password

# Message Broker
PIXEL_MQ_URL=tcp://artemis-mq:61616
PIXEL_MQ_USERNAME=admin
PIXEL_MQ_PASSWORD=admin

# Kafka
PIXEL_KAFKA_BOOTSTRAP=kafka:29092

# Application
PIXEL_ENV=production
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
```

## Health Checks

All services include comprehensive health checks:

```bash
# Check all services status
docker-compose ps

# Individual service health
docker-compose exec pixel-v2-app /opt/pixel-v2/health-check.sh
docker-compose exec postgresql pg_isready -U pixelv2
docker-compose exec artemis-mq curl -f http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"artemis\"/Started
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## Monitoring and Management

### Web Interfaces

- **Artemis Console**: http://localhost:8161/console (admin/admin)
- **Camel Dev Console**: http://localhost:8080/q/dev
- **PostgreSQL Adminer**: http://localhost:8090 (with --profile tools)
- **Kafka Kafdrop**: http://localhost:9000 (with --profile tools)

### API Endpoints

- **Health Check**: http://localhost:8080/q/health
- **Metrics**: http://localhost:9779/metrics
- **JMX**: localhost:8778

## Data Persistence

Persistent volumes are configured for:

- PostgreSQL data and logs
- Artemis message store and logs
- Application logs

```bash
# View volumes
docker volume ls | grep pixel-v2

# Backup database
docker-compose exec postgresql pg_dump -U pixelv2 pixelv2 > backup.sql

# View logs
docker-compose logs -f pixel-v2-app
```

## Troubleshooting

### Common Issues

1. **Port Conflicts**

   ```bash
   # Change ports in docker-compose.yml if needed
   sed -i 's/5432:5432/15432:5432/' docker-compose.yml
   ```

2. **Memory Issues**

   ```bash
   # Reduce memory allocation
   export JAVA_OPTS="-Xms256m -Xmx1g"
   docker-compose up -d pixel-v2-app
   ```

3. **Route Not Found**
   ```bash
   # Ensure route is in ./routes directory
   cp ../pacs008-flow/*.yaml ./routes/
   docker-compose restart pixel-v2-app
   ```

### Debug Mode

```bash
# Enable debug logging
docker-compose exec pixel-v2-app \
  env LOGGING_LEVEL_COM_PIXEL_V2=DEBUG \
  jbang camel@apache/camel run --logging-level=DEBUG /opt/pixel-v2/routes/pacs008-complete.yaml
```

## Development

### Building Images

```bash
# Build specific service
docker-compose build artemis-mq

# Build all services
docker-compose build

# No cache rebuild
docker-compose build --no-cache
```

### Custom Configuration

- Copy custom configs to respective directories before building
- Modify environment variables in docker-compose.yml
- Use .env file for sensitive values

### Testing

```bash
# Run integration tests against Docker stack
cd ../integration-tests
mvn test -Dtest.environment=docker
```

## Production Deployment

For production deployment:

1. **Security**: Change default passwords and enable TLS
2. **Scaling**: Use Docker Swarm or Kubernetes
3. **Monitoring**: Add Prometheus/Grafana stack
4. **Backup**: Implement automated backup procedures
5. **Networks**: Configure proper network isolation

### Example Production Override

```yaml
# docker-compose.prod.yml
version: "3.8"
services:
  postgresql:
    environment:
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
  artemis-mq:
    environment:
      ARTEMIS_PASSWORD: ${ARTEMIS_PASSWORD}
  pixel-v2-app:
    environment:
      PIXEL_DB_PASSWORD: ${POSTGRES_PASSWORD}
      PIXEL_MQ_PASSWORD: ${ARTEMIS_PASSWORD}
```

```bash
# Deploy with production overrides
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```
