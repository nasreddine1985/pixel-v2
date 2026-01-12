#!/bin/bash

# ActiveMQ REST API test message injection script
ACTIVEMQ_URL="http://localhost:8161"
QUEUE_NAME="pacs008.input.queue"
USERNAME="admin"
PASSWORD="admin"

# Sample PACS008 XML message
MESSAGE_BODY='<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="http://www.six-interbank-clearing.com/de/pacs.008.001.08.ch.02">
	<FIToFICstmrCdtTrf>
		<GrpHdr>
			<MsgId>1717480887/1XXXX</MsgId>
			<CreDtTm>2024-11-22T15:43:01+01:00</CreDtTm>
			<NbOfTxs>1</NbOfTxs>
			<TtlIntrBkSttlmAmt Ccy="CHF">1003</TtlIntrBkSttlmAmt>
			<SttlmInf>
				<SttlmMtd>CLRG</SttlmMtd>
				<ClrSys>
					<Cd>SIC</Cd>
				</ClrSys>
			</SttlmInf>
		</GrpHdr>
		<CdtTrfTxInf>
			<PmtId>
				<EndToEndId>cd53dafa016b451fa59574cc4ee65bd8</EndToEndId>
				<TxId>1717480887/1XXXX</TxId>
				<UETR>c8d513e9-d3e1-46ff-bc68-56fe389404d8</UETR>
			</PmtId>
			<PmtTpInf>
				<LclInstrm>
					<Prtry>CSTPMT</Prtry>
				</LclInstrm>
			</PmtTpInf>
			<IntrBkSttlmAmt Ccy="CHF">1260</IntrBkSttlmAmt>
			<IntrBkSttlmDt>2024-11-22</IntrBkSttlmDt>
			<SttlmTmIndctn>
				<CdtDtTm>2024-11-22T15:43:15</CdtDtTm>
			</SttlmTmIndctn>
			<ChrgBr>SHAR</ChrgBr>
			<InstgAgt>
				<FinInstnId>
					<ClrSysMmbId>
						<ClrSysId>
							<Cd>CHSIC</Cd>
						</ClrSysId>
						<MmbId>007781</MmbId>
					</ClrSysMmbId>
				</FinInstnId>
			</InstgAgt>
			<InstdAgt>
				<FinInstnId>
					<ClrSysMmbId>
						<ClrSysId>
							<Cd>CHSIC</Cd>
						</ClrSysId>
						<MmbId>086866</MmbId>
					</ClrSysMmbId>
				</FinInstnId>
			</InstdAgt>
			<Dbtr>
				<Nm>Verkehrsbetriebe Luzern AG</Nm>
				<PstlAdr>
					<Ctry>CH</Ctry>
					<AdrLine>Tribschenstrasse 65</AdrLine>
					<AdrLine>6002 Luzern</AdrLine>
				</PstlAdr>
			</Dbtr>
			<DbtrAcct>
				<Id>
					<IBAN>CH3300778010055393210</IBAN>
				</Id>
			</DbtrAcct>
			<DbtrAgt>
				<FinInstnId>
					<ClrSysMmbId>
						<ClrSysId>
							<Cd>CHSIC</Cd>
						</ClrSysId>
						<MmbId>007781</MmbId>
					</ClrSysMmbId>
					<Nm>Luzerner Kantonalbank AG</Nm>
					<PstlAdr>
						<Ctry>CH</Ctry>
						<AdrLine>Pilatusstrasse 12</AdrLine>
						<AdrLine>6003 Luzern</AdrLine>
					</PstlAdr>
				</FinInstnId>
			</DbtrAgt>
			<CdtrAgt>
				<FinInstnId>
					<ClrSysMmbId>
						<ClrSysId>
							<Cd>CHSIC</Cd>
						</ClrSysId>
						<MmbId>086866</MmbId>
					</ClrSysMmbId>
				</FinInstnId>
			</CdtrAgt>
			<Cdtr>
				<Nm>Daimler Buses Schweiz AG</Nm>
				<PstlAdr>
					<StrtNm>Wieshofstrasse</StrtNm>
					<PstCd>8404</PstCd>
					<TwnNm>Winterthur</TwnNm>
					<Ctry>CH</Ctry>
				</PstlAdr>
			</Cdtr>
			<CdtrAcct>
				<Id>
					<IBAN>CH9830154001133992002</IBAN>
				</Id>
			</CdtrAcct>
			<RmtInf>
				<Strd>
					<CdtrRefInf>
						<Tp>
							<CdOrPrtry>
								<Prtry>QRR</Prtry>
							</CdOrPrtry>
						</Tp>
						<Ref>000000000000000069000002809</Ref>
					</CdtrRefInf>
					<AddtlRmtInf>28.10.2024 38739388</AddtlRmtInf>
				</Strd>
			</RmtInf>
		</CdtTrfTxInf>
	</FIToFICstmrCdtTrf>
</Document>'

echo "Injecting test PACS008 message to ActiveMQ queue: $QUEUE_NAME"

# Send message using ActiveMQ REST API
curl -u $USERNAME:$PASSWORD \
     -X POST \
     -H "Content-Type: text/xml" \
     -d "$MESSAGE_BODY" \
     "$ACTIVEMQ_URL/api/message/$QUEUE_NAME?type=queue"

echo -e "\n Message injection completed!"
echo "Check the application logs for message processing..."