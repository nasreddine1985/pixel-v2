# Referential Runtime Docker

This directory contains the Docker configuration for building and running the referential service as a containerized Spring Boot application.

## Files

- `Dockerfile` - Multi-stage Docker build for the referential service
- `build.sh` - Build script with various options for building and pushing the Docker image
- `docker-compose.yml` - Docker Compose configuration for running the service
- `README.md` - This documentation file

## Building the Image

### Using the build script (recommended)

Make the build script executable and run it:

```bash
chmod +x build.sh
./build.sh
```

The build script supports various options:

```bash
# Build with default settings
./build.sh

# Build with specific tag
./build.sh -t v1.0.1

# Build and push to registry
./build.sh -t v1.0.1 -p -r your-registry.com

# Show help
./build.sh -h
```

### Using Docker directly

From the project root directory:

```bash
docker build -f docker/referential-runtime/Dockerfile -t pixel-v2/referential-runtime:latest .
```

From the docker directory:

```bash
docker build -f referential-runtime/Dockerfile -t pixel-v2/referential-runtime:latest ../
```

## Running the Container

### Using Docker Run

```bash
# Basic run command
docker run -d \
  --name referential-service \
  -p 8099:8099 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/pixelv2 \
  -e DATABASE_USERNAME=pixelv2 \
  -e DATABASE_PASSWORD=pixelv2_secure_password \
  pixel-v2/referential-runtime:latest

# With custom environment variables
docker run -d \
  --name referential-service \
  -p 8099:8099 \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/pixelv2 \
  -e DATABASE_USERNAME=your_username \
  -e DATABASE_PASSWORD=your_password \
  -e ALLOWED_ORIGINS=http://localhost:*,https://your-frontend.com \
  pixel-v2/referential-runtime:latest
```

### Using Docker Compose

```bash
# Run the referential service with dependencies
docker-compose up -d

# Build and run
docker-compose up -d --build

# View logs
docker-compose logs -f referential-runtime

# Stop the service
docker-compose down
```

### With existing Docker Compose setup

If you have an existing `docker-compose.yml` in the project root, you can include the referential service:

```bash
# From the project root
docker-compose -f docker/docker-compose.yml -f docker/referential-runtime/docker-compose.yml up -d
```

## Environment Variables

The following environment variables can be used to configure the application:

| Variable            | Default                                     | Description             |
| ------------------- | ------------------------------------------- | ----------------------- |
| `DATABASE_URL`      | `jdbc:postgresql://postgresql:5432/pixelv2` | PostgreSQL database URL |
| `DATABASE_USERNAME` | `pixelv2`                                   | Database username       |
| `DATABASE_PASSWORD` | `pixelv2_secure_password`                   | Database password       |
| `ALLOWED_ORIGINS`   | `http://localhost:*`                        | CORS allowed origins    |
| `JAVA_OPTS`         | See Dockerfile                              | JVM options             |

## Health Check

The container includes a health check that verifies the application is responding:

- **Endpoint**: `http://localhost:8099/actuator/health`
- **Interval**: 30 seconds
- **Timeout**: 3 seconds
- **Retries**: 3
- **Start Period**: 60 seconds

## API Endpoints

Once running, the following endpoints are available:

- **Health Check**: `http://localhost:8099/actuator/health`
- **Application Info**: `http://localhost:8099/actuator/info`
- **Flow API**: `http://localhost:8099/api/flows/{flowCode}/complete`

Example API call:

```bash
curl -s http://localhost:8099/api/flows/ICHSIC/complete | jq .
```

## Security Features

- Runs as non-root user (`spring:spring` with UID/GID 1001)
- Uses production-optimized JVM settings
- Includes memory and security constraints
- CORS configuration for web security

## Troubleshooting

### Check container logs

```bash
docker logs referential-service

# Follow logs
docker logs -f referential-service
```

### Check container status

```bash
docker ps
docker inspect referential-service
```

### Connect to database manually

```bash
docker exec -it pixel-v2-postgresql psql -U pixelv2 -d pixelv2
```

### Access container shell

```bash
docker exec -it referential-service sh
```

## Development

For development purposes, you can mount the application properties:

```bash
docker run -d \
  --name referential-dev \
  -p 8099:8099 \
  -v /path/to/your/application-dev.properties:/app/application-prod.properties \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/pixelv2 \
  pixel-v2/referential-runtime:latest
```
