package com.pixel.v2.filereceipt.processor;

import com.pixel.v2.filereceipt.model.ReceivedMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

@Component("fileMessagePersistenceProcessor")
public class MessagePersistenceProcessor implements Processor {

    private static final Logger logger =
            Logger.getLogger(MessagePersistenceProcessor.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
        Long splitIndex = exchange.getIn().getHeader("CamelSplitIndex", Long.class);
        
        ReceivedMessage rm = new ReceivedMessage();
        rm.setPayload(body);
        rm.setReceivedAt(OffsetDateTime.now());
        rm.setSource("file");
        rm.setFileName(fileName);
        rm.setLineNumber(splitIndex != null ? splitIndex + 1 : null); // Line numbers start from 1
        
        if (em != null) {
            em.persist(rm);
            logger.info(String.format("[file-receipt] Message persisted - File: %s, Line: %d, ID: %d", 
                fileName, rm.getLineNumber(), rm.getId()));
        } else {
            logger.warning(String.format("[file-receipt] EntityManager not injected - File: %s, Line: %d, Payload: %s", 
                fileName, rm.getLineNumber(), body));
        }
    }
}