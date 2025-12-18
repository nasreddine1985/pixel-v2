# k-identification-interne

Spring Cache-based internal identification kamelet for PIXEL-V2 payment processing system.

## Overview

The `k-identification-interne` module provides a memory-based alternative to the Redis-based `k-identification` module. It uses Spring Boot's internal caching framework with Caffeine cache provider for high-performance, low-latency reference data identification and caching.

## Features

- **Spring Cache Integration**: Uses Spring Boot's caching abstraction with Caffeine cache provider
- **Memory-Only Caching**: No external dependencies like Redis - all caching is done in application memory
- **High Performance**: Caffeine cache provides superior performance with TTL, size limits, and statistics
- **Fallback Support**: Automatic fallback to ConcurrentMap cache if Caffeine is not available
- **Referentiel Integration**: Automatic fetching from referentiel service with cache population
- **Cache Management**: Full cache lifecycle management with eviction, clearing, and statistics

## Architecture

```
Camel Route → k-identification-interne kamelet → Spring Cache → Referentiel Service (fallback)
```

## Components

### Kamelet

- `k-identification-interne.kamelet.yaml`: Main kamelet definition with Spring cache integration

### Processors

- `SpringCacheProcessor`: Handles cache operations (get, put, evict, clear)

### Services

- `IdentificationCacheService`: High-level cache management service with statistics

### Configuration

- `SpringCacheConfig`: Spring cache configuration with Caffeine and ConcurrentMap providers

## Dependencies

- Spring Boot Starter
- Spring Cache
- Apache Camel 4.16.0
- Caffeine Cache (primary provider)
- Jackson (JSON processing)
- Camel HTTP (referentiel service integration)
- Camel Kafka (cache refresh messaging)

## Usage

### Basic Usage

```yaml
- from:
    uri: "direct:identify-flow"
    steps:
      - to: "kamelet:k-identification-interne?flowCode={{header.flowCode}}"
```

### With Configuration

```yaml
- from:
    uri: "direct:identify-flow"
    steps:
      - to: "kamelet:k-identification-interne?flowCode={{header.flowCode}}&referentielServiceUrl=http://pixel-v2-referentiel:8099&springCacheName=flowConfigCache"
```

## Configuration Parameters

| Parameter               | Description             | Default                            | Required |
| ----------------------- | ----------------------- | ---------------------------------- | -------- |
| `flowCode`              | Flow code to identify   | -                                  | Yes      |
| `referentielServiceUrl` | Referentiel service URL | `http://pixel-v2-referentiel:8099` | No       |
| `kafkaBrokers`          | Kafka broker URLs       | `kafka:29092`                      | No       |
| `cacheTtl`              | Cache TTL in seconds    | `3600`                             | No       |
| `springCacheName`       | Spring cache name       | `flowConfigCache`                  | No       |

## Cache Configuration

The module uses Caffeine cache with the following default settings:

- **TTL**: 1 hour
- **Maximum Size**: 1000 entries per cache
- **Statistics**: Enabled
- **Cache Names**: `flowConfigCache`, `referentielCache`, `identificationCache`

### Custom Configuration

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: expireAfterWrite=1h,maximumSize=1000,recordStats
    cache-names:
      - flowConfigCache
      - referentielCache
      - identificationCache
```

## Cache Operations

### Automatic Operations

- **Cache Miss**: Automatically fetches from referentiel service and populates cache
- **Cache Hit**: Returns cached value immediately
- **Error Handling**: Graceful degradation on cache or service errors

### Manual Operations

```java
@Autowired
private IdentificationCacheService cacheService;

// Get flow configuration
Optional<String> config = cacheService.getFlowConfiguration("PACS008");

// Put flow configuration
cacheService.putFlowConfiguration("PACS008", configJson);

// Evict specific flow
cacheService.evictFlowConfiguration("PACS008");

// Clear all flows
cacheService.clearFlowConfigurations();

// Get statistics
Map<String, Object> stats = cacheService.getCacheStatistics("flowConfigCache");
```

## Headers

The kamelet preserves the original message body and uses headers for cache operations:

- `FlowConfiguration`: Contains the retrieved flow configuration (JSON)
- `OriginalMessageBody`: Preserved original message body
- `CacheKey`: Generated cache key
- `SpringCacheName`: Cache name for operations
- `SpringCacheKey`: Cache key for operations
- `SpringCacheValue`: Cache value for put operations

## Error Handling

- **Referentiel Service Unavailable**: Returns error JSON with service unavailable status
- **Cache Errors**: Logged but do not interrupt message flow
- **Invalid Parameters**: Logged with appropriate warnings

## Performance

### Cache Statistics

Monitor cache performance using the `IdentificationCacheService`:

```java
Map<String, Object> stats = cacheService.getCacheStatistics("flowConfigCache");
// Returns: hitCount, missCount, hitRate, evictionCount, estimatedSize
```

### Memory Usage

- Caffeine cache with 1000 entries limit per cache
- Automatic eviction based on size and TTL
- Statistics recording for monitoring

## Integration

### With Docker Compose

The module is automatically included in the `pixel-v2-camel` container builds and deployed with the technical framework.

### With Kubernetes

Deploy as part of the PIXEL-V2 technical framework pod with appropriate memory limits for cache sizing.

## Differences from k-identification

| Feature      | k-identification (Redis)   | k-identification-interne (Spring Cache) |
| ------------ | -------------------------- | --------------------------------------- |
| Storage      | External Redis             | In-memory (application)                 |
| Dependencies | Redis server required      | No external dependencies                |
| Persistence  | Persistent across restarts | Lost on application restart             |
| Sharing      | Shared across instances    | Instance-specific                       |
| Performance  | Network latency            | Memory access speed                     |
| Scalability  | Horizontal scaling         | Vertical scaling                        |
| Management   | External Redis tools       | Spring Boot actuator                    |

## Troubleshooting

### Common Issues

1. **Cache Not Found Warnings**: Check cache configuration and cache names
2. **Referentiel Service Errors**: Verify service URL and network connectivity
3. **Memory Issues**: Adjust cache size limits and JVM heap settings
4. **Statistics Not Available**: Ensure Caffeine cache provider is active

### Logging

Enable debug logging for cache operations:

```yaml
logging:
  level:
    com.pixel.v2.identification.interne: DEBUG
    org.springframework.cache: DEBUG
```

## Build

```bash
cd technical-framework/k-identification-interne
mvn clean install
```

## Testing

The module can be tested using the standard PIXEL-V2 testing framework with cache-specific test scenarios for hit/miss behavior and error handling.
