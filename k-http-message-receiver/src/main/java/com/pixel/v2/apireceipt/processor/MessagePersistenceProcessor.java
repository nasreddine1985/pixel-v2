package com.pixel.v2.apireceipt.processor;

import com.pixel.v2.apireceipt.model.ReceivedMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.logging.Logger;

@Component("messagePersistenceProcessor")
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
                        : "api");
        if (em != null) {
            em.persist(rm);
            logger.info("[api-receipt] Message persisted - ID: " + rm.getId() + ", Source: " + rm.getSource());
        } else {
            logger.warning("[api-receipt] EntityManager not injected - payload: " + body);
        }
    }
}