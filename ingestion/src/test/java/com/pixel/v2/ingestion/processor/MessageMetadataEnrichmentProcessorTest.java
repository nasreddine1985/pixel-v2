package com.pixel.v2.ingestion.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageMetadataEnrichmentProcessorTest {

    private MessageMetadataEnrichmentProcessor processor;
    private Exchange exchange;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        processor = new MessageMetadataEnrichmentProcessor();
        exchange = new DefaultExchange(new DefaultCamelContext());
        objectMapper = new ObjectMapper();
    }

    @Test
    void process_ShouldEnrichMessageWithMetadata_WhenValidExchange() throws Exception {
        // Given
        String originalPayload = "{\"messageType\":\"pacs.008\",\"content\":\"test\"}";
        exchange.getIn().setBody(originalPayload);

        // Set headers
        exchange.getIn().setHeader("ReceiptChannel", "HTTP");
        exchange.getIn().setHeader("MessageSource", "HTTP_API");
        exchange.getIn().setHeader("MessageId", "MSG-001");
        exchange.getIn().setHeader("ExpectedMessageType", "pacs.008");
        exchange.getIn().setHeader("IsValid", true);
        exchange.getIn().setHeader("IsDuplicate", false);
        exchange.getIn().setHeader("CanProcess", true);
        exchange.getIn().setHeader("MessagePersisted", true);
        exchange.getIn().setHeader("EnrichedDataPersisted", true);

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        assertNotNull(enrichedMessage);

        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);

        // Verify structure
        assertTrue(enrichedNode.has("metadata"));
        assertTrue(enrichedNode.has("payload"));

        // Verify metadata content
        JsonNode metadata = enrichedNode.get("metadata");
        assertEquals("HTTP", metadata.get("receiptChannel").asText());
        assertEquals("HTTP_API", metadata.get("messageSource").asText());
        assertEquals("MSG-001", metadata.get("messageId").asText());
        assertEquals("pacs.008", metadata.get("messageType").asText());
        assertTrue(metadata.get("validationPassed").asBoolean());
        assertTrue(metadata.get("duplicateCheck").asBoolean());
        assertTrue(metadata.get("canProcess").asBoolean());
        assertTrue(metadata.get("messagePersisted").asBoolean());
        assertTrue(metadata.get("enrichedDataPersisted").asBoolean());

        // Verify payload is preserved
        assertEquals(originalPayload, enrichedNode.get("payload").asText());

        // Verify processing headers
        assertEquals("application/json", exchange.getIn().getHeader("ContentType"));
        assertTrue((Boolean) exchange.getIn().getHeader("EnrichedForProcessing"));
    }

    @Test
    void process_ShouldHandleMissingHeaders_WhenHeadersAreNull() throws Exception {
        // Given
        String originalPayload = "{\"test\":\"data\"}";
        exchange.getIn().setBody(originalPayload);

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        JsonNode metadata = enrichedNode.get("metadata");

        // Verify empty strings for missing headers
        assertEquals("", metadata.get("receiptChannel").asText());
        assertEquals("", metadata.get("messageSource").asText());
        assertEquals("", metadata.get("messageId").asText());
        assertEquals("", metadata.get("messageType").asText());

        // Verify boolean defaults
        assertFalse(metadata.get("validationPassed").asBoolean());
        assertTrue(metadata.get("duplicateCheck").asBoolean()); // duplicateCheck = !IsDuplicate, so
                                                                // when IsDuplicate is
                                                                // false/missing, duplicateCheck is
                                                                // true
        assertFalse(metadata.get("canProcess").asBoolean());
        assertFalse(metadata.get("messagePersisted").asBoolean());
        assertFalse(metadata.get("enrichedDataPersisted").asBoolean());
    }

    @Test
    void process_ShouldHandleStringBooleanHeaders_WhenHeadersAreStringValues() throws Exception {
        // Given
        String originalPayload = "{\"test\":\"data\"}";
        exchange.getIn().setBody(originalPayload);

        exchange.getIn().setHeader("IsValid", "true");
        exchange.getIn().setHeader("IsDuplicate", "false");
        exchange.getIn().setHeader("CanProcess", "true");

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        JsonNode metadata = enrichedNode.get("metadata");

        assertTrue(metadata.get("validationPassed").asBoolean());
        assertTrue(metadata.get("duplicateCheck").asBoolean()); // Should be inverted from
                                                                // IsDuplicate
        assertTrue(metadata.get("canProcess").asBoolean());
    }

    @Test
    void process_ShouldIncludeTimestamp_WhenProcessing() throws Exception {
        // Given
        String originalPayload = "{\"test\":\"data\"}";
        exchange.getIn().setBody(originalPayload);

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        JsonNode metadata = enrichedNode.get("metadata");

        assertTrue(metadata.has("metadataEnrichmentTimestamp"));
        String timestamp = metadata.get("metadataEnrichmentTimestamp").asText();

        // Verify timestamp format (should be parseable)
        assertDoesNotThrow(() -> {
            LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        });
    }

    @Test
    void process_ShouldIncludeRoutingInfo_WhenProcessing() throws Exception {
        // Given
        String originalPayload = "{\"test\":\"data\"}";
        exchange.getIn().setBody(originalPayload);
        exchange.getIn().setHeader("ReceiptChannel", "MQ");

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        JsonNode metadata = enrichedNode.get("metadata");

        assertTrue(metadata.has("routingInfo"));
        JsonNode routingInfo = metadata.get("routingInfo");

        assertEquals("MQ", routingInfo.get("sourceChannel").asText());
        assertEquals("direct:kafka-message-processing", routingInfo.get("targetEndpoint").asText());
        assertTrue(routingInfo.get("bypassedKafka").asBoolean());
        assertTrue(routingInfo.get("realTimeProcessing").asBoolean());
    }

    @Test
    void process_ShouldHandleNullBody_WhenBodyIsNull() throws Exception {
        // Given
        exchange.getIn().setBody(null);

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        assertNotNull(enrichedMessage);

        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        assertTrue(enrichedNode.has("metadata"));
        assertTrue(enrichedNode.has("payload"));
        assertEquals("null", enrichedNode.get("payload").asText());
    }

    @Test
    void process_ShouldIncludeExchangeId_WhenProcessing() throws Exception {
        // Given
        String originalPayload = "{\"test\":\"data\"}";
        exchange.getIn().setBody(originalPayload);

        // When
        processor.process(exchange);

        // Then
        String enrichedMessage = exchange.getIn().getBody(String.class);
        JsonNode enrichedNode = objectMapper.readTree(enrichedMessage);
        JsonNode metadata = enrichedNode.get("metadata");

        assertTrue(metadata.has("exchangeId"));
        assertEquals(exchange.getExchangeId(), metadata.get("exchangeId").asText());
    }
}
