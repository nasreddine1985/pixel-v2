package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * PACS008 Payment Processing Route
 * 
 * Main route for processing PACS.008 payment messages using k-mq-message-receiver kamelet
 */
@Component
public class Pacs008ProcessingRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Error handler configuration
        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(3)
            .redeliveryDelay(5000)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN));

        // Main PACS008 processing route using kamelet
        from("kamelet:k-mq-message-receiver?" +
             "queueName=pacs008.input.queue&" +
             "brokerUrl=tcp://pixel-v2-activemq:61616&" +
             "username=admin&" +
             "password=admin")
            .routeId("pacs008-processing-flow")
            .log("Received PACS008 message: ${body}")
            .setHeader("MessageType", constant("PACS008"))
            .setHeader("flowCode", constant("PACS008"))
            .setHeader("ProcessingTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
            
            
            // Process the message
            .to("direct:process-pacs008-message");

        // PACS008 processing logic
        from("direct:process-pacs008-message")
            .routeId("pacs008-message-processing")
            .log("Processing PACS008 message with ID: ${header.JMSMessageID}")
            
            // Enrich message with metadata
            .setHeader("RouteName", constant("PACS008-Processing"))
            .setHeader("ProcessingNode", simple("${sys.HOSTNAME}"))
            
            // // Transform and validate
            // .choice()
            //     .when(body().contains("<?xml"))
            //         .log("XML PACS008 message detected")
            //         .setHeader("MessageFormat", constant("XML"))
            //     .when(body().contains("{"))
            //         .log("JSON PACS008 message detected")
            //         .setHeader("MessageFormat", constant("JSON"))
            //     .otherwise()
            //         .log("Unknown message format: ${body}")
            //         .setHeader("MessageFormat", constant("UNKNOWN"))
            //         .to("direct:error-handling")
            //         .stop()
            // .end()
            
            // Send to Kafka for further processing  
            .to("kafka:pacs008-processed?" +
                "brokers=pixel-v2-kafka:9092&" +
                "keySerializer=org.apache.kafka.common.serialization.StringSerializer&" +
                "valueSerializer=org.apache.kafka.common.serialization.StringSerializer&" +
                "key=${header.JMSMessageID}")
            
            .log("PACS008 message successfully processed and sent to Kafka");

        // Error handling route
        from("direct:error-handling")
            .routeId("pacs008-error-handler")
            .log("Error processing message: ${exception.message}")
            .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
            .setHeader("ErrorReason", simple("${exception.message}"))
            
            // Send to error queue
            .to("jms:queue:pacs008.error.queue")
            .log("Message sent to error queue for manual review");
    }
}