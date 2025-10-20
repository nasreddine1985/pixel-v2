package com.pixel.v2.mqreceipt.processor;

import com.pixel.v2.mqreceipt.model.ReceivedMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

@Component("mqMessagePersistenceProcessor")
public class MessagePersistenceProcessor implements Processor {

    private static final Logger logger =
            Logger.getLogger(MessagePersistenceProcessor.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        ReceivedMessage rm = new ReceivedMessage();
        rm.setPayload(body);
        rm.setReceivedAt(OffsetDateTime.now());
        rm.setSource(
                exchange.getFromEndpoint() != null ? exchange.getFromEndpoint().getEndpointUri()
                        : "mq");
        if (em != null) {
            em.persist(rm);
        } else {
            logger.warning("[mq-receipt] EntityManager not injected - payload: " + body);
        }
    }
}
