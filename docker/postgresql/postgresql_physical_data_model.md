# TIB_AUDIT_TEC - PostgreSQL Physical Data Model

## Overview

This document presents the physical data model for the TIB_AUDIT_TEC schema, converted from Oracle to PostgreSQL format. The schema manages audit and technical configuration for message flows, transports, and partner integrations.

## Database Schema: `tib_audit_tec`

### Core Entity Relationship Diagram

```mermaid
erDiagram
    REF_APPLICATION {
        bigserial application_id PK
        varchar application_code UK
        varchar application_name
    }

    REF_FLOW {
        bigserial flow_id PK
        bigint func_process_id FK
        bigint flow_typ_id FK
        bigint tech_process_id FK
        varchar flow_name
        varchar flow_direction
        varchar flow_code UK
        varchar enable_flg
        timestamp creation_dte
        timestamp update_dte
        bigint application_id FK
        bigint max_file_size
    }

    REF_FLOW_TYP {
        bigserial flow_typ_id PK
        varchar flow_typ_name UK
    }

    REF_FUNC_PROCESS {
        bigserial func_process_id PK
        varchar func_process_name UK
        timestamp creation_dte
        timestamp update_dte
    }

    REF_TECH_PROCESS {
        bigserial tech_process_id PK
        timestamp creation_dte
        varchar tech_process_name UK
        timestamp update_dte
    }

    REF_PARTNER {
        bigserial partner_id PK
        bigint partner_type_id FK
        varchar partner_name
        varchar partner_code UK
        timestamp creation_dte
        timestamp update_dte
    }

    REF_PARTNER_TYP {
        bigserial partner_type_id PK
        varchar partner_type_name UK
    }

    REF_FLOW_PARTNER {
        bigint partner_id FK
        bigint flow_id FK
        bigint transport_id FK
        varchar partner_direction
        timestamp creation_dte
        timestamp update_dte
        bigint rule_id FK
        bigint charset_encoding_id FK
        varchar enable_out
        varchar enable_bmsa
    }

    REF_TRANSPORT {
        bigserial transport_id PK
        varchar transport_typ
    }

    REF_TRANSPORT_HTTP {
        bigint transport_id PK FK
        varchar http_uri
        varchar http_partner_code
        varchar client_port
        varchar client_host
        varchar client_method
        text client_body
        varchar issecure
        varchar expectedcode_succes
        varchar apigee_host
        varchar apigee_port
        varchar apigee_uri
        varchar apigee_method
        numeric throughput_max
        numeric nb_retry
    }

    REF_TRANSPORT_EMAIL {
        bigint transport_id PK FK
        varchar email_name UK
        varchar email_from
        varchar email_recipient_to
        varchar email_recipient_cc
        varchar email_subject
        text email_body
        text email_signature
        varchar has_attachment
    }

    REF_TRANSPORT_JMS {
        bigint transport_id PK FK
        varchar jms_q_name UK
    }

    REF_TRANSPORT_MQS {
        bigint transport_id PK FK
        varchar mqs_q_name
        varchar mqs_q_manager
    }

    REF_TRANSPORT_CFT {
        bigint transport_id PK FK
        varchar cft_idf
        varchar cft_partner_code
    }

    REF_COUNTRY {
        bigserial country_id PK
        varchar country_name
        varchar country_iso_code UK
        varchar is_sepa
        varchar region
    }

    REF_FLOW_COUNTRY {
        bigint flow_id FK
        bigint country_id FK
    }

    REF_CHARSET_ENCODING {
        bigserial charset_encoding_id PK
        varchar charset_code UK
        varchar charset_desc
    }

    REF_FUNCTION {
        bigserial function_id PK
        varchar function_typ
        varchar function_name
        varchar function_tech_id
    }

    REF_PRTY_FLOW {
        bigserial prty_flow_id PK
        varchar prty_flow_name
        varchar prty_flow_desc
        varchar prty_flow_typ
    }

    REF_FUNC_PROCESS_PRTY {
        bigint func_process_id FK
        bigint prty_flow_id FK
        varchar flow_prty_value
    }

    REF_ROUTE_RULE {
        bigserial rule_id PK
        varchar rule_name UK
    }

    REF_APPLICATION ||--o{ REF_FLOW : "has"
    REF_FLOW ||--|| REF_FLOW_TYP : "is_of_type"
    REF_FLOW ||--|| REF_FUNC_PROCESS : "uses_functional_process"
    REF_FLOW ||--|| REF_TECH_PROCESS : "uses_technical_process"
    REF_FLOW ||--o{ REF_FLOW_PARTNER : "has_partners"
    REF_FLOW ||--o{ REF_FLOW_COUNTRY : "serves_countries"
    REF_PARTNER ||--|| REF_PARTNER_TYP : "is_of_type"
    REF_PARTNER ||--o{ REF_FLOW_PARTNER : "participates_in"
    REF_FLOW_PARTNER ||--|| REF_TRANSPORT : "uses_transport"
    REF_FLOW_PARTNER ||--|| REF_ROUTE_RULE : "follows_rule"
    REF_FLOW_PARTNER ||--o| REF_CHARSET_ENCODING : "uses_encoding"
    REF_TRANSPORT ||--o| REF_TRANSPORT_HTTP : "http_config"
    REF_TRANSPORT ||--o| REF_TRANSPORT_EMAIL : "email_config"
    REF_TRANSPORT ||--o| REF_TRANSPORT_JMS : "jms_config"
    REF_TRANSPORT ||--o| REF_TRANSPORT_MQS : "mqs_config"
    REF_TRANSPORT ||--o| REF_TRANSPORT_CFT : "cft_config"
    REF_COUNTRY ||--o{ REF_FLOW_COUNTRY : "served_by"
    REF_FUNC_PROCESS ||--o{ REF_FUNC_PROCESS_PRTY : "has_properties"
    REF_PRTY_FLOW ||--o{ REF_FUNC_PROCESS_PRTY : "defines_property"
```

