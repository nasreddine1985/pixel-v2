#!/bin/bash

# PACS.008 Message Injection Script for ActiveMQ Artemis
# This script injects sample PACS.008 messages into the local ActiveMQ Artemis broker

set -e  # Exit on any error

# Configuration
ARTEMIS_HOME=${ARTEMIS_HOME:-"/opt/artemis"}
BROKER_URL="tcp://localhost:61616"
USERNAME="artemis"
PASSWORD="artemis"
QUEUE_NAME="PACS008_QUEUE"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MESSAGES_DIR="$SCRIPT_DIR/pacs008-samples"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are available
check_requirements() {
    log_info "Checking requirements..."
    
    # Check if Java is available
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check if curl is available for REST API
    if ! command -v curl &> /dev/null; then
        log_error "curl is not installed"
        exit 1
    fi
    
    log_success "Requirements check passed"
}

# Create sample PACS.008 messages
create_sample_messages() {
    log_info "Creating sample PACS.008 messages..."
    
    # Create messages directory
    mkdir -p "$MESSAGES_DIR"
    
    # Sample 1: Simple credit transfer
    cat > "$MESSAGES_DIR/pacs008-sample-1.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>PACS008-001-20241028-001</MsgId>
            <CreDtTm>2024-10-28T10:30:00.000Z</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="EUR">1500.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>2024-10-28</IntrBkSttlmDt>
            <SttlmInf>
                <SttlmMtd>CLRG</SttlmMtd>
            </SttlmInf>
            <InstgAgt>
                <FinInstnId>
                    <BICFI>DEUTDEFF</BICFI>
                </FinInstnId>
            </InstgAgt>
            <InstdAgt>
                <FinInstnId>
                    <BICFI>BNPAFRPP</BICFI>
                </FinInstnId>
            </InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>INST001-20241028</InstrId>
                <EndToEndId>E2E001-20241028</EndToEndId>
                <TxId>TXN001-20241028</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="EUR">1500.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr>
                <Nm>John Smith</Nm>
                <PstlAdr>
                    <Ctry>DE</Ctry>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <IBAN>DE89370400440532013000</IBAN>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>DEUTDEFF</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>BNPAFRPP</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>Marie Dupont</Nm>
                <PstlAdr>
                    <Ctry>FR</Ctry>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <IBAN>FR1420041010050500013M02606</IBAN>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Payment for invoice INV-2024-001</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
EOF

    # Sample 2: Larger amount transfer
    cat > "$MESSAGES_DIR/pacs008-sample-2.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>PACS008-002-20241028-002</MsgId>
            <CreDtTm>2024-10-28T11:15:00.000Z</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="USD">25000.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>2024-10-28</IntrBkSttlmDt>
            <SttlmInf>
                <SttlmMtd>INDA</SttlmMtd>
            </SttlmInf>
            <InstgAgt>
                <FinInstnId>
                    <BICFI>CHASUS33</BICFI>
                </FinInstnId>
            </InstgAgt>
            <InstdAgt>
                <FinInstnId>
                    <BICFI>CITIUS33</BICFI>
                </FinInstnId>
            </InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>INST002-20241028</InstrId>
                <EndToEndId>E2E002-20241028</EndToEndId>
                <TxId>TXN002-20241028</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="USD">25000.00</IntrBkSttlmAmt>
            <ChrgBr>DEBT</ChrgBr>
            <Dbtr>
                <Nm>Tech Solutions Corp</Nm>
                <PstlAdr>
                    <Ctry>US</Ctry>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <Othr>
                        <Id>1234567890</Id>
                    </Othr>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>CHASUS33</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>CITIUS33</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>Global Manufacturing Ltd</Nm>
                <PstlAdr>
                    <Ctry>US</Ctry>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <Othr>
                        <Id>9876543210</Id>
                    </Othr>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Monthly service fee payment</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
EOF

    # Sample 3: Multi-transaction batch
    cat > "$MESSAGES_DIR/pacs008-sample-3.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>PACS008-003-20241028-003</MsgId>
            <CreDtTm>2024-10-28T12:00:00.000Z</CreDtTm>
            <NbOfTxs>2</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="GBP">5750.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>2024-10-28</IntrBkSttlmDt>
            <SttlmInf>
                <SttlmMtd>CLRG</SttlmMtd>
            </SttlmInf>
            <InstgAgt>
                <FinInstnId>
                    <BICFI>BARCGB22</BICFI>
                </FinInstnId>
            </InstgAgt>
            <InstdAgt>
                <FinInstnId>
                    <BICFI>HBUKGB4B</BICFI>
                </FinInstnId>
            </InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>INST003A-20241028</InstrId>
                <EndToEndId>E2E003A-20241028</EndToEndId>
                <TxId>TXN003A-20241028</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="GBP">3250.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr>
                <Nm>UK Imports Ltd</Nm>
                <PstlAdr>
                    <Ctry>GB</Ctry>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <IBAN>GB33BUKB20201555555555</IBAN>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>BARCGB22</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>HBUKGB4B</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>European Suppliers SA</Nm>
                <PstlAdr>
                    <Ctry>GB</Ctry>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <IBAN>GB94BARC10201530093459</IBAN>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Goods payment batch 1</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>INST003B-20241028</InstrId>
                <EndToEndId>E2E003B-20241028</EndToEndId>
                <TxId>TXN003B-20241028</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="GBP">2500.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr>
                <Nm>UK Imports Ltd</Nm>
                <PstlAdr>
                    <Ctry>GB</Ctry>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <IBAN>GB33BUKB20201555555555</IBAN>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>BARCGB22</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>HBUKGB4B</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>Logistics Partners Ltd</Nm>
                <PstlAdr>
                    <Ctry>GB</Ctry>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <IBAN>GB29NWBK60161331926819</IBAN>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Shipping services Q4</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
EOF

    # Sample 4: Simple test message (minimal)
    cat > "$MESSAGES_DIR/pacs008-simple-test.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>TEST-001</MsgId>
            <CreDtTm>2024-10-28T13:00:00.000Z</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="EUR">100.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>2024-10-28</IntrBkSttlmDt>
            <SttlmInf>
                <SttlmMtd>CLRG</SttlmMtd>
            </SttlmInf>
            <InstgAgt>
                <FinInstnId>
                    <BICFI>TESTBANK</BICFI>
                </FinInstnId>
            </InstgAgt>
            <InstdAgt>
                <FinInstnId>
                    <BICFI>TESTBANK</BICFI>
                </FinInstnId>
            </InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>TEST-INST-001</InstrId>
                <EndToEndId>TEST-E2E-001</EndToEndId>
                <TxId>TEST-TXN-001</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="EUR">100.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr>
                <Nm>Test Debtor</Nm>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <IBAN>DE89370400440532013000</IBAN>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>TESTBANK</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>TESTBANK</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>Test Creditor</Nm>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <IBAN>FR1420041010050500013M02606</IBAN>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Test payment</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
EOF

    # Sample 5: Invalid/Malformed message for error testing
    cat > "$MESSAGES_DIR/pacs008-invalid.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>INVALID-001</MsgId>
            <!-- Missing required CreDtTm -->
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="EUR">INVALID_AMOUNT</TtlIntrBkSttlmAmt>
            <!-- Missing IntrBkSttlmDt -->
        </GrpHdr>
        <!-- Missing CdtTrfTxInf section -->
    </FIToFICstmrCdtTrf>
</Document>
EOF

    log_success "Created $(ls -1 "$MESSAGES_DIR" | wc -l) sample PACS.008 messages in $MESSAGES_DIR"
}

