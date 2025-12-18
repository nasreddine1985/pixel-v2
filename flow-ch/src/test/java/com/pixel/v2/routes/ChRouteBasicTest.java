package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Simple unit tests for CH payment processing route components
 */
public class ChRouteBasicTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Simple test routes
                from("direct:ch-payment-start").setHeader("flowCode", constant("CH"))
                        .setHeader("messageType", constant("PACS008")).to("mock:ch-processed");

                from("direct:identification-test").choice()
                        .when(header("identificationKey").isNotNull())
                        .setHeader("result", constant("IDENTIFIED")).otherwise()
                        .setHeader("result", constant("NOT_FOUND")).end()
                        .to("mock:identification-result");
            }
        };
    }

    @Test
    public void testChPaymentProcessing() throws Exception {
        getMockEndpoint("mock:ch-processed").expectedMessageCount(1);
        getMockEndpoint("mock:ch-processed").expectedHeaderReceived("flowCode", "CH");

        template.sendBody("direct:ch-payment-start", "Test Message");

        getMockEndpoint("mock:ch-processed").assertIsSatisfied();
        assertNotNull(context.getRoutes());
        assertTrue(context.getRoutes().size() > 0);
    }

    @Test
    public void testIdentificationWithKey() throws Exception {
        getMockEndpoint("mock:identification-result").expectedMessageCount(1);
        getMockEndpoint("mock:identification-result").expectedHeaderReceived("result",
                "IDENTIFIED");

        template.sendBodyAndHeader("direct:identification-test", "TEST_MESSAGE",
                "identificationKey", "TEST_KEY");

        getMockEndpoint("mock:identification-result").assertIsSatisfied();
    }

    @Test
    public void testIdentificationWithoutKey() throws Exception {
        getMockEndpoint("mock:identification-result").expectedMessageCount(1);
        getMockEndpoint("mock:identification-result").expectedHeaderReceived("result", "NOT_FOUND");

        template.sendBody("direct:identification-test", "TEST_MESSAGE");

        getMockEndpoint("mock:identification-result").assertIsSatisfied();
    }
}