## PostgreSQL Schema Definition

### 1. Core Reference Tables

#### Applications

```sql
CREATE TABLE tib_audit_tec.ref_application (
    application_id BIGSERIAL PRIMARY KEY,
    application_code VARCHAR(10) UNIQUE NOT NULL,
    application_name VARCHAR(100) NOT NULL
);
```

#### Countries

```sql
CREATE TABLE tib_audit_tec.ref_country (
    country_id BIGSERIAL PRIMARY KEY,
    country_name VARCHAR(50) NOT NULL,
    country_iso_code VARCHAR(2) UNIQUE NOT NULL,
    is_sepa VARCHAR(1),
    region VARCHAR(5)
);
```

#### Character Set Encoding

```sql
CREATE TABLE tib_audit_tec.ref_charset_encoding (
    charset_encoding_id BIGSERIAL PRIMARY KEY,
    charset_code VARCHAR(30) UNIQUE NOT NULL,
    charset_desc VARCHAR(100)
);
```

### 2. Process Definition Tables

#### Functional Process

```sql
CREATE TABLE tib_audit_tec.ref_func_process (
    func_process_id BIGSERIAL PRIMARY KEY,
    func_process_name VARCHAR(50) UNIQUE NOT NULL,
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6)
);
```

#### Technical Process

```sql
CREATE TABLE tib_audit_tec.ref_tech_process (
    tech_process_id BIGSERIAL PRIMARY KEY,
    creation_dte TIMESTAMP(6),
    tech_process_name VARCHAR(50) UNIQUE NOT NULL,
    update_dte TIMESTAMP(6)
);
```

#### Flow Types

```sql
CREATE TABLE tib_audit_tec.ref_flow_typ (
    flow_typ_id BIGSERIAL PRIMARY KEY,
    flow_typ_name VARCHAR(100) UNIQUE NOT NULL
);
```

#### Functional Properties

