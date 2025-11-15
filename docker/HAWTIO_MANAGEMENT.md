# Hawt.io Management Console for PIXEL-V2 Camel Routes

## Overview

Hawt.io is a web-based management console that provides real-time monitoring and management capabilities for Apache Camel routes running in the PIXEL-V2 camel-runtime container.

## Features

- **Route Monitoring**: Real-time visualization of Camel routes, processors, and endpoints
- **Message Tracing**: Track message flow through routes with detailed tracing
- **Performance Metrics**: Monitor throughput, processing times, and error rates
- **Route Management**: Start, stop, suspend, and resume routes dynamically
- **JMX Integration**: Access all JMX beans and operations
- **Log Monitoring**: View application logs in real-time

## Access Information

| Service            | URL                   | Port | Description                       |
| ------------------ | --------------------- | ---- | --------------------------------- |
| Hawt.io Console    | http://localhost:8090 | 8090 | Main management interface         |
| Camel Routes (JMX) | pixel-v2-app:8778     | 8778 | JMX endpoint for route management |
| Camel Application  | http://localhost:8080 | 8080 | Main application endpoint         |

## Starting Hawt.io

### Start with Tools Profile

```bash
cd /Users/n.abassi/sources/pixel-v2/docker
docker-compose --profile tools up hawtio -d
```

### Start All Tools (including Hawt.io, Adminer, Kafdrop)

```bash
cd /Users/n.abassi/sources/pixel-v2/docker
docker-compose --profile tools up -d
```

### Check Status

```bash
docker-compose ps hawtio
```

## Configuration Details

### Environment Variables

- `HAWTIO_PROXYWHITELIST`: pixel-v2-app
- `HAWTIO_PROXY_HOST`: pixel-v2-app
- `HAWTIO_PROXY_PORT`: 8778
- `HAWTIO_AUTH_ENABLED`: false (for development)

### JMX Configuration in Camel Runtime

The camel-runtime container is configured with:

- JMX Remote enabled on port 8778
- No authentication (development mode)
- SSL disabled for simplicity
- RMI server hostname set to container name

## Using Hawt.io Console

### 1. Connect to Camel Context

1. Open http://localhost:8090 in your browser
2. Navigate to "Connect" tab
3. The connection to `pixel-v2-app:8778` should be automatic
4. Click on "Camel" to access route management

### 2. Monitor PACS-008 Routes

- **Route Diagram**: Visualize the complete PACS-008 processing flow
- **Route Statistics**: Monitor message throughput and processing times
- **Error Handling**: View failed messages and error routes

### 3. Key Monitoring Points

- `pacs008-processing-flow-yaml`: Main PACS-008 processing route
- `pacs008-error-handler-yaml`: Error handling route
- `pacs008-health-check-yaml`: Health monitoring route

### 4. Performance Metrics

- Messages processed per second
- Average processing time
- Error rates
- Queue depths

## Troubleshooting

### Connection Issues

1. **Hawt.io can't connect to Camel**

   ```bash
   # Check if camel-runtime is running and healthy
   docker logs pixel-v2-camel

   # Verify JMX port is accessible
   docker exec pixel-v2-hawtio nc -zv pixel-v2-app 8778
   ```

2. **Routes not visible**
   ```bash
   # Restart camel-runtime with proper JMX configuration
   docker-compose restart pixel-v2-app
   ```

### Log Access

```bash
# Hawt.io container logs
docker logs pixel-v2-hawtio

# Camel runtime logs
docker logs pixel-v2-camel
```

## Route Management Operations

### Via Hawt.io Console

- **Start Route**: Routes → Select Route → Start
- **Stop Route**: Routes → Select Route → Stop
- **Suspend Route**: Routes → Select Route → Suspend
- **Route Properties**: Routes → Select Route → Properties

### Message Tracing

1. Enable tracing: Camel → Tracing → Enable
2. Send test messages through routes
3. View trace details in Tracing tab

## Security Considerations

### Development Mode

- Authentication disabled for easy development
- JMX without SSL/authentication
- All management ports exposed

### Production Deployment

For production, enable:

```yaml
environment:
  HAWTIO_AUTH_ENABLED: "true"
  HAWTIO_REALM: "properties"
  # Add authentication configuration
```

## Integration with PIXEL-V2 Components

### Monitoring Points

- **Message Reception**: k-mq-message-receiver kamelet
- **Message Validation**: k-xsd-validation kamelet
- **Message Transformation**: k-xsl-transformation kamelet
- **Database Operations**: k-db-tx kamelet
- **Message Routing**: Output queue operations

### Custom Dashboards

Create custom Hawt.io dashboards for:

- PACS-008 message flow visualization
- Technical framework kamelet performance
- Error analysis and troubleshooting
- System health monitoring

## Useful JMX Beans

### Camel Context

- `org.apache.camel:context=camel-1,type=context,name="camel-1"`

### Routes

- `org.apache.camel:context=camel-1,type=routes,name="pacs008-processing-flow-yaml"`

### Endpoints

- `org.apache.camel:context=camel-1,type=endpoints,name="*"`

### Error Handlers

- `org.apache.camel:context=camel-1,type=errorhandlers,name="*"`

## Best Practices

1. **Regular Monitoring**: Check route health and performance regularly
2. **Error Analysis**: Monitor error routes for processing issues
3. **Performance Tuning**: Use metrics to optimize route performance
4. **Capacity Planning**: Monitor message throughput for scaling decisions
5. **Debugging**: Use message tracing for troubleshooting complex flows

## Related Services

- **ActiveMQ Console**: http://localhost:8161/console
- **PostgreSQL (Adminer)**: http://localhost:8091
- **Kafka (Kafdrop)**: http://localhost:9000
- **Application Health**: http://localhost:8080/actuator/health
