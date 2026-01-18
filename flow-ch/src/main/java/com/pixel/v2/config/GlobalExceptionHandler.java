package com.pixel.v2.config;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.CamelEvent.ExchangeFailedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Global Exception Handler using Camel Event Notifier This bean will catch all exceptions across
 * all Camel routes and trigger error handling
 */
@Configuration
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private CamelContext camelContext;


    @Component
    public class ExceptionNotifier extends EventNotifierSupport {

        @Override
        public void notify(CamelEvent event) throws Exception {
            if (event instanceof ExchangeFailedEvent exchangeFailedEvent) {
                Exchange exchange = exchangeFailedEvent.getExchange();
                Exception exception = exchange.getException();

                // Skip ResolveEndpointFailedException
                if (exception instanceof ResolveEndpointFailedException) {
                    return;
                }

                if (exception != null) {
                    logger.info("Global Exception Handler caught: {} - {}",
                            exception.getClass().getSimpleName(), exception.getMessage());

                    // Set error headers
                    exchange.getIn().setHeader("ErrorType", exception.getClass().getSimpleName());
                    exchange.getIn().setHeader("ErrorReason", exception.getMessage());
                    exchange.getIn().setHeader("ErrorDetails", getStackTrace(exception));
                    exchange.getIn().setHeader("OriginalRouteId", exchange.getFromRouteId());

                    try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                        // Send body and headers to error handler (doesn't re-throw exceptions)
                        template.sendBodyAndHeaders("direct:handleError",
                                exchange.getIn().getBody(), exchange.getIn().getHeaders());
                    }

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

    @EventListener(ContextRefreshedEvent.class)
    public void registerExceptionHandler() {
        // Register our global exception handler as an event notifier
        camelContext.getManagementStrategy().addEventNotifier(new ExceptionNotifier());
        logger.info("Global Exception Handler registered successfully");
    }
}