```sql
CREATE TABLE tib_audit_tec.ref_prty_flow (
    prty_flow_id BIGSERIAL PRIMARY KEY,
    prty_flow_name VARCHAR(50) NOT NULL,
    prty_flow_desc VARCHAR(255),
    prty_flow_typ VARCHAR(50) NOT NULL
);
```

#### Functional Process Properties

```sql
CREATE TABLE tib_audit_tec.ref_func_process_prty (
    func_process_id BIGINT NOT NULL,
    prty_flow_id BIGINT NOT NULL,
    flow_prty_value VARCHAR(255),
    PRIMARY KEY (func_process_id, prty_flow_id),
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id),
    FOREIGN KEY (prty_flow_id) REFERENCES tib_audit_tec.ref_prty_flow(prty_flow_id)
);
```

### 3. Partner Management

#### Partner Types

```sql
CREATE TABLE tib_audit_tec.ref_partner_typ (
    partner_type_id BIGSERIAL PRIMARY KEY,
    partner_type_name VARCHAR(100) UNIQUE NOT NULL
);
```

#### Partners

```sql
CREATE TABLE tib_audit_tec.ref_partner (
    partner_id BIGSERIAL PRIMARY KEY,
    partner_type_id BIGINT NOT NULL,
    partner_name VARCHAR(100),
    partner_code VARCHAR(10) UNIQUE NOT NULL,
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    FOREIGN KEY (partner_type_id) REFERENCES tib_audit_tec.ref_partner_typ(partner_type_id)
);
```

### 4. Flow Management

#### Flows

```sql
CREATE TABLE tib_audit_tec.ref_flow (
    flow_id BIGSERIAL PRIMARY KEY,
    func_process_id BIGINT NOT NULL,
    flow_typ_id BIGINT NOT NULL,
    tech_process_id BIGINT NOT NULL,
    flow_name VARCHAR(100),
    flow_direction VARCHAR(3),
    flow_code VARCHAR(50) UNIQUE NOT NULL,
    enable_flg VARCHAR(1),
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    application_id BIGINT,
    max_file_size BIGINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id),
    FOREIGN KEY (flow_typ_id) REFERENCES tib_audit_tec.ref_flow_typ(flow_typ_id),
    FOREIGN KEY (tech_process_id) REFERENCES tib_audit_tec.ref_tech_process(tech_process_id),
    FOREIGN KEY (application_id) REFERENCES tib_audit_tec.ref_application(application_id)
);
```

#### Flow Control

```sql
CREATE TABLE tib_audit_tec.ref_flow_control (
    flow_id BIGINT PRIMARY KEY,
    flow_control_type VARCHAR(100),
    release_hour INTEGER,
    release_min INTEGER,
    release_sec INTEGER,
    batch_count INTEGER,
    batch_duration_sec INTEGER,
    is_active VARCHAR(1),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id)
);
```

#### Flow Country Association

```sql
CREATE TABLE tib_audit_tec.ref_flow_country (
    flow_id BIGINT NOT NULL,
    country_id BIGINT NOT NULL,
    PRIMARY KEY (flow_id, country_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (country_id) REFERENCES tib_audit_tec.ref_country(country_id)
);
```

#### Flow Rules

```sql
CREATE TABLE tib_audit_tec.ref_flow_rules (
    flowcode VARCHAR(200) PRIMARY KEY,
    transporttype VARCHAR(50) NOT NULL,
    isunitary VARCHAR(20) NOT NULL,
    priority NUMERIC,
    urgency VARCHAR(100),
    flowcontrolledenabled VARCHAR(100),
    flowmaximum NUMERIC,
    flowretentionenabled VARCHAR(100),
    retentioncycleperiod NUMERIC,
    write_file VARCHAR(20) DEFAULT 'false',
    minrequiredfilesize NUMERIC,
    ignoreoutputdupcheck VARCHAR(20) DEFAULT 'false',
    logall VARCHAR(20) DEFAULT 'false',
    CHECK (isunitary IN ('true', 'false')),
    CHECK (flowcontrolledenabled IN ('true', 'false')),
    CHECK (retentioncycleperiod > 0),
    CHECK (flowmaximum > 0)
);
```

