package com.pixel.v2.flow.processor;

import com.pixel.v2.flow.model.MessageError;
import com.pixel.v2.flow.repository.MessageErrorRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("errorPersistenceProcessor")
public class ErrorPersistenceProcessor implements Processor {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorPersistenceProcessor.class);
    
    @Autowired
    private MessageErrorRepository messageErrorRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            // Extract error data
            String messageBody = exchange.getIn().getBody(String.class);
            String jmsMessageId = exchange.getIn().getHeader("JMSMessageID", String.class);
            String errorRoute = exchange.getIn().getHeader("ErrorRoute", String.class);
            
            // Get exception information if available
            Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
            String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
            
            logger.info("Processing error persistence for JMSMessageID: {} in route: {}", jmsMessageId, errorRoute);
            
            // Create and save error entity
            MessageError error = new MessageError();
            error.setJmsMessageId(jmsMessageId);
            error.setErrorRoute(errorRoute);
            error.setErrorMessage(errorMessage);
            error.setMessageBody(messageBody);
            error.setErrorTimestamp(LocalDateTime.now());
            error.setCreatedAt(LocalDateTime.now());
            
            MessageError savedError = messageErrorRepository.save(error);
            
            logger.info("Successfully persisted error with ID: {} for JMSMessageID: {}", 
                       savedError.getId(), jmsMessageId);
            
            // Set the saved error ID in the exchange
            exchange.getIn().setHeader("PersistedErrorId", savedError.getId());
            
        } catch (Exception e) {
            logger.error("Error persisting message error: {}", e.getMessage(), e);
            throw e;
        }
    }
}