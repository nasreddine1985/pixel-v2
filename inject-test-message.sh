#!/bin/bash

# ActiveMQ REST API test message injection script
ACTIVEMQ_URL="http://localhost:8161"
QUEUE_NAME="pacs008.input.queue"
USERNAME="admin"
PASSWORD="admin"

# Sample PACS008 XML message
MESSAGE_BODY='<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02">
  <FIToFICstmrCdtTrf>
    <GrpHdr>
      <MsgId>MSGID001</MsgId>
      <CreDtTm>2025-12-04T08:50:00.000Z</CreDtTm>
      <NbOfTxs>1</NbOfTxs>
      <SttlmInf>
        <SttlmMtd>INDA</SttlmMtd>
      </SttlmInf>
    </GrpHdr>
    <CdtTrfTxInf>
      <PmtId>
        <InstrId>INSTRID001</InstrId>
        <EndToEndId>ENDTOENDID001</EndToEndId>
        <TxId>TXID001</TxId>
      </PmtId>
      <IntrBkSttlmAmt Ccy="EUR">100.00</IntrBkSttlmAmt>
      <Dbtr>
        <Nm>Test Debtor</Nm>
      </Dbtr>
      <Cdtr>
        <Nm>Test Creditor</Nm>
      </Cdtr>
    </CdtTrfTxInf>
  </FIToFICstmrCdtTrf>
</Document>'

echo "🚀 Injecting test PACS008 message to ActiveMQ queue: $QUEUE_NAME"

# Send message using ActiveMQ REST API
curl -u $USERNAME:$PASSWORD \
     -X POST \
     -H "Content-Type: text/xml" \
     -d "$MESSAGE_BODY" \
     "$ACTIVEMQ_URL/api/message/$QUEUE_NAME?type=queue"

echo -e "\n✅ Message injection completed!"
echo "Check the application logs for message processing..."