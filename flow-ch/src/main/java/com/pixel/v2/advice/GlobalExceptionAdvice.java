package com.pixel.v2.advice;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.CamelEvent.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Global Exception Advice using Camel Event Notifier This bean will catch all exceptions across all
 * Camel routes and trigger error handling
 */
@Component
public class GlobalExceptionAdvice extends EventNotifierSupport {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    @Autowired
    private CamelContext camelContext;

    @Override
    public void notify(CamelEvent event) throws Exception {
        if (event instanceof ExchangeFailedEvent exchangeFailedEvent) {
            Exchange exchange = exchangeFailedEvent.getExchange();
            Exception exception = exchange.getException();

            if (exception != null) {
                logger.info("Global Exception Advice caught: {} - {}",
                        exception.getClass().getSimpleName(), exception.getMessage());

                // Set error headers
                exchange.getIn().setHeader("ErrorType", exception.getClass().getSimpleName());
                exchange.getIn().setHeader("ErrorReason", exception.getMessage());
                exchange.getIn().setHeader("ErrorDetails", getStackTrace(exception));
                exchange.getIn().setHeader("OriginalRouteId", exchange.getFromRouteId());

                // Send to error handling route using ProducerTemplate
                try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                    template.sendBodyAndHeaders("direct:error-handling", exchange.getIn().getBody(),
                            exchange.getIn().getHeaders());
                }

                logger.info("Exception forwarded to error-handling route");
            }
        }
    }

    @Override
    public boolean isEnabled(CamelEvent event) {
        // Only process ExchangeFailedEvent
        return event instanceof ExchangeFailedEvent;
    }

    private String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage())
                .append("\n");

        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        return sb.toString();
    }
}
