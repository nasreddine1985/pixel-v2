package com.pixel.v2.persistence.processor;

import com.pixel.v2.persistence.model.MessageError;
import com.pixel.v2.persistence.repository.MessageErrorRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processor for persisting PACS.008 processing errors to the database. Used for error logging and
 * audit trail purposes.
 */
@Component("pacs008ErrorPersistenceProcessor")
public class Pacs008ErrorPersistenceProcessor implements Processor {

    private static final Logger logger =
            LoggerFactory.getLogger(Pacs008ErrorPersistenceProcessor.class);

    private static final String PERSISTENCE_STATUS_HEADER = "persistenceStatus";
    private static final String PERSISTENCE_ERROR_HEADER = "persistenceError";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    @Autowired
    private MessageErrorRepository messageErrorRepository;

    @Override
    @Transactional
    public void process(Exchange exchange) throws Exception {
        try {
            // Extract error information from the exchange
            Exception causedByException =
                    exchange.getProperty("CamelExceptionCaught", Exception.class);
            String errorMessage =
                    causedByException != null ? causedByException.getMessage() : "Unknown error";
            String errorType =
                    causedByException != null ? causedByException.getClass().getSimpleName()
                            : "UnknownError";

            // Get JMS message information
            String jmsMessageId = exchange.getIn().getHeader("JMSMessageID", String.class);
            String messageBody = exchange.getIn().getBody(String.class);
            String processingRoute = exchange.getFromRouteId();

            // Create error entity
            MessageError messageError = new MessageError();
            messageError.setJmsMessageId(jmsMessageId);
            messageError.setErrorRoute(processingRoute); // Changed from setProcessingRoute
            messageError.setErrorMessage(errorType + ": " + errorMessage); // Combined error type
                                                                           // and message
            messageError.setMessageBody(messageBody);
            messageError.setErrorTimestamp(LocalDateTime.now()); // Changed from setOccurredAt

            // Add stack trace to error message if available
            if (causedByException != null) {
                StringBuilder fullErrorMessage = new StringBuilder();
                fullErrorMessage.append(errorType).append(": ").append(errorMessage)
                        .append("\n\nStack Trace:\n");
                for (StackTraceElement element : causedByException.getStackTrace()) {
                    fullErrorMessage.append(element.toString()).append("\n");
                }
                messageError.setErrorMessage(fullErrorMessage.toString());
            }

            // Persist the error
            MessageError savedError = messageErrorRepository.save(messageError);

            // Set success headers
            exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_SUCCESS);
            exchange.getIn().setHeader("savedErrorId", savedError.getId());

            logger.info("Successfully persisted PACS.008 error with ID: {} for JMS Message: {}",
                    savedError.getId(), jmsMessageId);

        } catch (Exception e) {
            logger.error("Error persisting PACS.008 error: {}", e.getMessage(), e);
            exchange.getIn().setHeader(PERSISTENCE_STATUS_HEADER, STATUS_ERROR);
            exchange.getIn().setHeader(PERSISTENCE_ERROR_HEADER, e.getMessage());
            throw e;
        }
    }
}
