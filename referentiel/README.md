# Referentiel Configuration Service

The Referentiel Configuration Service is a Spring Boot REST API that provides configuration management for the PIXEL-V2 payment message processing system. It manages flow configurations, country mappings, partner settings, business rules, and character encodings.

## Overview

This service acts as the central configuration repository for the PIXEL-V2 system, providing:

- Flow configuration management (payment flows like SEPA, SIC, etc.)
- Country and partner mappings
- Business rules and validation parameters
- Character encoding configurations
- Real-time configuration retrieval via REST API

## Technology Stack

- **Framework**: Spring Boot 3.4.1
- **Java Version**: Java 21
- **Database**: PostgreSQL with JPA/Hibernate
- **Build Tool**: Maven 3.9+
- **Containerization**: Docker with multi-stage builds
- **Architecture**: Microservice with RESTful API

## Database Schema

The service uses the `tib_audit_tec` schema in PostgreSQL with the following main entities:

- `ref_flow` - Payment flow definitions
- `ref_flow_country` - Flow-country associations
- `ref_flow_partner` - Flow-partner configurations
- `ref_flow_rules` - Business rules per flow
- `ref_charset_encoding` - Character encoding definitions

## API Endpoints

### Flow Configuration API

#### Get Complete Flow Configuration

```http
GET /api/flows/{flowCode}/complete
```

Returns comprehensive flow information including countries, partners, rules, and encoding settings.

**Example Request:**

```bash
curl -X GET "http://localhost:8099/api/flows/ICHSIC/complete" \
  -H "Accept: application/json"
```

**Example Response:**

```json
{
  "flowId": 71,
  "flowName": "Switzerland IN SEPA SIC",
  "flowCode": "ICHSIC",
  "flowDirection": "IN",
  "enableFlg": "Y",
  "countries": [
    {
      "flowId": 71,
      "countryId": 756
    }
  ],
  "partners": [
    {
      "partnerId": 12,
      "transportId": 3,
      "partnerDirection": "IN",
      "charsetEncodingId": 1
    }
  ],
  "rules": {
    "flowCode": "ICHSIC",
    "transportType": "MQ",
    "priority": "High",
    "flowMaximum": 800
  },
  "charsetEncodings": [
    {
      "charsetEncodingId": 1,
      "charsetCode": "UTF-8",
      "charsetDesc": "UTF-8 Unicode encoding"
    }
  ]
}
```

#### Get Basic Flow Information

```http
GET /api/flows/{flowCode}
```

Returns basic flow information without related entities.

#### List All Flows

```http
GET /api/flows
```

Returns a list of all available flow configurations.

### Management Endpoints

- **Health Check**: `/actuator/health`
- **Application Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

## Development

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL 15+ (or Docker for containerized development)
- Docker (optional, for containerized development)

### Local Development Setup

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd pixel-v2/referentiel
   ```

2. **Configure the database:**

   Update `src/main/resources/application.properties` with your local database settings:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/pixelv2
   spring.datasource.username=pixelv2
   spring.datasource.password=pixelv2_secure_password
   ```

3. **Start PostgreSQL (using Docker):**

   ```bash
   cd ../
   docker-compose -f docker/docker-compose.yml up -d postgresql
   ```

4. **Build and run the application:**

   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

5. **Verify the application:**
   ```bash
   curl http://localhost:8099/api/flows/ICHSIC/complete
   ```

### Configuration

#### Application Properties

**Development (`application.properties`):**

- Database connection to local PostgreSQL
- Debug logging enabled
- Development CORS settings

**Production (`application-prod.properties`):**

- Environment variable-based configuration
- Optimized logging levels
- Production-ready connection pooling
- Security-enhanced CORS settings

#### Environment Variables

| Variable            | Description               | Default                                    |
| ------------------- | ------------------------- | ------------------------------------------ |
| `DATABASE_URL`      | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/pixelv2` |
| `DATABASE_USERNAME` | Database username         | `pixelv2`                                  |
| `DATABASE_PASSWORD` | Database password         | `pixelv2_secure_password`                  |
| `ALLOWED_ORIGINS`   | CORS allowed origins      | `http://localhost:*`                       |

## Docker Deployment

### Building the Docker Image

The service uses a multi-stage Docker build for optimal image size and security:

```bash
# From the project root
docker build -f docker/referentiel-runtime/Dockerfile -t pixel-v2/referentiel-runtime:latest .
```

### Running with Docker

#### Using the Management Script (Recommended)

```bash
# From project root
./start-referentiel.sh                    # Build and start service
./start-referentiel.sh --build-only      # Build only
./start-referentiel.sh --rebuild --logs  # Rebuild and follow logs
./start-referentiel.sh --stop            # Stop service
./start-referentiel.sh --restart         # Restart service
```