### 5. Transport Configuration

#### Base Transport

```sql
CREATE TABLE tib_audit_tec.ref_transport (
    transport_id BIGSERIAL PRIMARY KEY,
    transport_typ VARCHAR(20)
);
```

#### Route Rules

```sql
CREATE TABLE tib_audit_tec.ref_route_rule (
    rule_id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) UNIQUE NOT NULL
);
```

#### Flow Partner Association

```sql
CREATE TABLE tib_audit_tec.ref_flow_partner (
    partner_id BIGINT NOT NULL,
    flow_id BIGINT NOT NULL,
    transport_id BIGINT NOT NULL,
    partner_direction VARCHAR(10),
    creation_dte TIMESTAMP(6),
    update_dte TIMESTAMP(6),
    rule_id BIGINT DEFAULT 1 NOT NULL,
    charset_encoding_id BIGINT,
    enable_out VARCHAR(1) DEFAULT 'Y',
    enable_bmsa VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (partner_id, flow_id, rule_id, transport_id),
    FOREIGN KEY (partner_id) REFERENCES tib_audit_tec.ref_partner(partner_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id),
    FOREIGN KEY (rule_id) REFERENCES tib_audit_tec.ref_route_rule(rule_id),
    FOREIGN KEY (charset_encoding_id) REFERENCES tib_audit_tec.ref_charset_encoding(charset_encoding_id)
);
```

#### HTTP Transport Configuration

```sql
CREATE TABLE tib_audit_tec.ref_transport_http (
    transport_id BIGINT PRIMARY KEY,
    http_uri VARCHAR(50) NOT NULL,
    http_partner_code VARCHAR(50) NOT NULL,
    client_port VARCHAR(10),
    client_host VARCHAR(100),
    client_method VARCHAR(20),
    client_body TEXT,
    issecure VARCHAR(10),
    expectedcode_succes VARCHAR(100),
    apigee_host VARCHAR(100),
    apigee_port VARCHAR(10),
    apigee_uri VARCHAR(100),
    apigee_method VARCHAR(30),
    throughput_max NUMERIC,
    nb_retry NUMERIC,
    UNIQUE (http_uri, http_partner_code),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);
```

#### Email Transport Configuration

```sql
CREATE TABLE tib_audit_tec.ref_transport_email (
    transport_id BIGINT PRIMARY KEY,
    email_name VARCHAR(50) UNIQUE NOT NULL,
    email_from VARCHAR(250) NOT NULL,
    email_recipient_to VARCHAR(250) NOT NULL,
    email_recipient_cc VARCHAR(250),
    email_subject VARCHAR(150) NOT NULL,
    email_body TEXT,
    email_signature TEXT,
    has_attachment VARCHAR(1) NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);
```

#### JMS Transport Configuration

```sql
CREATE TABLE tib_audit_tec.ref_transport_jms (
    transport_id BIGINT PRIMARY KEY,
    jms_q_name VARCHAR(128) UNIQUE NOT NULL,
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);
```

#### MQ Series Transport Configuration

```sql
CREATE TABLE tib_audit_tec.ref_transport_mqs (
    transport_id BIGINT PRIMARY KEY,
    mqs_q_name VARCHAR(50) NOT NULL,
    mqs_q_manager VARCHAR(30) NOT NULL,
    UNIQUE (mqs_q_name, mqs_q_manager),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);
```

#### CFT Transport Configuration

```sql
CREATE TABLE tib_audit_tec.ref_transport_cft (
    transport_id BIGINT PRIMARY KEY,
    cft_idf VARCHAR(30) NOT NULL,
    cft_partner_code VARCHAR(30) NOT NULL,
    UNIQUE (cft_idf, cft_partner_code),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport(transport_id)
);
```

