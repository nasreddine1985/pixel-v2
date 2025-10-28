# PACS.008 Message Injection Solution - Summary

## 🎯 Overview

A comprehensive solution for injecting PACS.008 test messages into your local ActiveMQ Artemis broker to test the Flow application. The solution includes multiple injection methods, realistic sample messages, and validation tools.

## 📁 Files Created

### Core Scripts

- **`inject-pacs008-messages.sh`** - Comprehensive injection with multiple methods
- **`quick-inject.sh`** - Fast single/batch message injection
- **`run.sh`** - Convenient command runner
- **`test-setup.sh`** - Setup validation and connectivity testing

### Documentation

- **`README.md`** - Detailed usage guide and troubleshooting
- **`pacs008-samples/`** - Directory with 5 sample PACS.008 XML messages

## ✅ Validation Results

Your setup has been tested and validated:

```
✅ Java availability
✅ curl availability
✅ netcat available
✅ Artemis JMS port (61616)
✅ Artemis web console (8161)
✅ Web console authentication
✅ Script permissions
✅ Message injection functionality
```

**ActiveMQ Artemis broker is running and accessible!**

## 🚀 Quick Start

### 1. Validate Setup

```bash
cd /Users/n.abassi/sources/pixel-v2/flow/scripts
./run.sh test
```

### 2. Send Test Message

```bash
# Single test message
./run.sh quick

# Multiple test messages
./run.sh batch 5
```

### 3. Send Sample Messages

```bash
# All PACS.008 samples
./run.sh samples

# With custom delay
./run.sh samples --delay 3
```

## 📊 Sample Messages Available

1. **pacs008-sample-1.xml** - Simple EUR credit transfer (1,500 EUR)
2. **pacs008-sample-2.xml** - Large USD corporate payment (25,000 USD)
3. **pacs008-sample-3.xml** - Multi-transaction GBP batch (5,750 GBP total)
4. **pacs008-simple-test.xml** - Minimal test message (100 EUR)
5. **pacs008-invalid.xml** - Malformed message for error testing

## 🔧 Configuration

**ActiveMQ Artemis:**

- JMS Connection: `tcp://localhost:61616`
- Web Console: `http://localhost:8161/console`
- Credentials: `artemis/artemis`
- Target Queue: `PACS008_QUEUE`

**Flow Application:**

- Queue Listener: `PACS008_QUEUE`
- Database: PostgreSQL (pixelv2)
- Processing Route: PACS.008 → JMS → Validation → Persistence

## 🔄 Message Flow

```
Script → ActiveMQ Artemis → Flow Application → PostgreSQL
  ↓         ↓                    ↓               ↓
XML       PACS008_QUEUE      Route Processing  Database
Message   JMS Properties     Header Extraction  Persistence
          Message Body       Validation         Error Handling
```

## 📋 Available Injection Methods

1. **Artemis CLI** (if available) - Native broker client
2. **Java JMS Client** - Direct JMS connection with persistence
3. **REST API** - HTTP-based injection via web console

Scripts automatically try methods in order and fall back as needed.

## 🧪 Testing Workflow

### Development Testing

```bash
# Terminal 1: Start Flow application
cd /Users/n.abassi/sources/pixel-v2/flow
mvn spring-boot:run

# Terminal 2: Send messages
cd scripts
./run.sh quick                    # Single message test
./run.sh samples                  # All samples
```

### Load Testing

```bash
./run.sh batch 10                 # 10 quick messages
./inject-pacs008-messages.sh --delay 1  # All samples, 1s delay
```

### Error Testing

```bash
# Send invalid message to test error handling
./inject-pacs008-messages.sh --message pacs008-invalid.xml
```

## 📈 Monitoring

**Check Message Injection:**

- Scripts provide detailed logs with success/failure status
- JMS Message IDs are logged for tracking
- HTTP response codes shown for REST API methods

**Check Flow Processing:**

- Monitor Flow application logs for: "Received PACS.008 message"
- Check database for persisted messages
- Look for error handling logs for invalid messages

**Check Queue Status:**

```bash
# Via web console
open http://localhost:8161/console

# Via REST API
curl -u artemis:artemis "http://localhost:8161/console/jolokia/read/org.apache.activemq.artemis:broker=\"0.0.0.0\",component=addresses,address=\"PACS008_QUEUE\"/MessageCount"
```

## 🔍 Troubleshooting

### Common Issues & Solutions

**"Cannot connect to broker"**

```bash
# Check if Artemis is running
netstat -an | grep 61616
ps aux | grep artemis

# Start if needed
cd /path/to/artemis-broker
./bin/artemis run
```

**"Message injection failed"**

```bash
# Test connectivity
./run.sh check

# Validate setup
./run.sh test

# Try alternative method
./quick-inject.sh single
```

**"Flow app not processing messages"**

- Ensure Flow app is started: `mvn spring-boot:run`
- Check Flow app can connect to Artemis
- Verify queue name matches: `PACS008_QUEUE`
- Check Spring profiles are correct (not test profile)

## 📝 Next Steps

1. **Start Flow Application** (if not running):

   ```bash
   cd /Users/n.abassi/sources/pixel-v2/flow
   mvn spring-boot:run
   ```

2. **Begin Message Testing**:

   ```bash
   cd scripts
   ./run.sh quick        # Test single message
   ./run.sh samples      # Test all samples
   ```

3. **Monitor Processing**:

   - Watch Flow application logs
   - Check database for persisted messages
   - Verify error handling works

4. **Customize as Needed**:
   - Modify sample messages in `pacs008-samples/`
   - Adjust delays and batch sizes
   - Add new message types or test scenarios

## 🎉 Success Criteria

✅ **Scripts Created**: All injection scripts are executable and tested  
✅ **Samples Generated**: 5 realistic PACS.008 messages ready for testing  
✅ **Connectivity Verified**: ActiveMQ Artemis is accessible and authenticated  
✅ **Injection Tested**: Messages can be successfully sent to target queue  
✅ **Documentation Complete**: Full usage guide and troubleshooting available

**Your PACS.008 message injection solution is ready for production testing!**

## 📞 Support

For issues or questions:

1. Check `scripts/README.md` for detailed documentation
2. Run `./run.sh test` to validate setup
3. Use `./inject-pacs008-messages.sh --help` for advanced options
4. Monitor ActiveMQ Artemis web console for queue status
