# K-Identification Kamelet

## Overview

The k-identification kamelet is a specialized Apache Camel Kamelet that provides Redis-based reference data identification and caching for CH payment processing in the PIXEL-V2 framework. It handles flow configuration retrieval from Redis cache with automatic fallback to the referentiel service when cache misses occur.

## Features

- **Redis Caching**: Automatic caching of flow configuration data with configurable TTL
- **Cache Fallback**: Seamless fallback to referentiel service on cache miss
- **Body Preservation**: Maintains original message body throughout the identification process
- **Cache Refresh**: Kafka-based cache invalidation for real-time updates
- **Error Handling**: Robust error handling with graceful degradation

## Architecture

```
Input Message
     ↓
Cache Lookup (Redis)
     ↓
Cache HIT? → YES → Return Cached Config
     ↓                        ↓
    NO                 Original Message
     ↓                        ↓
Referentiel Service    Continue Processing
     ↓
Cache Update
     ↓
Original Message
     ↓
Continue Processing
```

## Configuration Parameters

| Parameter               | Description                     | Default                            | Required |
| ----------------------- | ------------------------------- | ---------------------------------- | -------- |
| `flowCode`              | Flow code to identify           | -                                  | Yes      |
| `referentielServiceUrl` | Base URL of referentiel service | `http://pixel-v2-referentiel:8099` | No       |
| `kafkaBrokers`          | Kafka broker URLs               | `kafka:29092`                      | No       |
| `cacheTtl`              | Cache TTL in seconds            | `3600`                             | No       |

## Usage

### Basic Usage

```yaml
- to: "kamelet:k-identification?flowCode=ICHSIC"
```

### Advanced Usage with Custom Configuration

```yaml
- to: "kamelet:k-identification?flowCode=${header.flowCode}&referentielServiceUrl=http://custom-referentiel:8099&cacheTtl=7200"
```

### Java DSL Usage

```java
// In RouteBuilder
.to("kamelet:k-identification?flowCode=${header.flowCode}&referentielServiceUrl={{pixel.referentiel.service.url}}&cacheTtl={{pixel.cache.ttl}}")
```

## Headers

### Input Headers

- `flowCode`: Flow code for identification (can be set via header instead of parameter)

### Output Headers

- `FlowConfiguration`: Retrieved flow configuration (JSON string)
- `CacheKey`: Redis cache key used
- `RedisError`: Error message if Redis operation fails (optional)

## Cache Refresh

The kamelet supports real-time cache invalidation via Kafka messages:

```json
{
  "flowCode": "ICHSIC",
  "action": "refresh"
}
```

Send this message to the `ch-refresh` Kafka topic to invalidate specific flow configuration cache.

## Dependencies

- Apache Camel Kamelet
- Spring Boot Data Redis
- Camel HTTP Component
- Camel Jackson
- Camel Kafka

## Configuration Properties

Add these properties to your `application.properties`:

```properties
# K-Identification Kamelet Configuration
pixel.referentiel.service.url=http://pixel-v2-referentiel:8099
pixel.kafka.brokers=kafka:29092
pixel.cache.ttl=3600

# Redis Configuration
spring.data.redis.host=pixel-v2-redis
spring.data.redis.port=6379
```

## Integration Example

### ChProcessingRoute Integration

```java
@Component
public class ChProcessingRoute extends RouteBuilder {

    private static final String K_IDENTIFICATION_ENDPOINT =
        "kamelet:k-identification?flowCode=${header.flowCode}&referentielServiceUrl={{pixel.referentiel.service.url}}&cacheTtl={{pixel.cache.ttl}}";

    @Override
    public void configure() throws Exception {
        from("direct:process-ch-message")
            .setHeader("flowCode", constant("ICHSIC"))
            .to(K_IDENTIFICATION_ENDPOINT)  // Body preserved, FlowConfiguration in header
            .to("kamelet:k-xsd-validation?xsdFileName=pacs.008.001.02.ch.02.xsd")
            .to("kamelet:k-xsl-transformation?xslFileName=overall-xslt-ch-pacs008-001-02.xsl");
    }
}
```

## Error Handling

The kamelet provides graceful error handling:

1. **Redis Connection Issues**: Falls back to referentiel service
2. **Referentiel Service Unavailable**: Returns error configuration with service status
3. **Cache Operation Failures**: Continues processing without caching

## Monitoring

Monitor the kamelet performance through:

- Cache hit/miss ratio logs
- Redis operation metrics
- Referentiel service response times
- Error rates per operation type

## Troubleshooting

### Common Issues

1. **Cache Always Missing**: Check Redis connectivity and configuration
2. **Body Lost**: Verify no custom processors modify the message body
3. **Configuration Not Found**: Verify flowCode and referentiel service availability
4. **Cache Not Refreshing**: Check Kafka connectivity and topic configuration

### Debug Logging

Enable debug logging for troubleshooting:

```properties
logging.level.com.pixel.kcah.identification=DEBUG
```

## Performance Considerations

- **Cache TTL**: Balance between data freshness and performance
- **Redis Pool**: Configure connection pool based on load
- **Timeout Settings**: Set appropriate timeouts for Redis and HTTP operations
- **Async Processing**: Consider async cache refresh for high-throughput scenarios

## Version History

- **1.0.1-SNAPSHOT**: Initial release with Redis caching and referentiel fallback