### 6. Function Management

#### Functions

```sql
CREATE TABLE tib_audit_tec.ref_function (
    function_id BIGSERIAL PRIMARY KEY,
    function_typ VARCHAR(20),
    function_name VARCHAR(20),
    function_tech_id VARCHAR(30)
);
```

#### External Services

```sql
CREATE TABLE tib_audit_tec.ref_external_service (
    function_id BIGINT PRIMARY KEY,
    service_code VARCHAR(64) UNIQUE NOT NULL,
    transport_request BIGINT NOT NULL,
    transport_reply BIGINT,
    transport_feedback BIGINT,
    transport_feedback_reply BIGINT,
    transport_late_reply BIGINT,
    timeout_soft INTEGER,
    timeout_hard INTEGER,
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id) ON DELETE CASCADE
);
```

#### Function Process Definition

```sql
CREATE TABLE tib_audit_tec.ref_func_process_def (
    function_id BIGINT NOT NULL,
    func_process_id BIGINT NOT NULL,
    function_process_order INTEGER,
    PRIMARY KEY (function_id, func_process_id),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id),
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id)
);
```

### 7. Character Replacement and Properties

#### Property Flow

```sql
CREATE TABLE tib_audit_tec.ref_prty_flow (
    prty_flow_id BIGSERIAL PRIMARY KEY,
    prty_flow_name VARCHAR(100) NOT NULL,
    prty_flow_desc VARCHAR(100) NOT NULL,
    prty_flow_typ VARCHAR(20) NOT NULL
);
```

#### Function Process Properties

```sql
CREATE TABLE tib_audit_tec.ref_func_process_prty (
    func_process_id BIGINT NOT NULL,
    prty_flow_id BIGINT NOT NULL,
    flow_prty_value VARCHAR(512),
    PRIMARY KEY (func_process_id, prty_flow_id),
    FOREIGN KEY (func_process_id) REFERENCES tib_audit_tec.ref_func_process(func_process_id),
    FOREIGN KEY (prty_flow_id) REFERENCES tib_audit_tec.ref_prty_flow(prty_flow_id)
);
```

#### Set Replacement

```sql
CREATE TABLE tib_audit_tec.ref_set_replacement (
    set_replacement_id BIGSERIAL PRIMARY KEY,
    set_replacement_name VARCHAR(50) UNIQUE NOT NULL,
    set_replacement_desc VARCHAR(100)
);
```

#### Character Replacement

```sql
CREATE TABLE tib_audit_tec.ref_char_replace (
    set_replacement_id BIGINT NOT NULL,
    char_old_version VARCHAR(50) NOT NULL,
    char_new_version VARCHAR(50),
    char_to_replace_typ VARCHAR(20),
    PRIMARY KEY (set_replacement_id, char_old_version),
    FOREIGN KEY (set_replacement_id) REFERENCES tib_audit_tec.ref_set_replacement(set_replacement_id)
);
```

#### Function Character Replace

```sql
CREATE TABLE tib_audit_tec.ref_func_char_rplc (
    function_id BIGINT PRIMARY KEY,
    char_replace_typ VARCHAR(20),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_function(function_id)
);
```

#### Function Set Replace

```sql
CREATE TABLE tib_audit_tec.ref_func_set_rplc (
    partner_id BIGINT NOT NULL,
    flow_id BIGINT NOT NULL,
    function_id BIGINT NOT NULL,
    set_replacement_id BIGINT NOT NULL,
    PRIMARY KEY (partner_id, flow_id, function_id, set_replacement_id),
    FOREIGN KEY (partner_id) REFERENCES tib_audit_tec.ref_partner(partner_id),
    FOREIGN KEY (flow_id) REFERENCES tib_audit_tec.ref_flow(flow_id),
    FOREIGN KEY (function_id) REFERENCES tib_audit_tec.ref_func_char_rplc(function_id),
    FOREIGN KEY (set_replacement_id) REFERENCES tib_audit_tec.ref_set_replacement(set_replacement_id)
);
```