#### Using Docker Commands Directly

```bash
# Build image
docker build -f docker/referentiel-runtime/Dockerfile -t pixel-v2/referentiel-runtime:latest .

# Run container
docker run -d \\
  --name pixel-v2-referentiel \\
  --network pixel-v2-network \\
  -p 8099:8099 \\
  -e DATABASE_URL=jdbc:postgresql://postgresql:5432/pixelv2 \\
  -e DATABASE_USERNAME=pixelv2 \\
  -e DATABASE_PASSWORD=pixelv2_secure_password \\
  pixel-v2/referentiel-runtime:latest
```

### Container Features

- **Base Image**: Amazon Corretto 21 Alpine (optimized JVM)
- **Security**: Non-root user execution
- **Health Checks**: Built-in container health monitoring
- **Resource Management**: JVM optimized for containerized environments
- **Logging**: Structured logging with configurable levels

## Architecture

### Data Access Layer

The service uses **Spring Data JPA** with **Hibernate** for data persistence:

- **Repository Pattern**: Clean separation of data access logic
- **Entity Relationships**: Proper JPA entity mappings with lazy loading
- **Connection Pooling**: HikariCP for optimal database performance
- **Composite Keys**: Support for complex primary key relationships

### Service Layer

- **Business Logic**: Centralized in service classes
- **DTO Mapping**: Clean separation between entities and API responses
- **Transaction Management**: Declarative transaction handling
- **Error Handling**: Comprehensive error handling with proper HTTP status codes

### REST API Layer

- **Spring Web MVC**: RESTful API implementation
- **Content Negotiation**: JSON response format
- **CORS Support**: Configurable cross-origin resource sharing
- **Validation**: Input validation with proper error responses

## Testing

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn integration-test

# All tests with coverage
mvn clean test jacoco:report
```

### API Testing

```bash
# Test health endpoint
curl http://localhost:8099/actuator/health

# Test complete flow configuration
curl http://localhost:8099/api/flows/ICHSIC/complete | jq .

# Test with different flow codes
curl http://localhost:8099/api/flows/pacs008/complete | jq .
```

## Monitoring and Observability

### Health Checks

The service provides comprehensive health checks:

- **Liveness**: Basic application responsiveness
- **Readiness**: Database connectivity and dependencies
- **Custom**: Business logic validation

### Metrics

Available metrics through Spring Boot Actuator:

- JVM metrics (memory, GC, threads)
- Database connection pool metrics
- HTTP request metrics
- Custom business metrics

### Logging

Structured logging with:

- **Levels**: Configurable per package/class
- **Format**: JSON format for production environments
- **Content**: Request/response logging, error tracking
- **Performance**: Async logging for high throughput

## Production Deployment

### System Requirements

- **CPU**: 2+ cores recommended
- **Memory**: 512MB - 1GB heap (depending on load)
- **Storage**: Minimal (application is stateless)
- **Network**: PostgreSQL database connectivity required

### Scaling Considerations

- **Stateless Design**: Horizontal scaling supported
- **Database**: Shared PostgreSQL instance
- **Load Balancing**: Standard HTTP load balancing
- **Caching**: Consider Redis for high-traffic scenarios

### Security

- **Non-root Execution**: Container runs as dedicated user
- **Environment Variables**: Sensitive configuration externalized
- **Network Security**: Restricted network access
- **Input Validation**: All API inputs validated

## Troubleshooting

### Common Issues

1. **Database Connection Issues**

   ```bash
   # Check database connectivity
   docker exec -it pixel-v2-postgresql psql -U pixelv2 -d pixelv2 -c "SELECT 1"
   ```

2. **Port Conflicts**

   ```bash
   # Check what's using port 8099
   lsof -i :8099
   ```

3. **Container Issues**

   ```bash
   # Check container logs
   docker logs pixel-v2-referentiel

   # Check container status
   docker ps --filter name=pixel-v2-referentiel
   ```

### Debug Mode

Enable debug logging in `application.properties`:

```properties
logging.level.com.pixel.v2.referentiel=DEBUG
logging.level.org.springframework.web=DEBUG
spring.jpa.show-sql=true
```

## Contributing

1. **Code Style**: Follow Spring Boot conventions
2. **Testing**: Add unit tests for new features
3. **Documentation**: Update API documentation
4. **Database Changes**: Include migration scripts
5. **Docker**: Update Dockerfile if dependencies change

## License

[Add license information]

## Support

For support and questions:

- **Documentation**: See project wiki
- **Issues**: Create GitHub issues
- **Development**: See CONTRIBUTING.md
