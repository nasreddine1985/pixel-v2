package com.pixel.v2.flow.config;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("messageBatchAggregationStrategy")
public class MessageBatchAggregationStrategy implements AggregationStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(MessageBatchAggregationStrategy.class);

    @Override
    @SuppressWarnings("unchecked")
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        if (oldExchange == null) {
            // First message - create new list
            List<String> messages = new ArrayList<>();
            messages.add(newExchange.getIn().getBody(String.class));

            // Set the body to the list
            newExchange.getIn().setBody(messages);

            // Copy essential headers from first message
            newExchange.getIn().setHeader("FirstMessageTimestamp",
                    newExchange.getIn().getHeader("ProcessingTimestamp"));
            newExchange.getIn().setHeader("MessageType",
                    newExchange.getIn().getHeader("MessageType"));
            newExchange.getIn().setHeader("ProcessingRoute",
                    newExchange.getIn().getHeader("ProcessingRoute"));
            newExchange.getIn().setHeader("MessageSource",
                    newExchange.getIn().getHeader("MessageSource"));

            log.debug("[BATCH-AGGREGATION] Started new batch with first message");
            return newExchange;
        }

        // Add message to existing batch
        List<String> messages = oldExchange.getIn().getBody(List.class);
        messages.add(newExchange.getIn().getBody(String.class));

        // Update batch metadata
        oldExchange.getIn().setHeader("LastMessageTimestamp",
                newExchange.getIn().getHeader("ProcessingTimestamp"));
        oldExchange.getIn().setHeader("BatchSize", messages.size());

        log.debug("[BATCH-AGGREGATION] Added message to batch, current size: {}", messages.size());

        return oldExchange;
    }
}