### 8. Notification and Email Management

#### Email Recipients

```sql
CREATE TABLE tib_audit_tec.ref_email_recipient (
    email_recipient_id BIGSERIAL PRIMARY KEY,
    owner_app VARCHAR(50) NOT NULL,
    param VARCHAR(100),
    transport_id BIGINT NOT NULL,
    mail_from VARCHAR(250),
    mail_to VARCHAR(250),
    mail_cc VARCHAR(250),
    mail_bcc VARCHAR(250),
    is_html CHAR(1),
    FOREIGN KEY (transport_id) REFERENCES tib_audit_tec.ref_transport_email(transport_id)
);
```

#### Post Flow Notification

```sql
CREATE TABLE tib_audit_tec.ref_postflow_notify (
    functionality VARCHAR(64),
    channel VARCHAR(64),
    destination VARCHAR(512)
);
```

## Sample Data Configuration

### OCHSIC Flow Example

The schema includes comprehensive sample data for an OCHSIC (CH Switzerland ACH Clearing) payment flow that demonstrates the complete integration pattern.

#### Flow Configuration

```sql
-- OCHSIC Payment Flow
Flow Code: OCHSIC
Flow Name: OCHSIC Payment Flow
Direction: BID (Bidirectional)
Type: Payment Processing
Application: PIXEL (Pixel Integration Platform)
```

#### Partner Configuration

| Partner Code | Partner Name                  | Type             | Direction | Transport | Encoding |
| ------------ | ----------------------------- | ---------------- | --------- | --------- | -------- |
| CHSIC        | CH - Switzerland ACH Clearing | Internal Partner | IN        | MQS       | UTF-8    |
| DOME         | Domestic payment engine       | Internal Partner | OUT       | CFT       | UTF-8    |
| CHBANK01     | Swiss Bank Partner            | External Bank    | INOUT     | MQ        | UTF-8    |

#### Transport Configurations

**MQS Transport (CHSIC - IN Partner)**

```sql
Queue Name: OITARTMI01
Queue Manager: FRITL01Z
```

**CFT Transport (DOME - OUT Partner)**

```sql
CFT IDF: DOME_CFT_IDF
Partner Code: DOME
```

**MQ Transport (CHBANK01 - General Partner)**

```sql
Queue Name: OCHSIC.QUEUE
Queue Manager: QM_PIXEL_V2
```

#### Functional Properties

| Property Key | Type        | Description                              | Value                            |
| ------------ | ----------- | ---------------------------------------- | -------------------------------- |
| BIC          | Enrichment  | FP for CH SIC CLEARING Incoming payments | BPPBCHGGXXX                      |
| PcssA2Bank   | Enrichment  | FP for CH SIC CLEARING Incoming payments | P01                              |
| XmlSchema    | Validation  | FP for CH SIC CLEARING Incoming payments | pacs.008.001.08.ch.02.itl.v2.xsd |
| Application  | Application | FP for CH SIC CLEARING Incoming payments | ITL                              |

#### Flow Rules Configuration

```sql
Transport Type: MQ
Priority: 1 (HIGH)
Flow Maximum: 100 messages
Flow Retention: Enabled (7 days)
Controlled Flow: Disabled
Unitary Processing: True
```

This example demonstrates:

- **Multi-protocol integration**: MQS (IN), CFT (OUT), MQ (General)
- **Functional properties**: Enrichment, validation, and application context
- **Partner direction handling**: Dedicated IN/OUT partners plus bidirectional
- **Rule-based routing**: Different routing rules for different partners
- **Country-specific flows**: Switzerland (CH) regional configuration

## Key Design Patterns

### 1. **Central Flow Management**

- `REF_FLOW` serves as the central entity connecting functional and technical processes
- Supports versioning through creation and update timestamps
- Configurable flow control through `REF_FLOW_CONTROL`

### 2. **Multi-Protocol Transport Support**

