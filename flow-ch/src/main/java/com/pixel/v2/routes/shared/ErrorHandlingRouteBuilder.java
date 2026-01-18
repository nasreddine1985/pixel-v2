package com.pixel.v2.routes.shared;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Global exception handling route that processes errors and sends them to the k-error-handling
 * kamelet
 */
@Component
public class ErrorHandlingRouteBuilder extends RouteBuilder {


    @Value("${pixel.kafka.error.log.topic-name}")
    private String kafkaErrorLogTopicName;

    @Override
    public void configure() throws Exception {
        // Disable error handler on this route since we're already handling errors
        errorHandler(noErrorHandler());

        from("direct:handleError").routeId("error-handling-route")
                // Clear the exception so the route can continue
                .process(exchange -> {
                    exchange.setException(null);
                    exchange.getIn().setHeader("kafkaErrorLogTopicName", kafkaErrorLogTopicName);

                })
                // The kamelet resolves its internal toD URIs using these exchange properties
                .to("kamelet:k-error-handling");
    }
}
