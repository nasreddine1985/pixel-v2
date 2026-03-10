# BH Payment Processing Flow (Bahrain WPS)

## Overview

This module implements the **Bahrain Wages Protection System (WPS)** payment processing flow for PIXEL-V2. It handles WPS-mandated salary payments through integration with WPS Benefit, ATLAS2 for account validation, and DOME for payment processing.

## Business Context

### What is WPS?

The Wages Protection System (WPS) is a mandatory system introduced by the Bahrain regulator to ensure timely and accurate payment of employee salaries. All BNPP Bahrain clients must pay salaries through the WPS system.

### Workflow

```
LMRA Portal → WPS Benefit → BNPP PIXEL-V2 → ATLAS2 (validation) → DOME (processing) → EFTS Bahrain (settlement)
```

### Key Requirements

- **Currency**: BHD (Bahraini Dinar)
- **Payment Type**: Salary payments only
- **Go Live**: June 2026 (target)
- **Expected Volumes**:
  - Average: 3,800 monthly salary payments
  - Peak: 5,289 monthly salary payments
  - Files: 5-20 files per day

## Architecture

### Components

1. **BhWpsAccountValidationRoute**: Handles account validation requests from WPS Benefit
2. **BhWpsPayrollRoute**: Processes payroll files and routes payments to DOME
3. **Integration Points**:
   - WPS Benefit (HTTP inbound)
   - ATLAS2 (account validation)
   - DOME (payment processing)
   - EFTS Bahrain/FAWRI (settlement)

### SLA Requirements

| Request Type               | SLA                  | Description                        |
| -------------------------- | -------------------- | ---------------------------------- |
| Account validation receipt | 1 minute             | Confirm receiving request from WPS |
| Debit account validation   | 15:00-15:30 same day | Validate debtor account            |
| Payroll file receipt       | 1 minute             | Confirm receiving payroll file     |
| EFTS validation status #1  | By 12:55 value date  | Bank validation status             |
| EFTS validation status #2  | By 14:00 value date  | Bank validation status             |
| Employer approval status   | 12:55-13:55 same day | Approval status                    |
| Payment failure status     | By 15:30 value date  | Processing failure notification    |

## Key Features

### 1. Account Validation

- Receives validation requests from WPS Benefit via HTTP
- Forwards to ATLAS2 for real-time account status check
- Returns validation response within 1 minute SLA
- Logs all validation events to Kafka for audit

### 2. Payroll Processing

- Accepts payroll files from WPS Benefit
- Performs duplicate check and validation
- Archives to NAS file system
- Routes payments to DOME
- Sends status updates back to WPS Benefit
- Tracks payment through EFTS settlement

### 3. Event-Driven Architecture

- Kafka-based event logging
- Real-time status updates
- Complete audit trail
- Integration with monitoring systems

## Configuration

### Application Properties

Key configuration sections:

```properties
# WPS HTTP Server
pixel.wps.port=8081
pixel.wps.host=0.0.0.0

# External Systems
pixel.wps.benefit.host=wps-benefit-host
pixel.atlas2.host=atlas2-host
pixel.dome.host=dome-host
pixel.efts.host=efts-host

# Flow Configuration
pixel.flow.code=BHWPS
pixel.flow.name=Bahrain WPS Payment Processing

# Archive Paths
pixel.nas.bh.archive.path=/opt/nas/BH/ARCHIVE
```

### Environment Variables

| Variable           | Description                    | Default   |
| ------------------ | ------------------------------ | --------- |
| `WPS_BENEFIT_HOST` | WPS Benefit system hostname    | localhost |
| `ATLAS2_HOST`      | ATLAS2 account validation host | localhost |
| `DOME_HOST`        | DOME payment processing host   | localhost |
| `EFTS_HOST`        | EFTS Bahrain settlement host   | localhost |

## HTTP Endpoints

### 1. Account Validation

**POST** `http://localhost:8081/wps/account-validation`

Request:

```json
{
  "accountNumber": "BH67BMAG00001299123456",
  "clientId": "CLIENT123",
  "requestId": "REQ20260310001"
}
```

Response:

```json
{
  "status": "SUCCESS",
  "accountValid": true,
  "accountStatus": "ACTIVE",
  "timestamp": "2026-03-10T10:00:00"
}
```

### 2. Payroll Processing

**POST** `http://localhost:8081/wps/payroll`

Request: XML or JSON payroll file

Response:

```json
{
  "status": "SUCCESS",
  "flowOccurId": "12345",
  "timestamp": "2026-03-10T10:00:00"
}
```

## Kamelets Used

- `k-identification`: Flow configuration and reference data
- `k-duplicate-check`: Prevent duplicate payment processing
- `k-xsd-validation`: Validate payroll file structure
- `k-dynamic-publisher`: Route payments to DOME
- `k-log-flow-summary`: Comprehensive flow logging
- `k-log-events`: Event-driven audit logging

## Building and Running

### Build

```bash
cd flow-bh
mvn clean install
```

### Run Locally

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d pixel-bh-app
```

## Monitoring

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Hawtio Console

Access at: `http://localhost:8081/actuator/hawtio`

### Metrics

Prometheus metrics available at: `http://localhost:8081/actuator/prometheus`

## Testing

### Test Account Validation

```bash
curl -X POST http://localhost:8081/wps/account-validation \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "BH67BMAG00001299123456",
    "clientId": "CLIENT123",
    "requestId": "REQ20260310001"
  }'
```

### Test Payroll Processing

```bash
curl -X POST http://localhost:8081/wps/payroll \
  -H "Content-Type: application/xml" \
  -d @sample-payroll.xml
```

## Troubleshooting

### Common Issues

1. **Connection timeout to WPS Benefit**:
   - Check `pixel.wps.benefit.host` configuration
   - Verify network connectivity

2. **Account validation failures**:
   - Check ATLAS2 connectivity
   - Verify account format (IBAN)

3. **Payroll processing delays**:
   - Monitor DOME system status
   - Check EFTS availability
   - Review Kafka event logs

### Logs

```bash
# View application logs
docker logs pixel-bh-app -f

# Check Kafka events
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic BHWPS-log-event-topic --from-beginning
```

## Security Considerations

- All HTTP endpoints should be secured with authentication in production
- Use HTTPS for all external communications
- Implement rate limiting for WPS endpoints
- Encrypt sensitive data in NAS archives
- Audit all account validation requests

## Compliance

- Full audit trail via Kafka event logging
- NAS archiving for regulatory requirements
- SLA monitoring and alerting
- Data retention policies

## References

- [GFS-BH Specification](../data/spec/GFS-BH.txt)
- [WPS Bahrain Official Documentation](https://www.lmra.gov.bh/)
- [PIXEL-V2 Technical Framework](../technical-framework/)
- [ISO 20022 Standards](https://www.iso20022.org/)
