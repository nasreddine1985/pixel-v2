# PACS-008 Flow - Processing with JBang

Complete PACS-008 credit transfer message processing pipeline implemented as executable YAML routes using JBang and Apache Camel kamelets.

## PACS-008 Processing Flow

10-step message processing pipeline:

1. **k-mq-message-receiver** - Receive messages from Artemis MQ
2. **k-message-concat** - Aggregate messages (10s timeout or 1000 messages)
3. **k-db-tx** - Persist received messages to database
4. **k-referentiel-data-loader** - Load PACS-008 referential data
5. **k-db-tx** - Persist referential data
6. **k-xsd-validation** - Validate PACS-008 XSD structure
7. **k-xsl-transformation** - Transform PACS-008 to CDM format
8. **k-xsd-validation** - Validate CDM XSD structure
9. **k-message-split** - Split messages for individual processing
10. **JMS Producer** - Send individual messages to output queue

## Key Kamelets

- **k-mq-message-receiver** - Artemis MQ message input
- **k-message-concat** - Message aggregation (10s/1000 msgs)
- **k-db-tx** - Database persistence
- **k-referentiel-data-loader** - Configuration loading
- **k-xsd-validation** - XML schema validation
- **k-xsl-transformation** - PACS-008 to CDM transformation

## JBang Execution

**Prerequisites:** Java 11+ and JBang CLI

**Development Run:**

```bash
# Development mode with H2 database and debug logging
./run-pacs008.sh
```

**Production Run:**

```bash
# Set required environment variables
export DB_PASSWORD=your_db_password
export ARTEMIS_PASSWORD=your_artemis_password

# Run production version
./run-pacs008-prod.sh

# Or direct execution with production properties
./pacs008-complete.yaml --properties=application-prod.properties
```

## Project Structure

- **pacs008-complete.yaml** - YAML route with JBang for all environments
- **run-pacs008.sh** - Development runner script
- **run-pacs008-prod.sh** - Production runner script with env validation
- **application-dev.properties** - Development configuration (H2, debug logging)
- **application-prod.properties** - Production configuration (PostgreSQL, env vars)
- **test-setup.sh** - Prerequisites validation script
- **pom.xml** - Maven configuration with JBang plugin

## Configuration Profiles

### Development (`application-dev.properties`)

- **Database**: H2 in-memory for easy testing
- **Logging**: Debug level with SQL logging
- **Aggregation**: Smaller batch sizes (100 messages, 5s timeout)
- **Error Handling**: Fewer retries for faster feedback
- **Features**: Tracing enabled, detailed monitoring

### Production (`application-prod.properties`)

- **Database**: PostgreSQL with connection pooling
- **Logging**: INFO/WARN levels for performance
- **Aggregation**: Production batch sizes (1000 messages, 10s timeout)
- **Error Handling**: Full retry policy with backoff
- **Security**: Environment variables for sensitive data

## Production Features

✅ **Real Kamelet Integration** - Direct kamelet calls (no demo timers)  
✅ **Environment Variables** - Secure configuration via env vars  
✅ **Error Handling** - Dead letter channel with retry policy  
✅ **Performance Monitoring** - Timing headers and metrics  
✅ **Health Checks** - Periodic flow status monitoring  
✅ **Database Persistence** - Full audit trail support  
✅ **Correlation Tracking** - Message correlation throughout pipeline
