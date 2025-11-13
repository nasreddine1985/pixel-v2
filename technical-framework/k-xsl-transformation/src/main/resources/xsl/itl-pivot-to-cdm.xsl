<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:itl="http://bnpparibas.com/G02">

    <!-- Sample ITL Pivot to CDM format transformation -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" />

    <!-- Root template -->
    <xsl:template match="/itl:Document">
        <CDMPayment>
            <xsl:apply-templates select="itl:ITLHdr" />
            <xsl:apply-templates select="itl:PmtPvt" />
        </CDMPayment>
    </xsl:template>

    <!-- ITL Header template -->
    <xsl:template match="itl:ITLHdr">
        <MessageHeader>
            <FlowCode>
                <xsl:value-of select="itl:ITLFlow/itl:ITLFlowCd" />
            </FlowCode>
            <FlowOccurrence>
                <xsl:value-of select="itl:ITLFlow/itl:ITLFlowOcc" />
            </FlowOccurrence>
            <xsl:if test="itl:ITLMsgNorm">
                <MessageNorm>
                    <xsl:value-of select="itl:ITLMsgNorm" />
                </MessageNorm>
            </xsl:if>
        </MessageHeader>
    </xsl:template>

    <!-- Payment Pivot template -->
    <xsl:template match="itl:PmtPvt">
        <PaymentInstructions>
            <xsl:apply-templates select="itl:InstrInf" />
            <Transactions>
                <xsl:apply-templates select="itl:TxInf" />
            </Transactions>
        </PaymentInstructions>
    </xsl:template>

    <!-- Instruction Information template -->
    <xsl:template match="itl:InstrInf">
        <InstructionInfo>
            <InstructionId>
                <xsl:value-of select="itl:InstrId" />
            </InstructionId>
            <EndToEndId>
                <xsl:value-of select="itl:EndToEndId" />
            </EndToEndId>
            <xsl:if test="itl:TxId">
                <TransactionId>
                    <xsl:value-of select="itl:TxId" />
                </TransactionId>
            </xsl:if>
        </InstructionInfo>
    </xsl:template>

    <!-- Transaction Information template -->
    <xsl:template match="itl:TxInf">
        <TransactionInfo>
            <TransactionId>
                <xsl:value-of select="itl:TxId" />
            </TransactionId>
            <xsl:if test="itl:IntrBkSttlmAmt">
                <SettlementAmount>
                    <Amount>
                        <xsl:value-of select="itl:IntrBkSttlmAmt" />
                    </Amount>
                    <Currency>
                        <xsl:value-of select="itl:IntrBkSttlmAmt/@Ccy" />
                    </Currency>
                </SettlementAmount>
            </xsl:if>
            <xsl:apply-templates select="itl:Dbtr" />
            <xsl:apply-templates select="itl:Cdtr" />
        </TransactionInfo>
    </xsl:template>

    <!-- Debtor template -->
    <xsl:template match="itl:Dbtr">
        <DebtorInfo>
            <xsl:if test="itl:Nm">
                <Name>
                    <xsl:value-of select="itl:Nm" />
                </Name>
            </xsl:if>
            <xsl:if test="itl:Id">
                <Id>
                    <xsl:value-of select="itl:Id" />
                </Id>
            </xsl:if>
        </DebtorInfo>
    </xsl:template>

    <!-- Creditor template -->
    <xsl:template match="itl:Cdtr">
        <CreditorInfo>
            <xsl:if test="itl:Nm">
                <Name>
                    <xsl:value-of select="itl:Nm" />
                </Name>
            </xsl:if>
            <xsl:if test="itl:Id">
                <Id>
                    <xsl:value-of select="itl:Id" />
                </Id>
            </xsl:if>
        </CreditorInfo>
    </xsl:template>

</xsl:stylesheet>