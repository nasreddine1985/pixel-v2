package com.pixel.v2.flow.processor;

import com.pixel.v2.flow.model.Pacs008Message;
import com.pixel.v2.flow.repository.Pacs008MessageRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("messageBatchPersistenceProcessor")
public class MessageBatchPersistenceProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(MessageBatchPersistenceProcessor.class);

    @Autowired
    private Pacs008MessageRepository pacs008MessageRepository;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) throws Exception {
        List<Exchange> batchExchanges = exchange.getIn().getBody(List.class);

        if (batchExchanges == null || batchExchanges.isEmpty()) {
            log.warn("Received empty batch for persistence");
            return;
        }

        log.info("Processing batch persistence for {} messages", batchExchanges.size());

        List<Pacs008Message> messagesToPersist = new ArrayList<>();
        List<String> processedMessageIds = new ArrayList<>();

        try {
            // Process each message in the batch
            for (Exchange messageExchange : batchExchanges) {
                Pacs008Message message = createPacs008Message(messageExchange);
                messagesToPersist.add(message);
                processedMessageIds.add(message.getJmsMessageId());
            }

            // Batch save all messages
            List<Pacs008Message> savedMessages =
                    pacs008MessageRepository.saveAll(messagesToPersist);

            log.info("Successfully persisted batch of {} PACS.008 messages", savedMessages.size());

            // Set response headers
            exchange.getIn().setHeader("BatchSize", savedMessages.size());
            exchange.getIn().setHeader("ProcessedMessageIds", processedMessageIds);
            exchange.getIn().setHeader("BatchPersistenceTimestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error persisting message batch: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Pacs008Message createPacs008Message(Exchange exchange) {
        Pacs008Message message = new Pacs008Message();

        // Extract message body
        String messageBody = exchange.getIn().getBody(String.class);
        message.setMessageBody(messageBody);

        // Extract JMS headers
        message.setJmsMessageId((String) exchange.getIn().getHeader("JMSMessageID"));
        message.setJmsCorrelationId((String) exchange.getIn().getHeader("JMSCorrelationID"));

        // Handle JMSPriority
        Object priorityObj = exchange.getIn().getHeader("JMSPriority");
        if (priorityObj != null) {
            message.setJmsPriority(Integer.valueOf(priorityObj.toString()));
        }

        // Handle JMSTimestamp
        Object timestampObj = exchange.getIn().getHeader("JMSTimestamp");
        if (timestampObj != null) {
            message.setJmsTimestamp(Long.valueOf(timestampObj.toString()));
        }

        // Set processing metadata
        message.setMessageType((String) exchange.getIn().getHeader("MessageType"));
        message.setProcessingRoute((String) exchange.getIn().getHeader("ProcessingRoute"));

        // Set timestamps
        message.setCreatedAt(LocalDateTime.now());
        message.setProcessedTimestamp(LocalDateTime.now());

        return message;
    }
}
