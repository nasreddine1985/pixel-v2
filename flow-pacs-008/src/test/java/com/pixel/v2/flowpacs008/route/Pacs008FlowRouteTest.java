package com.pixel.v2.flowpacs008.route;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Pacs008FlowRouteTest extends CamelTestSupport {

    @EndpointInject("mock:enrichPacs008Message")
    private MockEndpoint mockEnrichEndpoint;

    @EndpointInject("mock:transformPacs008Message")
    private MockEndpoint mockTransformEndpoint;

    @EndpointInject("mock:completePacs008Processing")
    private MockEndpoint mockCompleteEndpoint;

    @EndpointInject("mock:publishProcessedMessage")
    private MockEndpoint mockPublishEndpoint;

    @EndpointInject("mock:handleEmptyTransformation")
    private MockEndpoint mockEmptyTransformationEndpoint;

    @EndpointInject("mock:jms:output")
    private MockEndpoint mockJmsOutputEndpoint;

    @EndpointInject("mock:jms:error")
    private MockEndpoint mockJmsErrorEndpoint;

    private static final String SAMPLE_PACS008_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.002\">" +
        "<FIToFICstmrCdtTrf>" +
        "<GrpHdr>" +
        "<MsgId>TEST123</MsgId>" +
        "<CreDtTm>2025-10-17T10:00:00</CreDtTm>" +
        "<NbOfTxs>1</NbOfTxs>" +
        "</GrpHdr>" +
        "</FIToFICstmrCdtTrf>" +
        "</Document>";

    private static final String CDM_JSON = "{" +
        "\"messageId\": \"TEST123\"," +
        "\"creationDateTime\": \"2025-10-17T10:00:00\"," +
        "\"numberOfTransactions\": 1," +
        "\"enrichmentStatus\": \"ENRICHED\"" +
        "}";

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = new DefaultCamelContext();
        
        // Add properties for placeholders used in the route
        context.getPropertiesComponent().setInitialProperties(java.util.Map.of(
            "pacs008.mq.queue", "TEST.PACS008.IN",
            "pacs008.mq.connectionFactoryRef", "testConnectionFactory",
            "pacs008.enrichment.serviceUrl", "http://localhost:9999/api/enrich",
            "pacs008.transformation.xsltResource", "classpath:xslt/test.xslt",
            "pacs008.output.queue", "TEST.PACS008.OUT",
            "pacs008.error.queue", "TEST.PACS008.ERROR"
        ));
        
        return context;
    }

    @Override
    protected boolean isUseAdviceWith() {
        return true;
    }

    @Override
    public void setupResources() throws Exception {
        // Add the route to test
        context.addRoutes(new Pacs008FlowRoute());
        
        // Mock external endpoints to avoid dependencies on kamelets and JMS
        AdviceWith.adviceWith(context, "pacs008-receipt-flow", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("kamelet:*");
            routeBuilder.mockEndpointsAndSkip("direct:enrichPacs008Message");
        });

        AdviceWith.adviceWith(context, "pacs008-enrichment-flow", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("kamelet:*");
            routeBuilder.mockEndpointsAndSkip("direct:transformPacs008Message");
        });

        AdviceWith.adviceWith(context, "pacs008-transformation-flow", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("kamelet:*");
            routeBuilder.mockEndpointsAndSkip("direct:completePacs008Processing");
        });

        AdviceWith.adviceWith(context, "pacs008-completion-flow", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("direct:publishProcessedMessage");
            routeBuilder.mockEndpointsAndSkip("direct:handleEmptyTransformation");
        });

        AdviceWith.adviceWith(context, "pacs008-output-publisher", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("jms:*");
        });

        AdviceWith.adviceWith(context, "pacs008-empty-transformation-handler", routeBuilder -> {
            routeBuilder.mockEndpointsAndSkip("jms:*");
        });
    }

    @Test
    void testReceiptPacs008MessageFlow() throws Exception {
        // Arrange
        mockEnrichEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:receiptPacs008Message", "trigger");

        // Assert
        mockEnrichEndpoint.assertIsSatisfied();
    }

    @Test
    void testEnrichPacs008MessageFlow() throws Exception {
        // Arrange
        mockTransformEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:enrichPacs008Message", SAMPLE_PACS008_XML);

        // Assert
        mockTransformEndpoint.assertIsSatisfied();
    }

    @Test
    void testTransformPacs008MessageFlow() throws Exception {
        // Arrange
        mockCompleteEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:transformPacs008Message", SAMPLE_PACS008_XML);

        // Assert
        mockCompleteEndpoint.assertIsSatisfied();
    }

    @Test
    void testCompletePacs008ProcessingWithValidBody() throws Exception {
        // Arrange
        mockPublishEndpoint.expectedMessageCount(1);
        mockEmptyTransformationEndpoint.expectedMessageCount(0);

        // Act
        template.sendBody("direct:completePacs008Processing", CDM_JSON);

        // Assert
        mockPublishEndpoint.assertIsSatisfied();
        mockEmptyTransformationEndpoint.assertIsSatisfied();
    }

    @Test
    void testCompletePacs008ProcessingWithEmptyBody() throws Exception {
        // Arrange
        mockPublishEndpoint.expectedMessageCount(0);
        mockEmptyTransformationEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:completePacs008Processing", "");

        // Assert
        mockPublishEndpoint.assertIsSatisfied();
        mockEmptyTransformationEndpoint.assertIsSatisfied();
    }

    @Test
    void testCompletePacs008ProcessingWithNullBody() throws Exception {
        // Arrange
        mockPublishEndpoint.expectedMessageCount(0);
        mockEmptyTransformationEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:completePacs008Processing", null);

        // Assert
        mockPublishEndpoint.assertIsSatisfied();
        mockEmptyTransformationEndpoint.assertIsSatisfied();
    }

    @Test
    void testPublishProcessedMessageFlow() throws Exception {
        // Arrange
        mockJmsOutputEndpoint.expectedMessageCount(1);
        mockJmsOutputEndpoint.expectedBodiesReceived(CDM_JSON);

        // Act
        template.sendBody("direct:publishProcessedMessage", CDM_JSON);

        // Assert
        mockJmsOutputEndpoint.assertIsSatisfied();
    }

    @Test
    void testHandleEmptyTransformationFlow() throws Exception {
        // Arrange
        mockJmsErrorEndpoint.expectedMessageCount(1);
        String expectedErrorXml = "<error><type>EMPTY_TRANSFORMATION</type><message>Transformation resulted in empty body</message></error>";
        mockJmsErrorEndpoint.expectedBodiesReceived(expectedErrorXml);

        // Act
        template.sendBody("direct:handleEmptyTransformation", "");

        // Assert
        mockJmsErrorEndpoint.assertIsSatisfied();
        
        // Verify headers are set correctly
        String errorType = mockJmsErrorEndpoint.getExchanges().get(0).getIn().getHeader("ErrorType", String.class);
        assertEquals("EMPTY_TRANSFORMATION", errorType);
        
        String originalBody = mockJmsErrorEndpoint.getExchanges().get(0).getIn().getHeader("OriginalBody", String.class);
        assertEquals("", originalBody);
    }

    @Test
    void testManualTriggerFlow() throws Exception {
        // Arrange
        mockEnrichEndpoint.expectedMessageCount(1);

        // Act
        template.sendBody("direct:triggerPacs008Flow", "manual-trigger");

        // Assert
        mockEnrichEndpoint.assertIsSatisfied();
    }

    @Test
    void testRouteConfiguration() {
        // Test that all routes are properly configured
        assertNotNull(context.getRoute("pacs008-flow-orchestrator"));
        assertNotNull(context.getRoute("pacs008-receipt-flow"));
        assertNotNull(context.getRoute("pacs008-enrichment-flow"));
        assertNotNull(context.getRoute("pacs008-transformation-flow"));
        assertNotNull(context.getRoute("pacs008-completion-flow"));
        assertNotNull(context.getRoute("pacs008-output-publisher"));
        assertNotNull(context.getRoute("pacs008-empty-transformation-handler"));
        assertNotNull(context.getRoute("pacs008-health-check"));
        assertNotNull(context.getRoute("pacs008-manual-trigger"));
    }

    @Test
    void testErrorHandlerConfiguration() {
        // Verify that error handler is configured
        assertFalse(context.getRoutes().isEmpty());
        
        // Test that the route can handle basic message processing
        String result = template.requestBody("direct:completePacs008Processing", CDM_JSON, String.class);
        assertNotNull(result);
    }

    @Test
    void testHealthCheckRoute() {
        // Verify the health check route exists and is configured
        assertNotNull(context.getRoute("pacs008-health-check"));
        
        // Verify the route is started
        assertTrue(context.getRouteController().getRouteStatus("pacs008-health-check").isStarted());
    }

    @Test
    void testChoiceLogicInCompletionFlow() throws Exception {
        // Test the choice logic that routes to different endpoints based on body content
        
        // Test with valid body - should go to publish endpoint
        mockPublishEndpoint.reset();
        mockEmptyTransformationEndpoint.reset();
        
        mockPublishEndpoint.expectedMessageCount(1);
        mockEmptyTransformationEndpoint.expectedMessageCount(0);
        
        template.sendBody("direct:completePacs008Processing", "Valid Content");
        
        mockPublishEndpoint.assertIsSatisfied();
        mockEmptyTransformationEndpoint.assertIsSatisfied();
        
        // Test with empty body - should go to empty transformation handler
        mockPublishEndpoint.reset();
        mockEmptyTransformationEndpoint.reset();
        
        mockPublishEndpoint.expectedMessageCount(0);
        mockEmptyTransformationEndpoint.expectedMessageCount(1);
        
        template.sendBody("direct:completePacs008Processing", "");
        
        mockPublishEndpoint.assertIsSatisfied();
        mockEmptyTransformationEndpoint.assertIsSatisfied();
    }

    @Test
    void testMessageHeaders() throws Exception {
        // Test that proper headers are set in the output publisher
        mockJmsOutputEndpoint.expectedHeaderReceived("CamelJmsDestinationName", "TEST.PACS008.OUT");
        
        template.sendBody("direct:publishProcessedMessage", CDM_JSON);
        
        mockJmsOutputEndpoint.assertIsSatisfied();
    }

    @Test
    void testErrorMessageStructure() throws Exception {
        // Test the structure of error messages sent to error queue
        mockJmsErrorEndpoint.expectedMessageCount(1);
        
        template.sendBody("direct:handleEmptyTransformation", "original content");
        
        mockJmsErrorEndpoint.assertIsSatisfied();
        
        // Verify the error XML structure
        String errorBody = mockJmsErrorEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        assertTrue(errorBody.contains("<type>EMPTY_TRANSFORMATION</type>"));
        assertTrue(errorBody.contains("<message>Transformation resulted in empty body</message>"));
        
        // Verify headers
        assertEquals("EMPTY_TRANSFORMATION", 
            mockJmsErrorEndpoint.getExchanges().get(0).getIn().getHeader("ErrorType"));
        assertEquals("original content", 
            mockJmsErrorEndpoint.getExchanges().get(0).getIn().getHeader("OriginalBody"));
    }
}
