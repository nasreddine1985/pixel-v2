package com.pixel.v2.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Redis Cache Routes - Handles all Redis operations and caching logic
 */
@Component
public class RedisRoutes extends RouteBuilder {

    // Route endpoint constants
    private static final String FETCH_REFERENCE_DATA_ENDPOINT =
            "direct:fetchReferenceDataFromRedis";
    private static final String FETCH_FROM_REFERENTIEL_ENDPOINT =
            "direct:fetchFromReferentielService";
    private static final String REDIS_GET_ENDPOINT = "direct:redisGet";
    private static final String REDIS_SET_ENDPOINT = "direct:redisSet";
    private static final String REDIS_DELETE_ENDPOINT = "direct:redisDelete";

    // Header constants
    private static final String CACHE_KEY_HEADER = "CacheKey";
    private static final String CACHE_TTL_HEADER = "CacheTTL";
    private static final String FLOW_CONFIGURATION_HEADER = "FlowConfiguration";
    private static final String ORIGINAL_MESSAGE_BODY_HEADER = "OriginalMessageBody";

    // Expression constants
    private static final String BODY_EXPRESSION = "${body}";

    // Camel Redis component headers
    // Redis processor operations
    private static final String REDIS_OPERATION_HEADER = "RedisOperation";
    private static final String REDIS_PROCESSOR = "redisProcessor";

    // Cache key template
    private static final String CACHE_KEY_TEMPLATE = "flow_config_${header.flowCode}";
    private static final Long CACHE_TTL_VALUE = 3600L; // 1 hour TTL

    @Override
    public void configure() throws Exception {

        // Redis-specific exception handler for cache fallback
        onException(Exception.class)
                .onWhen(simple("${routeId} == 'fetch-reference-data-from-redis'"))
                .to(FETCH_FROM_REFERENTIEL_ENDPOINT).handled(true);

        // Redis cache route for fetching reference data
        from(FETCH_REFERENCE_DATA_ENDPOINT).routeId("fetch-reference-data-from-redis")
                .setHeader(ORIGINAL_MESSAGE_BODY_HEADER, simple(BODY_EXPRESSION))
                .setHeader(CACHE_KEY_HEADER, simple(CACHE_KEY_TEMPLATE)).to(REDIS_GET_ENDPOINT)
                .choice().when(simple("${body} == null"))
                .log("Cache MISS: Fetching flow config for ${header.flowCode} from referentiel service")
                .to(FETCH_FROM_REFERENTIEL_ENDPOINT).endChoice().when(simple("${body} == ''"))
                .log("Cache MISS: Fetching flow config for ${header.flowCode} from referentiel service (empty)")
                .to(FETCH_FROM_REFERENTIEL_ENDPOINT).endChoice().otherwise()
                .log("Cache HIT: Using cached flow config for ${header.flowCode}")
                .setHeader(FLOW_CONFIGURATION_HEADER, simple(BODY_EXPRESSION)).endChoice().end()
                .setBody(header(ORIGINAL_MESSAGE_BODY_HEADER))
                .removeHeader(ORIGINAL_MESSAGE_BODY_HEADER);

        // Referentiel service route with cache population
        from(FETCH_FROM_REFERENTIEL_ENDPOINT).routeId("fetch-from-referentiel-service")
                .setHeader(ORIGINAL_MESSAGE_BODY_HEADER, simple(BODY_EXPRESSION))
                .setHeader("CamelHttpMethod", constant("GET"))
                .setHeader("Content-Type", constant("application/json"))
                .toD("{{referentiel.service.url}}/api/flows/${header.flowCode}/complete?bridgeEndpoint=true")
                .convertBodyTo(String.class)
                .log("Successfully retrieved flow config for ${header.flowCode} from referentiel service")
                .setHeader(FLOW_CONFIGURATION_HEADER, simple(BODY_EXPRESSION))
                .setHeader(CACHE_KEY_HEADER, simple(CACHE_KEY_TEMPLATE))
                .setHeader(CACHE_TTL_HEADER, constant(CACHE_TTL_VALUE)).to(REDIS_SET_ENDPOINT)
                .setBody(header(ORIGINAL_MESSAGE_BODY_HEADER))
                .removeHeader(ORIGINAL_MESSAGE_BODY_HEADER);

        // Redis cache refresh route triggered by Kafka events
        from("kafka:ch-refresh?brokers={{camel.component.kafka.brokers}}&groupId=ch-cache-refresh&autoOffsetReset=latest")
                .routeId("redis-cache-refresh").unmarshal().json()
                .setHeader("flowCode", jsonpath("$.flowCode"))
                .setHeader(CACHE_KEY_HEADER, simple(CACHE_KEY_TEMPLATE)).to(REDIS_DELETE_ENDPOINT);

        // Redis GET operation route
        from(REDIS_GET_ENDPOINT).routeId("redis-get-operation").onException(Exception.class)
                .setBody(constant((Object) null)).handled(true).end()
                .setHeader(REDIS_OPERATION_HEADER, constant("GET")).process(REDIS_PROCESSOR);

        // Redis SET operation route with TTL
        from(REDIS_SET_ENDPOINT).routeId("redis-set-operation").onException(Exception.class)
                .handled(true).end().setHeader(REDIS_OPERATION_HEADER, constant("SET"))
                .process(REDIS_PROCESSOR);

        // Redis DELETE operation route
        from(REDIS_DELETE_ENDPOINT).routeId("redis-delete-operation").onException(Exception.class)
                .handled(true).end().setHeader(REDIS_OPERATION_HEADER, constant("DELETE"))
                .process(REDIS_PROCESSOR);
    }
}
