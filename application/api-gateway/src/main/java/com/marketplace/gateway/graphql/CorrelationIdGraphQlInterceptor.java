package com.marketplace.gateway.graphql;

import com.marketplace.gateway.config.CorrelationIdFilter;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Ensures correlationId is available inside GraphQL execution context (even when execution hops threads).
 */
@Component
public class CorrelationIdGraphQlInterceptor implements WebGraphQlInterceptor {

    public static final String GRAPHQL_CONTEXT_KEY = "correlationId";

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String correlationId = request.getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;
        request.configureExecutionInput((executionInput, builder) ->
            builder
                .graphQLContext(ctx -> ctx.put(GRAPHQL_CONTEXT_KEY, finalCorrelationId))
                .build()
        );

        return chain.next(request);
    }
}

