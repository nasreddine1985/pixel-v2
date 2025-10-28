#!/bin/bash

# Quick PACS.008 Message Injector
# Simple script for testing message injection using curl

set -e

# Configuration
BROKER_URL="http://localhost:8161"
USERNAME="artemis"
PASSWORD="artemis"
QUEUE_NAME="PACS008_QUEUE"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Simple test message
send_test_message() {
    local message_id=${1:-"TEST-$(date +%s)"}
    
    log "Sending simple test message with ID: $message_id"
    
    local test_message='<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>'$message_id'</MsgId>
            <CreDtTm>'$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")'</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="EUR">100.00</TtlIntrBkSttlmAmt>
            <IntrBkSttlmDt>'$(date -u +"%Y-%m-%d")'</IntrBkSttlmDt>
            <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
            <InstgAgt><FinInstnId><BICFI>TESTBANK</BICFI></FinInstnId></InstgAgt>
            <InstdAgt><FinInstnId><BICFI>TESTBANK</BICFI></FinInstnId></InstdAgt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>QUICK-TEST-'$(date +%s)'</InstrId>
                <EndToEndId>E2E-'$(date +%s)'</EndToEndId>
                <TxId>TXN-'$(date +%s)'</TxId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="EUR">100.00</IntrBkSttlmAmt>
            <ChrgBr>SLEV</ChrgBr>
            <Dbtr><Nm>Quick Test Sender</Nm></Dbtr>
            <DbtrAcct><Id><IBAN>DE89370400440532013000</IBAN></Id></DbtrAcct>
            <DbtrAgt><FinInstnId><BICFI>TESTBANK</BICFI></FinInstnId></DbtrAgt>
            <CdtrAgt><FinInstnId><BICFI>TESTBANK</BICFI></FinInstnId></CdtrAgt>
            <Cdtr><Nm>Quick Test Receiver</Nm></Cdtr>
            <CdtrAcct><Id><IBAN>FR1420041010050500013M02606</IBAN></Id></CdtrAcct>
            <RmtInf><Ustrd>Quick test payment</Ustrd></RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>'

    # Try using ActiveMQ web console API
    local response=$(curl -s -w "%{http_code}" -o /tmp/artemis_response.txt \
        -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -u "$USERNAME:$PASSWORD" \
        -d "destination=queue%3A%2F%2F$QUEUE_NAME&messageType=text&messageText=$(echo "$test_message" | jq -sRr @uri)" \
        "$BROKER_URL/console/send.jsp" 2>/dev/null)
    
    local http_code="${response: -3}"
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 302 ]; then
        log "✅ Test message sent successfully! (HTTP: $http_code)"
        log "   Message ID: $message_id"
        log "   Queue: $QUEUE_NAME"
        return 0
    else
        error "❌ Failed to send message (HTTP: $http_code)"
        warn "Response saved to /tmp/artemis_response.txt"
        
        # Try alternative method with raw text
        log "Trying alternative method..."
        
        local simple_response=$(curl -s -w "%{http_code}" \
            -X POST \
            -H "Content-Type: text/plain" \
            -u "$USERNAME:$PASSWORD" \
            -d "$test_message" \
            "$BROKER_URL/api/message/$QUEUE_NAME" 2>/dev/null || echo "000")
            
        local simple_code="${simple_response: -3}"
        
        if [ "$simple_code" -eq 200 ] || [ "$simple_code" -eq 201 ]; then
            log "✅ Test message sent via alternative method! (HTTP: $simple_code)"
            return 0
        else
            error "❌ All methods failed. Check if ActiveMQ Artemis is running on localhost:8161"
            return 1
        fi
    fi
}

# Check broker status
check_broker() {
    log "Checking ActiveMQ Artemis status..."
    
    local response=$(curl -s -w "%{http_code}" -o /dev/null \
        -u "$USERNAME:$PASSWORD" \
        "$BROKER_URL/console/" 2>/dev/null || echo "000")
    
    if [ "$response" -eq 200 ] || [ "$response" -eq 302 ]; then
        log "✅ ActiveMQ Artemis is running and accessible"
        return 0
    else
        error "❌ Cannot connect to ActiveMQ Artemis web console"
        error "   Expected: $BROKER_URL/console/"
        error "   Received HTTP: $response"
        warn "Make sure ActiveMQ Artemis is started and web console is enabled"
        return 1
    fi
}

# Send multiple test messages
send_multiple() {
    local count=${1:-3}
    local delay=${2:-2}
    
    log "Sending $count test messages with ${delay}s delay..."
    
    for i in $(seq 1 $count); do
        local msg_id="BATCH-$(date +%s)-$(printf "%03d" $i)"
        if send_test_message "$msg_id"; then
            log "Message $i/$count sent"
        else
            error "Failed to send message $i/$count"
        fi
        
        if [ $i -lt $count ]; then
            sleep $delay
        fi
    done
    
    log "Batch sending completed!"
}

# Main function
main() {
    echo
    log "Quick PACS.008 Message Injector"
    log "==============================="
    
    case "${1:-single}" in
        "check")
            check_broker
            ;;
        "single")
            if check_broker; then
                send_test_message
            fi
            ;;
        "multiple"|"batch")
            if check_broker; then
                send_multiple "${2:-3}" "${3:-2}"
            fi
            ;;
        "help"|"-h"|"--help")
            cat << 'HELP'
Quick PACS.008 Message Injector

Usage:
    ./quick-inject.sh [command] [options]

Commands:
    single                 Send one test message (default)
    multiple [count] [delay]  Send multiple messages
    batch [count] [delay]     Same as multiple  
    check                  Check broker connection only
    help                   Show this help

Examples:
    ./quick-inject.sh                    # Send one message
    ./quick-inject.sh multiple 5 3      # Send 5 messages with 3s delay
    ./quick-inject.sh check              # Check broker only

Configuration:
    Broker: http://localhost:8161
    Username: artemis
    Password: artemis  
    Queue: PACS008_QUEUE
HELP
            ;;
        *)
            error "Unknown command: $1"
            log "Use 'help' for usage information"
            exit 1
            ;;
    esac
    
    echo
}

main "$@"