package com.pixel.v2.aggregation.strategy;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Message batch aggregation strategy for batching messages Collects messages into a list for batch
 * processing Used by YAML routes via bean reference
 */
@Component("messageBatchAggregationStrategy")
public class MessageBatchAggregationStrategy implements AggregationStrategy {

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