package com.pixel.v2.flow.route;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class Pacs008Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // PACS.008 Message Processing Route - with message aggregation for batch processing
        from("jms:queue:PACS008_QUEUE").routeId("pacs008-processing-route")
                .log("Received PACS.008 message: JMSMessageID=${header.JMSMessageID}, "
                        + "JMSCorrelationID=${header.JMSCorrelationID}, "
                        + "MessageSize=${body.length()}")

                // Add route metadata
                .setHeader("ProcessingRoute", constant("PACS008"))
                .setHeader("MessageType", constant("pacs.008.001.08"))
                .setHeader("ProcessedTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))

                // Validate message is not empty
                .choice().when(simple("${body} == null || ${body} == ''"))
                .log("ERROR: Received empty PACS.008 message with JMSMessageID=${header.JMSMessageID}")
                .to("direct:error-handler").otherwise()
                .log("Processing valid PACS.008 message with ${body.length()} characters")

                // Aggregate messages for batch processing
                .aggregate(constant("PACS008_BATCH")).completionSize(1000) // Complete when 1000
                                                                           // messages collected
                .completionTimeout(1000) // Complete after 1 second timeout
                .aggregationStrategy(new MessageBatchAggregationStrategy())
                .to("direct:persist-batch").end();

        // Batch persistence route - handles aggregated messages
        from("direct:persist-batch").routeId("persist-batch-route")
                .log("Persisting batch of ${header.CamelAggregatedSize} messages to database")
                .to("bean:messageBatchPersistenceProcessor")
                .log("Batch persisted successfully: ${header.CamelAggregatedSize} messages");

        // Database persistence route (for individual messages if needed)
        from("direct:persist-message").routeId("persist-message-route")
                .log("Persisting message to database with JMSMessageID: ${header.JMSMessageID}")
                .to("bean:messagePersistenceProcessor")
                .log("Message persisted successfully with ID: ${header.PersistedMessageId}");

        // Error handling route
        from("direct:error-handler").routeId("pacs008-error-handler")
                .log("ERROR: Failed to process PACS.008 message: ${exception.message}")
                .setHeader("ErrorTimestamp", simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"))
                .setHeader("ErrorRoute", constant("PACS008"))

                // Store error information in database
                .to("bean:errorPersistenceProcessor")

                .log("Error information stored with ID: ${header.PersistedErrorId}");
    }

    /**
     * Custom aggregation strategy for batching messages Collects messages into a list for batch
     * processing
     */
    private static class MessageBatchAggregationStrategy implements AggregationStrategy {

        @Override
        @SuppressWarnings("unchecked")
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                // First message in the batch
                List<Exchange> batch = new ArrayList<>();
                batch.add(newExchange);
                newExchange.getIn().setBody(batch);
                return newExchange;
            } else {
                // Add to existing batch
                List<Exchange> batch = oldExchange.getIn().getBody(List.class);
                batch.add(newExchange);
                return oldExchange;
            }
        }
    }
}