# Send message using Artemis CLI
send_message_with_cli() {
    local message_file="$1"
    local message_content
    
    if [ ! -f "$message_file" ]; then
        log_error "Message file not found: $message_file"
        return 1
    fi
    
    message_content=$(cat "$message_file")
    local filename=$(basename "$message_file")
    
    log_info "Sending message from $filename using Artemis CLI..."
    
    # Try to use artemis CLI if available
    if command -v artemis &> /dev/null; then
        artemis producer \
            --url "$BROKER_URL" \
            --user "$USERNAME" \
            --password "$PASSWORD" \
            --destination "queue://$QUEUE_NAME" \
            --message-count 1 \
            --message "$message_content" \
            --verbose 2>/dev/null
        
        if [ $? -eq 0 ]; then
            log_success "Message sent successfully: $filename"
            return 0
        else
            log_warning "Artemis CLI failed, trying alternative method..."
            return 1
        fi
    else
        log_warning "Artemis CLI not found, trying alternative method..."
        return 1
    fi
}

# Send message using REST API
send_message_with_rest() {
    local message_file="$1"
    local message_content
    
    if [ ! -f "$message_file" ]; then
        log_error "Message file not found: $message_file"
        return 1
    fi
    
    message_content=$(cat "$message_file")
    local filename=$(basename "$message_file")
    
    log_info "Sending message from $filename using REST API..."
    
    # Escape the message content for JSON
    local escaped_message=$(echo "$message_content" | sed 's/"/\\"/g' | sed ':a;N;$!ba;s/\n/\\n/g')
    
    # Create JSON payload
    local json_payload=$(cat << EOF
{
    "type": "TEXT",
    "message": "$escaped_message",
    "headers": {
        "MessageType": "pacs.008.001.08",
        "Source": "inject-script",
        "Timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")"
    }
}
EOF
)
    
    # Send via REST API (if Artemis console is running on port 8161)
    local response=$(curl -s -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -u "$USERNAME:$PASSWORD" \
        -d "$json_payload" \
        "http://localhost:8161/console/jolokia/exec/org.apache.activemq.artemis:broker=\"0.0.0.0\"/sendMessage/java.lang.String/java.lang.String/java.lang.String/boolean/java.util.Map/$QUEUE_NAME/TEXT/$escaped_message/false/{}")
    
    local http_code="${response: -3}"
    if [ "$http_code" -eq 200 ]; then
        log_success "Message sent successfully via REST API: $filename"
        return 0
    else
        log_warning "REST API failed with HTTP code: $http_code"
        return 1
    fi
}

