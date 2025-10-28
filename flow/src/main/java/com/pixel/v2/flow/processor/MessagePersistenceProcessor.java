package com.pixel.v2.flow.processor;

import com.pixel.v2.flow.model.Pacs008Message;
import com.pixel.v2.flow.repository.Pacs008MessageRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("messagePersistenceProcessor")
public class MessagePersistenceProcessor implements Processor {
    
    private static final Logger logger = LoggerFactory.getLogger(MessagePersistenceProcessor.class);
    
    @Autowired
    private Pacs008MessageRepository pacs008MessageRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            // Extract message data
            String messageBody = exchange.getIn().getBody(String.class);
            String jmsMessageId = exchange.getIn().getHeader("JMSMessageID", String.class);
            String jmsCorrelationId = exchange.getIn().getHeader("JMSCorrelationID", String.class);
            Long jmsTimestamp = exchange.getIn().getHeader("JMSTimestamp", Long.class);
            Integer jmsPriority = exchange.getIn().getHeader("JMSPriority", Integer.class);
            String messageType = exchange.getIn().getHeader("MessageType", String.class);
            String processingRoute = exchange.getIn().getHeader("ProcessingRoute", String.class);
            
            logger.info("Processing message persistence for JMSMessageID: {}", jmsMessageId);
            
            // Create and save entity
            Pacs008Message message = new Pacs008Message();
            message.setJmsMessageId(jmsMessageId);
            message.setJmsCorrelationId(jmsCorrelationId);
            message.setJmsTimestamp(jmsTimestamp);
            message.setJmsPriority(jmsPriority);
            message.setMessageType(messageType);
            message.setProcessingRoute(processingRoute);
            message.setMessageBody(messageBody);
            message.setCreatedAt(LocalDateTime.now());
            message.setProcessedTimestamp(LocalDateTime.now());
            
            Pacs008Message savedMessage = pacs008MessageRepository.save(message);
            
            logger.info("Successfully persisted PACS.008 message with ID: {} for JMSMessageID: {}", 
                       savedMessage.getId(), jmsMessageId);
            
            // Set the saved entity ID in the exchange for further processing
            exchange.getIn().setHeader("PersistedMessageId", savedMessage.getId());
            
        } catch (Exception e) {
            logger.error("Error persisting PACS.008 message: {}", e.getMessage(), e);
            throw e;
        }
    }
}