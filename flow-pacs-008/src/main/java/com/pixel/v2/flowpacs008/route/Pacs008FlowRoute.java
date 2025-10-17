package com.pixel.v2.flowpacs008.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Pacs008FlowRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Global error handler
        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .logStackTrace(true)
            .onRedelivery(exchange -> {
                log.warn("Redelivering message: attempt {}", exchange.getIn().getHeader("CamelRedeliveryCounter"));
            }));

        // Main PACS.008 processing flow
        from("timer://pacs008-flow-trigger?period=30000&repeatCount=0")
            .routeId("pacs008-flow-orchestrator")
            .log("Starting PACS.008 flow orchestration")
            .to("direct:receiptPacs008Message");

        // Step 1: Receipt PACS.008 message using k-mq-receipt kamelet
        from("direct:receiptPacs008Message")
            .routeId("pacs008-receipt-flow")
            .log("Step 1: Receiving PACS.008 message from MQ")
            .to("kamelet:k-mq-receipt?"
                + "destination={{pacs008.mq.queue}}"
                + "&jmsConnectionFactoryRef={{pacs008.mq.connectionFactoryRef}}")
            .log("PACS.008 message received: ${body}")
            .to("direct:enrichPacs008Message");

        // Step 2: Enrich XML using k-ref-loader kamelet
        from("direct:enrichPacs008Message")
            .routeId("pacs008-enrichment-flow")
            .log("Step 2: Enriching PACS.008 XML message")
            .to("kamelet:k-ref-loader?serviceUrl={{pacs008.enrichment.serviceUrl}}")
            .log("PACS.008 message enriched: ${body}")
            .to("direct:transformPacs008Message");

        // Step 3: Transform XML to CDM using k-pacs-008-to-cdm kamelet
        from("direct:transformPacs008Message")
            .routeId("pacs008-transformation-flow")
            .log("Step 3: Transforming PACS.008 XML to CDM format")
            .to("kamelet:k-pacs-008-to-cdm?xsltResource={{pacs008.transformation.xsltResource}}")
            .log("PACS.008 message transformed to CDM: ${body}")
            .to("direct:completePacs008Processing");

        // Step 4: Complete processing (optional additional steps)
        from("direct:completePacs008Processing")
            .routeId("pacs008-completion-flow")
            .log("Step 4: Completing PACS.008 message processing")
            .choice()
                .when(simple("${body} != null && ${body} != ''"))
                    .log("PACS.008 flow completed successfully")
                    .to("direct:publishProcessedMessage")
                .otherwise()
                    .log("PACS.008 transformation resulted in empty body")
                    .to("direct:handleEmptyTransformation");

        // Publish processed message to output queue
        from("direct:publishProcessedMessage")
            .routeId("pacs008-output-publisher")
            .log("Publishing processed CDM message to output queue")
            .setHeader("CamelJmsDestinationName", constant("{{pacs008.output.queue}}"))
            .to("jms:queue:{{pacs008.output.queue}}?connectionFactory=#{{pacs008.mq.connectionFactoryRef}}")
            .log("CDM message published successfully");

        // Handle empty transformation results
        from("direct:handleEmptyTransformation")
            .routeId("pacs008-empty-transformation-handler")
            .log("Handling empty transformation result")
            .setHeader("ErrorType", constant("EMPTY_TRANSFORMATION"))
            .setHeader("OriginalBody", simple("${body}"))
            .setBody(constant("<error><type>EMPTY_TRANSFORMATION</type><message>Transformation resulted in empty body</message></error>"))
            .to("jms:queue:{{pacs008.error.queue}}?connectionFactory=#{{pacs008.mq.connectionFactoryRef}}")
            .log("Error message sent to error queue");

        // Health check endpoint
        from("timer://health-check?period=60000")
            .routeId("pacs008-health-check")
            .log("PACS.008 Flow Service - Health Check OK")
            .setBody(constant("PACS.008 Flow Service is healthy"));

        // Manual flow trigger endpoint (for testing/debugging)
        from("direct:triggerPacs008Flow")
            .routeId("pacs008-manual-trigger")
            .log("Manual trigger for PACS.008 flow")
            .to("direct:receiptPacs008Message");

    }
}