# Send message using Java JMS client
send_message_with_java() {
    local message_file="$1"
    local message_content
    
    if [ ! -f "$message_file" ]; then
        log_error "Message file not found: $message_file"
        return 1
    fi
    
    message_content=$(cat "$message_file")
    local filename=$(basename "$message_file")
    
    log_info "Sending message from $filename using Java JMS client..."
    
    # Create temporary Java sender
    local java_file="$SCRIPT_DIR/MessageSender.java"
    cat > "$java_file" << 'JAVA_EOF'
import javax.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MessageSender {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: java MessageSender <brokerUrl> <username> <password> <queueName> <messageFile>");
            System.exit(1);
        }
        
        String brokerUrl = args[0];
        String username = args[1];
        String password = args[2];
        String queueName = args[3];
        String messageFile = args[4];
        
        try {
            // Read message content
            String messageContent = new String(Files.readAllBytes(Paths.get(messageFile)));
            
            // Create connection factory
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setUser(username);
            factory.setPassword(password);
            
            // Create connection and session
            try (Connection connection = factory.createConnection();) {
                connection.start();
                
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(queueName);
                MessageProducer producer = session.createProducer(queue);
                
                // Create and send message
                TextMessage message = session.createTextMessage(messageContent);
                message.setStringProperty("MessageType", "pacs.008.001.08");
                message.setStringProperty("Source", "inject-script");
                message.setLongProperty("InjectTimestamp", System.currentTimeMillis());
                
                producer.send(message);
                
                System.out.println("Message sent successfully: " + messageFile);
                System.out.println("JMS Message ID: " + message.getJMSMessageID());
                
                producer.close();
                session.close();
            }
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
JAVA_EOF

    # Check if we have artemis client JAR
    local artemis_jar=""
    
    # Look for Artemis client JAR in common locations
    for jar_path in \
        "$ARTEMIS_HOME/lib/artemis-jms-client-all-*.jar" \
        "/opt/artemis/lib/artemis-jms-client-all-*.jar" \
        "$HOME/.m2/repository/org/apache/activemq/artemis-jms-client-all/*/artemis-jms-client-all-*.jar" \
        "./lib/artemis-jms-client-all-*.jar"; do
        
        if ls $jar_path 1> /dev/null 2>&1; then
            artemis_jar=$(ls $jar_path | head -1)
            break
        fi
    done
    
    if [ -z "$artemis_jar" ]; then
        log_warning "Artemis client JAR not found. Trying to download..."
        
        # Download Artemis client JAR if not found
        local artemis_version="2.31.2"
        artemis_jar="$SCRIPT_DIR/artemis-jms-client-all-${artemis_version}.jar"
        
        if [ ! -f "$artemis_jar" ]; then
            local download_url="https://repo1.maven.org/maven2/org/apache/activemq/artemis-jms-client-all/${artemis_version}/artemis-jms-client-all-${artemis_version}.jar"
            log_info "Downloading Artemis client JAR..."
            
            if command -v wget &> /dev/null; then
                wget -q -O "$artemis_jar" "$download_url"
            elif command -v curl &> /dev/null; then
                curl -s -L -o "$artemis_jar" "$download_url"
            else
                log_error "Neither wget nor curl available for downloading JAR"
                rm -f "$java_file"
                return 1
            fi
            
            if [ ! -f "$artemis_jar" ]; then
                log_error "Failed to download Artemis client JAR"
                rm -f "$java_file"
                return 1
            fi
        fi
    fi
    
    log_info "Using Artemis client JAR: $artemis_jar"
    
    # Compile and run Java sender
    javac -cp "$artemis_jar" "$java_file" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        java -cp ".:$artemis_jar:$SCRIPT_DIR" MessageSender \
            "$BROKER_URL" "$USERNAME" "$PASSWORD" "$QUEUE_NAME" "$message_file"
        
        local exit_code=$?
        
        # Cleanup
        rm -f "$java_file" "$SCRIPT_DIR/MessageSender.class"
        
        if [ $exit_code -eq 0 ]; then
            log_success "Message sent successfully via Java JMS: $filename"
            return 0
        else
            log_error "Failed to send message via Java JMS: $filename"
            return 1
        fi
    else
        log_error "Failed to compile Java sender"
        rm -f "$java_file"
        return 1
    fi
}

# Send a single message using available methods
send_message() {
    local message_file="$1"
    local filename=$(basename "$message_file")
    
    log_info "Attempting to send: $filename"
    
    # Try different methods in order of preference
    if send_message_with_cli "$message_file"; then
        return 0
    elif send_message_with_java "$message_file"; then
        return 0
    elif send_message_with_rest "$message_file"; then
        return 0
    else
        log_error "All methods failed for: $filename"
        return 1
    fi
}

# Generate dynamic PACS.008 message for load testing
generate_load_test_message() {
    local message_id="$1"
    local timestamp="$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")"
    local amount=$((RANDOM % 50000 + 1000))  # Random amount between 1000-50999
    local currencies=("EUR" "USD" "GBP" "CHF" "JPY")
    local currency=${currencies[$((RANDOM % ${#currencies[@]}))]}
    
    # Generate unique identifiers
    local msg_ref="LOAD-$(printf "%06d" $message_id)-$(date +%Y%m%d)"
    local instr_id="INST-$(printf "%06d" $message_id)"
    local e2e_id="E2E-$(printf "%06d" $message_id)"
    local txn_id="TXN-$(printf "%06d" $message_id)"
    
    cat << EOF
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>$msg_ref</MsgId>
            <CreDtTm>$timestamp</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="$currency">$amount.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>$(date -u +"%Y-%m-%d")</IntrBkSttlmDt>
            <SttlmInf>
                <SttlmMtd>CLRG</SttlmMtd>
            </SttlmInf>
            <InstgAgt>
                <FinInstnId>
                    <BICFI>LOADTEST</BICFI>
                </FinInstnId>
            </InstgAgt>
            <InstdAgt>
                <FinInstnId>
                    <BICFI>LOADTEST</BICFI>
                </FinInstnId>
            </InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>$instr_id</InstrId>
                <EndToEndId>$e2e_id</EndToEndId>
                <TxId>$txn_id</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="$currency">$amount.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr>
                <Nm>Load Test Debtor $message_id</Nm>
                <PstlAdr>
                    <Ctry>DE</Ctry>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <IBAN>DE89370400440532$(printf "%06d" $message_id)</IBAN>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>LOADTEST</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>LOADTEST</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>Load Test Creditor $message_id</Nm>
                <PstlAdr>
                    <Ctry>FR</Ctry>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <IBAN>FR1420041010050500$(printf "%06d" $message_id)06</IBAN>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>Load test payment $message_id</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>
EOF
}

# Send message directly via Java JMS (optimized for load testing)
send_load_test_message() {
    local message_content="$1"
    local message_id="$2"
    
    # Create temporary Java sender for batch processing
    local java_file="$SCRIPT_DIR/LoadTestSender.java"
    
    if [ ! -f "$java_file" ]; then
        cat > "$java_file" << 'JAVA_EOF'
import javax.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import java.io.*;

public class LoadTestSender {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: java LoadTestSender <brokerUrl> <username> <password> <queueName>");
            System.exit(1);
        }
        
        String brokerUrl = args[0];
        String username = args[1];
        String password = args[2];
        String queueName = args[3];
        
        try {
            // Create connection factory
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setUser(username);
            factory.setPassword(password);
            
            // Create connection and session
            try (Connection connection = factory.createConnection();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(queueName);
                MessageProducer producer = session.createProducer(queue);
                
                String line;
                int count = 0;
                StringBuilder messageBuilder = new StringBuilder();
                
                // Read message content from stdin
                while ((line = reader.readLine()) != null) {
                    if (line.equals("---MESSAGE_END---")) {
                        if (messageBuilder.length() > 0) {
                            // Create and send message
                            TextMessage message = session.createTextMessage(messageBuilder.toString());
                            message.setStringProperty("MessageType", "pacs.008.001.08");
                            message.setStringProperty("Source", "load-test");
                            message.setLongProperty("InjectTimestamp", System.currentTimeMillis());
                            
                            producer.send(message);
                            count++;
                            
                            if (count % 1000 == 0) {
                                System.err.println("Sent " + count + " messages...");
                            }
                            
                            messageBuilder.setLength(0);
                        }
                    } else {
                        messageBuilder.append(line).append("\n");
                    }
                }
                
                System.out.println("Total messages sent: " + count);
                
                producer.close();
                session.close();
            }
        } catch (Exception e) {
            System.err.println("Error sending messages: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
JAVA_EOF
    fi
    
    # Check if compiled class exists
    if [ ! -f "$SCRIPT_DIR/LoadTestSender.class" ]; then
        # Find Artemis JAR
        local artemis_jar=""
        for jar_path in \
            "$ARTEMIS_HOME/lib/artemis-jms-client-all-*.jar" \
            "/opt/artemis/lib/artemis-jms-client-all-*.jar" \
            "$HOME/.m2/repository/org/apache/activemq/artemis-jms-client-all/*/artemis-jms-client-all-*.jar" \
            "./lib/artemis-jms-client-all-*.jar"; do
            
            if ls $jar_path 1> /dev/null 2>&1; then
                artemis_jar=$(ls $jar_path | head -1)
                break
            fi
        done
        
        if [ -z "$artemis_jar" ]; then
            # Download if not found
            local artemis_version="2.31.2"
            artemis_jar="$SCRIPT_DIR/artemis-jms-client-all-${artemis_version}.jar"
            
            if [ ! -f "$artemis_jar" ]; then
                log_info "Downloading Artemis client JAR for load testing..."
                local download_url="https://repo1.maven.org/maven2/org/apache/activemq/artemis-jms-client-all/${artemis_version}/artemis-jms-client-all-${artemis_version}.jar"
                
                if command -v curl &> /dev/null; then
                    curl -s -L -o "$artemis_jar" "$download_url"
                elif command -v wget &> /dev/null; then
                    wget -q -O "$artemis_jar" "$download_url"
                else
                    log_error "Cannot download Artemis JAR - neither curl nor wget available"
                    return 1
                fi
            fi
        fi
        
        # Compile Java class
        javac -cp "$artemis_jar" "$java_file" 2>/dev/null || {
            log_error "Failed to compile Java load test sender"
            return 1
        }
    fi
    
    echo "$message_content"
    echo "---MESSAGE_END---"
}

# Send all messages (updated for load testing)
send_all_messages() {
    local success_count=0
    local total_count=0
    local delay=${1:-0}  # Default no delay for load testing
    local message_count=${2:-100000}  # Default 100,000 messages
    local batch_size=${3:-1000}  # Process in batches of 1000
    
    log_info "Starting load test: sending $message_count PACS.008 messages..."
    log_info "Batch size: $batch_size messages, Delay between messages: ${delay}s"
    
    # For load testing, send sample messages first (if they exist)
    if [ -d "$MESSAGES_DIR" ] && [ "$(ls -A "$MESSAGES_DIR"/*.xml 2>/dev/null | wc -l)" -gt 0 ]; then
        log_info "Sending sample messages first..."
        for message_file in "$MESSAGES_DIR"/*.xml; do
            if [ -f "$message_file" ]; then
                total_count=$((total_count + 1))
                
                if send_message "$message_file"; then
                    success_count=$((success_count + 1))
                fi
                
                if [ $delay -gt 0 ]; then
                    sleep "$delay"
                fi
            fi
        done
        log_info "Sample messages sent: $success_count/$total_count"
    fi
    
    # Generate and send load test messages
    log_info "Generating and sending $message_count load test messages..."
    
    # Find Artemis JAR for compilation
    local artemis_jar=""
    for jar_path in \
        "$ARTEMIS_HOME/lib/artemis-jms-client-all-*.jar" \
        "/opt/artemis/lib/artemis-jms-client-all-*.jar" \
        "$HOME/.m2/repository/org/apache/activemq/artemis-jms-client-all/*/artemis-jms-client-all-*.jar" \
        "./lib/artemis-jms-client-all-*.jar"; do
        
        if ls $jar_path 1> /dev/null 2>&1; then
            artemis_jar=$(ls $jar_path | head -1)
            break
        fi
    done
    
    if [ -z "$artemis_jar" ]; then
        local artemis_version="2.31.2"
        artemis_jar="$SCRIPT_DIR/artemis-jms-client-all-${artemis_version}.jar"
        
        if [ ! -f "$artemis_jar" ]; then
            log_info "Downloading Artemis client JAR..."
            local download_url="https://repo1.maven.org/maven2/org/apache/activemq/artemis-jms-client-all/${artemis_version}/artemis-jms-client-all-${artemis_version}.jar"
            
            if command -v curl &> /dev/null; then
                curl -s -L -o "$artemis_jar" "$download_url"
            elif command -v wget &> /dev/null; then
                wget -q -O "$artemis_jar" "$download_url"
            fi
        fi
    fi
    
    # Prepare Java sender
    send_load_test_message "" 1  # This will compile the class
    
    # Start time for performance measurement
    local start_time=$(date +%s)
    local load_test_success=0
    local load_test_total=0
    
    # Send messages in batches
    for ((batch_start=1; batch_start<=message_count; batch_start+=batch_size)); do
        local batch_end=$((batch_start + batch_size - 1))
        if [ $batch_end -gt $message_count ]; then
            batch_end=$message_count
        fi
        
        log_info "Sending batch: messages $batch_start-$batch_end..."
        
        # Generate batch and pipe to Java sender
        {
            for ((i=batch_start; i<=batch_end; i++)); do
                generate_load_test_message $i
                echo "---MESSAGE_END---"
                load_test_total=$((load_test_total + 1))
            done
        } | java -cp ".:$artemis_jar:$SCRIPT_DIR" LoadTestSender \
            "$BROKER_URL" "$USERNAME" "$PASSWORD" "$QUEUE_NAME" 2>&1
        
        if [ $? -eq 0 ]; then
            local batch_count=$((batch_end - batch_start + 1))
            load_test_success=$((load_test_success + batch_count))
            log_success "Batch completed: $batch_count messages sent successfully"
        else
            log_error "Batch failed: messages $batch_start-$batch_end"
        fi
        
        # Small delay between batches if specified
        if [ $delay -gt 0 ] && [ $batch_end -lt $message_count ]; then
            sleep "$delay"
        fi
    done
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    local total_success=$((success_count + load_test_success))
    local total_messages=$((total_count + load_test_total))
    
    log_info "Load test completed in ${duration} seconds"
    log_success "Total messages sent successfully: $total_success/$total_messages"
    
    if [ $duration -gt 0 ]; then
        local messages_per_second=$((total_success / duration))
        log_info "Average throughput: $messages_per_second messages/second"
    fi
    
    # Cleanup
    rm -f "$SCRIPT_DIR/LoadTestSender.java" "$SCRIPT_DIR/LoadTestSender.class"
    
    if [ $total_success -lt $total_messages ]; then
        log_warning "Some messages failed to send. Check Artemis broker status."
        return 1
    fi
    
    return 0
}

# Check broker connectivity
check_broker_connection() {
    log_info "Checking ActiveMQ Artemis broker connectivity..."
    
    # Try to connect using telnet-like approach
    if command -v nc &> /dev/null; then
        if nc -z localhost 61616 2>/dev/null; then
            log_success "Broker is accessible on port 61616"
        else
            log_error "Cannot connect to broker on localhost:61616"
            log_error "Please ensure ActiveMQ Artemis is running"
            return 1
        fi
    else
        log_warning "netcat (nc) not available, skipping connection check"
    fi
    
    # Try REST API connection
    if command -v curl &> /dev/null; then
        local response=$(curl -s -w "%{http_code}" -o /dev/null \
            -u "$USERNAME:$PASSWORD" \
            "http://localhost:8161/console/" 2>/dev/null || echo "000")
        
        if [ "$response" -eq 200 ] || [ "$response" -eq 302 ]; then
            log_success "Broker web console is accessible on port 8161"
        else
            log_warning "Broker web console not accessible (HTTP: $response)"
        fi
    fi
}

# Main execution function
main() {
    echo
    log_info "PACS.008 Message Injection Script for ActiveMQ Artemis"
    log_info "======================================================"
    echo
    
    # Parse command line arguments
    local delay=0
    local send_all=true
    local specific_message=""
    local message_count=100000
    local batch_size=1000
    local load_test=true
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --delay|-d)
                delay="$2"
                shift 2
                ;;
            --message|-m)
                specific_message="$2"
                send_all=false
                load_test=false
                shift 2
                ;;
            --count|-c)
                message_count="$2"
                shift 2
                ;;
            --batch-size|-b)
                batch_size="$2"
                shift 2
                ;;
            --samples-only|-s)
                load_test=false
                shift
                ;;
            --help|-h)
                cat << 'HELP_EOF'
PACS.008 Message Injection Script - Load Testing Edition

Usage: ./inject-pacs008-messages.sh [OPTIONS]

Options:
    --delay, -d SECONDS         Delay between messages/batches (default: 0 for load test)
    --count, -c NUMBER          Number of load test messages (default: 100000)
    --batch-size, -b NUMBER     Batch size for load testing (default: 1000)
    --message, -m FILE          Send specific message file only
    --samples-only, -s          Send only sample messages (no load test)
    --help, -h                  Show this help message

Examples:
    ./inject-pacs008-messages.sh                           # Load test: 100,000 messages
    ./inject-pacs008-messages.sh --count 50000             # Load test: 50,000 messages
    ./inject-pacs008-messages.sh --batch-size 500          # Load test with 500 msg batches
    ./inject-pacs008-messages.sh --samples-only            # Send only sample messages
    ./inject-pacs008-messages.sh --delay 1 --count 1000    # 1000 messages with 1s delay
    ./inject-pacs008-messages.sh --message pacs008-sample-1.xml  # Send specific message

Configuration:
    Broker URL: tcp://localhost:61616
    Username: artemis
    Password: artemis
    Queue: PACS008_QUEUE

Load Testing Features:
- Generates unique PACS.008 messages dynamically
- Sends messages in optimized batches for high throughput
- Provides performance metrics (messages/second)
- Supports different currencies and random amounts
- Uses persistent JMS connections for efficiency

The script will:
1. Create sample PACS.008 XML messages (if needed)
2. Generate dynamic load test messages with unique IDs
3. Send messages via optimized Java JMS client
4. Report throughput and success metrics

HELP_EOF
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Execute steps
    check_requirements
    check_broker_connection
    
    if [ "$load_test" = true ]; then
        create_sample_messages
        log_info "Starting load test mode with $message_count messages in batches of $batch_size"
        send_all_messages "$delay" "$message_count" "$batch_size"
    elif [ "$send_all" = true ]; then
        create_sample_messages
        log_info "Sending sample messages only"
        # For samples only, use original logic with higher delay
        local sample_delay=${delay:-2}
        send_all_messages "$sample_delay" 0 1  # 0 load test messages, batch size 1
    else
        if [ -n "$specific_message" ]; then
            create_sample_messages
            local message_path="$MESSAGES_DIR/$specific_message"
            if [ ! -f "$message_path" ]; then
                message_path="$specific_message"
            fi
            send_message "$message_path"
        else
            log_error "No message specified"
            exit 1
        fi
    fi
    
    echo
    log_success "PACS.008 message injection completed!"
    if [ "$load_test" = true ]; then
        log_info "Load test completed with $message_count messages"
        log_info "Check your Flow application performance and database for processed messages."
    else
        log_info "Check your Flow application logs to see the processed messages."
    fi
    log_info "Queue: $QUEUE_NAME"
    log_info "Message samples location: $MESSAGES_DIR"
    log_info "Monitor your application at: http://localhost:8080/h2-console"
    echo
}

# Execute main function with all arguments
main "$@"