- Polymorphic transport configuration through specialized tables
- Supports HTTP, Email, JMS, MQ Series, and CFT protocols
- Each transport type has specific configuration parameters

### 3. **Partner Integration Framework**

- Many-to-many relationship between flows and partners
- Rule-based routing through `REF_ROUTE_RULE`
- Character encoding support for different partner systems

### 4. **Flexible Function Processing**

- Composable function processes with ordered execution
- **Functional Properties System**: Key-value configuration through `REF_PRTY_FLOW` and `REF_FUNC_PROCESS_PRTY`
- Property-based configuration with typed categories (Enrichment, Validation, Application)
- Character replacement for data transformation

### 5. **Functional Properties Framework**

- **Property Definitions**: `REF_PRTY_FLOW` defines reusable property templates
- **Process Binding**: `REF_FUNC_PROCESS_PRTY` binds properties to specific functional processes
- **Typed Properties**: Support for Enrichment, Validation, and Application property types
- **Value Storage**: Flexible string-based value storage with type-specific interpretation

### 6. **Geographic and Regulatory Support**

- Country-specific flow configurations
- SEPA compliance indicators
- Region-based grouping

## Data Types Conversion Notes

| Oracle Type        | PostgreSQL Type | Notes                 |
| ------------------ | --------------- | --------------------- |
| `NUMBER(10,0)`     | `BIGINT`        | For ID fields         |
| `NUMBER`           | `NUMERIC`       | Preserves precision   |
| `VARCHAR2(n BYTE)` | `VARCHAR(n)`    | Character length      |
| `CLOB`             | `TEXT`          | Large text objects    |
| `TIMESTAMP(6)`     | `TIMESTAMP(6)`  | Microsecond precision |
| `CHAR(1 BYTE)`     | `CHAR(1)`       | Single characters     |

## Indexes and Performance Considerations

### Primary Indexes

- All `*_id` fields use `BIGSERIAL` with automatic primary key indexes
- Composite primary keys for junction tables

### Unique Constraints

- Business natural keys (codes, names) have unique constraints
- Prevents duplicate configurations

### Foreign Key Indexes

- PostgreSQL automatically creates indexes for foreign keys
- Supports efficient joins and referential integrity

### Recommended Additional Indexes

```sql
-- Performance indexes for common queries
CREATE INDEX idx_flow_partner_flow_id ON tib_audit_tec.ref_flow_partner(flow_id);
CREATE INDEX idx_flow_partner_partner_id ON tib_audit_tec.ref_flow_partner(partner_id);
CREATE INDEX idx_flow_application_id ON tib_audit_tec.ref_flow(application_id);
CREATE INDEX idx_flow_creation_dte ON tib_audit_tec.ref_flow(creation_dte);
CREATE INDEX idx_func_process_prty_process_id ON tib_audit_tec.ref_func_process_prty(func_process_id);
CREATE INDEX idx_func_process_prty_prty_flow_id ON tib_audit_tec.ref_func_process_prty(prty_flow_id);
```

## Sample Data Initialization

The schema includes comprehensive sample data through the `insert_ochsic_sample_data.sql` script:

- **Reference Data**: Applications, flow types, partner types, countries, charsets, route rules
- **OCHSIC Flow**: Complete payment flow configuration with three-partner setup
- **Transport Configurations**: MQ, MQS, and CFT transport protocols
- **Functional Properties**: Property definitions and process-specific bindings
- **Verification Queries**: Comprehensive validation queries for data integrity

The sample data provides a working example of a Swiss payment clearing integration suitable for testing and development.

## Business Logic Constraints

### Check Constraints

- Boolean-like fields use string values ('true'/'false')
- Numeric validation for positive values
- Data quality rules embedded in schema

### Referential Integrity

- Cascade deletes for configuration cleanup
- Foreign key constraints maintain data consistency
- Junction tables support many-to-many relationships

This schema provides a robust foundation for managing complex message routing, partner integration, and audit requirements in a PostgreSQL environment.
