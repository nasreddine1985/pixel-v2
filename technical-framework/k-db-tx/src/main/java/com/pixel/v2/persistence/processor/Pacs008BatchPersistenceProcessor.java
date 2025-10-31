package com.pixel.v2.persistence.processor;

import com.pixel.v2.persistence.model.Pacs008Message;
import com.pixel.v2.persistence.repository.Pacs008MessageRepository;
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

/**
 * Processor for persisting batches of PACS.008 messages to the database. Used for batch persistence
 * operations to improve performance.
 */
@Component("pacs008BatchPersistenceProcessor")
public class Pacs008BatchPersistenceProcessor implements Processor {


    private static final Logger logger =
            LoggerFactory.getLogger(Pacs008BatchPersistenceProcessor.class);

    @Autowired
    private Pacs008MessageRepository pacs008MessageRepository;

    @Override
    @Transactional
    public void process(Exchange exchange) throws Exception {
        try {
            List<?> messageList = exchange.getIn().getBody(List.class);

            if (messageList == null || messageList.isEmpty()) {
                logger.warn("Received empty or null message list for batch persistence");
                exchange.getIn().setHeader("persistenceStatus", "ERROR");
                exchange.getIn().setHeader("persistenceError", "Empty message list");
                return;
            }

            List<Pacs008Message> pacs008Messages = new ArrayList<>();
            LocalDateTime processingTime = LocalDateTime.now();
            String processingRoute = exchange.getFromRouteId();

            // Convert each message in the batch to Pacs008Message entity
            for (Object messageObj : messageList) {

                if (messageObj instanceof Exchange) {
                    Exchange messageExchange = (Exchange) messageObj;
                    Pacs008Message pacs008Message =
                            createPacs008Message(messageExchange, processingRoute, processingTime);
                    pacs008Messages.add(pacs008Message);
                } else {
                    // Handle direct string messages
                    Pacs008Message pacs008Message = createPacs008MessageFromString(
                            messageObj.toString(), processingRoute, processingTime);
                    pacs008Messages.add(pacs008Message);
                }
            }

            // Batch save all messages
            List<Pacs008Message> savedMessages = pacs008MessageRepository.saveAll(pacs008Messages);

            // Set success headers
            exchange.getIn().setHeader("persistenceStatus", "SUCCESS");
            exchange.getIn().setHeader("persistedCount", savedMessages.size());
            exchange.getIn().setHeader("persistedMessageIds",
                    savedMessages.stream().map(Pacs008Message::getId).toArray());

            logger.info("Successfully persisted batch of {} PACS.008 messages",
                    savedMessages.size());

        } catch (Exception e) {
            logger.error("Error persisting PACS.008 message batch: {}", e.getMessage(), e);
            exchange.getIn().setHeader("persistenceStatus", "ERROR");
            exchange.getIn().setHeader("persistenceError", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a Pacs008Message entity from a Camel Exchange
     */
    private Pacs008Message createPacs008Message(Exchange messageExchange, String processingRoute,
            LocalDateTime processingTime) {
        Pacs008Message message = new Pacs008Message();

        // Extract message metadata from headers
        String jmsMessageId = messageExchange.getIn().getHeader("JMSMessageID", String.class);
        String jmsCorrelationId =
                messageExchange.getIn().getHeader("JMSCorrelationID", String.class);
        Integer jmsPriority = messageExchange.getIn().getHeader("JMSPriority", Integer.class);
        Long jmsTimestamp = messageExchange.getIn().getHeader("JMSTimestamp", Long.class);

        // Set message properties
        message.setJmsMessageId(jmsMessageId);
        message.setJmsCorrelationId(jmsCorrelationId);
        message.setJmsPriority(jmsPriority != null ? jmsPriority : 0);
        message.setJmsTimestamp(jmsTimestamp != null ? jmsTimestamp : System.currentTimeMillis());
        message.setMessageBody(messageExchange.getIn().getBody(String.class));
        message.setMessageType("PACS.008");
        message.setProcessingRoute(processingRoute);
        message.setCreatedAt(processingTime);
        message.setProcessedTimestamp(processingTime);

        return message;
    }

    /**
     * Creates a Pacs008Message entity from a string message
     */
    private Pacs008Message createPacs008MessageFromString(String messageBody,
            String processingRoute, LocalDateTime processingTime) {
        Pacs008Message message = new Pacs008Message();

        // Set basic properties for string-based messages
        message.setJmsMessageId(null);
        message.setJmsCorrelationId(null);
        message.setJmsPriority(0);
        message.setJmsTimestamp(System.currentTimeMillis());
        message.setMessageBody(messageBody);
        message.setMessageType("PACS.008");
        message.setProcessingRoute(processingRoute);
        message.setCreatedAt(processingTime);
        message.setProcessedTimestamp(processingTime);

        return message;
    }
}
