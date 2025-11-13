<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pacs="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">

    <!-- Sample PACS.008 to simplified format transformation -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />

    <!-- Root template -->
    <xsl:template match="/pacs:Document">
        <TransformedPayment>
            <xsl:apply-templates select="pacs:FIToFICstmrCdtTrf" />
        </TransformedPayment>
    </xsl:template>

    <!-- Customer Credit Transfer template -->
    <xsl:template match="pacs:FIToFICstmrCdtTrf">
        <Header>
            <xsl:apply-templates select="pacs:GrpHdr" />
        </Header>
        <Transactions>
            <xsl:apply-templates select="pacs:CdtTrfTxInf" />
        </Transactions>
    </xsl:template>

    <!-- Group Header template -->
    <xsl:template match="pacs:GrpHdr">
        <MessageId>
            <xsl:value-of select="pacs:MsgId" />
        </MessageId>
        <CreationDateTime>
            <xsl:value-of select="pacs:CreDtTm" />
        </CreationDateTime>
        <NumberOfTransactions>
            <xsl:value-of select="pacs:NbOfTxs" />
        </NumberOfTransactions>
        <xsl:if
            test="pacs:TtlIntrBkSttlmAmt">
            <TotalAmount>
                <Amount>
                    <xsl:value-of select="pacs:TtlIntrBkSttlmAmt" />
                </Amount>
                <Currency>
                    <xsl:value-of select="pacs:TtlIntrBkSttlmAmt/@Ccy" />
                </Currency>
            </TotalAmount>
        </xsl:if>
    </xsl:template>

    <!-- Transaction Information template -->
    <xsl:template match="pacs:CdtTrfTxInf">
        <Transaction>
            <PaymentId>
                <xsl:value-of select="pacs:PmtId/pacs:InstrId" />
            </PaymentId>
            <EndToEndId>
                <xsl:value-of select="pacs:PmtId/pacs:EndToEndId" />
            </EndToEndId>
            <xsl:if test="pacs:IntrBkSttlmAmt">
                <SettlementAmount>
                    <Amount>
                        <xsl:value-of select="pacs:IntrBkSttlmAmt" />
                    </Amount>
                    <Currency>
                        <xsl:value-of select="pacs:IntrBkSttlmAmt/@Ccy" />
                    </Currency>
                </SettlementAmount>
            </xsl:if>
            <xsl:apply-templates select="pacs:Dbtr" />
            <xsl:apply-templates select="pacs:Cdtr" />
        </Transaction>
    </xsl:template>

    <!-- Debtor template -->
    <xsl:template match="pacs:Dbtr">
        <Debtor>
            <Name>
                <xsl:value-of select="pacs:Nm" />
            </Name>
            <xsl:if test="pacs:Id">
                <Id>
                    <xsl:value-of select="pacs:Id" />
                </Id>
            </xsl:if>
        </Debtor>
    </xsl:template>

    <!-- Creditor template -->
    <xsl:template match="pacs:Cdtr">
        <Creditor>
            <Name>
                <xsl:value-of select="pacs:Nm" />
            </Name>
            <xsl:if test="pacs:Id">
                <Id>
                    <xsl:value-of select="pacs:Id" />
                </Id>
            </xsl:if>
        </Creditor>
    </xsl:template>

</xsl:stylesheet>