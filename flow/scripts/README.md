# PACS.008 Message Injection Scripts

This directory contains scripts to inject PACS.008 sample messages into your local ActiveMQ Artemis broker for testing the Flow application.

## Scripts Overview

### 1. `inject-pacs008-messages.sh` - Comprehensive Message Injector

A full-featured script that creates realistic PACS.008 XML samples and injects them using multiple methods.

**Features:**

- Creates 5 different PACS.008 message samples (valid and invalid)
- Supports multiple injection methods (CLI, Java JMS, REST API)
- Configurable delays between messages
- Detailed logging and error handling
- Downloads required dependencies automatically

**Usage:**

```bash
# Send all sample messages with default 2s delay
./inject-pacs008-messages.sh

# Send all messages with custom delay
./inject-pacs008-messages.sh --delay 5

# Send a specific message only
./inject-pacs008-messages.sh --message pacs008-sample-1.xml

# Show help
./inject-pacs008-messages.sh --help
```

### 2. `quick-inject.sh` - Simple Test Message Sender

A lightweight script for quick testing using HTTP REST API.

**Features:**

- Generates simple test messages on-the-fly
- Fast execution using curl
- Broker connectivity check
- Batch message sending

**Usage:**

```bash
# Send one test message
./quick-inject.sh

# Send multiple messages
./quick-inject.sh multiple 5 3    # 5 messages with 3s delay

# Check broker connection only
./quick-inject.sh check

# Show help
./quick-inject.sh help
```

## Prerequisites

### ActiveMQ Artemis Setup

1. **Install ActiveMQ Artemis:**

```bash
# Download Artemis (if not already installed)
wget https://downloads.apache.org/activemq/activemq-artemis/2.31.2/apache-artemis-2.31.2-bin.tar.gz
tar -xzf apache-artemis-2.31.2-bin.tar.gz
export ARTEMIS_HOME=/path/to/apache-artemis-2.31.2
```

2. **Create and Start Broker:**

```bash
# Create broker instance
$ARTEMIS_HOME/bin/artemis create --user artemis --password artemis --allow-anonymous mybroker

# Start the broker
cd mybroker
./bin/artemis run
```

3. **Verify Broker is Running:**

- Console: http://localhost:8161/console (artemis/artemis)
- Port 61616 for JMS connections
- Port 8161 for web console

### Required Tools

The scripts will check for and use available tools:

- **curl** - For REST API calls (required)
- **java** - For JMS client method
- **nc (netcat)** - For connection testing (optional)
- **wget** - For downloading dependencies (optional, curl as fallback)

## Configuration

Both scripts use the same default configuration matching your Flow application:

```bash
# ActiveMQ Artemis Connection
BROKER_URL="tcp://localhost:61616"     # JMS connection
USERNAME="artemis"
PASSWORD="artemis"
QUEUE_NAME="PACS008_QUEUE"            # Target queue

# Web Console (for REST API)
CONSOLE_URL="http://localhost:8161"
```

## Sample Messages

The comprehensive script creates these message types:

1. **pacs008-sample-1.xml** - Simple EUR credit transfer
2. **pacs008-sample-2.xml** - Large USD corporate payment
3. **pacs008-sample-3.xml** - Multi-transaction GBP batch
4. **pacs008-simple-test.xml** - Minimal test message
5. **pacs008-invalid.xml** - Malformed message for error testing

## Testing Workflow

### 1. Quick Testing

```bash
# Start your Flow application in one terminal
cd /Users/n.abassi/sources/pixel-v2/flow
mvn spring-boot:run

# In another terminal, send a test message
./scripts/quick-inject.sh

# Check Flow application logs for processing
```

### 2. Comprehensive Testing

```bash
# Send all sample messages
./scripts/inject-pacs008-messages.sh

# Monitor the Flow application logs to see:
# - Message reception
# - JMS property extraction
# - Database persistence simulation
# - Error handling (for invalid message)
```

### 3. Load Testing

```bash
# Send multiple batches with different delays
./scripts/quick-inject.sh multiple 10 1    # 10 messages, 1s delay
./scripts/inject-pacs008-messages.sh --delay 0.5  # All samples, 0.5s delay
```

## Troubleshooting

### Common Issues

1. **"Cannot connect to broker"**

   - Check if Artemis is running: `netstat -an | grep 61616`
   - Verify credentials: artemis/artemis
   - Check broker logs for errors

2. **"HTTP 401 Unauthorized"**

   - Verify username/password in scripts match broker configuration
   - Check if web console is enabled

3. **"Queue not found"**

   - The queue `PACS008_QUEUE` is created automatically by the Flow application
   - Make sure your Flow application has started and connected to Artemis

4. **"Java compilation failed"**
   - Ensure Java is installed and in PATH
   - The script will try to download Artemis client JAR automatically

### Validation

**Check if messages were sent:**

```bash
# Via Artemis console
curl -u artemis:artemis "http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\",component=addresses,address=\"PACS008_QUEUE\",subcomponent=queues,routing-type=\"anycast\",queue=\"PACS008_QUEUE\"/MessageCount"

# Via Flow application logs
tail -f /path/to/flow/logs/application.log | grep PACS008
```

**Check Flow application processing:**

- Look for log messages: "Received PACS.008 message"
- Check database for persisted messages (if persistence is implemented)
- Monitor for any error messages in Flow logs

## Integration with Flow Application

The messages injected by these scripts will be processed by your Flow application through this flow:

1. **Message Reception**: JMS listener in `Pacs008Route` receives messages
2. **Header Extraction**: JMS properties are extracted and logged
3. **Validation**: Messages are validated (non-empty check)
4. **Processing**: Valid messages go through the processing pipeline
5. **Persistence**: Messages are persisted using the database route
6. **Error Handling**: Invalid messages are routed to error handler

## Script Maintenance

### Updating Message Samples

To modify or add new PACS.008 samples, edit the `create_sample_messages()` function in `inject-pacs008-messages.sh`.

### Configuration Changes

Update the configuration variables at the top of each script if your Artemis setup differs:

- Different ports
- Different credentials
- Different queue names
- Different broker locations

### Adding New Injection Methods

The comprehensive script supports multiple injection methods. To add a new method:

1. Create a new function `send_message_with_newmethod()`
2. Add it to the method chain in `send_message()`
3. Test with sample messages

## Performance Notes

- **Quick Script**: Fastest for single messages, uses HTTP REST API
- **Comprehensive Script**: More robust, tries multiple methods, includes extensive samples
- **Batch Operations**: Both scripts support batch sending with configurable delays
- **Resource Usage**: Java JMS method uses more memory but is most reliable

For high-volume testing, consider using the Java JMS method as it maintains persistent connections.
